package com.rebound.login;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import java.util.stream.Collectors;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.rebound.R;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HelpCenterActivity extends AppCompatActivity {

    private LinearLayout faqContainer;
    private Typeface montserratRegular;
    private Typeface montserratBold;
    private Typeface montserratItalic;
    ImageView imgHelpCenterButtonBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_help_center);

        addViews();
        addEvents();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tabFaqs = findViewById(R.id.txtHelpcenterFaqsText);
        TextView tabContact = findViewById(R.id.txtHelpcenterContactUs);
        FrameLayout tabContent = findViewById(R.id.layoutHelpcenterInfo);
        LayoutInflater inflater = LayoutInflater.from(this);
        SearchView searchView = findViewById(R.id.search_view);

        ScrollView contactUsView = new ScrollView(this);
        LinearLayout contactContainer = new LinearLayout(this);
        contactContainer.setOrientation(LinearLayout.VERTICAL);
        contactContainer.setPadding(32, 32, 32, 32);
        contactUsView.addView(contactContainer);
        addContactsToLayout(contactContainer);

        View faqsView = inflater.inflate(R.layout.layout_faqs, null);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // xử lý ngay khi gõ
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (tabContent.getChildAt(0) == faqsView) {
                    filterFaqs(newText);
                } else {
                    filterContacts(contactContainer, newText);
                }
                return true;
            }
        });

        tabContent.addView(faqsView);

        tabFaqs.setOnClickListener(v -> {
            tabFaqs.setBackgroundResource(R.drawable.tab_selected);
            tabFaqs.setTextColor(Color.WHITE);
            tabContact.setBackgroundColor(Color.TRANSPARENT);
            tabContact.setTextColor(Color.parseColor("#847c4a"));

            tabContent.removeAllViews();
            tabContent.addView(faqsView);
        });

        tabContact.setOnClickListener(v -> {
            tabContact.setBackgroundResource(R.drawable.tab_selected);
            tabContact.setTextColor(Color.WHITE);
            tabFaqs.setBackgroundColor(Color.TRANSPARENT);
            tabFaqs.setTextColor(Color.parseColor("#847c4a"));

            tabContent.removeAllViews();
            tabContent.addView(contactUsView);
        });

        faqContainer = faqsView.findViewById(R.id.layoutFaqsContainer);
        montserratRegular = ResourcesCompat.getFont(this, R.font.montserrat_regular);
        montserratBold = ResourcesCompat.getFont(this, R.font.montserrat_bold);
        montserratItalic = ResourcesCompat.getFont(this, R.font.montserrat_italic);
        Map<String, List<FAQItem>> faqData = getFaqData();
        addFaqsToLayout(faqData);
    }

    private void addEvents() {
        imgHelpCenterButtonBack.setOnClickListener(v -> finish());
    }

    private void addViews() {
        imgHelpCenterButtonBack = findViewById(R.id.imgHelpCenterButtonBack);
    }

    private Map<String, List<FAQItem>> getFaqData() {
        Map<String, List<FAQItem>> data = new LinkedHashMap<>();

        data.put(getString(R.string.faq_category_delivery), List.of(
                new FAQItem(getString(R.string.faq_delivery_missed), getString(R.string.faq_delivery_missed_answer)),
                new FAQItem(getString(R.string.faq_delivery_time), getString(R.string.faq_delivery_time_answer)),
                new FAQItem(getString(R.string.faq_delivery_address_change), getString(R.string.faq_delivery_address_change_answer)),
                new FAQItem(getString(R.string.faq_delivery_express), getString(R.string.faq_delivery_express_answer))
        ));

        data.put(getString(R.string.faq_category_order), List.of(
                new FAQItem(getString(R.string.faq_order_track), getString(R.string.faq_order_track_answer)),
                new FAQItem(getString(R.string.faq_order_cancel), getString(R.string.faq_order_cancel_answer)),
                new FAQItem(getString(R.string.faq_order_wrong_item), getString(R.string.faq_order_wrong_item_answer)),
                new FAQItem(getString(R.string.faq_order_modify), getString(R.string.faq_order_modify_answer))
        ));

        data.put(getString(R.string.faq_category_pricing), List.of(
                new FAQItem(getString(R.string.faq_pricing_calc), getString(R.string.faq_pricing_calc_answer)),
                new FAQItem(getString(R.string.faq_pricing_hidden_fees), getString(R.string.faq_pricing_hidden_fees_answer)),
                new FAQItem(getString(R.string.faq_pricing_bulk_discount), getString(R.string.faq_pricing_bulk_discount_answer)),
                new FAQItem(getString(R.string.faq_pricing_methods), getString(R.string.faq_pricing_methods_answer))
        ));

        data.put(getString(R.string.faq_category_service), List.of(
                new FAQItem(getString(R.string.faq_service_weekends), getString(R.string.faq_service_weekends_answer)),
                new FAQItem(getString(R.string.faq_service_return), getString(R.string.faq_service_return_answer)),
                new FAQItem(getString(R.string.faq_service_installation), getString(R.string.faq_service_installation_answer)),
                new FAQItem(getString(R.string.faq_service_schedule), getString(R.string.faq_service_schedule_answer))
        ));

        return data;
    }

    private void addFaqsToLayout(Map<String, List<FAQItem>> faqData) {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Map.Entry<String, List<FAQItem>> entry : faqData.entrySet()) {
            TextView groupHeader = new TextView(this);
            groupHeader.setText(entry.getKey());
            groupHeader.setTextSize(20);
            groupHeader.setTextColor(Color.parseColor("#7C6F34"));
            groupHeader.setPadding(48, 24, 48, 12);
            groupHeader.setTypeface(montserratBold);
            faqContainer.addView(groupHeader);

            for (FAQItem item : entry.getValue()) {
                View faqItemView = inflater.inflate(R.layout.faq_item, faqContainer, false);
                TextView question = faqItemView.findViewById(R.id.txtFaqsQuestion);
                TextView answer = faqItemView.findViewById(R.id.txtFaqsAnswer);
                ImageView toggleIcon = faqItemView.findViewById(R.id.imgFaqsButtonAdd);

                question.setText(item.question);
                question.setTypeface(montserratRegular);
                answer.setText(item.answer);
                answer.setTypeface(montserratItalic);

                faqItemView.findViewById(R.id.layoutFaqsQuestion).setOnClickListener(v -> {
                    if (answer.getVisibility() == View.GONE) {
                        answer.setVisibility(View.VISIBLE);
                        toggleIcon.setImageResource(R.mipmap.ic_minus_detail);
                    } else {
                        answer.setVisibility(View.GONE);
                        toggleIcon.setImageResource(R.mipmap.ic_plus_detail);
                    }
                });

                faqContainer.addView(faqItemView);
            }
        }
    }

    private static class FAQItem {
        String question, answer;

        FAQItem(String q, String a) {
            this.question = q;
            this.answer = a;
        }
    }

    private static class ContactItem {
        String title, detail;
        int iconResId;

        ContactItem(String t, String d, int id) {
            this.title = t;
            this.detail = d;
            this.iconResId = id;
        }
    }

    private void filterContacts(LinearLayout container, String query) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        List<ContactItem> contacts = List.of(
                new ContactItem("Customer Service", "090-767-767", R.mipmap.ic_customer_service_help),
                new ContactItem("Facebook", "facebook.com/reboundpiercing", R.mipmap.ic_facebook_help),
                new ContactItem("Website", "www.reboundpiericng.vn", R.mipmap.ic_web_help),
                new ContactItem("WhatsApp", "012-345-6789", R.mipmap.ic_whatsapp_help),
                new ContactItem("Twitter", "twitter.com/reboundpiercing", R.mipmap.ic_twitter_help),
                new ContactItem("Instagram", "instagram.com/reboundpiercing", R.mipmap.ic_instagram_help)
        );

        for (ContactItem item : contacts) {
            if (item.title.toLowerCase().contains(query.toLowerCase()) ||
                    item.detail.toLowerCase().contains(query.toLowerCase())) {

                View view = inflater.inflate(R.layout.contact_item, container, false);
                ImageView icon = view.findViewById(R.id.imgContactIcon);
                TextView title = view.findViewById(R.id.txtContactusContactname);
                TextView detail = view.findViewById(R.id.txtContactusContactDetail);
                ImageView toggleIcon = view.findViewById(R.id.imgContactusButtonMore);

                icon.setImageResource(item.iconResId);
                title.setText(item.title);
                detail.setText(item.detail);

                detail.setVisibility(View.GONE);
                toggleIcon.setImageResource(R.mipmap.ic_plus_detail);

                view.findViewById(R.id.contact_item_layout).setOnClickListener(v -> {
                    if (detail.getVisibility() == View.GONE) {
                        detail.setVisibility(View.VISIBLE);
                        toggleIcon.setImageResource(R.mipmap.ic_minus_detail);
                    } else {
                        detail.setVisibility(View.GONE);
                        toggleIcon.setImageResource(R.mipmap.ic_plus_detail);
                    }
                });

                container.addView(view);
            }
        }
    }
    private void filterFaqs(String query) {
        faqContainer.removeAllViews();
        Map<String, List<FAQItem>> allData = getFaqData();

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Map.Entry<String, List<FAQItem>> entry : allData.entrySet()) {
            List<FAQItem> filtered = entry.getValue().stream()
                    .filter(item -> item.question.toLowerCase().contains(query.toLowerCase()) ||
                            item.answer.toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());

            if (!filtered.isEmpty()) {
                TextView groupHeader = new TextView(this);
                groupHeader.setText(entry.getKey());
                groupHeader.setTextSize(20);
                groupHeader.setTextColor(Color.parseColor("#7C6F34"));
                groupHeader.setPadding(48, 24, 48, 12);
                groupHeader.setTypeface(montserratBold);
                faqContainer.addView(groupHeader);

                for (FAQItem item : filtered) {
                    View faqItemView = inflater.inflate(R.layout.faq_item, faqContainer, false);
                    TextView question = faqItemView.findViewById(R.id.txtFaqsQuestion);
                    TextView answer = faqItemView.findViewById(R.id.txtFaqsAnswer);
                    ImageView toggleIcon = faqItemView.findViewById(R.id.imgFaqsButtonAdd);

                    question.setText(item.question);
                    question.setTypeface(montserratRegular);
                    answer.setText(item.answer);
                    answer.setTypeface(montserratItalic);

                    faqItemView.findViewById(R.id.layoutFaqsQuestion).setOnClickListener(v -> {
                        if (answer.getVisibility() == View.GONE) {
                            answer.setVisibility(View.VISIBLE);
                            toggleIcon.setImageResource(R.mipmap.ic_minus_detail);
                        } else {
                            answer.setVisibility(View.GONE);
                            toggleIcon.setImageResource(R.mipmap.ic_plus_detail);
                        }
                    });

                    faqContainer.addView(faqItemView);
                }
            }
        }
    }
    private void addContactsToLayout(LinearLayout container) {
        LayoutInflater inflater = LayoutInflater.from(this);

        List<ContactItem> contacts = List.of(
                new ContactItem("Customer Service", "090-767-767", R.mipmap.ic_customer_service_help),
                new ContactItem("Facebook", "facebook.com/reboundpiercing", R.mipmap.ic_facebook_help),
                new ContactItem("Website", "www.reboundpiericng.vn", R.mipmap.ic_web_help),
                new ContactItem("WhatsApp", "012-345-6789", R.mipmap.ic_whatsapp_help),
                new ContactItem("Twitter", "twitter.com/reboundpiercing", R.mipmap.ic_twitter_help),
                new ContactItem("Instagram", "instagram.com/reboundpiercing", R.mipmap.ic_instagram_help)
        );

        for (ContactItem item : contacts) {
            View view = inflater.inflate(R.layout.contact_item, container, false);
            ImageView icon = view.findViewById(R.id.imgContactIcon);
            TextView title = view.findViewById(R.id.txtContactusContactname);
            TextView detail = view.findViewById(R.id.txtContactusContactDetail);
            ImageView toggleIcon = view.findViewById(R.id.imgContactusButtonMore);

            icon.setImageResource(item.iconResId);
            title.setText(item.title);
            detail.setText(item.detail);

            detail.setVisibility(View.GONE);
            toggleIcon.setImageResource(R.mipmap.ic_plus_detail);

            view.findViewById(R.id.contact_item_layout).setOnClickListener(v -> {
                if (detail.getVisibility() == View.GONE) {
                    detail.setVisibility(View.VISIBLE);
                    toggleIcon.setImageResource(R.mipmap.ic_minus_detail);
                } else {
                    detail.setVisibility(View.GONE);
                    toggleIcon.setImageResource(R.mipmap.ic_plus_detail);
                }
            });

            container.addView(view);
        }
    }


}
