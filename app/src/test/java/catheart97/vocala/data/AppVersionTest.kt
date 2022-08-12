package catheart97.vocala.data

import org.junit.Assert.assertEquals
import org.junit.Test

class AppVersionTest
{
    @Test fun testComparison()
    {
        var a = AppVersion("1.2.0")
        var b = AppVersion("1.0.0")
        
        assertEquals(a.compareTo(b).toLong(), 1)
        
        a = AppVersion("2.2.0")
        b = AppVersion("1.1.0")
        
        assertEquals(a.compareTo(b).toLong(), 1)
        
        a = AppVersion("2.2.1")
        b = AppVersion("2.2.0")
        
        assertEquals(a.compareTo(b).toLong(), 1)
        
        a = AppVersion("1.1.0")
        b = AppVersion("1.1.0")
        
        assertEquals(a.compareTo(b).toLong(), 0)
        
        b = AppVersion("1.2.0")
        a = AppVersion("1.0.0")
        
        assertEquals(a.compareTo(b).toLong(), -1)
        
        b = AppVersion("2.2.0")
        a = AppVersion("1.1.0")
        
        assertEquals(a.compareTo(b).toLong(), -1)
        
        b = AppVersion("2.2.1")
        a = AppVersion("2.2.0")
        
        assertEquals(a.compareTo(b).toLong(), -1)
    }
}
