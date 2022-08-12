package catheart97.vocala

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.nex3z.togglebuttongroup.SingleSelectToggleGroup
import catheart97.vocala.data.Exercise
import catheart97.vocala.data.Tone
import catheart97.vocala.data.Storage
import catheart97.vocala.view.ScoreView
import kotlinx.android.synthetic.main.activity_exercise_view.*
import kotlinx.android.synthetic.main.activity_exercise_view_frame.*
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

/**
 * View for editing and displaying exercises-de
 *
 * @author Ronja Schnur
 */
class ExerciseViewActivity : VocalaAActivity()
{
    companion object
    {
        private const val TAG = "Storage.EXERCISES.V"
    }
    
    private var scoreView: ScoreView? = null
    private var exercise: Exercise? = null
    private var clefSpinner: Spinner? = null
    
    private lateinit var octaveSelectionGroup: SingleSelectToggleGroup
    private lateinit var noteLengthGroup: RadioGroup
    
    private var toneBreak = false
    private var toneSharp = false // indicates if tone is incremented (#)
    
    /**
     * @return octave depending on group selection
     */
    private val noteOctave: Int
        get()
        {
            return when (octaveSelectionGroup.checkedId)
            {
                R.id.octave_0 -> 0
                R.id.octave_1 -> 1
                R.id.octave_2 -> 2
                R.id.octave_3 -> 3
                R.id.octave_4 -> 4
                R.id.octave_5 -> 5
                R.id.octave_6 -> 6
                R.id.octave_7 -> 7
                else -> 8
            }
        }
    
