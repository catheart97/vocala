package catheart97.vocala.data

import java.io.Serializable
import java.util.*
import kotlin.math.*

/**
 * Class representing a music note within range of A0 to C8
 *
 * @author Ronja Schnur 
 */
class Tone : Comparable<Tone>, Serializable
{
    var whiteKey: Boolean private set // black or white key on piano, basically same as #
    var octave: Int private set // octave currently operating in A4 specifies 440Hz
    var position: Int private set // position within the octave C = 0, D = 1 ...
    
    var toneLength: ToneLength
    var toneBreak: Boolean private set
    
    val noteLengthInSixtyFourths: Int
        get()
        {
            return when (this.toneLength)
            {
                ToneLength.WHOLE -> 64
                ToneLength.HALF -> 32
                ToneLength.EIGHTH -> 8
                ToneLength.SIXTEENTH -> 4
                ToneLength.THIRTY_SECOND -> 2
                ToneLength.SIXTY_FOURTH -> 1
                else -> 16
            }
        }
    
    val vexFlowString: String
        get()
        {
            return if (toneBreak)
            {
                "b/4"
            }
            else
            {
                StringBuilder().append(TONE[if (octave == 0) position + 5 else position].toLowerCase(
                    Locale.GERMAN)).append("/")
                    .append(octave).toString()
            }
        }
    
    val vexFlowDurationString: String
        get()
        {
            val builder = StringBuilder()
            when (toneLength)
            {
                ToneLength.WHOLE -> builder.append("w")
                ToneLength.HALF -> builder.append("h")
                ToneLength.QUARTER -> builder.append("q")
                ToneLength.EIGHTH -> builder.append("8")
                ToneLength.SIXTEENTH -> builder.append("16")
                ToneLength.THIRTY_SECOND -> builder.append("32")
                else -> builder.append("64")
            }
            if (toneBreak) builder.append("r")
            return builder.toString()
        }
    
    val pitch: Pitch
        get() = if (octave == 0)
        {
            when (position)
            {
                0 -> Pitch.A
                1 -> Pitch.B
                else -> Pitch.A
            }
        }
        else
        {
            when (position)
            {
                0 -> Pitch.C
                1 -> Pitch.D
                2 -> Pitch.E
                3 -> Pitch.F
                4 -> Pitch.G
                5 -> Pitch.A
                else -> Pitch.B
            }
        }
    
    /**
     * @return Calculates the number of Tone on a 88-Keys Piano
     */
    val pianoKeyNumber: Int
        get() = ((if (octave > 0) 3 + (octave - 1) * 12 else 0) + position + (if (position > 2) position - 1 else position) + if (whiteKey) 0 else 1)
    
    /**
     * @return Calculates the Tone frequency based on the current a4Frequency
     */
    val frequency: Double
        get() = FREQUENCY_FACTOR.pow((pianoKeyNumber - 48).toDouble()) * a4Frequency
    
    companion object
    {
        private val TONE = arrayOf("C", "D", "E", "F", "G", "A", "B")
        private val FREQUENCY_FACTOR = sqrt(sqrt(Math.cbrt(2.0)))
        
        private var frequencyA4: Int = 440
        var a4Frequency: Int
            get() = frequencyA4
            /**
             * Set global parameter of normed A4-Pitch Frequency
             * @param[frequency] the frequency
             */
            set(frequency)
            {
                if (frequency in 431..449) frequencyA4 = frequency
            }
        
        /**
         * @param frequency
         * @return Tone instance which matches frequency best
         */
        fun getNote(frequency: Double): Tone
        {
            return getNote((log10(frequency / a4Frequency) / log10(2.0) * 12.0 + 48.0).roundToLong().toInt())
        }
        
        /**
         * @param[key] number of key on 88-keys piano e.g.: A0 -> 0
         */
        fun getNote(key: Int): Tone
        {
            val tone = Tone(0, Pitch.A)
            tone.goHigher(key)
            return tone
        }
        
        /**
         * @param[fst] first tone
         * @param[snd] second tone
         *
         * @return the interval size in half tone steps between tones fst and snd
         */
        fun intervalSize(fst: Tone, snd: Tone): Int
        {
            return abs(fst.pianoKeyNumber - snd.pianoKeyNumber)
        }
    }
    
    enum class Pitch
    {
        C, D, E, F, G, A, B
    }
    
    enum class ToneLength
    {
        WHOLE, HALF, QUARTER, EIGHTH, SIXTEENTH, THIRTY_SECOND, SIXTY_FOURTH
    }
    
