package com.polidea.rxandroidble.sample.util;

import com.polidea.rxandroidble.RxBleConnection.RxBleConnectionState;

import java.util.HashMap;

/**
 * Created by yoyfook on 17/4/6.
 */

public final class RxBleUtils {
    private static final HashMap<RxBleConnectionState, String> sConnectionStateStrs = new HashMap<RxBleConnectionState, String>() {
        {
            put(RxBleConnectionState.DISCONNECTED, "已断开");
            put(RxBleConnectionState.DISCONNECTING, "断开中");
            put(RxBleConnectionState.CONNECTED, "已连接");
            put(RxBleConnectionState.CONNECTING, "连接中");
        }
    };

    public static String getConnectionStateStr(RxBleConnectionState newState) {
        final String str = sConnectionStateStrs.get(newState);
        return str == null ? "未知" : str;
    }
}
