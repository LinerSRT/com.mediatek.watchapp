package com.nostra13.universalimageloader.utils;

import android.util.Log;
import com.nostra13.universalimageloader.core.ImageLoader;

/* renamed from: com.nostra13.universalimageloader.utils.L */
public final class C0197L {
    private static volatile boolean writeDebugLogs = false;
    private static volatile boolean writeLogs = true;

    private C0197L() {
    }

    public static void writeDebugLogs(boolean writeDebugLogs) {
        writeDebugLogs = writeDebugLogs;
    }

    /* renamed from: d */
    public static void m0d(String message, Object... args) {
        if (writeDebugLogs) {
            C0197L.log(3, null, message, args);
        }
    }

    /* renamed from: i */
    public static void m3i(String message, Object... args) {
        C0197L.log(4, null, message, args);
    }

    /* renamed from: w */
    public static void m4w(String message, Object... args) {
        C0197L.log(5, null, message, args);
    }

    /* renamed from: e */
    public static void m2e(Throwable ex) {
        C0197L.log(6, ex, null, new Object[0]);
    }

    /* renamed from: e */
    public static void m1e(String message, Object... args) {
        C0197L.log(6, null, message, args);
    }

    private static void log(int priority, Throwable ex, String message, Object... args) {
        if (writeLogs) {
            String log;
            if (args.length > 0) {
                message = String.format(message, args);
            }
            if (ex != null) {
                String logMessage = message != null ? message : ex.getMessage();
                String logBody = Log.getStackTraceString(ex);
                log = String.format("%1$s\n%2$s", new Object[]{logMessage, logBody});
            } else {
                log = message;
            }
            Log.println(priority, ImageLoader.TAG, log);
        }
    }
}
