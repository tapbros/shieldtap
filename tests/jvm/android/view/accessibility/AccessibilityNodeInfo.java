package android.view.accessibility;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AccessibilityNodeInfo {
    public static final int ACTION_CLICK = 16;
    public String packageName;
    public boolean checked;
    public final Map<String, List<AccessibilityNodeInfo>> nodes = new HashMap<>();
    public CharSequence getPackageName() { return packageName; }
    public boolean isChecked() { return checked; }
    public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByViewId(String id) {
        return nodes.containsKey(id) ? nodes.get(id) : Collections.emptyList();
    }
    public boolean performAction(int action) {
        throw new AssertionError("Unarmed observation must not click");
    }
}
