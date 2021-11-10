package flussonic.watcher.sample.presentation;

import android.annotation.SuppressLint;
import android.os.Build;
import androidx.annotation.NonNull;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import flussonic.watcher.sample.BuildConfig;
import timber.log.Timber;

class LoggingTree extends Timber.Tree {

    private static final String DEFAULT_TAG = "FLUS";
    private static final int MAX_TAG_LENGTH = 23;
    private static final Pattern ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$");

    private String getTag() {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            String className = stackTraceElement.getClassName();
            if (!className.startsWith(Timber.class.getName())
                    && !className.startsWith(getClass().getName())) {
                return createStackElementTag(className);
            }
        }
        return DEFAULT_TAG;
    }

    private String createStackElementTag(String tag) {
        if (!BuildConfig.ENABLE_MINIFY) {
            // minify disabled -- remove package name part from class name
            Matcher m = ANONYMOUS_CLASS.matcher(tag);
            if (m.find()) {
                tag = m.replaceAll("");
            }
            tag = tag.substring(tag.lastIndexOf('.') + 1);
        }
        // Tag length limit was removed in API 24.
        if (tag.length() <= MAX_TAG_LENGTH || Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return tag;
        }
        return tag.substring(tag.length() - MAX_TAG_LENGTH, tag.length());
    }

    @SuppressLint("LogNotTimber")
    @Override
    protected void log(int priority, String tag, @NonNull String message, Throwable throwable) {
        tag = getTag();

        if (BuildConfig.PRINT_LOGS_TO_LOGCAT) {
            Log.println(priority, tag, message);
            if (throwable != null) {
                Log.e(tag, message, throwable);
            }
        }
    }
}
