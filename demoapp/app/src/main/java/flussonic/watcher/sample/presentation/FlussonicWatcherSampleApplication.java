package flussonic.watcher.sample.presentation;

import android.app.Application;
import android.content.Context;
import androidx.multidex.MultiDex;

import com.squareup.leakcanary.LeakCanary;

import flussonic.watcher.sample.BuildConfig;
import timber.log.Timber;

@SuppressWarnings("WeakerAccess")
public class FlussonicWatcherSampleApplication extends Application {

    @Override
    public void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            MultiDex.install(this);
        } catch (RuntimeException multiDexException) {
            multiDexException.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
        Timber.plant(new LoggingTree());
    }
}
