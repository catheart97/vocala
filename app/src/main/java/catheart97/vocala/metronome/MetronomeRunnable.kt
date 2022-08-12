package catheart97.vocala.metronome

/**
 * Class implementing metronome sound timing
 *
 * @author Ronja Schnur 
 */
internal class MetronomeRunnable(private val metronome: Metronome) : Runnable
{
    override fun run()
    {
        var beat = 0
        while (metronome.playing)
        {
            if (metronome.timeSignatureDenominator != 0)
            {
                if (beat == 0) metronome.playBing()
                else metronome.playClick()
                
                beat = (beat + 1) % metronome.timeSignatureNumerator
            }
            else
            {
                metronome.playClick()
            }
            
            try
            {
                Thread.sleep(metronome.beatDuration)
            }
            catch (e: Exception)
            {
                throw RuntimeException(e)
            }
        }
    }
}
