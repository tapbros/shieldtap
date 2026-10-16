package android.database;

import android.os.Handler;

public abstract class ContentObserver {
    public ContentObserver(Handler handler) {}
    public abstract void onChange(boolean selfChange);
}
