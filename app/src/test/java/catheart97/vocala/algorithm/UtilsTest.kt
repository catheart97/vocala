package catheart97.vocala.algorithm

import org.junit.Assert.assertEquals
import org.junit.Test

class UtilsTest
{
    @Test fun niceDouble()
    {
        assertEquals("1,2", Utils.niceDouble(1.22222))
        assertEquals("2,0", Utils.niceDouble(2.001))
        assertEquals("-1,2", Utils.niceDouble(-1.25))
        assertEquals("0,0", Utils.niceDouble(0.0))
        
    }
    
    @Test fun getDurationString()
    {
        assertEquals(Utils.getDurationString(2000), "00:00:02")
        assertEquals(Utils.getDurationString(2500), "00:00:02")
        assertEquals(Utils.getDurationString(60000), "00:01:00")
        assertEquals(Utils.getDurationString(3600000), "01:00:00")
        assertEquals(Utils.getDurationString(3661000), "01:01:01")
    }
}
