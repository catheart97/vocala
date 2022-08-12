package catheart97.vocala

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.appcompat.widget.AppCompatSpinner
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.android.AndroidAudioPlayer
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.writer.WriterProcessor
import com.chengtao.pianoview.entity.AutoPlayEntity
import com.chengtao.pianoview.listener.OnLoadAudioListener
import com.chengtao.pianoview.listener.OnPianoAutoPlayListener
import com.github.florent37.expansionpanel.ExpansionLayout
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.snackbar.Snackbar
import catheart97.vocala.data.Exercise
import catheart97.vocala.data.Tone
import catheart97.vocala.data.Storage
import catheart97.vocala.metronome.Metronome
import catheart97.vocala.metronome.MetronomeDialog
import catheart97.vocala.view.ClavierView
import catheart97.vocala.view.PitchGraph
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_button_layout.*
import kotlinx.android.synthetic.main.layout_piano.*
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.*

/**
 * VocalaActivity for Storage-Application
 *
 * @author Ronja Schnur 
 */
class VocalaActivity : VocalaAActivity()
{
    companion object
    {
        private const val TAG = "Storage.MAIN"
        
        private const val REQUEST_EXERCISES = 3
        private const val REQUEST_RECORDING = 2
        private const val REQUEST_SETTINGS = 1
        
        var exercisePaused = false
    }
    
    private var clavierView: ClavierView? = null
    private var pitchGraph: PitchGraph? = null
    private var clavierLayout: ExpansionLayout? = null
    private var playButton: ImageButton? = null
    private var recordButton: ImageButton? = null
    private var saveButton: ImageButton? = null
    
    private var metronome: Metronome? = null
    
    // STATE
    private var state = State.LISTENING
    
    // AUDIO STUFF
    private var audiodispatcherThread: Thread? = null
    private var audiodispatcher: AudioDispatcher? = null
    private var audiodispatcherStoppedManual = false // auto continuation
    
    private var loadedRecord: File? = null
    private var loadedRecordSaved = false
    private var loadedRecordPaused = false
    
    private var loadedExercise: Exercise? = null
    private var loadedExerciseList: ArrayList<AutoPlayEntity?>? = null
    private var cascadeExercise = false
    private var lowestExercise = Tone.getNote(0)
    private var highestExercise = Tone.getNote(0)
    private var waitTimeExercise = 3
    
    /**
     * @return tmp record file instance
     */
    private val tempWAVFile: File
        get() = File(Storage.getDirectory(this, Storage.Directory.DIR_TMP), "tmp.wav")
    
