package android.content;

import java.util.HashMap;
import java.util.Map;

/** In-memory Android boundary for the service tests, not an Android implementation. */
public class Context {
    public static final int MODE_PRIVATE = 0;
    public final ContentResolver resolver = new ContentResolver();
    private final Map<String, SharedPreferences> preferences = new HashMap<>();

    public ContentResolver getContentResolver() { return resolver; }

    public SharedPreferences getSharedPreferences(String name, int mode) {
        if (!preferences.containsKey(name)) preferences.put(name, new SharedPreferences());
        return preferences.get(name);
    }
}
