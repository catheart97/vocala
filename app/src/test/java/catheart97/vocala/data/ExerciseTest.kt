package catheart97.vocala.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class ExerciseTest
{
    @Test fun testDuration()
    {
        var toneList = ArrayList<Tone>()
        
        toneList.add(Tone(4, Tone.Pitch.C, true, Tone.ToneLength.QUARTER, false))
        toneList.add(Tone(4, Tone.Pitch.C, true, Tone.ToneLength.QUARTER, false))
        toneList.add(Tone(4, Tone.Pitch.C, true, Tone.ToneLength.QUARTER, false))
        toneList.add(Tone(4, Tone.Pitch.C, true, Tone.ToneLength.QUARTER, false))
        
        var exercise = Exercise("", "", Exercise.Clef.TrebleClef, toneList, true, 60)
        assertEquals(4000f, exercise.exerciseDuration.toFloat(), 10f)
        
        
        toneList = ArrayList()
        
        toneList.add(Tone(4, Tone.Pitch.C, true, Tone.ToneLength.EIGHTH, false))
        toneList.add(Tone(4, Tone.Pitch.C, true, Tone.ToneLength.EIGHTH, false))
        toneList.add(Tone(4, Tone.Pitch.C, true, Tone.ToneLength.SIXTEENTH, false))
        toneList.add(Tone(4, Tone.Pitch.C, true, Tone.ToneLength.SIXTEENTH, false))
        toneList.add(Tone(4, Tone.Pitch.C, true, Tone.ToneLength.SIXTEENTH, false))
        toneList.add(Tone(4, Tone.Pitch.C, true, Tone.ToneLength.SIXTEENTH, false))
        toneList.add(Tone(4, Tone.Pitch.C, true, Tone.ToneLength.QUARTER, false))
        toneList.add(Tone(4, Tone.Pitch.C, true, Tone.ToneLength.SIXTY_FOURTH, false))
        toneList.add(Tone(4, Tone.Pitch.C, true, Tone.ToneLength.SIXTY_FOURTH, false))
        toneList.add(Tone(4, Tone.Pitch.C, true, Tone.ToneLength.THIRTY_SECOND, false))
        toneList.add(Tone(4, Tone.Pitch.C, true, Tone.ToneLength.EIGHTH, false))
        toneList.add(Tone(4, Tone.Pitch.C, true, Tone.ToneLength.SIXTEENTH, false))
        
        
        exercise = Exercise("", "", Exercise.Clef.TrebleClef, toneList, true, 60)
        assertEquals(4000f, exercise.exerciseDuration.toFloat(), 10f)
    }
}