    private enum class State
    {
        PLAYING_REC, LOADED_REC, PLAYING_EX, LOADED_EX, RECORDING, LISTENING
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        when (requestCode)
        {
            REQUEST_SETTINGS -> this.updatePreferences()
            
            REQUEST_RECORDING -> if (resultCode == Activity.RESULT_OK)
            {
                loadedRecord = File(
                    Storage.getDirectory(
                        this, Storage.Directory.DIR_REC
                    ), data!!.data!!.toString()
                )
                loadRecordData()
            }
            
            REQUEST_EXERCISES -> if (resultCode == Activity.RESULT_OK)
            {
                val exercise = data!!.getSerializableExtra("EXTRA_EXERCISE") as Exercise?
                
                if (exercise!!.sheetSize > 0) onLoadExercise(exercise)
                else showMessage(getString(R.string.empty_exercise))
            }
            
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
    
    private fun initMetronome()
    {
        metronome = Metronome(this)
    }
    
    /**
     * Show message on SnackBar
     *
     * @param s message
     */
    private fun showMessage(s: String)
    {
        Snackbar.make(findViewById(R.id.layout_coord), s, Snackbar.LENGTH_LONG).show()
    }
    
    /**
     * init buttons
     */
    private fun initButtons()
    {
        saveButton = button_save
        playButton = button_play
        recordButton = button_record
        
        setState(State.LISTENING)
        
        saveButton!!.setOnClickListener {
            if (state == State.LOADED_REC && !loadedRecordSaved && loadedRecord != null)
            {
                val destinationFile = File(
                    Storage.getDirectory(
                        this, Storage.Directory.DIR_REC
                    ), String.format("REC %s.wav", Date(loadedRecord!!.lastModified()))
                )
                if (loadedRecord!!.renameTo(destinationFile))
                {
                    loadedRecord = destinationFile
                    loadedRecordSaved = true
                    updateButtons()
                    showMessage(getString(R.string.Record) + destinationFile.name + getString(R.string.Saved))
                }
                else
                {
                    showMessage(getString(R.string.save_fail))
                }
            }
        }
        
        playButton!!.setOnClickListener {
            when (state)
            {
                State.RECORDING, State.LISTENING ->
                {
                }
                
                State.PLAYING_REC ->
                {
                    loadedRecordPaused = !loadedRecordPaused
                    updateButtons()
                }
                
                State.LOADED_REC ->
                {
                    setState(State.PLAYING_REC)
                    dispatcherInit()
                }
                
                State.LOADED_EX ->
                {
                    clavierView!!.autoPlay(loadedExerciseList)
                    setState(State.PLAYING_EX)
                    clavierLayout!!.expand(true)
                }
                
                State.PLAYING_EX ->
                {
                    exercisePaused = !exercisePaused
                    updateButtons()
                }
            }
        }
        
        recordButton!!.setOnClickListener {
            when (state)
            {
                State.LOADED_EX, State.LOADED_REC, State.LISTENING ->
                {
                    setState(State.RECORDING)
                    removeTempWAVFile()
                    loadedRecordSaved = false
                    dispatcherInit()
                }
                
                State.RECORDING ->
                {
                    setState(State.LOADED_REC)
                    loadedRecord = tempWAVFile
                    loadedRecordSaved = false
                    dispatcherInit()
                }
                
                State.PLAYING_REC ->
                {
                    setState(State.LOADED_REC)
                    loadedRecordPaused = false
                    dispatcherInit()
                }
                
                State.PLAYING_EX ->
                {
                    clavierStopAutoplay()
                    updateButtons()
                }
            }
        }
        
    }
    
    /**
     * updates buttons depending on state
     */
    private fun updateButtons()
    {
        when (state)
        {
            State.LISTENING, State.LOADED_EX, State.LOADED_REC ->
            {
                recordButton!!.setImageDrawable(getDrawable(R.drawable.ic_fluent_record_24_filled))
                playButton!!.setImageDrawable(getDrawable(R.drawable.ic_fluent_play_24_filled))
            }
            
            State.RECORDING ->
            {
                recordButton!!.setImageDrawable(getDrawable(R.drawable.ic_fluent_stop_24_filled))
                playButton!!.setImageDrawable(getDrawable(R.drawable.ic_fluent_play_24_filled))
            }
            
            else -> // PLAYING EX AND PLAYING_REC
            {
                recordButton!!.setImageDrawable(getDrawable(R.drawable.ic_fluent_stop_24_filled))
                if (if (state == State.PLAYING_EX) !exercisePaused
                    else !loadedRecordPaused
                )
                {
                    playButton!!.setImageDrawable(getDrawable(R.drawable.ic_fluent_pause_24_filled))
                }
                else
                {
                    playButton!!.setImageDrawable(getDrawable(R.drawable.ic_fluent_play_24_filled))
                }
                return
            }
        }
        
        saveButton!!.isEnabled = false
        
        when (state)
        {
            State.LISTENING ->
            {
                playButton!!.isEnabled = this.loadedRecordPaused
            }
            
            State.RECORDING ->
            {
                playButton!!.isEnabled = false
            }
            
            State.LOADED_REC ->
            {
                if (!loadedRecordSaved) saveButton!!.isEnabled = true
                playButton!!.isEnabled = true
            }
            
            State.LOADED_EX, State.PLAYING_EX, State.PLAYING_REC ->
            {
                playButton!!.isEnabled = true
            }
        }
    }
    
    /**
     * Sets loaded exercise and does needed initialisations like
     * dispatcher reinitialisation and state setting
     *
     * @param e
     */
    private fun setLoadedExercise(e: Exercise?)
    {
        if (e != null)
        {
            loadedExercise = e
            loadedExerciseList =
                if (cascadeExercise) e.toAutoPlayList(lowestExercise, highestExercise, waitTimeExercise)
                else e.toAutoPlayList()
            
            setState(State.LOADED_EX)
            dispatcherInit()
        }
        else
        {
            loadedExercise = null
            loadedExerciseList = null
            setState(State.LISTENING)
            dispatcherInit()
        }
    }
    
    @SuppressLint("InflateParams") private fun onLoadExercise(exercise: Exercise)
    {
        val builder = AlertDialog.Builder(this)
        
        val inflater = layoutInflater
        
        val layout = inflater.inflate(R.layout.layout_load_exercise_dialog, null)
        
        val toneNames = arrayOfNulls<String>(88)
        for (i in 0..87) toneNames[i] = Tone.getNote(i).toString()
        
        val lowest = layout.findViewById<AppCompatSpinner>(R.id.lowest_tone)
        lowest.adapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, android.R.id.text1, toneNames
        )
        lowest.setSelection(Storage.getLowestTone(this)!!.pianoKeyNumber)
        
        val highest = layout.findViewById<AppCompatSpinner>(R.id.highest_tone)
        highest.adapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, android.R.id.text1, toneNames
        )
        highest.setSelection(Storage.getHighestTone(this)!!.pianoKeyNumber)
        
