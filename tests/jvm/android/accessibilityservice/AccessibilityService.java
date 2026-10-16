package android.accessibilityservice;

import android.content.Context;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public abstract class AccessibilityService extends Context {
    public static final int GLOBAL_ACTION_BACK = 1;
    public static final int GLOBAL_ACTION_HOME = 2;
    public AccessibilityNodeInfo root;
    protected void onServiceConnected() {}
    public boolean onUnbind(Intent intent) { return false; }
    public void onDestroy() {}
    public abstract void onInterrupt();
    public abstract void onAccessibilityEvent(AccessibilityEvent event);
    public AccessibilityNodeInfo getRootInActiveWindow() { return root; }
    public boolean performGlobalAction(int action) {
        throw new AssertionError("Unarmed observation must not navigate");
    }
}
