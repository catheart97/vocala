@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package catheart97.vocala.data

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import androidx.preference.PreferenceManager
import androidx.core.content.ContextCompat
import be.tarsos.dsp.pitch.PitchProcessor
import catheart97.vocala.R
import catheart97.vocala.view.VocalRangePreference
import java.io.File

/**
 * Preferences and public constants class for Storage application
 *
 * @author Ronja Schnur 
 */
object Storage
{
    // PUBLIC /////////////////////////////////////////////////////////////////////////////////////
    const val RECORD_SAMPLERATE = 44100
    val RECORD_BUFFER_SIZE = AudioRecord.getMinBufferSize(
        RECORD_SAMPLERATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
    )
    const val RECORD_BUFFER_OVERLAP = 0 // MUST BE SET TO 0 TO MAKE WRITER PROCESSOR WORK
    
    // PRIVATE ////////////////////////////////////////////////////////////////////////////////////
    private const val OCTAVE_KEYS = 12
    private const val METRONOME_BPM_KEY = "setting_metronome_bpm_key"
    private const val METRONOME_NUMERATOR_KEY = "setting_metronome_numerator_key"
    private const val METRONOME_DENOMINATOR_KEY = "setting_metronome_denominator_key"
    private const val VERSION_KEY = "setting_last_installed_version_key"
    private const val FIRST_RUN_MAIN_KEY = "setting_first_run_main_key"
    private const val FIRST_RUN_EXERCISES_VIEW_KEY = "setting_first_run_exercises_view_key"
    
    // DATA ///////////////////////////////////////////////////////////////////////////////////////
    private var graph_key_color: IntArray = IntArray(0)
    private var graphColor: Int = 0
    var rangeColor: Int = 0
        private set
    var lowestTone: Tone = Tone.getNote(0)
        private set
    var highestTone: Tone = Tone.getNote(0)
        private set
    private var pitchAlgorithm: PitchProcessor.PitchEstimationAlgorithm = PitchProcessor.PitchEstimationAlgorithm.YIN
    private var exerciseWaitTime: Int = 0
    private var firstRunMain = false
    private var firstRunExercisesView = false
    private var metronomeBPM = 80
    private var metronomeNumerator = 4
    private var showDefaultExercises = true
    var metronomeDenominator = 4
    var lastAppVersion: String = ""
    val a4Frequency: Int
        get() = Tone.a4Frequency
    
    enum class Directory
    {
        DIR_REC, // directory for recordings only
        DIR_EXERCISE, // directory for exercises-de only
        DIR_TMP; // directory for temporary files
        
        override fun toString(): String
        {
            return when (this)
            {
                // returns actual folder name
                DIR_REC -> "rec"
                DIR_EXERCISE -> ""
                else -> "tmp"
            }
        }
    }
    
    // STATIC METHODS /////////////////////////////////////////////////////////////////////////////
    /**
     * Loads all preferences at once
     * @param[context] the application context
     */
    fun loadPreferences(context: Context)
    {
        loadGraphColor(context)
        loadVocalRange(context)
        loadRangeColor(context)
        loadKeyColor(context)
        loadPitchAlgorithm(context)
        loadA4Frequency(context)
        loadFirstRun(context)
        loadLastAppVersion(context)
        loadMetronomeBPM(context)
        loadMetronomeDenominator(context)
        loadMetronomeNumerator(context)
        loadExerciseWaitTime(context)
        loadShowDefaultExercises(context)
    }
    
    /**
     * @param[context] Activity Context
     * @return true exactly one time after installation
     */
    private fun loadFirstRun(context: Context)
    {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        firstRunMain = preferences.getBoolean(FIRST_RUN_MAIN_KEY, true)
        firstRunExercisesView = preferences.getBoolean(FIRST_RUN_EXERCISES_VIEW_KEY, true)
    }
    
    fun loadShowDefaultExercises(context: Context) : Boolean
    {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        showDefaultExercises = preferences.getBoolean(context.getString(R.string.show_preset_exercises), true)
        return showDefaultExercises
    }
    
    /**
     * @param[context] Activity Context
     * @return true exactly one time after installation
     */
    fun isMainFirstRun(context: Context): Boolean
    {
        val value = firstRunMain
        firstRunMain = false
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().putBoolean(FIRST_RUN_MAIN_KEY, false).apply()
        return value
    }
    
