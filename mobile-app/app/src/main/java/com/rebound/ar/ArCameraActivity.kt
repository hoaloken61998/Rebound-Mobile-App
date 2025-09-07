package com.rebound.ar

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.mediapipe.examples.handlandmarker.HandLandmarkerHelper
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.rebound.R
import com.rebound.adapters.ProductARAdapter
import com.rebound.callback.FirebaseListCallback
import com.rebound.connectors.FirebaseProductConnector
import com.rebound.main.NavBarActivity
import com.rebound.models.Cart.ProductItem
import com.rebound.utils.CartManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.sqrt

class ArCameraActivity : AppCompatActivity(), HandLandmarkerHelper.LandmarkerListener {

    // --- Variables for CameraX, Hand Tracking, and 3D Rendering ---
    private lateinit var handLandmarkerHelper: HandLandmarkerHelper
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: ExecutorService
    private var imageAnalyzer: ImageAnalysis? = null
    private var modelDisplayFragment: ModelDisplayFragment? = null
    private lateinit var fragmentContainer: FrameLayout
    private var currentLensFacing = CameraSelector.LENS_FACING_FRONT
    private var currentModelName: String? = null
    private var isCameraProviderInitialized = false
    private var isModelLoading = false

    // --- UI and Business Logic Variables ---
    private lateinit var recyclerProductAR: RecyclerView
    private lateinit var productARAdapter: ProductARAdapter
    private var currentProductList: MutableList<ProductItem> = ArrayList()
    private lateinit var categoryIconsLayout: LinearLayout
    private lateinit var btnSwitchCamera: ImageButton
    private lateinit var btnAddToCart: View
    private var selectedProduct: ProductItem? = null
    private lateinit var cartManager: CartManager

