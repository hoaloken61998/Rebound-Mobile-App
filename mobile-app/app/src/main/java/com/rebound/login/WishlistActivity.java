package com.rebound.login;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.rebound.R;
import androidx.recyclerview.widget.GridLayoutManager;
import android.widget.ImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.rebound.models.Cart.ProductItem;
import com.rebound.utils.WishlistManager;
import com.rebound.adapters.WishlistAdapter;
import java.util.List;

    public class WishlistActivity extends AppCompatActivity {

        private RecyclerView recyclerView;
        private WishlistAdapter adapter;
        private ImageView backBtn;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_wishlist);

            recyclerView = findViewById(R.id.recyclerWishlist);
            backBtn = findViewById(R.id.imgWishlistBack);

            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

            List<ProductItem> wishlist = WishlistManager.getInstance(this).getWishlist();
            adapter = new WishlistAdapter(wishlist, this);
            recyclerView.setAdapter(adapter);

            backBtn.setOnClickListener(v -> finish());
        }
    }

