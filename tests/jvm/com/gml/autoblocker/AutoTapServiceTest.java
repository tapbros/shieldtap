package com.gml.autoblocker;

import android.os.SystemClock;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.Collections;

/** Runs the production service with synchronous Android boundaries and an explicit clock. */
public final class AutoTapServiceTest {
    private static int cases;

    public static void main(String[] args) {
        reconnectMatrix();
        retryAfterRegistrationFailure();
        duplicateConnectionKeepsFreshScreen();
        reconnectAfterUnbind();
        screenOverridesInference();
        notificationBeforeScreen();
        screenBeforeNotification();
        notificationBoundaries();
        absorbedNotificationStartsDedupWindow();
        unknownNotificationStaysUnknown();
        hexColors();
        System.out.println("PASS AutoTapService/HexColor: " + cases + " deterministic cases");
    }

    private static AutoTapService fixture(int cached) {
        // Given: no live service, isolated cache/log and no pending arm.
        AutoBlockerWidget.state = cached;
        AutoBlockerWidget.src = AutoBlockerWidget.SRC_SCREEN;
        AutoBlockerWidget.refreshes = 0;
        StateLog.events.clear();
        SystemClock.now = 10000;
        return new AutoTapService();
    }

    private static void equal(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("expected " + expected + ", got " + actual);
        }
    }

    private static void state(int value, String source) {
        equal(value, AutoBlockerWidget.state);
        equal(source, AutoBlockerWidget.src);
    }

    private static void finish(AutoTapService service) {
        service.onDestroy();
        equal(false, AutoTapService.isRunning());
        cases++;
    }

    private static void reconnectMatrix() {
        // Given: each prior value, registration outcome and debug signal.
        for (int cached : new int[] {-1, 0, 1}) {
            for (int registration = 0; registration < 3; registration++) {
                for (int debug = 0; debug < 9; debug++) {
                    AutoTapService service = fixture(cached);
                    if (registration == 1) service.resolver.registrationFailure = new SecurityException("denied");
                    if (registration == 2) service.resolver.registrationFailure = new IllegalStateException("failed");
                    if (debug == 1 || debug == 3) service.resolver.globals.put(Settings.Global.ADB_ENABLED, 1);
                    if (debug == 2) service.resolver.globals.put(AutoTapService.ADB_WIFI_KEY, 1);
                    if (debug == 3 || debug == 4) {
                        service.resolver.readFailures.put(AutoTapService.ADB_WIFI_KEY, new SecurityException("denied"));
                    }
                    if (debug == 5) {
                        service.resolver.readFailures.put(Settings.Global.ADB_ENABLED, new IllegalStateException("failed"));
                    }
                    if (debug == 6) {
                        service.resolver.globals.put(Settings.Global.ADB_ENABLED, 0);
                        service.resolver.globals.put(AutoTapService.ADB_WIFI_KEY, 0);
                    }
                    if (debug == 7) {
                        service.resolver.readFailures.put(Settings.Global.ADB_ENABLED, new SecurityException("denied"));
                        service.resolver.readFailures.put(AutoTapService.ADB_WIFI_KEY, new SecurityException("denied"));
                    }
                    if (debug == 8) {
                        service.resolver.readFailures.put(Settings.Global.ADB_ENABLED, new IllegalStateException("failed"));
                        service.resolver.globals.put(AutoTapService.ADB_WIFI_KEY, 1);
                    }
                    // When: the actual service connects, including the registration-throw path.
                    service.onServiceConnected();
                    // Then: only an explicit 1 implies Off; missing/denied/error never imply On.
                    boolean enabled = (debug >= 1 && debug <= 3) || debug == 8;
                    state(enabled ? 0 : -1, enabled ? AutoBlockerWidget.SRC_DEBUG : AutoBlockerWidget.SRC_SCREEN);
                    equal(1, service.resolver.registrations);
                    equal(true, AutoBlockerWidget.refreshes > 0);
                    equal(true, StateLog.events.contains(registration == 0 ? "observer_ok" : "observer_fail"));
                    finish(service);
                }
            }
        }
    }

    private static void retryAfterRegistrationFailure() {
        AutoTapService service = fixture(1);
        service.resolver.registrationFailure = new SecurityException("denied");
        service.onServiceConnected();
        screen(service, 1, 11000);
        service.resolver.registrationFailure = null;
        // When: another connection callback retries after an unobserved gap.
        service.onServiceConnected();
        // Then: the screen cache from before the retry is invalidated.
        state(-1, AutoBlockerWidget.SRC_SCREEN);
        equal(2, service.resolver.registrations);
        finish(service);
    }

    private static void duplicateConnectionKeepsFreshScreen() {
        AutoTapService service = fixture(-1);
        service.onServiceConnected();
        screen(service, 1, 11000);
        // When: Android repeats connection while the observer is still registered.
        service.onServiceConnected();
        // Then: no new gap, registration or cache invalidation.
        state(1, AutoBlockerWidget.SRC_SCREEN);
        equal(1, service.resolver.registrations);
        finish(service);
    }

    private static void reconnectAfterUnbind() {
        AutoTapService service = fixture(-1);
        service.onServiceConnected();
        screen(service, 1, 11000);
        service.onUnbind(new android.content.Intent());
        service.resolver.registrationFailure = new SecurityException("denied");
        // When: the same instance reconnects after an explicit disconnect.
        service.onServiceConnected();
        // Then: even a prior screen-confirmed On cannot survive the gap.
        state(-1, AutoBlockerWidget.SRC_SCREEN);
        equal(true, StateLog.events.contains("gap_unknown"));
        finish(service);
    }

    private static void screen(AutoTapService service, int checked, long at) {
        SystemClock.now = at;
        AccessibilityNodeInfo root = new AccessibilityNodeInfo();
        root.packageName = AutoTapService.RAMPART_PKG;
        AccessibilityNodeInfo toggle = new AccessibilityNodeInfo();
        toggle.checked = checked == 1;
        root.nodes.put(AutoTapService.SWITCH_ID, Collections.singletonList(toggle));
        service.root = root;
        service.onAccessibilityEvent(new AccessibilityEvent());
    }

    private static void notify(AutoTapService service, long at) {
        SystemClock.now = at;
        service.resolver.notifyChange();
    }

    private static void screenOverridesInference() {
        for (boolean registrationFails : new boolean[] {false, true}) {
            for (int observed : new int[] {0, 1}) {
                AutoTapService service = fixture(1);
                service.resolver.globals.put(Settings.Global.ADB_ENABLED, 1);
                if (registrationFails) service.resolver.registrationFailure = new SecurityException("denied");
                service.onServiceConnected();
                // When: a real screen value agrees or disagrees with debug inference.
                screen(service, observed, 11000);
                // Then: the screen is authoritative in either case.
                state(observed, AutoBlockerWidget.SRC_SCREEN);
                equal(true, StateLog.events.contains(observed == 0 ? "screen_confirm" : "mismatch_override"));
                finish(service);
            }
        }
    }

    private static void notificationBeforeScreen() {
        for (int observed : new int[] {0, 1}) {
            AutoTapService service = fixture(-1);
            service.resolver.globals.put(Settings.Global.ADB_ENABLED, 1);
            service.onServiceConnected();
            notify(service, 11000);
            // When: the screen follows a notification-based flip.
            screen(service, observed, 11001);
            // Then: matching inference is promoted; conflicting inference is overridden.
            state(observed, AutoBlockerWidget.SRC_SCREEN);
            equal(true, StateLog.events.contains(observed == 1 ? "screen_confirm" : "mismatch_override"));
            finish(service);
        }
    }

    private static void screenBeforeNotification() {
        for (long delta : new long[] {1, 2000, 2001}) {
            AutoTapService service = fixture(-1);
            service.onServiceConnected();
            screen(service, 1, 11000);
            // When: a notification arrives inside, at or after the authority limit.
            notify(service, 11000 + delta);
            // Then: the existing inclusive 2s authority rule is preserved.
            state(delta <= 2000 ? 1 : 0, delta <= 2000 ? AutoBlockerWidget.SRC_SCREEN : AutoBlockerWidget.SRC_FLIP);
            finish(service);
        }
    }

    private static void notificationBoundaries() {
        for (long delta : new long[] {1, 999, 1000, 1001}) {
            AutoTapService service = fixture(-1);
            service.resolver.globals.put(Settings.Global.ADB_ENABLED, 1);
            service.onServiceConnected();
            notify(service, 11000); // Off -> On, no screen authority.
            // When: a second notification hits the dedup boundary.
            notify(service, 11000 + delta);
            // Then: only intervals strictly below 1s are suppressed.
            state(delta < 1000 ? 1 : 0, AutoBlockerWidget.SRC_FLIP);
            finish(service);
        }
    }

    private static void absorbedNotificationStartsDedupWindow() {
        AutoTapService service = fixture(-1);
        service.onServiceConnected();
        screen(service, 1, 11000);
        notify(service, 13000); // Absorbed at the inclusive screen limit.
        notify(service, 13999); // Still a duplicate, despite authority having expired.
        // When: another notification is 1s after the last accepted notification.
        notify(service, 14000);
        // Then: ignored duplicates did not extend the dedup window.
        state(0, AutoBlockerWidget.SRC_FLIP);
        equal(true, StateLog.events.contains("on_change_absorbed"));
        equal(true, StateLog.events.contains("on_change_dup"));
        finish(service);
    }

    private static void unknownNotificationStaysUnknown() {
        AutoTapService service = fixture(1);
        service.onServiceConnected();
        // When: a notification has no known baseline.
        notify(service, 11000);
        // Then: the service cannot infer a state from the notification alone.
        state(-1, AutoBlockerWidget.SRC_SCREEN);
        equal(true, StateLog.events.contains("on_change"));
        finish(service);
    }

    private static void hexColors() {
        // Given/When/Then: parse real production HexColor inputs and compare explicit values.
        equal(Integer.valueOf(0xFF12ABEF), HexColor.parse(" #12abEF "));
        equal(Integer.valueOf(0xFF000000), HexColor.parse("000000"));
        for (String invalid : new String[] {null, "", "#123", "#12345678", "gg0000", "123456x"}) {
            if (HexColor.parse(invalid) != null) throw new AssertionError("Accepted invalid color: " + invalid);
        }
        equal("#12ABEF", HexColor.format(0xFF12ABEF));
        equal(true, HexColor.darkText(0xFFFFFFFF));
        equal(false, HexColor.darkText(0xFF000000));
        cases++;
    }
}
