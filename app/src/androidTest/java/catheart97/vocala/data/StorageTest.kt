package catheart97.vocala.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class) class StorageTest
{
    @Test fun testSaving()
    {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        Storage.loadPreferences(appContext)
    
        Storage.saveMetronomeBPM(appContext, 260)
        assertEquals(Storage.getMetronomeBPM(appContext), 260)
    
        Storage.saveMetronomeDenominator(appContext, 8)
        assertEquals(Storage.getMetronomeDenominator(appContext), 8)
    
        Storage.saveMetronomeNumerator(appContext, 6)
        assertEquals(Storage.getMetronomeNumerator(appContext), 6)
    
        // Note: All other settings are controlled by SettingsFragment
        // (therefore no save functions exists in Storage)
    }
}
