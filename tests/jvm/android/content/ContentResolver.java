package android.content;

import android.database.ContentObserver;
import java.util.HashMap;
import java.util.Map;

public final class ContentResolver {
    public final Map<String, Integer> globals = new HashMap<>();
    public final Map<String, RuntimeException> readFailures = new HashMap<>();
    public RuntimeException registrationFailure;
    public ContentObserver observer;
    public int registrations;

    public void registerContentObserver(String uri, boolean descendants, ContentObserver value) {
        registrations++;
        if (registrationFailure != null) throw registrationFailure;
        observer = value;
    }

    public void unregisterContentObserver(ContentObserver value) {
        if (observer != value) throw new AssertionError("Unregistering a different observer");
        observer = null;
    }

    public int readGlobal(String key, int fallback) {
        if (readFailures.containsKey(key)) throw readFailures.get(key);
        return globals.containsKey(key) ? globals.get(key) : fallback;
    }

    public void notifyChange() {
        if (observer == null) throw new AssertionError("No registered observer");
        observer.onChange(false);
    }
}