    // Scaling and positioning constants
    companion object {
        private const val TAG = "ArCameraActivity"
        private const val UNIFIED_SCALE_FACTOR_FRONT = 20.0f
        private const val UNIFIED_SCALE_FACTOR_BACK = 32.0f
        private const val RING_FINGER_DIAMETER_RATIO = 0.22f
        private const val Z_MODULATION_SENSITIVITY = -0.1f
        private const val POSITION_SCALE_FACTOR_FRONT = 0.4f
        private const val POSITION_SCALE_FACTOR_BACK = 0.6f
        private const val Y_OFFSET_FRONT = -0.25f
        private const val Y_OFFSET_BACK = 0f
        private const val DEPTH_SCALE_FACTOR = -0.1f
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                ensureCameraIsSetupAndRunning()
            } else {
                Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_LONG).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ar_camera)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        CartManager.init(applicationContext)
        cartManager = CartManager.getInstance()

        initializeUI()
        initializeArComponents()
        setupListeners()
        setupProductRecyclerView()
        requestCameraPermission()
    }

    private fun initializeUI() {
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera)
        categoryIconsLayout = findViewById(R.id.categoryIconsLayout)
        recyclerProductAR = findViewById(R.id.recyclerProductAR)
        fragmentContainer = findViewById(R.id.fragment_container)
        btnAddToCart = findViewById(R.id.btnAddCart)
        fragmentContainer.visibility = View.GONE
    }

    private fun initializeArComponents() {
        if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
            modelDisplayFragment = ModelDisplayFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, modelDisplayFragment!!)
                .commitNow()
        } else {
            modelDisplayFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? ModelDisplayFragment
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        handLandmarkerHelper = HandLandmarkerHelper(
            context = this,
            runningMode = RunningMode.LIVE_STREAM,
            minHandDetectionConfidence = 0.5f,
            minHandTrackingConfidence = 0.5f,
            minHandPresenceConfidence = 0.5f,
            maxNumHands = 1,
            currentDelegate = HandLandmarkerHelper.DELEGATE_GPU,
            handLandmarkerHelperListener = this
        )
    }

    private fun setupListeners() {
        btnSwitchCamera.setOnClickListener { switchCamera() }

        btnAddToCart.setOnClickListener {
            selectedProduct?.let { product ->
                addToCart(product)
            } ?: run {
                Toast.makeText(this, "Please select a product first.", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageView>(R.id.btnCloseArTop).setOnClickListener {
            val intent = Intent(this@ArCameraActivity, NavBarActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        findViewById<ImageView>(R.id.btnCloseAR).setOnClickListener {
            recyclerProductAR.visibility = View.GONE
            categoryIconsLayout.visibility = View.VISIBLE
        }

        val imgARNecklaces: ImageView = findViewById(R.id.imgARNecklaces)
        val imgAREarrings: ImageView = findViewById(R.id.imgAREarrings)
        val imgARRings: ImageView = findViewById(R.id.imgARRings)
        val imgARBodyPiercing: ImageView = findViewById(R.id.imgARBodyPiercing)

        imgARRings.setOnClickListener {
            FirebaseProductConnector.getAllProducts(
                "Product",
                ProductItem::class.java,
                object : FirebaseListCallback<ProductItem> {
                    override fun onSuccess(productList: ArrayList<ProductItem>) {
                        val filteredProducts = productList.filter {
                            it.getProductID() == 21L || it.getProductID() == 23L || it.getProductID() == 27L
                        }
                        showProducts(filteredProducts)
                    }
                    override fun onFailure(errorMessage: String?) {
                        Toast.makeText(this@ArCameraActivity, "Failed to load products", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
        val unsupportedCategoryListener = View.OnClickListener {
            Toast.makeText(this, "This Category is not supported", Toast.LENGTH_SHORT).show()
        }
        imgARNecklaces.setOnClickListener(unsupportedCategoryListener)
        imgAREarrings.setOnClickListener(unsupportedCategoryListener)
        imgARBodyPiercing.setOnClickListener(unsupportedCategoryListener)
    }

    private fun setupProductRecyclerView() {
        productARAdapter = ProductARAdapter(currentProductList, this) { product ->
            val modelName = when (product.getProductID()) {
                21L -> "21.glb"
                23L -> "23.glb"
                27L -> "27.glb"
                else -> null
            }
            modelName?.let {
                selectedProduct = product
                loadModel(it)
                Toast.makeText(this, "Loading ${product.getProductName()}", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerProductAR.apply {
            layoutManager = LinearLayoutManager(this@ArCameraActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = productARAdapter
        }
    }

    private fun showProducts(products: List<ProductItem>) {
        categoryIconsLayout.visibility = View.GONE
        recyclerProductAR.visibility = View.VISIBLE
        currentProductList.clear()
        currentProductList.addAll(products)
        productARAdapter.notifyDataSetChanged()
    }

    private fun loadModel(modelName: String) {
        isModelLoading = true
        currentModelName = modelName
        fragmentContainer.visibility = View.GONE
        modelDisplayFragment?.reinitializeFilamentBridgeAndLoadModel(currentModelName!!) {
            Log.d(TAG, "Filament reinitialization complete for model $currentModelName.")
            bindCameraUseCases()
            isModelLoading = false
        }
    }

    private fun switchCamera() {
        currentLensFacing = if (currentLensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }

        currentModelName?.let { model ->
            isModelLoading = true
            fragmentContainer.visibility = View.GONE
            modelDisplayFragment?.reinitializeFilamentBridgeAndLoadModel(model) {
                Log.d(TAG, "Re-initialized model for camera switch.")
                bindCameraUseCases()
                isModelLoading = false
            }
        } ?: run {
            bindCameraUseCases()
        }
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                setupCameraProvider()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                AlertDialog.Builder(this)
                    .setTitle("Camera Permission Required")
                    .setMessage("This feature requires camera access to show a live preview.")
                    .setPositiveButton("OK") { _, _ ->
                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                    .create().show()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun ensureCameraIsSetupAndRunning() {
        if (handLandmarkerHelper.isClose()) {
            handLandmarkerHelper.setupHandLandmarker()
        }
        if (!isCameraProviderInitialized && cameraProvider == null) {
            setupCameraProvider()
        } else if (cameraProvider != null) {
            bindCameraUseCases()
        }
    }

    private fun setupCameraProvider() {
        if (isCameraProviderInitialized) return
        isCameraProviderInitialized = true

        ProcessCameraProvider.getInstance(this).addListener({
            try {
                cameraProvider = ProcessCameraProvider.getInstance(this).get()
                bindCameraUseCases()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get camera provider", e)
                isCameraProviderInitialized = false
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: return
        val previewView = findViewById<androidx.camera.view.PreviewView>(R.id.preview_view)
        previewView.implementationMode = androidx.camera.view.PreviewView.ImplementationMode.COMPATIBLE

        val cameraSelector = CameraSelector.Builder().requireLensFacing(currentLensFacing).build()

        val preview = Preview.Builder()
            .setTargetRotation(previewView.display.rotation)
            .build()
            .also { it.setSurfaceProvider(previewView.surfaceProvider) }

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetRotation(previewView.display.rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor) { imageProxy -> detectHand(imageProxy) }
            }

        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun detectHand(imageProxy: ImageProxy) {
        handLandmarkerHelper.detectLiveStream(
            imageProxy = imageProxy,
            isFrontCamera = (currentLensFacing == CameraSelector.LENS_FACING_FRONT)
        )
    }

    override fun onResults(resultBundle: HandLandmarkerHelper.ResultBundle) {
        runOnUiThread {
            if (isModelLoading) {
                fragmentContainer.visibility = View.GONE
                return@runOnUiThread
            }
            if (currentModelName != null && resultBundle.results.isNotEmpty() && resultBundle.results.first().landmarks().isNotEmpty()) {
                val handLandmarkerResult = resultBundle.results.first()
                if (handLandmarkerResult.landmarks().first().size > 15 &&
                    handLandmarkerResult.worldLandmarks().isNotEmpty() &&
                    handLandmarkerResult.worldLandmarks().first().size >= 21) {

                    fragmentContainer.visibility = View.VISIBLE
                    val landmarks = handLandmarkerResult.landmarks().first()
                    val worldLandmarks = handLandmarkerResult.worldLandmarks().first()
                    val pip_screen = landmarks[14]
                    val dip_screen = landmarks[15]
                    val anchorXNorm = (pip_screen.x() + dip_screen.x()) / 2f
                    val anchorYNorm = (pip_screen.y() + dip_screen.y()) / 2f
                    val anchorZNorm = (pip_screen.z() + dip_screen.z()) / 2f
                    val positionScaleFactor: Float
                    val yOffset: Float
                    val baseModelSpecificScaleFactor: Float

                    if (currentLensFacing == CameraSelector.LENS_FACING_FRONT) {
                        positionScaleFactor = POSITION_SCALE_FACTOR_FRONT
                        yOffset = Y_OFFSET_FRONT
                        baseModelSpecificScaleFactor = UNIFIED_SCALE_FACTOR_FRONT
                    } else {
                        positionScaleFactor = POSITION_SCALE_FACTOR_BACK
                        yOffset = Y_OFFSET_BACK
                        baseModelSpecificScaleFactor = UNIFIED_SCALE_FACTOR_BACK
                    }

                    val imageAspectRatio = resultBundle.inputImageWidth.toFloat() / resultBundle.inputImageHeight.toFloat()
                    val landmarkZ = anchorZNorm
                    val zModulation = 1.0f + (landmarkZ - 0.5f) * Z_MODULATION_SENSITIVITY
                    val dynamicModelSpecificScaleFactor = baseModelSpecificScaleFactor * zModulation.coerceIn(0.8f, 1.2f)
                    val x = (anchorXNorm - 0.5f) * positionScaleFactor * imageAspectRatio
                    val y = ((0.5f - anchorYNorm) * positionScaleFactor) + yOffset
                    val z = landmarkZ * DEPTH_SCALE_FACTOR
                    val pip_world = worldLandmarks[14]
                    val dip_world = worldLandmarks[15]
                    var fingerDirX = dip_world.x() - pip_world.x()
                    var fingerDirY = dip_world.y() - pip_world.y()
                    var fingerDirZ = dip_world.z() - pip_world.z()

                    val len = sqrt(fingerDirX * fingerDirX + fingerDirY * fingerDirY + fingerDirZ * fingerDirZ)
                    if (len > 0.0001f) {
                        fingerDirX /= len
                        fingerDirY /= len
                        fingerDirZ /= len
                    } else {
                        fingerDirX = 0f; fingerDirY = 1f; fingerDirZ = 0f
                    }
                    val estimatedFingerDiameter = HandLandmarkerHelper.estimateFingerDiameter(worldLandmarks, RING_FINGER_DIAMETER_RATIO)
                    val dynamicScale = estimatedFingerDiameter * dynamicModelSpecificScaleFactor
                    val transformMatrix = calculateTransformMatrix(x, y, z, dynamicScale, fingerDirX, fingerDirY, fingerDirZ, currentModelName!!)
                    modelDisplayFragment?.updateModelTransform(transformMatrix)
                } else {
                    fragmentContainer.visibility = View.GONE
                }
            } else {
                fragmentContainer.visibility = View.GONE
            }
        }
    }


    override fun onError(error: String, errorCode: Int) {
        runOnUiThread {
            Log.e(TAG, "HandLandmarkerHelper Error: $error (Code: $errorCode)")
            Toast.makeText(this, "MediaPipe Error: $error", Toast.LENGTH_SHORT).show()
            fragmentContainer.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            ensureCameraIsSetupAndRunning()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        handLandmarkerHelper.clearHandLandmarker()
        cameraProvider?.unbindAll()
    }

    private fun addToCart(product: ProductItem) {
        val authPrefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val email = authPrefs.getString("current_user", null)

        if (email != null && !email.isEmpty()) {
            cartManager.setUserEmail(email)

            val itemToAdd = ProductItem()
            itemToAdd.productID = product.productID
            itemToAdd.productName = product.productName
            itemToAdd.productPrice = product.productPrice
            itemToAdd.imageLink = product.imageLink
            itemToAdd.setCategoryID(product.getCategoryID())
            itemToAdd.productDescription = product.productDescription
            itemToAdd.productStockQuantity = 1L

            cartManager.addToCart(itemToAdd)

            Toast.makeText(this, "'${product.productName}' added to cart.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Please log in to add items to your cart.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateTransformMatrix(x: Float, y: Float, z: Float, dynamicScale: Float, fingerDirX: Float, fingerDirY: Float, fingerDirZ: Float, modelName: String): FloatArray {
        val modelY_aligned = normalize(floatArrayOf(fingerDirX, fingerDirY, fingerDirZ))
        val upVector = floatArrayOf(0f, 1f, 0f)

        val dotProd = dot(upVector, modelY_aligned)
        val projectedZ = floatArrayOf(
            upVector[0] - dotProd * modelY_aligned[0],
            upVector[1] - dotProd * modelY_aligned[1],
            upVector[2] - dotProd * modelY_aligned[2]
        )

        var modelZ_aligned: FloatArray
        val lenSq = projectedZ[0] * projectedZ[0] + projectedZ[1] * projectedZ[1] + projectedZ[2] * projectedZ[2]
        if (lenSq < 0.00001f) {
            val alternativeUp = floatArrayOf(1f, 0f, 0f)
            val dotProd2 = dot(alternativeUp, modelY_aligned)
            val projectedZ2 = floatArrayOf(
                alternativeUp[0] - dotProd2 * modelY_aligned[0],
                alternativeUp[1] - dotProd2 * modelY_aligned[1],
                alternativeUp[2] - dotProd2 * modelY_aligned[2]
            )
            modelZ_aligned = normalize(projectedZ2)
        } else {
            modelZ_aligned = normalize(projectedZ)
        }

        val modelX_aligned = normalize(cross(modelY_aligned, modelZ_aligned))
        val baseX = modelY_aligned
        val baseY = modelX_aligned
        val baseZ = modelZ_aligned

        return when (modelName) {
            "21.glb" -> calculateTransformMatrixFor21(x, y, z, dynamicScale, baseX, baseY, baseZ)
            "23.glb" -> calculateTransformMatrixFor23(x, y, z, dynamicScale, baseX, baseY, baseZ)
            "27.glb" -> calculateTransformMatrixFor27(x, y, z, dynamicScale, baseX, baseY, baseZ)
            else -> FloatArray(16) { if (it % 5 == 0) 1f else 0f }
        }
    }

    private fun calculateTransformMatrixFor21(x: Float, y: Float, z: Float, dynamicScale: Float, baseX: FloatArray, baseY: FloatArray, baseZ: FloatArray): FloatArray {
        fun scale(v: FloatArray, s: Float): FloatArray = floatArrayOf(v[0] * s, v[1] * s, v[2] * s)
        fun add(v1: FloatArray, v2: FloatArray): FloatArray = floatArrayOf(v1[0] + v2[0], v1[1] + v2[1], v1[2] + v2[2])
        val pitchRadians = -10f * (Math.PI.toFloat() / 180f)
        val cosP = kotlin.math.cos(pitchRadians); val sinP = kotlin.math.sin(pitchRadians)
        val y1 = add(scale(baseY, cosP), scale(baseZ, -sinP)); val z1 = add(scale(baseY, sinP), scale(baseZ, cosP)); val x1 = baseX
        val x2 = scale(x1, -1f); val y2 = y1; val z2 = z1
        val yawRadians = 5f * (Math.PI.toFloat() / 180f)
        val cosY = kotlin.math.cos(yawRadians); val sinY = kotlin.math.sin(yawRadians)
        val x3 = add(scale(x2, cosY), scale(z2, sinY)); val z3 = add(scale(x2, -sinY), scale(z2, cosY)); val y3 = y2
        val rollRadians = 20f * (Math.PI.toFloat() / 180f)
        val cosR = kotlin.math.cos(rollRadians); val sinR = kotlin.math.sin(rollRadians)
        val x4 = add(scale(x3, cosR), scale(y3, -sinR)); val y4 = add(scale(x3, sinR), scale(y3, cosR)); val z4 = z3
        val z5 = scale(z4, -1f); val x5 = x4; val y5 = y4
        val finalPitchRad = 54f * (Math.PI.toFloat() / 180f)
        val cosC = kotlin.math.cos(finalPitchRad); val sinC = kotlin.math.sin(finalPitchRad)
        val y6 = add(scale(y5, cosC), scale(z5, -sinC)); val z6 = add(scale(y5, sinC), scale(z5, cosC)); val x6 = x5
        val finalX = x6; val finalY = y6; val finalZ = z6
        val s = dynamicScale
        return floatArrayOf(s*finalX[0],s*finalX[1],s*finalX[2],0f,s*finalY[0],s*finalY[1],s*finalY[2],0f,s*finalZ[0],s*finalZ[1],s*finalZ[2],0f,x,y,z,1f)
    }

    private fun calculateTransformMatrixFor23(x: Float, y: Float, z: Float, dynamicScale: Float, baseX: FloatArray, baseY: FloatArray, baseZ: FloatArray): FloatArray {
        fun scale(v: FloatArray, s: Float): FloatArray = floatArrayOf(v[0] * s, v[1] * s, v[2] * s)
        fun add(v1: FloatArray, v2: FloatArray): FloatArray = floatArrayOf(v1[0] + v2[0], v1[1] + v2[1], v1[2] + v2[2])
        val pitchRadians = -10f * (Math.PI.toFloat() / 180f)
        val cosP = kotlin.math.cos(pitchRadians); val sinP = kotlin.math.sin(pitchRadians)
        val y1 = add(scale(baseY, cosP), scale(baseZ, -sinP)); val z1 = add(scale(baseY, sinP), scale(baseZ, cosP)); val x1 = baseX
        val x2 = scale(x1, -1f); val y2 = y1; val z2 = z1
        val yawRadians = 20f * (Math.PI.toFloat() / 180f)
        val cosY = kotlin.math.cos(yawRadians); val sinY = kotlin.math.sin(yawRadians)
        val x3 = add(scale(x2, cosY), scale(z2, sinY)); val z3 = add(scale(x2, -sinY), scale(z2, cosY)); val y3 = y2
        val rollRadians = 20f * (Math.PI.toFloat() / 180f)
        val cosR = kotlin.math.cos(rollRadians); val sinR = kotlin.math.sin(rollRadians)
        val x4 = add(scale(x3, cosR), scale(y3, -sinR)); val y4 = add(scale(x3, sinR), scale(y3, cosR)); val z4 = z3
        val z5 = scale(z4, -1.1f); val x5 = x4; val y5 = y4
        val finalPitchRad = 170f * (Math.PI.toFloat() / 180f)
        val cosC = kotlin.math.cos(finalPitchRad); val sinC = kotlin.math.sin(finalPitchRad)
        val y6 = add(scale(y5, cosC), scale(z5, -sinC)); val z6 = add(scale(y5, sinC), scale(z5, cosC)); val x6 = x5
        val finalX = x6; val finalY = y6; val finalZ = z6
        val s = dynamicScale
        return floatArrayOf(s*finalX[0],s*finalX[1],s*finalX[2],0f,s*finalY[0],s*finalY[1],s*finalY[2],0f,s*finalZ[0],s*finalZ[1],s*finalZ[2],0f,x,y,z,1f)
    }

    private fun calculateTransformMatrixFor27(x: Float, y: Float, z: Float, dynamicScale: Float, baseX: FloatArray, baseY: FloatArray, baseZ: FloatArray): FloatArray {
        fun scale(v: FloatArray, s: Float): FloatArray = floatArrayOf(v[0] * s, v[1] * s, v[2] * s)
        fun add(v1: FloatArray, v2: FloatArray): FloatArray = floatArrayOf(v1[0] + v2[0], v1[1] + v2[1], v1[2] + v2[2])
        val pitchRadians = -10f * (Math.PI.toFloat() / 180f)
        val cosP = kotlin.math.cos(pitchRadians); val sinP = kotlin.math.sin(pitchRadians)
        val y1 = add(scale(baseY, cosP), scale(baseZ, -sinP)); val z1 = add(scale(baseY, sinP), scale(baseZ, cosP)); val x1 = baseX
        val x2 = scale(x1, -1f); val y2 = y1; val z2 = z1
        val yawRadians = 80f * (Math.PI.toFloat() / 180f)
        val cosY = kotlin.math.cos(yawRadians); val sinY = kotlin.math.sin(yawRadians)
        val x3 = add(scale(x2, cosY), scale(z2, sinY)); val z3 = add(scale(x2, -sinY), scale(z2, cosY)); val y3 = y2
        val rollRadians = 20f * (Math.PI.toFloat() / 180f)
        val cosR = kotlin.math.cos(rollRadians); val sinR = kotlin.math.sin(rollRadians)
        val x4 = add(scale(x3, cosR), scale(y3, -sinR)); val y4 = add(scale(x3, sinR), scale(y3, cosR)); val z4 = z3
        val z5 = scale(z4, -1f); val x5 = x4; val y5 = y4
        val finalPitchRad = 120f * (Math.PI.toFloat() / 180f)
        val cosC = kotlin.math.cos(finalPitchRad); val sinC = kotlin.math.sin(finalPitchRad)
        val y6 = add(scale(y5, cosC), scale(z5, -sinC)); val z6 = add(scale(y5, sinC), scale(z5, cosC)); val x6 = x5
        val finalX = x6; val finalY = y6; val finalZ = z6
        val s = dynamicScale * 1.2f
        return floatArrayOf(s*finalX[0],s*finalX[1],s*finalX[2],0f,s*finalY[0],s*finalY[1],s*finalY[2],0f,s*finalZ[0],s*finalZ[1],s*finalZ[2],0f,x,y,z,1f)
    }

    private fun dot(a: FloatArray, b: FloatArray): Float {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]
    }

    private fun normalize(v: FloatArray): FloatArray {
        val len = sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2])
        if (len == 0f) return floatArrayOf(0f, 0f, 0f)
        return floatArrayOf(v[0] / len, v[1] / len, v[2] / len)
    }

    private fun cross(a: FloatArray, b: FloatArray): FloatArray {
        return floatArrayOf(
            a[1] * b[2] - a[2] * b[1],
            a[2] * b[0] - a[0] * b[2],
            a[0] * b[1] - a[1] * b[0]
        )
    }
}