    /**
     * @return ToneLength from current selection
     */
    private val toneLength: Tone.ToneLength
        get()
        {
            return when (noteLengthGroup.checkedRadioButtonId)
            {
                R.id.view_whole -> Tone.ToneLength.WHOLE
                R.id.view_half -> Tone.ToneLength.HALF
                R.id.view_quarter -> Tone.ToneLength.QUARTER
                R.id.view_eigth -> Tone.ToneLength.EIGHTH
                R.id.view_sixteenth -> Tone.ToneLength.SIXTEENTH
                R.id.view_thirty_second -> Tone.ToneLength.THIRTY_SECOND
                else -> Tone.ToneLength.SIXTY_FOURTH
            }
        }
    
    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.hold, R.anim.right2left_in)
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        menuInflater.inflate(R.menu.exercises_view_actionbar_menu, menu)
        return true
    }
    
    /**
     * Shows dialog to rename exercise
     */
    @SuppressLint("InflateParams") private fun editName()
    {
        val builder = AlertDialog.Builder(this)
        
        val inflater = layoutInflater
        
        val layout = inflater.inflate(R.layout.layout_edit_dialog, null)
        val inputEdit = layout.findViewById<EditText>(R.id.edit_dialog_text)
        inputEdit.setText(exercise!!.name)
        
        builder.setView(layout).setPositiveButton(android.R.string.ok) { _, _ ->
            val newName = inputEdit.editableText.toString()
            exercise!!.name = newName
            updateTitle()
        }.setNegativeButton(
            android.R.string.cancel
        ) { dialog, _ -> dialog.cancel() }.setMessage(getString(R.string.edit_title))
        
        
        val dialog = builder.create()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.window?.attributes?.windowAnimations = R.style.VocalaDialogAnimation
        dialog.show()
    }
    
    /**
     * Shows dialog for editing summary
     */
    @SuppressLint("InflateParams") private fun editSummary()
    {
        val builder = AlertDialog.Builder(this)
        
        val inflater = layoutInflater
        
        val layout = inflater.inflate(R.layout.layout_edit_dialog, null)
        val inputEdit = layout.findViewById<EditText>(R.id.edit_dialog_text)
        inputEdit.setText(exercise!!.summary)
        
        builder.setView(layout).setPositiveButton(android.R.string.ok) { _, _ ->
            val newName = inputEdit.editableText.toString()
            exercise!!.summary = newName
            updateTitle()
        }.setNegativeButton(
            android.R.string.cancel
        ) { dialog, _ -> dialog.cancel() }.setMessage(getString(R.string.edit_summary))
        
        
        val dialog = builder.create()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.window?.attributes?.windowAnimations = R.style.VocalaDialogAnimation
        dialog.show()
    }
    
    /**
     * Loads the name form exercise and sets it as title of the activity
     */
    private fun updateTitle()
    {
        title = exercise!!.name
    }
    
    /**
     * Sets Spinner selection depending on clef
     */
    private fun updateClef()
    {
        clefSpinner!!.setSelection(
            when (exercise!!.clef)
            {
                Exercise.Clef.BassClef -> 0
                Exercise.Clef.SopranoClef -> 1
                Exercise.Clef.MezzoClef -> 2
                Exercise.Clef.AltoClef -> 3
                Exercise.Clef.TenorClef -> 4
                Exercise.Clef.BaritoneCClef -> 5
                Exercise.Clef.BaritoneFClef -> 6
                Exercise.Clef.SubbassClef -> 7
                else -> 8
            }
        )
    }
    
    /**
     * Dis- and enables buttons depending on selections
     */
    private fun updateInputButtons()
    {
        val checkedId = octaveSelectionGroup.checkedId
        
        if (checkedId == R.id.octave_0)
        {
            view_button_c.isEnabled = false
            view_button_d.isEnabled = false
            view_button_e.isEnabled = false
            view_button_f.isEnabled = false
            view_button_g.isEnabled = false
            view_button_a.isEnabled = true
            view_button_b.isEnabled = !toneSharp
        }
        else if (checkedId == R.id.octave_8)
        {
            view_button_c.isEnabled = true
            view_button_d.isEnabled = false
            view_button_e.isEnabled = false
            view_button_f.isEnabled = false
            view_button_g.isEnabled = false
            view_button_a.isEnabled = false
            view_button_b.isEnabled = false
        }
        else
        {
            view_button_c.isEnabled = true
            view_button_d.isEnabled = true
            view_button_f.isEnabled = true
            view_button_g.isEnabled = true
            view_button_a.isEnabled = true
            if (!toneSharp)
            {
                view_button_b.isEnabled = true
                view_button_e.isEnabled = true
            }
            else
            {
                view_button_b.isEnabled = false
                view_button_e.isEnabled = false
            }
        }
    }
    
    /**
     * Sets up Spinner for Clef selection
     */
    private fun initClefSpinner()
    {
        clefSpinner = findViewById(R.id.clef_spin)
        
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.clef_types, android.R.layout.simple_spinner_item
        )
        
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        clefSpinner!!.adapter = adapter
        clefSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
        {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long)
            {
                exercise!!.clef = when (position)
                {
                    0 -> Exercise.Clef.BassClef
                    1 -> Exercise.Clef.SopranoClef
                    2 -> Exercise.Clef.MezzoClef
                    3 -> Exercise.Clef.AltoClef
                    4 -> Exercise.Clef.TenorClef
                    5 -> Exercise.Clef.BaritoneCClef
                    6 -> Exercise.Clef.BaritoneFClef
                    7 -> Exercise.Clef.SubbassClef
                    else -> Exercise.Clef.TrebleClef
                }
                scoreView!!.reload()
            }
            
            override fun onNothingSelected(parent: AdapterView<*>)
            {
                clefSpinner!!.setSelection(8)
            }
        }
        
        updateClef()
    }
    
    /**
     * Inits Octave selection group and does default selection
     */
    private fun initOctaveSelectionGroup()
    {
        octaveSelectionGroup = findViewById(R.id.octave_selection_group)
        octaveSelectionGroup.setOnCheckedChangeListener { _, _ -> updateInputButtons() }
        octaveSelectionGroup.check(R.id.octave_4)
    }
    
    /**
     * Inits bmp number selection
     */
    private fun initBPM()
    {
        bpmPicker.value = exercise!!.bpm
        bpmPicker.setOnValueChangedListener{ _, _, newVal ->
            exercise!!.bpm = newVal
        }
    }
    
    /**
     * Initializes up-and down-button for exercise kind selection
     */
    private fun initUpDownButton()
    {
        if (this.exercise!!.up)
        {
            view_button_up.setImageDrawable(getDrawable(R.drawable.ic_fluent_arrow_up_24_filled))
        }
        else
        {
            view_button_up.setImageDrawable(getDrawable(R.drawable.ic_fluent_arrow_down_24_filled))
        }
        
        view_button_up.setOnClickListener { v ->
            val bV = v as ImageButton
            exercise!!.up = !exercise!!.up // toggle
            
            if (this.exercise!!.up)
            {
                bV.setImageDrawable(getDrawable(R.drawable.ic_fluent_arrow_up_24_filled))
            }
            else
            {
                bV.setImageDrawable(getDrawable(R.drawable.ic_fluent_arrow_down_24_filled))
            }
        }
    }
    
    /**
     * initializes buttons for break and hash or not break and not hash
     */
    private fun initModifierButtons()
    {
        findViewById<View>(R.id.view_button_black).setOnClickListener { v ->
            val buttonView = v as ImageButton
            toneSharp = !toneSharp
            if (toneSharp)
            {
                buttonView.setImageDrawable(getDrawable(R.drawable.ic_hash))
            }
            else
            {
                buttonView.setImageDrawable(getDrawable(R.drawable.ic_nohash))
            }
            updateInputButtons()
        }
        
        findViewById<View>(R.id.view_button_break).setOnClickListener { v ->
            val buttonView = v as ImageButton
            toneBreak = !toneBreak
            if (toneBreak) buttonView.setImageDrawable(getDrawable(R.drawable.ic_break))
            else buttonView.setImageDrawable(getDrawable(R.drawable.ic_nobreak))
            if (toneSharp)
            {
                findViewById<View>(R.id.view_button_black).callOnClick()
                findViewById<View>(R.id.view_button_black).isEnabled = false
            }
            else
            {
                findViewById<View>(R.id.view_button_black).isEnabled = true
            }
        }
    }
    
    /**
     * Inits C,D,E,F,G,A,B Buttons and Back Button
     */
    private fun initInputButtons()
    {
        findViewById<View>(R.id.view_button_back).setOnClickListener {
            if (exercise!!.sheetSize > 0) exercise!!.popNote()
            scoreView!!.reload()
        }
        
        view_button_c.setOnClickListener { addExerciseNote(Tone.Pitch.C) }
        view_button_d.setOnClickListener { addExerciseNote(Tone.Pitch.D) }
        view_button_e.setOnClickListener { addExerciseNote(Tone.Pitch.E) }
        view_button_f.setOnClickListener { addExerciseNote(Tone.Pitch.F) }
        view_button_g.setOnClickListener { addExerciseNote(Tone.Pitch.G) }
        view_button_a.setOnClickListener { addExerciseNote(Tone.Pitch.A) }
        view_button_b.setOnClickListener { addExerciseNote(Tone.Pitch.B) }
    }
    
    /**
     * Initializes GUI Logic and Values
     */
    private fun initGUI()
    {
        noteLengthGroup = findViewById(R.id.view_note_length_group)
        
        initClefSpinner()
        initOctaveSelectionGroup()
        initScoreView()
        initBPM()
        initUpDownButton()
        initModifierButtons()
        initInputButtons()
        
        updateTitle()
    }
    
    /**
     * Initializes ScoreView
     */
    private fun initScoreView()
    {
        scoreView = findViewById(R.id.score_view)
        scoreView!!.setExercise(exercise!!)
    }
    
    /**
     * Adds note to exercise with pitch and current view states
     *
     * @param pitch
     */
    private fun addExerciseNote(pitch: Tone.Pitch)
    {
        exercise!!.addTone(
            Tone(
                noteOctave, pitch, !toneSharp, toneLength, toneBreak
            )
        )
        scoreView!!.reload()
    }
    
    /**
     * Writes current exercise to the given file
     *
     * @param f
     */
    private fun writeExercise(f: File)
    {
        try
        {
            f.createNewFile()
            val w = BufferedWriter(FileWriter(f))
            w.write(exercise!!.toStorageString())
            w.close()
        }
        catch (e: Throwable)
        {
            Log.e(TAG, e.message!!)
        }
        
    }
    
    /**
     * Saves current exercise
     */
    private fun save()
    {
        var f: File
        if (exercise!!.hasFilename())
        { // file already exists -> override old file (NOTE: fileNAME stays the same)
            f = Storage.getExercise(this, exercise!!.filename!!)
            f.delete()
        }
        else
        // create new exercise file depending on exercise name and count up if already exists
        {
            var c = 0
            do
            {
                f = Storage.getExercise(
                    this, exercise!!.name + "-" + (c++).toString() + ".exercise"
                )
            }
            while (f.exists())
        }
        writeExercise(f)
        finish()
    }
    
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_view)
        setSupportActionBar(exercise_view_layout_actionbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        
        exercise = intent.getSerializableExtra(getString(R.string.EXTRA_EXERCISE)) as Exercise?
        initGUI()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            android.R.id.home ->
            {
                this.finish()
                return true
            }
            
            R.id.view_save ->
            {
                save()
                return true
            }
            
            R.id.view_edit_name ->
            {
                editName()
                return true
            }
            
            R.id.view_edit_summary ->
            {
                editSummary()
                return true
            }
            
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
