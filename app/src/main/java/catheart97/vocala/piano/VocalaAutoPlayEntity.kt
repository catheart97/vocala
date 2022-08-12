package catheart97.vocala.piano

import android.util.Log
import com.chengtao.pianoview.entity.AutoPlayEntity
import com.chengtao.pianoview.entity.Piano
import catheart97.vocala.VocalaActivity
import catheart97.vocala.data.Tone

/**
 * Implementation of AutoPlayEntity which allowes pausing of piano autoplay
 *
 * @author Ronja Schnur 
 */
class VocalaAutoPlayEntity : AutoPlayEntity
{
    companion object
    {
        private const val TAG = "Storage.Entity"
    }
    
    internal constructor(type: Piano.PianoKeyType, group: Int, position: Int, currentBreakTime: Long) : super(
        type, group, position, currentBreakTime
    )
    
    constructor(tone: Tone, duration_time: Long) : this(
        if (tone.whiteKey) Piano.PianoKeyType.WHITE else Piano.PianoKeyType.BLACK,
        tone.octave,
        if (!tone.whiteKey && tone.position > 2) tone.position - 1 else tone.position,
        duration_time
    )
    
    /**
     * Adds time to currentBreakTime
     * @param time
     */
    fun addCurrentBreakTime(time: Long)
    {
        currentBreakTime += time
    }
    
    override fun getType(): Piano.PianoKeyType
    {
        while (VocalaActivity.exercisePaused) // TRICK TO ENABLE A PAUSE FEATURE :D
        {
            try
            {
                Thread.sleep(16)
            }
            catch (e: Throwable)
            {
                Log.e(TAG, e.message!!)
            }
        }
        return super.getType()
    }
}
