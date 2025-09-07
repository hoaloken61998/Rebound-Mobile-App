package com.rebound.main;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rebound.R;
import com.rebound.adapters.ProductAdapter;
import com.rebound.connectors.FirebaseConnector;
import com.rebound.callback.FirebaseListCallback;
import com.rebound.connectors.FirebaseProductConnector;
import com.rebound.models.Cart.ProductItem;

import java.util.ArrayList;
import java.util.HashMap;

public class CategoryProductActivity extends AppCompatActivity {
    ImageView imgBackCategory;
    private static final HashMap<String, String> CATEGORY_ID_MAP = new HashMap<>();

    static {
        CATEGORY_ID_MAP.put("Necklaces", "3");
        CATEGORY_ID_MAP.put("Earrings", "1");
        CATEGORY_ID_MAP.put("Rings", "2");
        CATEGORY_ID_MAP.put("Body Piercing", "4");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_product);

        String category = getIntent().getStringExtra("category");

        TextView txtTitle = findViewById(R.id.txtCategoryTitle);
        txtTitle.setText(category);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewCategoryProduct);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        ProductAdapter adapter = new ProductAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        imgBackCategory = findViewById(R.id.imgBackCategory);
        imgBackCategory.setOnClickListener(v -> finish());

        String categoryIdStr = CATEGORY_ID_MAP.get(category);
        if (categoryIdStr != null) {
            long categoryId; // Use long for consistency with ProductItem.CategoryID
            try {
                categoryId = Long.parseLong(categoryIdStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid categoryId: " + categoryIdStr, Toast.LENGTH_LONG).show();
                adapter.updateList(new ArrayList<>());
                return;
            }
            FirebaseProductConnector.getProductsByCategoryNumber("Product", categoryId, ProductItem.class, new FirebaseListCallback<ProductItem>() {
                @Override
                public void onSuccess(ArrayList<ProductItem> result) {
                    adapter.updateList(result);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(CategoryProductActivity.this, "Failed to load: " + errorMessage, Toast.LENGTH_LONG).show();
                    adapter.updateList(new ArrayList<>());
                }
            });
        } else {
            Toast.makeText(this, "No categoryId found for: " + category, Toast.LENGTH_LONG).show();
            adapter.updateList(new ArrayList<>());
        }
    }
}