    /**
     * @param[context] Activity Context
     * @return true exactly one time after installation
     */
    fun isExercisesViewFirstRun(context: Context): Boolean
    {
        val result = firstRunExercisesView
        firstRunExercisesView = false
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().putBoolean(FIRST_RUN_EXERCISES_VIEW_KEY, false).apply()
        return result
    }
    
    private fun loadExerciseWaitTime(context: Context): Int
    {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        exerciseWaitTime = preferences.getInt(context.getString(R.string.setting_exercise_wait_time_key), 4)
        return exerciseWaitTime
    }
    
    fun getExerciseWaitTime(context: Context): Int
    {
        return loadExerciseWaitTime(context)
    }
    
    private fun loadA4Frequency(context: Context): Int
    {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        Tone.a4Frequency = preferences.getInt(
            context.getString(R.string.setting_a4_frequency_key), 440
        )
        return Tone.a4Frequency
    }
    
    fun getA4Frequency(context: Context): Int
    {
        return loadA4Frequency(context)
    }
    
    private fun loadPitchAlgorithm(context: Context): PitchProcessor.PitchEstimationAlgorithm
    {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        pitchAlgorithm = PitchProcessor.PitchEstimationAlgorithm.valueOf(
            preferences.getString(
                context.getString(R.string.setting_algo_key), "YIN"
            )!!
        )
        return pitchAlgorithm
    }
    
    fun getPitchAlgorithm(context: Context): PitchProcessor.PitchEstimationAlgorithm
    {
        return loadPitchAlgorithm(context)
    }
    
    fun getKeyColor(key: Tone): Int
    {
        return when (key.pitch)
        {
            Tone.Pitch.C -> if (key.whiteKey) graph_key_color[0]
            else graph_key_color[1]
            Tone.Pitch.D -> if (key.whiteKey) graph_key_color[2]
            else graph_key_color[3]
            Tone.Pitch.E -> graph_key_color[4]
            Tone.Pitch.F -> if (key.whiteKey) graph_key_color[5]
            else graph_key_color[6]
            Tone.Pitch.G -> if (key.whiteKey) graph_key_color[7]
            else graph_key_color[8]
            Tone.Pitch.A -> if (key.whiteKey) graph_key_color[9]
            else graph_key_color[10]
            else -> graph_key_color[11]
        }
    }
    
    fun loadKeyColor(context: Context)
    {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        
        graph_key_color = IntArray(OCTAVE_KEYS)
        
        graph_key_color[0] = preferences.getInt(
            context.getString(R.string.setting_color_c_key), ContextCompat.getColor(context, R.color.red)
        )
        graph_key_color[1] = preferences.getInt(
            context.getString(R.string.setting_color_cis_key), ContextCompat.getColor(context, R.color.dark_gray)
        )
        graph_key_color[2] = preferences.getInt(
            context.getString(R.string.setting_color_d_key), ContextCompat.getColor(context, R.color.dark_gray)
        )
        graph_key_color[3] = preferences.getInt(
            context.getString(R.string.setting_color_dis_key), ContextCompat.getColor(context, R.color.dark_gray)
        )
        graph_key_color[4] = preferences.getInt(
            context.getString(R.string.setting_color_e_key), ContextCompat.getColor(context, R.color.dark_gray)
        )
        graph_key_color[5] = preferences.getInt(
            context.getString(R.string.setting_color_f_key), ContextCompat.getColor(context, R.color.dark_gray)
        )
        graph_key_color[6] = preferences.getInt(
            context.getString(R.string.setting_color_fis_key), ContextCompat.getColor(context, R.color.dark_gray)
        )
        graph_key_color[7] = preferences.getInt(
            context.getString(R.string.setting_color_g_key), ContextCompat.getColor(context, R.color.dark_gray)
        )
        graph_key_color[8] = preferences.getInt(
            context.getString(R.string.setting_color_gis_key), ContextCompat.getColor(context, R.color.dark_gray)
        )
        graph_key_color[9] = preferences.getInt(
            context.getString(R.string.setting_color_a_key), ContextCompat.getColor(context, R.color.dark_gray)
        )
        graph_key_color[10] = preferences.getInt(
            context.getString(R.string.setting_color_ais_key), ContextCompat.getColor(context, R.color.dark_gray)
        )
        graph_key_color[11] = preferences.getInt(
            context.getString(R.string.setting_color_b_key), ContextCompat.getColor(context, R.color.dark_gray)
        )
    }
    
