package flussonic.watcher.sample.presentation.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import io.reactivex.annotations.NonNull;

public class Settings {

    private static final String KEY_ALLOW_DOWNLOAD = "KEY_ALLOW_DOWNLOAD";
    private static final boolean DEFAULT_ALLOW_DOWNLOAD = true;

    public static boolean allowDownload(@NonNull Context context) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.getBoolean(KEY_ALLOW_DOWNLOAD, DEFAULT_ALLOW_DOWNLOAD);
    }

    public static void setAllowDownload(@NonNull Context context, boolean value) {
        SharedPreferences preferences = getPreferences(context);
        preferences.edit().putBoolean(KEY_ALLOW_DOWNLOAD, value).apply();
    }

    private static SharedPreferences getPreferences(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
