package catheart97.vocala.algorithm

import android.annotation.SuppressLint
import android.content.Context
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Contains Utility Functions
 *
 * @author Ronja Schnur 
 */
object Utils
{
    /**
     * @param[d] the input value
     * @return beautified human-readable string representation of double with one digit after comma
     */
    fun niceDouble(d: Double): String
    {
        return DecimalFormat("0.0").format(d)
    }
    
    /**
     * @param[milliseconds] delta of time to display
     * @return String representation of long in milliseconds of form HH:MM:SS
     */
    @SuppressLint("DefaultLocale") fun getDurationString(milliseconds: Long): String
    {
        return String.format(
            "%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(milliseconds),
            TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(
                TimeUnit.MILLISECONDS.toHours(milliseconds)
            ),
            TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(milliseconds)
            )
        )
    }
    
    /**
     * @param[date] the date to express
     * @return String representation of date in form dd/MM/yyyy HH:mm:ss
     */
    @SuppressLint("SimpleDateFormat") fun getDateString(date: Date): String
    {
        return SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date)
    }
    
    /**
     * Converts density pixel to display pixel
     * @param[context] The application context
     * @param[dp] density value
     */
    fun dp2px(context: Context, dp: Int): Int
    {
        return (context.resources.displayMetrics.density * dp + 0.5f).toInt()
    }
}
