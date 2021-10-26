package flussonic.watcher.sample;

import android.content.Context;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    public static Context getAppContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    public static Context getTestContext() {
        return InstrumentationRegistry.getContext();
    }

    @Test
    public void useAppContext() {
        Context appContext = getAppContext();
        Context testContext = getTestContext();

        assertEquals("flussonic.watcher.sample", appContext.getPackageName());
        assertEquals("flussonic.watcher.sample.test", testContext.getPackageName());
    }
}
