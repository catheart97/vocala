package catheart97.vocala.view

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import catheart97.vocala.R
import catheart97.vocala.data.Tone
import me.tom.range.slider.RangeSliderView
import java.util.*

/**
 * Preference to set Vocal Range by a range slider
 * The stored value is a String of form int;int with first integer representing lower border
 * and second the upper one.
 * To get the note from the integer use Tone.getTone(int)
 *
 * @author Ronja Schnur
 */
class VocalRangePreference @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0) :
        Preference(context, attrs, defStyleAttr)
{
    companion object
    {
        fun defaultValue(): String
        {
            return StringBuilder().append(
                Tone(3, Tone.Pitch.D).pianoKeyNumber
            ).append(";").append(
                Tone(5, Tone.Pitch.F, true).pianoKeyNumber
            ).toString()
        }
    }
    
    private var lowest = Tone(3, Tone.Pitch.D)
    private var highest = Tone(5, Tone.Pitch.F, true)
    
    init
    {
        layoutResource = R.layout.layout_vocalrangepreference
    }

    //override fun onGetDefaultValue(a: TypedArray?, index: Int): Any
    //{
    //    return defaultValue()
    //}
    
    override fun onSetInitialValue(defaultValue: Any?)
    {
        super.onSetInitialValue(defaultValue)
        val rangeStr =
            getPersistedString(defaultValue()).split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        lowest = Tone.getNote(Integer.parseInt(rangeStr[0]))
        highest = Tone.getNote(Integer.parseInt(rangeStr[1]))
    }
    
    
    override fun onBindViewHolder(holder: PreferenceViewHolder)
    {
        super.onBindViewHolder(holder)
        holder.itemView.isClickable = false // disable parent click
        
        val rangeBar = holder.findViewById(R.id.pref_rangebar) as RangeSliderView
        val keys = ArrayList<Int>()
        for (i in 0..86) keys.add(i)
        rangeBar.setRangeValues(keys)
        rangeBar.setMinAndMaxValue(lowest.pianoKeyNumber, highest.pianoKeyNumber)
        rangeBar.setOnValueChangedListener(object : RangeSliderView.OnValueChangedListener()
        {
            override fun onValueChanged(i: Int, i1: Int)
            {
                lowest = Tone.getNote(i)
                highest = Tone.getNote(i1)
                persistString(
                    StringBuilder().append(lowest.pianoKeyNumber).append(";").append(highest.pianoKeyNumber).toString()
                )
            }
            
            override fun parseMinValueDisplayText(minValue: Int): String
            {
                var note = Tone.getNote(minValue).toString()
                if (note.length != 3) note = " $note "
                return note
            }
            
            override fun parseMaxValueDisplayText(maxValue: Int): String
            {
                var note = Tone.getNote(maxValue).toString()
                if (note.length != 3) note = " $note "
                return note
            }
        })
    }
}