    fun getKeyColor(context: Context, key: Tone): Int
    {
        loadKeyColor(context)
        return getKeyColor(key)
    }
    
    private fun loadRangeColor(context: Context): Int
    {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        rangeColor = preferences.getInt(
            context.getString(R.string.setting_color_range_key), ContextCompat.getColor(context, R.color.white)
        )
        return rangeColor
    }
    
    fun getRangeColor(context: Context): Int
    {
        return loadRangeColor(context)
    }
    
    fun getHighestTone(context: Context): Tone?
    {
        loadVocalRange(context)
        return highestTone
    }
    
    fun getLowestTone(context: Context): Tone?
    {
        loadVocalRange(context)
        return lowestTone
    }
    
    private fun loadVocalRange(context: Context)
    {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val rangeStr = preferences.getString(
            context.getString(R.string.setting_range_key), VocalRangePreference.defaultValue()
        )!!.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        
        lowestTone = Tone.getNote(Integer.parseInt(rangeStr[0]))
        highestTone = Tone.getNote(Integer.parseInt(rangeStr[1]))
    }
    
    private fun loadGraphColor(context: Context): Int
    {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        graphColor = preferences.getInt(
            context.getString(R.string.setting_color_graph_key), ContextCompat.getColor(context, R.color.red)
        )
        return graphColor
    }
    
    fun getGraphColor(context: Context): Int
    {
        return loadGraphColor(context)
    }
    
    private fun loadLastAppVersion(context: Context): String?
    {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        lastAppVersion = preferences.getString(
            VERSION_KEY, context.getString(R.string.app_version)
        )!!
        return lastAppVersion
    }
    
    fun updateLastAppVersion(context: Context)
    {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().putString(
            VERSION_KEY, context.getString(R.string.app_version)
        ).apply() // Updates last app version for next run
    }
    
    private fun loadMetronomeBPM(context: Context): Int
    {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        metronomeBPM = preferences.getInt(METRONOME_BPM_KEY, metronomeBPM)
        return metronomeBPM
    }
    
    fun saveMetronomeBPM(context: Context, metronome_bpm: Int)
    {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().putInt(METRONOME_BPM_KEY, metronome_bpm).apply()
        metronomeBPM = metronome_bpm
    }
    
    fun getMetronomeBPM(context: Context): Int
    {
        val tmp = loadMetronomeBPM(context)
        
        if (tmp != metronomeBPM)
        {
            saveMetronomeBPM(context, tmp)
        }
        
        return metronomeBPM
    }
    
    private fun loadMetronomeNumerator(context: Context): Int
    {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        metronomeNumerator = preferences.getInt(
            METRONOME_NUMERATOR_KEY, metronomeNumerator
        )
        return metronomeNumerator
    }
    
    fun saveMetronomeNumerator(context: Context, metronome_numerator: Int)
    {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().putInt(METRONOME_NUMERATOR_KEY, metronome_numerator).apply()
        metronomeNumerator = metronome_numerator
    }
    
    fun getMetronomeNumerator(context: Context): Int
    {
        val tmp = loadMetronomeNumerator(context)
        
        if (tmp != metronomeNumerator)
        {
            saveMetronomeNumerator(context, tmp)
        }
        
        return metronomeNumerator
    }
    
    private fun loadMetronomeDenominator(context: Context): Int
    {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        metronomeDenominator = preferences.getInt(
            METRONOME_DENOMINATOR_KEY, metronomeDenominator
        )
        return metronomeDenominator
    }
    
    fun saveMetronomeDenominator(context: Context, metronome_denominator: Int)
    {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().putInt(METRONOME_DENOMINATOR_KEY, metronome_denominator).apply()
        metronomeDenominator = metronome_denominator
    }
    
    fun getMetronomeDenominator(context: Context): Int
    {
        val tmp = loadMetronomeDenominator(context)
        
        if (tmp != metronomeDenominator)
            saveMetronomeDenominator(context, tmp)
        
        return metronomeDenominator
    }
    
    fun getDirectory(context: Context, directory_type: Directory): File
    {
        val directory = File(context.filesDir, directory_type.toString())
        directory.mkdir()
        return directory
    }
    
    fun getRecord(context: Context, name: String): File
    {
        return File(getDirectory(context, Directory.DIR_REC), name)
    }
    
    fun getExercise(context: Context, name: String): File
    {
        return File(getDirectory(context, Directory.DIR_EXERCISE), name)
    }
}
