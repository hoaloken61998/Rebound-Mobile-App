package com.rebound.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rebound.R;
import com.rebound.adapters.LastCollectionAdapter;
import com.rebound.adapters.ProductAdapter;
import com.rebound.callback.FirebaseListCallback;
import com.rebound.connectors.FirebaseConnector;
import com.rebound.models.Cart.Category;
import com.rebound.models.Cart.ProductItem;

import android.text.Editable;
import android.text.TextWatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private SearchView searchView;
    private LastCollectionAdapter adapter;
    private ProductAdapter productAdapter;
    private RecyclerView recyclerView;
    private List<ProductItem> allProducts;
    private FlowLayout recentContainer, popularContainer;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "search_prefs";
    private static final String KEYWORDS_KEY = "recent_keywords";

    // Add these fields for filter state
    private Long selectedCategoryId = null;
    private float minPrice = 0;
    private float maxPrice = 10000000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        searchView = findViewById(R.id.searchView);
        searchView.setIconified(false);
        searchView.setFocusable(true);
        searchView.setFocusableInTouchMode(true);
        searchView.requestFocus();
        ImageView btnSearchFilter = findViewById(R.id.btnSearchFilter);
        LinearLayout filterDropdownContainer = findViewById(R.id.filterDropdownContainer);
        EditText edtMinPrice = findViewById(R.id.edtMinPrice);
        EditText edtMaxPrice = findViewById(R.id.edtMaxPrice);
        Spinner spinnerCategory = findViewById(R.id.spinnerCategory);
        Spinner spinnerPriceRange = findViewById(R.id.spinnerPriceRange);
        Button btnApplyFilter = findViewById(R.id.btnApplyFilter);
        // Hide overlay initially
        filterDropdownContainer.setVisibility(View.GONE);
        btnSearchFilter.setOnClickListener(v -> {
            if (filterDropdownContainer.getVisibility() == View.VISIBLE) {
                filterDropdownContainer.setVisibility(View.GONE);
            } else {
                filterDropdownContainer.setVisibility(View.VISIBLE);
            }
        });
        // Remove auto-filtering from EditTexts and Spinners
        edtMinPrice.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                float min = 0;
                try { min = Float.parseFloat(s.toString()); } catch (Exception ignored) {}
                if (min > maxPrice) min = maxPrice;
                minPrice = min;
                // Do not call applyFilter() here
            }
        });
        edtMaxPrice.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                float max = 10000000;
                try { max = Float.parseFloat(s.toString()); } catch (Exception ignored) {}
                if (max < minPrice) max = minPrice;
                maxPrice = max;
                // Do not call applyFilter() here
            }
        });
        allProducts = new ArrayList<>(); // Ensure allProducts is initialized before use
        // Load all products from Firebase
        FirebaseConnector.getAllItems(
            "Product",
            com.rebound.models.Cart.ProductItem.class,
            new FirebaseListCallback<ProductItem>() {
                @Override
                public void onSuccess(ArrayList<com.rebound.models.Cart.ProductItem> result) {
                    if (result == null) result = new ArrayList<>(); // Defensive null check
                    allProducts.clear();
                    allProducts.addAll(result);
                    productAdapter.updateList(result);

                    // Populate filter spinners after data is loaded
                    List<String> categoryNames = new ArrayList<>();
                    List<Long> categoryIds = new ArrayList<>();
                    categoryNames.add("All");
                    categoryIds.add(null);
                    // Use Category.getDefaultCategories() for names
                    List<Category> defaultCategories = com.rebound.models.Cart.Category.getDefaultCategories();
                    for (Category cat : defaultCategories) {
                        categoryIds.add(cat.getCategoryID());
                        categoryNames.add(cat.getCategoryName());
                    }
                    ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(SearchActivity.this, android.R.layout.simple_spinner_item, categoryNames);
                    categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(categoryAdapter);

                    spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCategoryId = categoryIds.get(position);
                            // Do not call applyFilter() here
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                }
                @Override
                public void onFailure(String errorMessage) {
                    productAdapter.updateList(new ArrayList<>());
                }
            }
        );

        // RecyclerView setup (nếu muốn hiển thị kết quả ngay bên dưới layout)
        recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter(new ArrayList<>());
        recyclerView.setAdapter(productAdapter);

        // Add RecyclerView to the FrameLayout container (does not affect LinearLayout/FlowLayouts)
        FrameLayout resultContainer = findViewById(R.id.searchResultContainer);
        if (resultContainer != null) {
            resultContainer.addView(recyclerView);
        }

        // Back
        ImageView imgBack = findViewById(R.id.imgBack);
        imgBack.setOnClickListener(v -> finish());

        // Load từ khóa
        recentContainer = findViewById(R.id.searchRecentContainer);
        if (recentContainer == null) {
            Log.e("SearchActivity", "recentContainer is null!");
        }
        popularContainer = findViewById(R.id.searchPopularContainer);
        if (popularContainer == null) {
            Log.e("SearchActivity", "popularContainer is null!");
        }
        loadRecentKeywords();
        loadPopularKeywords();

        // Gắn query từ intent (nếu có)
        String query = getIntent().getStringExtra("query");
        if (query != null && !query.trim().isEmpty()) {
            searchView.setQuery(query, false);
            performSearch(query);
            saveSearchKeyword(query);
        }

        // Sự kiện khi nhập
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("SearchActivity", "onQueryTextSubmit: " + query);
                performSearch(query);
                saveSearchKeyword(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("SearchActivity", "onQueryTextChange: " + newText);
                performSearch(newText);
                return true;
            }
        });

        // Price range spinner setup
        // Example price ranges (customize as needed)
        List<String> priceRanges = Arrays.asList(
            "All",
            "0 - 1.000.000",
            "1.000.000 - 3.000.000",
            "3.000.000 - 5.000.000",
            "5.000.000 - 10.000.000",
            "10.000.000+"
        );
        ArrayAdapter<String> priceRangeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priceRanges);
        priceRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriceRange.setAdapter(priceRangeAdapter);
        spinnerPriceRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePriceRangeFromSpinner(position);
                // Do not call applyFilter() here
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnApplyFilter.setOnClickListener(v -> {
            applyFilter();
            filterDropdownContainer.setVisibility(View.GONE);
        });
    }

    private void performSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            // If the search bar is empty, show nothing (or all, if you prefer)
            productAdapter.updateList(new ArrayList<>());
            Log.d("SearchActivity", "Search keyword is empty. No products to show.");
            return;
        }
        List<ProductItem> filtered = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        for (ProductItem item : allProducts) {
            Log.d("SearchActivity", "Raw ProductItem: " + item.toString());
            String nameStr = item.getProductName() != null ? String.valueOf(item.getProductName()).toLowerCase() : "";
            String descStr = item.getProductDescription() != null ? String.valueOf(item.getProductDescription()).toLowerCase() : "";
            boolean nameMatch = nameStr.contains(lowerKeyword);
            boolean descMatch = descStr.contains(lowerKeyword);
            Log.d("SearchActivity", "Checking product: name='" + nameStr + "', desc='" + descStr + "', nameMatch=" + nameMatch + ", descMatch=" + descMatch);
            // Match in name or description
            if (nameMatch || descMatch) {
                filtered.add(item);
            }
        }
        Log.d("SearchActivity", "Filtered size: " + filtered.size() + ", Query: '" + keyword + "'");
        productAdapter.updateList(filtered);
        if (filtered.isEmpty()) {
            //Toast.makeText(this, "No result found", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveSearchKeyword(String keyword) {
        String existing = prefs.getString(KEYWORDS_KEY, "");
        List<String> keywords = new ArrayList<>(Arrays.asList(existing.split(",")));
        keywords.remove(keyword); // remove duplicates
        keywords.add(0, keyword); // add latest first
        if (keywords.size() > 10) keywords = keywords.subList(0, 10);

        String newRaw = String.join(",", keywords);
        prefs.edit().putString(KEYWORDS_KEY, newRaw).apply();
        loadRecentKeywords();
    }

    private void loadRecentKeywords() {
        String raw = prefs.getString(KEYWORDS_KEY, "");
        recentContainer.removeAllViews();
        if (!raw.isEmpty()) {
            for (String kw : raw.split(",")) {
                addKeywordChip(kw, recentContainer);
            }
        }
    }

    private void loadPopularKeywords() {
        List<String> popular = Arrays.asList("Necklace", "Gold", "Minimal", "Gem", "Earring");
        for (String kw : popular) {
            addKeywordChip(kw, popularContainer);
        }
    }

    private void addKeywordChip(String keyword, FlowLayout container) {
        TextView chip = new TextView(this);
        chip.setText(keyword);
        chip.setPadding(24, 12, 24, 12);
        chip.setBackgroundResource(R.drawable.bg_keyword_chip); // drawable rounded background
        chip.setTextSize(14);
        chip.setTextColor(getResources().getColor(R.color.black));
        chip.setOnClickListener(v -> {
            searchView.setQuery(keyword, true);
        });
        container.addView(chip);
    }

    // Filter products based on selected price and category
    private void applyFilter() {
        List<ProductItem> filtered = new ArrayList<>();
        Log.d("SearchActivity", "Applying filter: minPrice=" + minPrice + ", maxPrice=" + maxPrice + ", selectedCategoryId=" + selectedCategoryId);
        for (ProductItem item : allProducts) {
            boolean matchesCategory = (selectedCategoryId == null || (item.getCategoryID() != null && item.getCategoryID().equals(selectedCategoryId)));
            long price = getProductPriceAsLong(item.getProductPrice());
            boolean matchesPrice = price >= minPrice && price <= maxPrice;
            Log.d("SearchActivity", "Product: " + item.getProductName() + ", Price: " + price + ", matchesPrice: " + matchesPrice + ", matchesCategory: " + matchesCategory);
            if (matchesCategory && matchesPrice) {
                filtered.add(item);
            }
        }
        Log.d("SearchActivity", "Filtered products count: " + filtered.size());
        productAdapter.updateList(filtered);
    }

    private void updatePriceRangeFromSpinner(int position) {
        switch (position) {
            case 0: minPrice = 0; maxPrice = 10000000; break;
            case 1: minPrice = 0; maxPrice = 1000000; break;
            case 2: minPrice = 1000001; maxPrice = 3000000; break;
            case 3: minPrice = 3000001; maxPrice = 5000000; break;
            case 4: minPrice = 5000001; maxPrice = 10000000; break;
            case 5: minPrice = 10000001; maxPrice = Float.MAX_VALUE; break;
        }
    }

    private long getProductPriceAsLong(Object priceObj) {
        Log.d("SearchActivity", "Raw ProductPrice value: " + priceObj + " (" + (priceObj == null ? "null" : priceObj.getClass().getName()) + ")");
        if (priceObj == null) return 0;
        if (priceObj instanceof Number) {
            return ((Number) priceObj).longValue();
        }
        if (priceObj instanceof String) {
            String s = ((String) priceObj).replace(",", "").replace(".", "").trim();
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                try {
                    return (long) Double.parseDouble(s);
                } catch (Exception ex) {
                    Log.d("SearchActivity", "Failed to parse ProductPrice string: " + s);
                    return 0;
                }
            }
        }
        Log.d("SearchActivity", "ProductPrice is not a recognized type: " + priceObj.getClass().getName());
        return 0;
    }
}
