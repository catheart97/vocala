package catheart97.vocala.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ToneTest
{
    @Test fun testIncrement()
    {
        val c4 = Tone(4, Tone.Pitch.C, true)
        c4.inc()
        assertEquals(c4.toString(), "C#4")
        
        val fis4 = Tone(4, Tone.Pitch.F, false)
        fis4.inc()
        assertEquals(fis4.toString(), "G4")
        
        val c8 = Tone(8, Tone.Pitch.C, true)
        c8.inc()
        assertEquals(c8.toString(), "C8")
    }
    
    @Test fun testDecrement()
    {
        val c4 = Tone(4, Tone.Pitch.C, true)
        c4.dec()
        assertEquals(c4.toString(), "B3")
        
        val fis4 = Tone(4, Tone.Pitch.F, false)
        fis4.dec()
        assertEquals(fis4.toString(), "F4")
        
        val a0 = Tone(0, Tone.Pitch.A, true)
        a0.dec()
        assertEquals(a0.toString(), "A0")
    }
    
    
}
