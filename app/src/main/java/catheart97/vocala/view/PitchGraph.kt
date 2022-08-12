package catheart97.vocala.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchDetectionResult
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import catheart97.vocala.algorithm.Utils
import catheart97.vocala.data.Tone
import catheart97.vocala.data.Storage
import java.util.*
import kotlin.math.log10

/**
 * GraphView displaying sung or spoken pitches. This class extends PhilJay's LineChart class.
 *
 * @author Ronja Schnur
 */
class PitchGraph : LineChart, PitchDetectionHandler
{
    companion object
    {
        private const val GRAPH_ELEMENTS = 150
        private const val PIANO_SIZE = 88
        private const val PIANO_MAX_KEY = PIANO_SIZE - 1
    }
    
    private var position = 0f
    private var dataSetIndex = 1
    private var wasGreater = true
    private lateinit var toneLabel: TextView
    private lateinit var frequencyLabel: TextView
    
    constructor(context: Context?) : super(context)
    {
        basicInit()
    }
    
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    {
        basicInit()
    }
    
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)
    {
        basicInit()
    }
    
    private fun basicInit()
    {
        val graphData = LineData()
        val dataList = ArrayList<Entry>()
        for (i in 0 until GRAPH_ELEMENTS) dataList.add(Entry(position++, -1f))
        val graphLegend = legend
        val axisX = xAxis
        val axisYLeft = axisLeft
        val axisYRight = axisRight
        
        setVisibleXRangeMinimum(GRAPH_ELEMENTS.toFloat())
        setVisibleXRangeMaximum(GRAPH_ELEMENTS.toFloat())
        
        graphData.addDataSet(createDataSet(dataList))
        graphData.addDataSet(createDataSet()) // EMPTY PITCH DATA SET
        graphData.setValueTextColor(Color.WHITE)
        graphData.isHighlightEnabled = false
        
        description.isEnabled = false
        isLogEnabled = false
        
        setTouchEnabled(true)
        
        isDragEnabled = true
        isScaleYEnabled = true
        isScaleXEnabled = false
        setDrawGridBackground(true)
        isHorizontalScrollBarEnabled = false
        
        setPinchZoom(false)
        setBackgroundColor(Color.TRANSPARENT)
        setGridBackgroundColor(Color.TRANSPARENT)
        setVisibleXRangeMaximum(GRAPH_ELEMENTS.toFloat())
        
        data = graphData
        isAutoScaleMinMaxEnabled = false
        
        setViewPortOffsets(80f, 15f, 0f, 10f)
        isHighlightPerDragEnabled = false
        isHighlightPerTapEnabled = false
        
        graphLegend.form = Legend.LegendForm.CIRCLE
        graphLegend.typeface = Typeface.SANS_SERIF
        graphLegend.textColor = Color.WHITE
        graphLegend.isEnabled = false
        
        axisX.typeface = Typeface.SANS_SERIF
        axisX.textColor = Color.WHITE
        axisX.setDrawGridLines(false)
        axisX.setAvoidFirstLastClipping(false)
        axisX.isEnabled = false
        
        axisYLeft.valueFormatter = object : ValueFormatter()
        {
            override fun getFormattedValue(value: Float): String
            {
                return Tone.getNote(value.toInt()).toString()
            }
        }
        axisYLeft.granularity = 1.0f
        axisYLeft.typeface = Typeface.SANS_SERIF
        axisYLeft.textColor = Color.WHITE
        axisYLeft.axisMaximum = PIANO_MAX_KEY.toFloat()
        axisYLeft.axisMinimum = 0.0f
        axisYLeft.setDrawGridLines(true)
        axisYLeft.setDrawTopYLabelEntry(true)
        axisYLeft.minWidth = 30f
        axisYLeft.labelCount = PIANO_SIZE
        axisYLeft.setDrawLimitLinesBehindData(true)
        
        initLineLimits(axisYLeft)
        
        viewPortHandler.setMaximumScaleY(5.0f)
        viewPortHandler.setMinimumScaleY(2.0f)
        viewPortHandler.setMaximumScaleY(5.0f)
        zoom(1.0f, 1.6f, 0.0f, 50.0f, YAxis.AxisDependency.LEFT)
        
        axisYRight.isEnabled = false
    }
    
    /**
     * updates frequency and note label
     *
     * @param pitchFrequency
     */
    @SuppressLint("SetTextI18n") private fun updateLabels(pitchFrequency: Double)
    {
        val note = Tone.getNote(pitchFrequency)
        
        if (pitchFrequency > 0)
        {
            toneLabel.text = note.toString()
            frequencyLabel.text = "${Utils.niceDouble(pitchFrequency)}Hz"
        }
    }
    
    /**
     * Initizializes LineLimits depending on preferences
     *
     * @param graph_axis_y
     */
    fun initLineLimits(graph_axis_y: YAxis)
    {
        Storage.loadKeyColor(context)
        
        graph_axis_y.removeAllLimitLines()
        for (i in 0..88)
        {
            val line = LimitLine(i.toFloat())
            
            if (i == Storage.lowestTone.pianoKeyNumber || i == Storage.highestTone.pianoKeyNumber)
            {
                line.lineColor = Storage.rangeColor
                graph_axis_y.addLimitLine(line)
                continue
            }
            
            line.lineColor = Storage.getKeyColor(Tone.getNote(i))
            
            graph_axis_y.addLimitLine(line)
        }
    }
    
    /**
     * Removes all empty datasets (except 0 and 1) and updates dataSetIndex
     */
    private fun removeOldDataSet()
    {
        var set = lineData.getDataSetByIndex(1)
        
        while (set.entryCount == 0 && lineData.dataSetCount > 2)
        {
            if (lineData.removeDataSet(1))
            {
                dataSetIndex--
                lineData.notifyDataChanged()
                notifyDataSetChanged()
                
            }
            set = lineData.getDataSetByIndex(1)
        }
    }
    
    /**
     * Removes all entries which left the graph_view from ALL datasets
     */
    private fun removeOldEntries()
    {
        if (lineData.getDataSetByIndex(0).entryCount > GRAPH_ELEMENTS)
        {
            lineData.getDataSetByIndex(0).removeFirst()
            lineData.notifyDataChanged()
            notifyDataSetChanged()
        }
        
        for (i in 1 until lineData.dataSetCount)
        {
            val dataSet = lineData.getDataSetByIndex(i)
            
            if (dataSet.entryCount > 0 && dataSet.getEntryForIndex(0).x < position - GRAPH_ELEMENTS)
            {
                dataSet.removeFirst()
                lineData.notifyDataChanged()
                notifyDataSetChanged()
            }
        }
    }
    
    /**
     * Handles problem might caused by position growing over Long.MAX_VALUE
     */
    private fun handleOverflow()
    {
        val distance = java.lang.Long.MAX_VALUE - 4
        position -= distance.toFloat()
        
        for (set in lineData.dataSets)
        {
            for (i in 0 until set.entryCount)
            {
                val e = set.getEntryForIndex(i)
                e.x = e.x - distance
                lineData.notifyDataChanged()
                notifyDataSetChanged()
            }
        }
    }
    
    /**
     * @param data_list
     * @return new LineDataSet with colors depending on preferences and data_list as base
     */
    @JvmOverloads internal fun createDataSet(data_list: List<Entry> = ArrayList()): LineDataSet
    {
        val graphDataSet = LineDataSet(data_list, "")
        graphDataSet.axisDependency = YAxis.AxisDependency.LEFT
        
        graphDataSet.color = Storage.getGraphColor(context)
        graphDataSet.setDrawCircles(false)
        graphDataSet.lineWidth = 2f
        graphDataSet.setDrawValues(false)
        graphDataSet.mode = LineDataSet.Mode.LINEAR
        return graphDataSet
    }
    
    override fun handlePitch(pitchDetectionResult: PitchDetectionResult, audioEvent: AudioEvent)
    {
        val pitchFrequency = pitchDetectionResult.pitch.toDouble()
        
        (context as Activity).runOnUiThread {
            val value = (log10(pitchFrequency / Tone.a4Frequency) / log10(2.0) * 12.0 + 48.0).toFloat()
            
            removeOldDataSet()
            
            if (position >= java.lang.Float.MAX_VALUE - 2) handleOverflow()
            
            lineData.addEntry(Entry(position, -1f), 0) //ADD "INVISIBLE" ENTRY
            
            if (pitchFrequency > 0 && pitchDetectionResult.probability > 0.9f)
            {
                updateLabels(pitchFrequency)
                
                if (wasGreater)
                {
                    lineData.addEntry(Entry(position, value), dataSetIndex)
                }
                else
                {
                    lineData.addDataSet(createDataSet())
                    lineData.addEntry(
                        Entry(position, value), ++dataSetIndex
                    )
                }
                wasGreater = true
            }
            else
            {
                wasGreater = false
            }
            position++
            
            removeOldEntries()
            
            lineData.notifyDataChanged()
            notifyDataSetChanged()
            invalidate()
        }
    }
    
    fun setFrequencyLabel(frequency_label: TextView)
    {
        this.frequencyLabel = frequency_label
    }
    
    fun setToneLabel(tone_label: TextView)
    {
        this.toneLabel = tone_label
    }
}
