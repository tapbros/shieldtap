package com.gml.autoblocker;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

/** Storage/rendering boundaries only. All service decisions run in AutoTapService.java. */
final class AutoBlockerWidget {
    static final String TAG = "test";
    static final String STATE_PREFS = "state";
    static final String SRC_FLIP = "flip", SRC_DEBUG = "debug", SRC_SCREEN = "screen";
    static int state = -1;
    static String src = SRC_SCREEN;
    static int refreshes;
    static int cached(Context context) { return state; }
    static String source(Context context) { return src; }
    static void clearState(Context context) { state = -1; src = SRC_SCREEN; }
    static void saveState(Context context, boolean on, String source) {
        state = on ? 1 : 0;
        src = source;
    }
    static void refresh(Context context) { refreshes++; }
}

final class StateLog {
    static final List<String> events = new ArrayList<>();
    static void add(Context context, String event, String body) { events.add(event); }
}

final class TrampolineActivity {
    static final String PREFS = "arm", ARMED_AT = "at", ARM_TARGET = "target", ARM_RETURN = "return";
    static final int RETURN_HOME = 0, RETURN_BACK = 1;
}

final class LocaleHelper {
    static Context wrap(Context context) { return context; }
}

final class R {
    static final class string {
        static final int toast_node_missing = 1;
    }
}
