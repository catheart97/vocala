package catheart97.vocala.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.SeekBar
import com.chengtao.pianoview.entity.Piano
import com.chengtao.pianoview.listener.OnPianoListener
import com.chengtao.pianoview.view.PianoView

/**
 * Piano View for Storage which extends ChengTao's PianoView
 *
 * @author Ronja Schnur
 */
class ClavierView : PianoView, OnPianoListener
{
    companion object
    {
        private const val SCROLL_PIANO_INIT = 50
    }
    
    private var clavierSeekbar: SeekBar? = null
    
    constructor(context: Context) : super(context)
    {
        basicInit()
    }
    
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    {
        basicInit()
    }
    
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    {
        basicInit()
    }
    
    @SuppressLint("ClickableViewAccessibility") private fun basicInit()
    {
        setSoundPollMaxStream(0)
        setPianoColors(
            arrayOf(
                "#FFFFFF", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#FFFFFF"
            )
        )
        
        setPianoListener(this)
        setOnTouchListener { _, _ -> false }
    }
    
    override fun onPianoInitFinish()
    {
        clavierSeekbar!!.incrementProgressBy(SCROLL_PIANO_INIT) // Scroll to something around C4
    }
    
    override fun onPianoClick(
        pianoKeyType: Piano.PianoKeyType, pianoVoice: Piano.PianoVoice, group: Int, position_of_group: Int
    )
    {
    }
    
    fun setSeekbar(clavier_seekbar: SeekBar?)
    {
        if (clavier_seekbar == null) return
        
        this.clavierSeekbar = clavier_seekbar
        
        this.clavierSeekbar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener
        {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean)
            {
                scroll(progress)
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar)
            {
            }
            
            override fun onStopTrackingTouch(seekBar: SeekBar)
            {
            }
        })
    }
}
