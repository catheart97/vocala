package catheart97.vocala.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.min

class RealTimeGraph : View
{
    constructor(context: Context) : super(context)
    {
        init()
    }
    
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    {
        init()
    }
    
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    {
        init()
    }
    
    private fun init()
    {
    
    }
    
    private fun measureDimension(desiredSize: Int, measureSpec: Int): Int
    {
        
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        
        val result = when (specMode)
        {
            MeasureSpec.EXACTLY -> specSize
            else ->
            {
                if (specMode == MeasureSpec.AT_MOST) min(desiredSize, specSize)
                else desiredSize
            }
        }
        
        if (result < desiredSize)
        {
            Log.e("RealTimeChart", "Error 001")
        }
        
        return result
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
    {
        Log.v("RealTimeChart", "On Measure Width: ${MeasureSpec.toString(widthMeasureSpec)}")
        Log.v("RealTimeChart", "On Measure Height: ${MeasureSpec.toString(heightMeasureSpec)}")
        
        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom
        
        setMeasuredDimension(
            measureDimension(desiredWidth, widthMeasureSpec), measureDimension(desiredHeight, heightMeasureSpec)
        )
    }
}