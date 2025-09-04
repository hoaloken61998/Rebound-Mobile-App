package com.rebound.data;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;

import com.rebound.R;

public class TermsData {

    public static Spanned getPolicyCancel(Context context) {
        return Html.fromHtml(
                context.getString(R.string.policy_cancel_html),
                Html.FROM_HTML_MODE_LEGACY
        );
    }

    public static Spanned getTermsPlain(Context context) {
        return Html.fromHtml(
                context.getString(R.string.terms_plain_html),
                Html.FROM_HTML_MODE_LEGACY
        );
    }
}
