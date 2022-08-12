package catheart97.vocala.metronome

import android.content.Context
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import com.chengtao.pianoview.R as pianoR

import catheart97.vocala.R

/**
 * Class playing sounds for metronome
 *
 * @author Ronja Schnur 
 */
class Metronome(context: Context)
{
    internal var timeSignatureNumerator = 4
        private set
    
    internal var timeSignatureDenominator = 4
        private set
    
    var bpm = 60
        set(bpm)
        {
            if (bpm > 0) field = bpm
        }

    private val playerClick: MediaPlayer = MediaPlayer.create(context, pianoR.raw.b73)
    private val playerBing: MediaPlayer = MediaPlayer.create(context, pianoR.raw.b72)
    
    var playing = false
        internal set
    
    internal val beatDuration: Long
        get() = 60000L / this.bpm
    
    private fun startPlayer(player: MediaPlayer)
    {
        if (player.isPlaying) player.stop()
        player.start()
    }
    
    internal fun playClick()
    {
        startPlayer(playerClick)
    }
    
    internal fun playBing()
    {
        startPlayer(playerBing)
    }
    
    fun setTimeSignature(numerator: Int, denominator: Int)
    {
        if (numerator > 0 && denominator >= 0)
        {
            this.timeSignatureNumerator = numerator
            this.timeSignatureDenominator = denominator
        }
    }
    
    fun stop()
    {
        this.playing = false
    }
    
    fun start()
    {
        this.playing = true
        Thread(MetronomeRunnable(this)).start()
    }
}