        val waitSlider = layout.findViewById<AppCompatSeekBar>(R.id.wait_time)
        waitSlider.max = 7
        waitSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener
        {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean)
            {
                layout.findViewById<TextView>(R.id.wait_time_indicator).text =
                    StringBuilder().append(progress + 3).append("s").toString()
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar)
            {
            }
            
            override fun onStopTrackingTouch(seekBar: SeekBar)
            {
            }
        })
        waitSlider.progress = Storage.getExerciseWaitTime(this) - 3
        
        val cascade = layout.findViewById<Switch>(R.id.cascade_exercise)
        cascade.setOnClickListener {
            waitSlider.isEnabled = cascade.isChecked
            lowest.isEnabled = cascade.isChecked
            highest.isEnabled = cascade.isChecked
        }
        
        builder.setView(layout).setPositiveButton(android.R.string.ok) { _, _ ->
            cascadeExercise = cascade.isChecked
            lowestExercise = Tone.getNote(lowest.selectedItemPosition)
            highestExercise = Tone.getNote(highest.selectedItemPosition)
            waitTimeExercise = waitSlider.progress + 3
            
            setLoadedExercise(exercise)
            clavierLayout!!.expand(true)
            showMessage(getString(R.string.Exercise) + loadedExercise!!.name + getString(R.string.loaded))
        }.setNegativeButton(
            android.R.string.cancel
        ) { dialog, _ -> dialog.cancel() }.setMessage("Load Exercise")
        
        builder.create().show()
    }
    
    /**
     * Sets state and updates buttons
     *
     * @param state
     */
    private fun setState(state: State)
    {
        this.state = state
        updateButtons()
        Log.d(TAG, "State changed: " + state.name)
    }
    
    /**
     * Deletes tmp record
     */
    private fun removeTempWAVFile()
    {
        tempWAVFile.delete()
    }
    
    /**
     * Loads and/or updates preferences values depending for this particular activity
     */
    private fun updatePreferences()
    {
        Storage.loadPreferences(this)
        for (set in pitchGraph!!.lineData.dataSets)
        {
            val lineSet = set as LineDataSet
            lineSet.color = Storage.getGraphColor(this)
        }
        pitchGraph!!.initLineLimits(pitchGraph!!.axisLeft)
        if (loadedExercise != null) setState(State.LISTENING)
    }
    
    /**
     * Kills current dispatcher if available
     */
    private fun dispatcherKill()
    {
        if (audiodispatcherThread != null && audiodispatcher != null && audiodispatcherThread!!.isAlive)
        {
            audiodispatcherStoppedManual = true // Must be set before stop() being called
            audiodispatcher!!.stop()
            try
            {
                audiodispatcherThread!!.join()
            }
            catch (e: InterruptedException)
            {
            }
        }
    }
    
    /**
     * Create new AudioDispatcher instance from given state
     */
    private fun dispatcherCreate()
    {
        try
        {
            when (state)
            {
                State.PLAYING_REC ->
                {
                    audiodispatcher = AudioDispatcherFactory.fromPipe(
                        loadedRecord!!.absolutePath,
                        Storage.RECORD_SAMPLERATE,
                        Storage.RECORD_BUFFER_SIZE,
                        Storage.RECORD_BUFFER_OVERLAP
                    )
                    return
                }
                State.RECORDING, State.LISTENING, State.LOADED_REC, State.PLAYING_EX, State.LOADED_EX ->
                {
                    audiodispatcher = AudioDispatcherFactory.fromDefaultMicrophone(
                        Storage.RECORD_SAMPLERATE, Storage.RECORD_BUFFER_SIZE, Storage.RECORD_BUFFER_OVERLAP
                    )
                    return
                }
                else ->
                {
                    audiodispatcher = AudioDispatcherFactory.fromDefaultMicrophone(
                        Storage.RECORD_SAMPLERATE, Storage.RECORD_BUFFER_SIZE, Storage.RECORD_BUFFER_OVERLAP
                    )
                    return
                }
            }
        }
        catch (e: Throwable)
        {
            Log.e(TAG, e.message!!)
        }
        
    }
    
    private fun dispatcherAddPitchProcessor()
    {
        if (audiodispatcher != null)
        {
            audiodispatcher!!.addAudioProcessor(
                PitchProcessor(
                    Storage.getPitchAlgorithm(this),
                    Storage.RECORD_SAMPLERATE.toFloat(),
                    Storage.RECORD_BUFFER_SIZE,
                    pitchGraph
                )
            )
        }
    }
    
    /**
     * Add recording audio processor which writes to tmp.wav file in tmp directory
     */
    private fun dispatcherAddWriterProcessor()
    {
        try
        {
            audiodispatcher!!.addAudioProcessor(
                WriterProcessor(
                    audiodispatcher!!.format, RandomAccessFile(tempWAVFile, "rw")
                )
            )
        }
        catch (e: Throwable)
        {
            Log.e(TAG, e.message!!)
        }
        
    }
    
    /**
     * Add AudioPlayer processor for current dispatcher
     */
    private fun dispatcherAddAndroidAudioPlayer()
    {
        try
        {
            audiodispatcher!!.addAudioProcessor(
                AndroidAudioPlayer(
                    audiodispatcher!!.format, Storage.RECORD_BUFFER_SIZE, AudioManager.STREAM_MUSIC
                )
            )
        }
        catch (e: Throwable)
        {
            Log.e(TAG, e.message!!)
        }
        
    }
    
    /**
     * Adds PauseProcessor for audio playing
     */
    private fun dispatcherAddPauseProcessor()
    {
        try
        {
            audiodispatcher!!.addAudioProcessor(object : AudioProcessor
            {
                override fun process(audioEvent: AudioEvent): Boolean
                {
                    while (loadedRecordPaused)
                    {
                        try
                        {
                            Thread.sleep(16) // this pauses the playing audio thread #Active waiting
                        }
                        catch (e: Throwable)
                        {
                            Log.e(TAG, e.message!!)
                        }
                        
                    }
                    return true
                }
                
                override fun processingFinished()
                {
                }
            })
        }
        catch (e: Throwable)
        {
            Log.e(TAG, e.message!!)
        }
        
    }
    
    /**
     * Start current dispatcher
     */
    private fun dispatcherStart()
    {
        audiodispatcherThread = Thread {
            audiodispatcherStoppedManual = false
            audiodispatcher!!.run()
            if (!audiodispatcherStoppedManual)
            // for continuing graph updating
            {
                runOnUiThread {
                    // DISPATCHER FINISHED
                    if (state == State.PLAYING_REC)
                    {
                        this@VocalaActivity.setState(State.LOADED_REC)
                        this@VocalaActivity.dispatcherInit(false)
                    }
                }
            }
        }
        audiodispatcherThread!!.start()
    }
    
    /**
     * Adds AudioProcessors depending on State
     */
    private fun dispatcherAddProcessors()
    {
        when (state)
        {
            State.LOADED_EX, State.PLAYING_EX, State.LOADED_REC, State.LISTENING -> dispatcherAddPitchProcessor()
            
            State.RECORDING ->
            {
                dispatcherAddWriterProcessor()
                dispatcherAddPitchProcessor()
            }
            
            State.PLAYING_REC ->
            {
                dispatcherAddAndroidAudioPlayer()
                dispatcherAddPitchProcessor()
                dispatcherAddPauseProcessor()
            }
        }
    }
    
    /**
     * Initialized pitch recognition
     *
     * @param kill_old (whether the old should be tried to kill (most times yes)
     */
    private fun dispatcherInit(kill_old: Boolean = true)
    {
        AndroidFFMPEGLocator(this)
        
        if (kill_old) dispatcherKill()
        
        dispatcherCreate()
        
        dispatcherAddProcessors()
        
        dispatcherStart()
    }
    
    /**
     * Initializes piano view and corresponding sound, seekbar etc.
     */
    private fun clavierInit()
    {
        clavierLayout = findViewById(R.id.piano_explayout)
        
        clavierView = findViewById(R.id.clavier_view)
        clavierView!!.setAutoPlayListener(object : OnPianoAutoPlayListener
        {
            override fun onPianoAutoPlayStart()
            {
            }
            
            override fun onPianoAutoPlayEnd()
            {
                setState(State.LOADED_EX)
                setLoadedExercise(loadedExercise)
                dispatcherInit()
            }
        })
        
        Log.e("TAG", "Initialized clavier ending.")
        
        clavierView!!.setLoadAudioListener(object : OnLoadAudioListener
        {
            override fun loadPianoAudioStart()
            {
            }
            
            override fun loadPianoAudioFinish()
            {
                showMessage(getString(R.string.piano_ready))
            }
            
            override fun loadPianoAudioError(e: Exception)
            {
            }
            
            override fun loadPianoAudioProgress(i: Int)
            {
            }
        })
        
        clavierView!!.setSeekbar(piano_seekbar)
        
        Handler().postDelayed({
            clavierLayout!!.collapse(true)
            pitchGraph!!.invalidate()
            clavierLayout!!.addListener { _, expanded ->
                val midRange =
                    (Storage.getLowestTone(this)!!.pianoKeyNumber + Storage.getHighestTone(this)!!.pianoKeyNumber) / 2
                if (expanded)
                {
                    pitchGraph!!.zoom(
                        1.0f, 4.0f, 0.0f, midRange.toFloat(), YAxis.AxisDependency.LEFT
                    )
                }
                else
                {
                    pitchGraph!!.zoom(
                        1.0f, -2.0f, 0.0f, midRange.toFloat(), YAxis.AxisDependency.LEFT
                    )
                }
                pitchGraph!!.invalidate()
            }
        }, 500)
    }
    
    /**
     * Stop PianoView Autoplay
     */
    private fun clavierStopAutoplay()
    {
        if (state == State.PLAYING_EX)
        {
            exercisePaused = false // make sure is running to stop looping
            loadedExerciseList!!.fill(null) // set whole exercise to null
            try
            {
                Thread.sleep(16) // wait for execution finish
            }
            catch (e: Throwable)
            {
                Log.e(TAG, e.message!!)
            }
            
            setLoadedExercise(loadedExercise)
            setState(State.LOADED_EX)
        }
    }
    
    override fun onBackPressed()
    {
        if (this.clavierLayout!!.isExpanded) clavierLayout!!.collapse(true)
        else super.onBackPressed()
    }
    
    private fun graphInit()
    {
        pitchGraph = findViewById(R.id.graph_view)
        pitchGraph!!.setToneLabel(pitch_tone)
        pitchGraph!!.setFrequencyLabel(pitch_frequency)
    }
    
    private fun loadRecordData(name: String = "")
    {
        if (loadedRecord!!.exists())
        {
            setState(State.LOADED_REC)
            loadedRecordSaved = true
            loadedRecordPaused = false
            dispatcherInit()
            if (name != "") showMessage(getString(R.string.Record) + name + getString(R.string.loaded))
            else showMessage(getString(R.string.Record) + loadedRecord!!.name + getString(R.string.loaded))
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        
        Storage.loadPreferences(this)
        
        setContentView(R.layout.activity_main)
        
        setSupportActionBar(layout_actionbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(false) // app drawer
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_fluent_text_align_justify_20_regular)
        
        initButtons()
        graphInit()
        initMetronome()
        clavierInit()
        
        loadFiles()
    }
    
    private fun loadFiles()
    {
        // Handle input intents
        if (intent.action == Intent.ACTION_VIEW && intent.hasExtra(getString(R.string.contentType)))
        {
            if (intent.extras!![getString(R.string.contentType)]!!.toString() == "wav")
            {
                // Copy Data
                val uri: Uri = intent.data!!
                
                val inputStream = contentResolver.openInputStream(uri)
                loadedRecord = File(Storage.getDirectory(this, Storage.Directory.DIR_TMP), "copy.wav")
                val outputStream = FileOutputStream(loadedRecord)
                
                val buffer = ByteArray(1024)
                var len = inputStream!!.read(buffer)
                while (len != -1)
                {
                    outputStream.write(buffer, 0, len)
                    len = inputStream.read(buffer)
                }
                
                inputStream.close()
                outputStream.close()
                
                loadRecordData(uri.path!!.substring(uri.path!!.lastIndexOf("/") + 1))
            }
            else if (intent.extras!![getString(R.string.contentType)]!!.toString() == "exercise")
            {
                val uri: Uri = intent.data!!
                
                val inputStream = contentResolver.openInputStream(uri)
                
                var i = 0
                do
                {
                    loadedRecord = File(
                        Storage.getDirectory(this, Storage.Directory.DIR_EXERCISE), "imported-" + i++ + ".exercise"
                    )
                }
                while (loadedRecord!!.exists())
                
                val outputStream = FileOutputStream(loadedRecord)
                val buffer = ByteArray(1024)
                var len = inputStream!!.read(buffer)
                while (len != -1)
                {
                    outputStream.write(buffer, 0, len)
                    len = inputStream.read(buffer)
                }
                
                inputStream.close()
                outputStream.close()
                
                showMessage("Successfully imported exercise.")
            }
        }
    }
    
    override fun onResume()
    {
        super.onResume()
        dispatcherInit()
    }
    
    override fun onPause()
    {
        super.onPause()
        dispatcherKill()
        if (state == State.PLAYING_EX)
        {
            clavierStopAutoplay()
            updateButtons()
        }
    }
    
    override fun onDestroy()
    {
        super.onDestroy()
        
        // Clean up
        val file = Storage.getDirectory(this, Storage.Directory.DIR_TMP)
        for (f in file.listFiles()!!) f.delete()
        
        if (clavierView != null) clavierView!!.releaseAutoPlay()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        menuInflater.inflate(R.menu.actionbar_menu, menu)
        Handler().post {
            findViewById<View>(R.id.show_metronome)?.setOnLongClickListener { _ ->
                MetronomeDialog().show(supportFragmentManager, "Metronome")
                true
            }
        }
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            R.id.show_metronome ->
            {
                if (metronome!!.playing)
                {
                    metronome!!.stop()
                }
                else
                {
                    metronome!!.bpm = Storage.getMetronomeBPM(this)
                    metronome!!.setTimeSignature(
                        Storage.getMetronomeNumerator(this), Storage.getMetronomeDenominator(this)
                    )
                    metronome!!.start()
                }
                return true
            }
            R.id.show_settings ->
            {
                startActivityForResult(
                    Intent(this, SettingsActivity::class.java), REQUEST_SETTINGS
                )
                overridePendingTransition(R.anim.left2right_out, R.anim.hold)
                return true
            }
            R.id.show_recordings ->
            {
                startActivityForResult(
                    Intent(this, RecordingsActivity::class.java), REQUEST_RECORDING
                )
                overridePendingTransition(R.anim.left2right_out, R.anim.hold)
                return true
            }
            R.id.show_exercises ->
            {
                startActivityForResult(
                    Intent(this, ExercisesActivity::class.java), REQUEST_EXERCISES
                )
                overridePendingTransition(R.anim.left2right_out, R.anim.hold)
                return true
            }
            else ->
            {
                return super.onOptionsItemSelected(item)
            }
        }
    }
}