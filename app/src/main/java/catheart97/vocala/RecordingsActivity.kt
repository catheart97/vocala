package catheart97.vocala

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import catheart97.vocala.adapter.RecordingsListAdapter
import catheart97.vocala.data.Recording
import catheart97.vocala.data.Storage
import kotlinx.android.synthetic.main.activity_recordings.*
import java.io.File
import java.util.*

/**
 * Activity listing recordings
 *
 * @author Ronja Schnur 
 */
class RecordingsActivity : VocalaAActivity()
{
    private var recyclerView: RecyclerView? = null
    private var listAdapter: RecordingsListAdapter? = null
    
    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.hold, R.anim.right2left_in)
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        
        menuInflater.inflate(R.menu.recordings_actionbar_menu, menu)
        return true
    }
    
    /**
     * Shows dialog for renaming file
     *
     * @return true
     */
    @SuppressLint("InflateParams") private fun renameFile(): Boolean
    {
        val recordingList = listAdapter!!.selected
        when
        {
            recordingList.size == 1 ->
            {
                val record = recordingList[0]
                
                val builder = AlertDialog.Builder(this)
                
                val inflater = layoutInflater
                
                val layout = inflater.inflate(R.layout.layout_edit_dialog, null)
                val inputEdit = layout.findViewById<EditText>(R.id.edit_dialog_text)
                
                val extension = record.name!!.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                inputEdit.setText(record.name!!.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
                
                builder.setView(layout).setPositiveButton(android.R.string.ok) { _, _ ->
                    val oldName = record.name
                    val newName = inputEdit.editableText.toString() + "." + extension
                    
                    if (newName.compareTo(record.name!!) != 0)
                    {
                        val src = Storage.getRecord(this, oldName!!)
                        val dst = Storage.getRecord(this, newName)
                        
                        if (dst.exists())
                        {
                            Snackbar.make(
                                findViewById(R.id.recordings_layout_coord),
                                getString(R.string.already_exists),
                                Snackbar.LENGTH_LONG
                            ).setAction(getString(R.string.try_again)) { renameFile() }.show()
                        }
                        else
                        {
                            if (src.renameTo(dst))
                            {
                                record.name = newName
                                listAdapter!!.notifyDataSetChanged()
                                Snackbar.make(
                                    findViewById(R.id.recordings_layout_coord),
                                    R.string.success_rename,
                                    Snackbar.LENGTH_LONG
                                ).setAction(R.string.undo) {
                                    dst.renameTo(src)
                                    record.name = oldName
                                    listAdapter!!.notifyDataSetChanged()
                                    showMessage(getString(R.string.success_undone_rename))
                                }.show()
                            }
                            else
                            {
                                showMessage(getString(R.string.sth_went_wrong))
                            }
                        }
                    }
                }.setNegativeButton(
                    android.R.string.cancel
                ) { dialog, _ -> dialog.cancel() }.setMessage(getString(R.string.edit_summary))
                
                
                val dialog = builder.create()
                dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                dialog.window?.attributes?.windowAnimations = R.style.VocalaDialogAnimation
                dialog.show()
            }
            recordingList.isEmpty() -> showMessage(getString(R.string.no_rec_selected))
            else -> showMessage(getString(R.string.only_one_rec_rename))
        }
        return true
    }
    
    /**
     * delete selected files
     *
     * @return true
     */
    private fun deleteFiles(): Boolean
    {
        val namesToDelete = ArrayList<String>()
        
        for (recording in listAdapter!!.selected)
        {
            namesToDelete.add(recording.name!!)
            listAdapter!!.removeRecording(recording)
        }
        
        for (filename in namesToDelete)
        {
            Storage.getRecord(this, filename).delete()
        }
        showMessage(getString(R.string.Deleted) + namesToDelete.size + getString(R.string.files))
        return true
    }
    
    /**
     * loads selected record into mainActivity
     *
     * @return true
     */
    private fun loadRecording(): Boolean
    {
        val selected = listAdapter!!.selected
        if (selected.size == 1)
        {
            setResult(Activity.RESULT_OK, Intent().setData(Uri.parse(selected[0].name)))
            finish()
        }
        else if (selected.isEmpty())
        {
            showMessage(getString(R.string.no_rec_selected))
        }
        else
        {
            showMessage(getString(R.string.only_one_rec_load))
        }
        return true
    }
    
    private fun shareRecording(): Boolean
    {
        val toShare = listAdapter!!.selected
        
        if (toShare.size == 1)
        {
            val uri = FileProvider.getUriForFile(
                this, getString(R.string.app_id) + ".FileProvider", File(
                    Storage.getDirectory(
                        this, Storage.Directory.DIR_REC
                    ), toShare[0].name!!
                )
            )
            
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.type = contentResolver.getType(uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(intent, getString(R.string.share_rec)))
        }
        else if (toShare.isEmpty())
        {
            showMessage(getString(R.string.no_rec_selected))
        }
        else
        {
            showMessage(getString(R.string.only_one_rec_share))
        }
        return true
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
            
            R.id.recordings_rename -> return renameFile()
            
            R.id.recordings_trash -> return deleteFiles()
            
            R.id.recordings_load -> return loadRecording()
            
            R.id.recordings_share -> return shareRecording()
            
            else -> return super.onOptionsItemSelected(item)
        }
    }
    
    private fun showMessage(s: String)
    {
        Snackbar.make(findViewById(R.id.recordings_layout_coord), s, Snackbar.LENGTH_SHORT).show()
    }
    
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recordings)
        setSupportActionBar(recordings_layout_actionbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        
        recyclerView = findViewById(R.id.recordings_list)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        
        val itemList = ArrayList<Recording>()
        for (file in File(
            Storage.getDirectory(
                this, Storage.Directory.DIR_REC
            ), ""
        ).listFiles()!!)
        {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            itemList.add(
                Recording(
                    file.name, Date(file.lastModified()), java.lang.Long.parseLong(
                        retriever.extractMetadata(
                            MediaMetadataRetriever.METADATA_KEY_DURATION
                        )
                    )
                )
            )
            retriever.release()
        }
        listAdapter = RecordingsListAdapter(itemList, applicationContext)
        
        recyclerView!!.adapter = listAdapter
    }
}
