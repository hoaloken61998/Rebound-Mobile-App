package com.rebound.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import com.rebound.connectors.FirebaseProductConnector;
import com.rebound.models.Customer.Customer;
import com.rebound.R;
import com.rebound.adapters.LastCollectionAdapter;
import com.rebound.utils.CartManager;
import com.rebound.utils.OrderManager;
import com.rebound.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.HashMap;

public class MainPageFragment extends Fragment {
    private LastCollectionAdapter adapter;
    private RecyclerView recyclerView;
    private static final HashMap<String, String> CATEGORY_ID_MAP = new HashMap<>();
    static {
        CATEGORY_ID_MAP.put("Necklaces", "3");
        CATEGORY_ID_MAP.put("Earrings", "1");
        CATEGORY_ID_MAP.put("Rings", "1");
        CATEGORY_ID_MAP.put("Body Piercing", "4");
    }

    public MainPageFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        OrderManager.init(requireContext());
        CartManager.init(requireContext());
        Customer currentCustomer = SharedPrefManager.getCurrentCustomer(requireContext());
        if (currentCustomer != null) {
            CartManager.getInstance().setUserEmail(currentCustomer.getUsername());
        }

        View view = inflater.inflate(R.layout.activity_main_page, container, false);

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Sự kiện click các nút phân loại
        view.findViewById(R.id.btnMainPageNecklaces).setOnClickListener(v -> openCategory("Necklaces"));
        view.findViewById(R.id.btnMainPageEarrings).setOnClickListener(v -> openCategory("Earrings"));
        view.findViewById(R.id.btnMainPageRings).setOnClickListener(v -> openCategory("Rings"));
        view.findViewById(R.id.btnMainPageBodyPiercing).setOnClickListener(v -> openCategory("Body Piercing"));

        // Khởi tạo RecyclerView "Latest Collection"
        recyclerView = view.findViewById(R.id.recyclerViewLastCollection);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        adapter = new LastCollectionAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Load random products from Firebase for Latest Collection
        FirebaseProductConnector.getAllProducts("Product", com.rebound.models.Cart.ProductItem.class, new com.rebound.callback.FirebaseListCallback<com.rebound.models.Cart.ProductItem>() {
            @Override
            public void onSuccess(ArrayList<com.rebound.models.Cart.ProductItem> result) {
                java.util.Collections.shuffle(result); // Randomize order
                adapter = new LastCollectionAdapter(result);
                recyclerView.setAdapter(adapter);
            }
            @Override
            public void onFailure(String errorMessage) {
                // Handle error (optional: show a Toast or log)
            }
        });

        // Giỏ hàng
        ImageView imgCart = view.findViewById(R.id.imgMainPageShoppingCart);
        imgCart.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.rebound.checkout.ShoppingCartActivity.class);
            startActivity(intent);
        });

        // Chuông thông báo
        ImageView imgBell = view.findViewById(R.id.imgBell);
        imgBell.setOnClickListener(v -> {
            Customer customer = SharedPrefManager.getCurrentCustomer(requireContext());
            Intent intent;
            if (customer != null) {
                // Đã đăng nhập
                intent = new Intent(requireContext(), NotificationActivity.class);
            } else {
                // Chưa đăng nhập
                intent = new Intent(requireContext(), NoNotificationActivity.class);
            }

            startActivity(intent);
        });

        // SearchView
        SearchView searchView = view.findViewById(R.id.search_view);

        // Khi người dùng nhập và nhấn "Tìm"
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus(); // Ẩn bàn phím

                // Mở SearchActivity và truyền query
                Intent intent = new Intent(requireContext(), SearchActivity.class);
                intent.putExtra("query", query.trim());
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText); // Lọc danh sách sản phẩm
                return true;
            }
        });

        return view;
    }


    private void openCategory(String category) {
        Intent intent = new Intent(requireContext(), CategoryProductActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }

}
