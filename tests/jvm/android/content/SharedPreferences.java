package android.content;

import java.util.HashMap;
import java.util.Map;

public final class SharedPreferences {
    private final Map<String, Number> values = new HashMap<>();

    public long getLong(String key, long fallback) {
        return values.containsKey(key) ? values.get(key).longValue() : fallback;
    }

    public int getInt(String key, int fallback) {
        return values.containsKey(key) ? values.get(key).intValue() : fallback;
    }

    public Editor edit() { return new Editor(); }

    public final class Editor {
        private final Map<String, Number> pending = new HashMap<>();
        public Editor putLong(String key, long value) { pending.put(key, value); return this; }
        public Editor putInt(String key, int value) { pending.put(key, value); return this; }
        public boolean commit() { values.putAll(pending); return true; }
    }
}
