package catheart97.vocala

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import catheart97.vocala.adapter.ExercisesListAdapter
import catheart97.vocala.data.Exercise
import catheart97.vocala.data.Storage
import kotlinx.android.synthetic.main.activity_exercises.*
import kotlinx.android.synthetic.main.activity_exercises_frame.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader
import java.util.*


/**
 * Activity listing exercises-de
 *
 * @author Ronja Schnur
 */
class ExercisesActivity : VocalaAActivity()
{
    companion object
    {
        private const val TAG = "Storage.EXERCISES"
        private const val REQUEST_EDIT = 0
    }
    
    private var recyclerView: RecyclerView? = null
    private var listAdapter: ExercisesListAdapter? = null
    
    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.hold, R.anim.right2left_in)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        if (requestCode == REQUEST_EDIT) loadExercises()
        super.onActivityResult(requestCode, resultCode, data)
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        menuInflater.inflate(R.menu.exercises_actionbar_menu, menu)
        return true
    }
    
    private fun showMessage(s: String)
    {
        Snackbar.make(exercises_layout_coord, s, Snackbar.LENGTH_SHORT).show()
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
            
            R.id.exercises_share -> return shareExercise()
            
            R.id.exercises_trash ->
            {
                val selected = listAdapter!!.selected
                if (selected != null && selected.hasFilename())
                {
                    if (selected.editable)
                    {
                        File(
                            Storage.getDirectory(this, Storage.Directory.DIR_EXERCISE), selected.filename!!
                        ).delete()
                        showMessage(selected.name + getString(R.string.was_deleted))
                        loadExercises()
                    }
                    else
                    {
                        showMessage(getString(R.string.default_ex_del))
                    }
                    
                }
                else
                {
                    showMessage(getString(R.string.no_exercise_selected))
                }
                return true
            }
            
            R.id.exercises_edit ->
            {
                if (listAdapter!!.selected != null)
                {
                    if (listAdapter!!.selected!!.editable)
                    {
                        val intent = Intent(
                            this@ExercisesActivity, ExerciseViewActivity::class.java
                        )
                        intent.putExtra(getString(R.string.EXTRA_EXERCISE), listAdapter!!.selected)
                        this@ExercisesActivity.startActivityForResult(intent, REQUEST_EDIT)
                    }
                    else
                    {
                        showMessage(getString(R.string.default_ex_edit))
                    }
                    
                }
                else
                {
                    showMessage(getString(R.string.no_exercise_selected))
                }
                
                return true
            }
            
            R.id.exercises_load ->
            {
                if (listAdapter!!.selected != null)
                {
                    val data = Intent()
                    data.putExtra(getString(R.string.EXTRA_EXERCISE), listAdapter!!.selected)
                    setResult(Activity.RESULT_OK, data)
                    this.finish()
                }
                else
                {
                    showMessage(getString(R.string.no_exercise_selected))
                }
                return true
            }
            
            else -> return super.onOptionsItemSelected(item)
        }
    }
    
    /**
     * Loads exercises-de from storage into ListAdapter
     */
    private fun loadExercises()
    {
        listAdapter!!.clear()
        val items = ArrayList<Exercise>()
        
        if (Storage.loadShowDefaultExercises(this))
        {
            val assetManager = assets
            for (fN in assetManager.list(getString(R.string.exercises_path))!!)
            {
                Log.e(TAG, fN)
                try
                {
                    val f = assetManager.open(getString(R.string.exercises_path) + "/" + fN)
                    val reader = BufferedReader(InputStreamReader(f))
                    val builder = StringBuilder()
                    
                    var str : String? = reader.readLine()
                    while (str != null)
                    {
                        builder.append(str).append("\n")
                        str = reader.readLine()
                    }
            
                    val e = Exercise(builder.toString(), false)
                    e.filename = "NAME"
                    items.add(e)
                    reader.close()
                    f.close()
                }
                catch (e: Throwable)
                {
                    Log.e(TAG, e.message!!)
                }
            }
        }
        
        for (f in Storage.getDirectory(this, Storage.Directory.DIR_EXERCISE).listFiles()!!)
        {
            try
            {
                val reader = BufferedReader(FileReader(f))
                val builder = StringBuilder()
                
                val lineList = mutableListOf<String>()
                f.useLines { lines -> lines.forEach { lineList.add(it) } }
                lineList.forEach { builder.append(it).append("\n") }
                
                val e = Exercise(builder.toString())
                e.filename = f.name
                items.add(e)
                reader.close()
            }
            catch (e: Throwable)
            {
                Log.e(TAG, e.message!!)
            }
        }
        listAdapter!!.addItems(items)
    }
    
    private fun shareExercise(): Boolean
    {
        val toShare = listAdapter!!.selected
        
        if (toShare != null)
        {
            if (toShare.editable)
            {
                val uri = FileProvider.getUriForFile(
                    this, getString(R.string.app_id) + ".FileProvider", File(
                        Storage.getDirectory(
                            this, Storage.Directory.DIR_EXERCISE
                        ), "exercises/" + toShare.filename
                    )
                )
    
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.type = contentResolver.getType(uri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(Intent.createChooser(intent, getString(R.string.share_exercise)))
            }
            else
            {
                showMessage(getString(R.string.default_ex_msg))
            }
        }
        else
        {
            showMessage(getString(R.string.no_exercise_selected))
        }
        return true
    }
    
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)
        setSupportActionBar(exercises_layout_actionbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        
        recyclerView = exercises_list
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        exercises_new.setOnClickListener {
            val intent = Intent(
                this@ExercisesActivity, ExerciseViewActivity::class.java
            )
            intent.putExtra(
                getString(R.string.EXTRA_EXERCISE), Exercise(
                    getString(R.string.default_ex_title),
                    getString(R.string.default_sum),
                    Exercise.Clef.TrebleClef,
                    ArrayList(),
                    true,
                    80
                )
            )
            this@ExercisesActivity.startActivityForResult(intent, REQUEST_EDIT)
        }
        
        listAdapter = ExercisesListAdapter(ArrayList(), applicationContext)
        recyclerView!!.adapter = listAdapter
        loadExercises()
    }
}
