package tk.standy66.deblurit.tools;

import android.content.Context;

public final class App {
    private static Context applicationContext = null;

    public static boolean locker = false;

    public static void setApplicationContext(Context c) {
        applicationContext = c;
    }

    public static Context getApplicationContext() {
        return applicationContext;
    }

}
