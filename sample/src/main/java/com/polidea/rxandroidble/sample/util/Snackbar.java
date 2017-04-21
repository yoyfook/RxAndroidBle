package com.polidea.rxandroidble.sample.util;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

/**
 * Created by yoyfook on 17/4/21.
 */

public final class Snackbar {

    public static final int LENGTH_INDEFINITE = android.support.design.widget.Snackbar.LENGTH_INDEFINITE;

    public static final int LENGTH_SHORT = android.support.design.widget.Snackbar.LENGTH_SHORT;

    public static final int LENGTH_LONG = android.support.design.widget.Snackbar.LENGTH_LONG;

    @NonNull
    public static android.support.design.widget.Snackbar make(@NonNull View view, @NonNull CharSequence text, int duration) {
        final android.support.design.widget.Snackbar snackbar =
                android.support.design.widget.Snackbar.make(view, text, duration);
        View snackbarView = snackbar.getView();
        TextView tv = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        tv.setMaxLines(100);
        return snackbar;
    }
}