    constructor(tone: Tone)
    {
        whiteKey = tone.whiteKey
        octave = tone.octave
        position = tone.position
        toneLength = tone.toneLength
        toneBreak = tone.toneBreak
    }
    
    @JvmOverloads constructor(
        _octave: Int,
        pitch: Pitch,
        is_white_key: Boolean = true,
        note_length: ToneLength = ToneLength.QUARTER,
        is_break: Boolean = false
    )
    {
        whiteKey = is_white_key
        toneLength = note_length
        toneBreak = is_break
        octave = _octave
        if (octave == 0)
        {
            when (pitch)
            {
                Pitch.B ->
                {
                    position = 1
                    whiteKey = true
                }
                else -> position = 0 // set all others to lowest tone A0
            }
        }
        else
        {
            when (pitch)
            { // switch position depending on pitch
                Pitch.C -> position = 0
                Pitch.D -> position = 1
                Pitch.E ->
                {
                    position = 2
                    whiteKey = true
                }
                Pitch.F -> position = 3
                Pitch.G -> position = 4
                Pitch.A -> position = 5
                else ->
                {
                    position = 6
                    whiteKey = true
                }
            }
        }
    }
    
    /**
     * Restore tone from a stored string reference
     *
     * NOTE: Does not check for correctness
     *
     * @param[storage] Tone representation as String
     */
    constructor(storage: String)
    {
        val str = storage.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        whiteKey = Integer.parseInt(str[0]) != 0
        octave = Integer.parseInt(str[1])
        position = Integer.parseInt(str[2])
        toneLength = ToneLength.valueOf(str[3])
        toneBreak = Integer.parseInt(str[4]) != 0
    }
    
    /**
     * @return Storable representation of tone as string. Can be used with string constructor.
     */
    fun toStorageString(): String
    {
        val builder = StringBuilder()
        builder.append(if (whiteKey) 1 else 0).append("-")
        builder.append(octave).append("-")
        builder.append(position).append("-")
        builder.append(toneLength.name).append("-")
        builder.append(if (toneBreak) 1 else 0)
        return builder.toString()
    }
    
    /**
     * @return printable (human-readable) representation of Tone
     */
    override fun toString(): String
    {
        val builder = StringBuilder()
        builder.append(TONE[if (octave == 0) position + 5 else position])
        if (!whiteKey) builder.append("#")
        builder.append(octave)
        return builder.toString()
    }
    
    override fun compareTo(other: Tone): Int
    {
        return this.pianoKeyNumber.compareTo(other.pianoKeyNumber)
    }
    
    private fun decrementHelper()
    {
        if (whiteKey) position--
        whiteKey = !whiteKey
    }
    
    /**
     * Decrease Tone by one half step
     */
    fun dec()
    {
        if (octave == 0 && position == 0 && whiteKey) return // Well, there are lower notes but...
        
        when (position)
        {
            6, 5, 4 ->
            { // B A G
                decrementHelper()
                return
            }
            3 ->
            {
                if (whiteKey) position--
                whiteKey = true
                return
            }
            2, 1 ->
            { // E D
                decrementHelper()
                return
            }
            0 ->
            { //C
                if (!whiteKey)
                {
                    whiteKey = true
                    return
                }
                position = if (octave == 1) 1 else 6
                octave--
                return
            }
        }
    }
    
    /**
     * Increase tone by interval
     * @param[interval] the interval given in half tone steps (> 0)
     */
    fun goHigher(interval: Int)
    {
        for (i in 0 until interval) this.inc()
    }
    
    /**
     * Decrease tone by interval
     * @param[interval] the interval given in half tone steps (> 0)
     */
    fun goLower(interval: Int)
    {
        for (i in 0 until interval) this.dec()
    }
    
    private fun incrementHelper()
    {
        if (!whiteKey) position++
        whiteKey = !whiteKey
    }
    
    /**
     * Increase tone by half step
     */
    fun inc()
    {
        if (octave == 8 && position == 0) return
        
        if (octave == 0)
        {
            when (position)
            {
                0 ->
                {
                    incrementHelper()
                    return
                }
                1 ->
                {
                    position = 0
                    octave++
                    return
                }
            }
        }
        else
        {
            when (position)
            {
                0, 1 ->
                {
                    incrementHelper()
                    return
                }
                2 ->
                {
                    position++
                    return
                }
                3, 4, 5 ->
                {
                    incrementHelper()
                    return
                }
                6 ->
                {
                    position = 0
                    octave++
                    return
                }
            }
        }
    }
}