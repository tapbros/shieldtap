package android.provider;

import android.content.ContentResolver;

public final class Settings {
    public static final class Secure {
        public static String getUriFor(String key) { return key; }
    }

    public static final class Global {
        public static final String ADB_ENABLED = "adb_enabled";
        public static int getInt(ContentResolver resolver, String key, int fallback) {
            return resolver.readGlobal(key, fallback);
        }
    }
}
