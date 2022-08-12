package catheart97.vocala.data

import com.chengtao.pianoview.entity.AutoPlayEntity
import catheart97.vocala.piano.VocalaAutoPlayEntity
import java.io.Serializable
import java.util.*

/**
 * Class representing an (vocal(a))-exercise
 *
 * @author Ronja Schnur 
 */
class Exercise(
    var name: String, var summary: String, var clef: Clef, private var sheet: ArrayList<Tone>, var up: Boolean, var bpm: Int
) : Serializable
{
    enum class Clef
    {
        BassClef, TrebleClef, TenorClef, AltoClef, SopranoClef, MezzoClef, BaritoneCClef, BaritoneFClef, SubbassClef
    }
    
    companion object
    {
        private const val MAX_BPM = 260
        private const val MIN_BPM = 30
        const val BEAT_VALUE: Int = 64
        
        /**
         * Performs a deep-copy of sheet list
         * @param[src] the source list
         * @return the copied sheet
         */
        private fun getSheetCopy(src: ArrayList<Tone>): ArrayList<Tone>
        {
            val result = ArrayList<Tone>()
            for (n in src)
            {
                result.add(Tone(n))
            }
            return result
        }
        
        /**
         * Transposes whole sheet by one half tone step
         * @param[sheet] the sheet to transpose
         * @param[up] if it should transpose up
         */
        private fun transposeSheetByOne(sheet: List<Tone>, up: Boolean)
        {
            for (n in sheet)
            {
                if (up) n.inc()
                else n.dec()
            }
        }
    }
    
    var filename: String? = null
    
    var editable = true
        private set
    
    constructor() : this("", "", Clef.TrebleClef, ArrayList<Tone>(), true, 80)
    constructor(e: Exercise) : this(e.name, e.summary, e.clef, getSheetCopy(e.sheet), e.up, e.bpm)
    
    constructor(storageString: String, editable : Boolean = true) : this()
    {
        this.editable = editable
        val str = storageString.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        name = str[0]
        summary = str[1]
        clef = Clef.valueOf(str[2])
        sheet = ArrayList()
        if (str[3].compareTo("") != 0)
        {
            for (n in str[3].split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            {
                sheet.add(Tone(n))
            }
        }
        up = Integer.parseInt(str[4]) != 0
        bpm = Integer.parseInt(str[5].replace("\n", ""))
    }
    
    val vexFlowClef: String
        get()
        {
            return when (this.clef)
            {
                Clef.BassClef -> "bass"
                Clef.SopranoClef -> "soprano"
                Clef.MezzoClef -> "mezzo-soprano"
                Clef.AltoClef -> "alto"
                Clef.TenorClef -> "tenor"
                Clef.BaritoneCClef -> "baritone-c"
                Clef.BaritoneFClef -> "baritone-f"
                Clef.SubbassClef -> "subbass"
                else -> "treble"
            }
        }
    
    
    val vexFlowNumBeats: Int
        get()
        {
            var beats = 0
            for (i in this.sheet.indices)
                beats += this.sheet[i].noteLengthInSixtyFourths
            return beats
        }
    
    val sheetSize: Int
        get() = this.sheet.size
    
    val exerciseDuration: Long
        get()
        {
            var result: Long = 0
            for (n in sheet)
                result += getNoteDuration(n.toneLength)
            return result
        }
    
    fun hasFilename(): Boolean
    {
        return this.filename != null
    }
    
    /**
     * Convert the whole exercise to a storeable String
     */
    fun toStorageString(): String
    {
        val builder = StringBuilder()
        builder.append(name).append(";")
        builder.append(summary).append(";")
        builder.append(clef).append(";")
        for (i in sheet.indices)
        {
            if (i != sheet.size - 1) builder.append(sheet[i].toStorageString()).append("_")
            else builder.append(sheet[i].toStorageString())
        }
        builder.append(";")
        builder.append(if (up) 1 else 0).append(";")
        builder.append(bpm)
        return builder.toString()
    }
    
    fun getVexFlowTone(i: Int): String
    {
        return this.sheet[i].vexFlowString
    }
    
    fun addTone(tone: Tone)
    {
        this.sheet.add(tone)
    }
    
    fun getTone(i: Int): Tone
    {
        return this.sheet[i]
    }
    
    fun popNote(): Tone?
    {
        return if (sheet.size > 0) sheet.removeAt(sheet.size - 1)
        else null
    }
    
    /**
     * decrement beats per minute
     * NOTE: Min and Max Values exist
     */
    fun decBPM()
    {
        if (bpm > MIN_BPM) bpm--
    }
    
    /**
     * decrement beats per minute
     * NOTE: Min and Max Values exist
     */
    fun incBPM()
    {
        if (bpm < MAX_BPM) bpm++
    }
    
    /**
     * Get tone duration in milliseconds
     * @param[length] The corresponding tone length
     */
    private fun getNoteDuration(length: Tone.ToneLength): Long
    {
        return when (length)
        {
            Tone.ToneLength.WHOLE -> 240000L / bpm
            Tone.ToneLength.HALF -> 120000L / bpm
            Tone.ToneLength.QUARTER -> 60000L / bpm
            Tone.ToneLength.EIGHTH -> 30000L / bpm
            Tone.ToneLength.SIXTEENTH -> 15000L / bpm
            Tone.ToneLength.THIRTY_SECOND -> 7500L / bpm
            else -> 3750L / bpm
        }
    }
    
    fun toAutoPlayList(): ArrayList<AutoPlayEntity?>
    {
        val result = ArrayList<VocalaAutoPlayEntity?>()
        
        for (i in sheet.indices)
        {
            val tone = sheet[i]
            
            if (tone.toneBreak)
            {
                if (result.size > 0) result[result.size - 1]!!.addCurrentBreakTime(getNoteDuration(tone.toneLength))
                else continue // beginning of exercise or just break
            }
            else result.add(VocalaAutoPlayEntity(tone, getNoteDuration(tone.toneLength)))
        }
        
        return ArrayList(result)
    }
    
    /**
     * Converts the sheet depending on the current exercise configuration to a list of AutoPlayEntities for
     * PianoView
     * @param[lowest] the lower bound of vocal range
     * @param[highest] the upper bound of vocal range
     * @return the list of AutoPlayEntities
     */
    fun toAutoPlayList(lowest: Tone, highest: Tone, waitTime: Int): ArrayList<AutoPlayEntity?>
    {
        val result = ArrayList<VocalaAutoPlayEntity>()
        val copiedSheet = getSheetCopy(sheet)
        var lowestTone = Collections.min(copiedSheet)
        var highestTone = Collections.max(copiedSheet)
        
        if (Tone.intervalSize(lowest, highest) < Tone.intervalSize(
                lowestTone, highestTone
            ) || copiedSheet.size == 0
        ) return ArrayList(result) // EMPTY LIST BECAUSE TO MUCH RANGE OR EMPTY SHEET
        
        // Transpose into range
        while (lowestTone < lowest) transposeSheetByOne(copiedSheet, true)
        while (highestTone > highest) transposeSheetByOne(copiedSheet, false)
        
        // Transpose to beginning of exercise
        while (if (up) lowestTone > lowest else highestTone < highest) transposeSheetByOne(copiedSheet, !up)
        
        
        while (if (up) highestTone <= highest else lowestTone >= lowest)
        {
            for (i in copiedSheet.indices)
            {
                val tone = copiedSheet[i]
                if (tone.toneBreak)
                {
                    if (result.size > 0) result[result.size - 1].addCurrentBreakTime(getNoteDuration(tone.toneLength))
                    else continue // beginning of exercise or just break
                }
                else
                {
                    result.add(
                        VocalaAutoPlayEntity(
                            tone, getNoteDuration(tone.toneLength)
                        )
                    )
                }
            }
            
            result[result.size - 1].addCurrentBreakTime(waitTime * 1000L)
            
            transposeSheetByOne(copiedSheet, up)
            highestTone = Collections.max(copiedSheet)
            lowestTone = Collections.min(copiedSheet)
        }
        return ArrayList(result)
    }
}