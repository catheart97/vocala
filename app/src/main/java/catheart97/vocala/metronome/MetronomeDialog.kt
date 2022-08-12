package catheart97.vocala.metronome

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.sdsmdg.harjot.crollerTest.Croller
import catheart97.vocala.R
import catheart97.vocala.data.Storage

/**
 * Dialog for editing Metronome preferences
 *
 * @author Ronja Schnur 
 */
class MetronomeDialog : DialogFragment()
{
    companion object
    {
        private const val MIN_BPM = 50 - 1
    }
    
    private lateinit var croller: Croller
    private lateinit var numeratorSpinner: Spinner
    private lateinit var denominatorSpinner: Spinner
    
    @SuppressLint("InflateParams") override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val builder = AlertDialog.Builder(activity!!)
        val inflater = requireActivity().layoutInflater
        val layout = inflater.inflate(R.layout.layout_metronome_dialog, null)
        builder.setView(layout).setPositiveButton(android.R.string.ok) { _, _ ->
            Storage.saveMetronomeBPM(context!!, croller.progress + MIN_BPM)
            Storage.saveMetronomeNumerator(context!!, numeratorSpinner.selectedItem as Int)
            Storage.saveMetronomeDenominator(context!!, denominatorSpinner.selectedItem as Int)
        }.setNegativeButton(
            android.R.string.cancel
        ) { _, _ -> this@MetronomeDialog.dialog!!.cancel() }.setMessage(context!!.getString(R.string.metronome))
        
        croller = layout.findViewById(R.id.metronome_bpm)
        
        croller.progress = Storage.getMetronomeBPM(context!!) - MIN_BPM
        croller.max = 180 - MIN_BPM
        croller.setOnProgressChangedListener { progress -> croller.label = (progress + MIN_BPM).toString() + " BPM" }
        croller.setIsContinuous(true)
        
        
        numeratorSpinner = layout.findViewById(R.id.metronome_spinner_numerator)
        val numeratorOptions = arrayOf(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
        numeratorSpinner.adapter = ArrayAdapter(
            context!!, android.R.layout.simple_list_item_1, android.R.id.text1, numeratorOptions
        )
        numeratorSpinner.setSelection(Storage.getMetronomeNumerator(context!!) - 2)
        
        denominatorSpinner = layout.findViewById(R.id.metronome_spinner_denominator)
        val denominatorOptions = arrayOf(0, 2, 3, 4, 5, 6, 7, 8, 10, 12, 16)
        denominatorSpinner.adapter = ArrayAdapter(
            context!!, android.R.layout.simple_list_item_1, android.R.id.text1, denominatorOptions
        )
        
        denominatorSpinner.setSelection(
            if (Storage.getMetronomeDenominator(context!!) == 0) 0
            else Storage.metronomeDenominator - 1
        )
        
        val dialog = builder.create()
        dialog.window?.attributes?.windowAnimations = R.style.VocalaDialogAnimation
        return dialog
    }
}
