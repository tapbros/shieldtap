package android.os;

/** Timers are not fired: these tests exercise unarmed state observations only. */
public final class Handler {
    public Handler(Looper looper) {}
    public void removeCallbacks(Runnable action) {}
    public boolean postDelayed(Runnable action, long delay) { return true; }
}
