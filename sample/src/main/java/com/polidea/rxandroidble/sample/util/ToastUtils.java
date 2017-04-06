package com.polidea.rxandroidble.sample.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by yoyfook on 17/4/6.
 */

public final class ToastUtils {
    public static void ts_show(Context ctx, CharSequence text) {
        Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show();
    }

    public static void ts_show_l(Context ctx, CharSequence text) {
        Toast.makeText(ctx, text, Toast.LENGTH_LONG).show();
    }
}
