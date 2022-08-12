package catheart97.vocala.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Base View Holder Class
 *
 * @author Ronja Schnur 
 */
abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
{
    private var currentPosition: Int = 0
    
    /**
     * Should clear everything contained
     */
    protected abstract fun clear()
    
    /**
     * Should bind a specific item
     */
    open fun onBind(position: Int)
    {
        currentPosition = position
        clear()
    }
}
