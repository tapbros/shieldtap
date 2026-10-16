package android.widget;

import android.content.Context;

public final class Toast {
    public static final int LENGTH_LONG = 1;
    public static Toast makeText(Context context, int message, int duration) {
        throw new AssertionError("Unarmed observation must not show a toast");
    }
    public void show() {}
}
