package catheart97.vocala.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class RecordingTest
{
    @Test fun testToggle()
    {
        val rec = Recording("Name", Date(), 200)
        val c = rec.checked
        rec.toggle()
        assertEquals(rec.checked, !c)
    }
}
