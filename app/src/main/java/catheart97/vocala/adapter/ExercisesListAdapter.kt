package catheart97.vocala.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import catheart97.vocala.R
import catheart97.vocala.data.Exercise

/**
 * ListAdapter for ExerciseView-List in Storage
 *
 * @author Ronja Schnur
 */
class ExercisesListAdapter(private val data: MutableList<Exercise>, private val appContext: Context) :
        RecyclerView.Adapter<BaseViewHolder>()
{
    companion object
    {
        const val VIEW_TYPE_EMPTY = 0
        const val VIEW_TYPE_NORMAL = 1
    }
    
    var selected: Exercise? = null
        private set
    
    private var selectedViewHolder: ViewHolder? = null
    
    inner class ViewHolder(itemView: View) : BaseViewHolder(itemView)
    {
        internal var title: AppCompatTextView = itemView.findViewById(R.id.exercise_item_name)
        private var summary: AppCompatTextView = itemView.findViewById(R.id.exercise_item_summary)
        internal var image: AppCompatImageView = itemView.findViewById(R.id.exercise_item_icon)
        internal var card: CardView = itemView.findViewById(R.id.card)
        
        override fun clear()
        {
            title.text = ""
            summary.text = ""
        }
        
        override fun onBind(position: Int)
        {
            super.onBind(position)
            
            val exercise = data[position]
            
            title.text = exercise.name
            title.setTextColor(Color.WHITE)
            summary.text = exercise.summary
            summary.setTextColor(Color.WHITE)
            summary.isSingleLine = false
            
            updateChecked(this, exercise)
            
            itemView.setOnClickListener {
                if (selected === exercise)
                {
                    updateChecked(selectedViewHolder, null)
                    selected = null
                    selectedViewHolder = null
                }
                else
                {
                    selected = exercise
                    updateChecked(this@ViewHolder, exercise)
                    if (selectedViewHolder != null) updateChecked(selectedViewHolder, null)
                    selectedViewHolder = this@ViewHolder
                }
            }
        }
    }
    
    inner class EmptyViewHolder internal constructor(itemView: View) : BaseViewHolder(itemView)
    {
        override fun clear()
        {
        }
    }
    
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int)
    {
        holder.onBind(position)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder
    {
        return when (viewType)
        {
            VIEW_TYPE_NORMAL -> ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.layout_exercises_exercise_item, parent, false)
            )
            VIEW_TYPE_EMPTY -> EmptyViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.layout_exercises_empty, parent, false)
            )
            else -> EmptyViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.layout_exercises_empty, parent, false
                )
            )
        }
    }
    
    fun clear()
    {
        data.clear()
        notifyDataSetChanged()
    }
    
    override fun getItemViewType(position: Int): Int
    {
        return if (data.size > 0) VIEW_TYPE_NORMAL else VIEW_TYPE_EMPTY
    }
    
    override fun getItemCount(): Int
    {
        return if (data.size > 0) data.size else 1 // for rendering empty list
    }
    
    fun addItems(item: List<Exercise>)
    {
        data.addAll(item)
        notifyDataSetChanged()
    }
    
    private fun updateChecked(view_holder: ViewHolder?, ex: Exercise?)
    {
        if (ex == null || ex !== selected)
        {
            view_holder!!.image.setImageDrawable(appContext.getDrawable(R.drawable.ic_fluent_music_note_2_24_filled))
            view_holder.card.setCardBackgroundColor(ContextCompat.getColor(appContext, R.color.mygray))
        }
        else
        {
            view_holder!!.image.setImageDrawable(appContext.getDrawable(R.drawable.ic_fluent_music_note_2_24_filled))
            view_holder.card.setCardBackgroundColor(ContextCompat.getColor(appContext, R.color.myblack))
        }
    }
    
    override fun getItemId(position: Int): Long
    {
        return position.toLong()
    }
}