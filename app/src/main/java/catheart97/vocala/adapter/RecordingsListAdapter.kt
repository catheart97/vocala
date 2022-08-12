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
import catheart97.vocala.algorithm.Utils
import catheart97.vocala.data.Recording
import java.util.*

/**
 * List Adapter for RecordingsView
 *
 * @author Ronja Schnur
 */
class RecordingsListAdapter(private val data: MutableList<Recording>, private val appContext: Context) :
        RecyclerView.Adapter<BaseViewHolder>()
{
    companion object
    {
        const val VIEW_TYPE_EMPTY = 0
        const val VIEW_TYPE_NORMAL = 1
    }
    
    val selected: List<Recording>
        get()
        {
            val result = ArrayList<Recording>()
            
            for (r in data)
            {
                if (r.checked) result.add(r)
            }
            
            return result
        }
    
    inner class ViewHolder(itemView: View) : BaseViewHolder(itemView)
    {
        internal var title: AppCompatTextView = itemView.findViewById(R.id.record_item_name)
        internal var date: AppCompatTextView = itemView.findViewById(R.id.record_item_date)
        private var length: AppCompatTextView = itemView.findViewById(R.id.record_item_length)
        internal var image: AppCompatImageView = itemView.findViewById(R.id.record_item_icon)
        internal var card: CardView = itemView.findViewById(R.id.card)
        
        override fun clear()
        {
            title.text = ""
            date.text = ""
        }
        
        override fun onBind(position: Int)
        {
            super.onBind(position)
            val record = data[position]
            
            title.text = record.name
            title.setTextColor(Color.WHITE)
            date.text = Utils.getDateString(record.date)
            date.setTextColor(Color.WHITE)
            length.text = Utils.getDurationString(record.length)
            length.setTextColor(Color.WHITE)
            updateChecked(this, position)
            
            itemView.setOnClickListener {
                data[position].toggle()
                updateChecked(this@ViewHolder, position)
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
                LayoutInflater.from(parent.context).inflate(
                    R.layout.layout_recordings_recording_item, parent, false
                )
            )
            VIEW_TYPE_EMPTY -> EmptyViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.layout_recordings_empty, parent, false
                )
            )
            else -> EmptyViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.layout_recordings_empty, parent, false
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
    
    fun addItems(item: List<Recording>)
    {
        data.addAll(item)
        notifyDataSetChanged()
    }
    
    private fun updateChecked(view_holder: ViewHolder, position: Int)
    {
        if (!data[position].checked)
        {
            view_holder.image.setImageDrawable(appContext.getDrawable(R.drawable.ic_fluent_music_note_1_20_filled))
            view_holder.card.setCardBackgroundColor(ContextCompat.getColor(appContext, R.color.mygray))
        }
        else
        {
            view_holder.image.setImageDrawable(appContext.getDrawable(R.drawable.ic_fluent_music_note_1_20_filled))
            view_holder.card.setCardBackgroundColor(ContextCompat.getColor(appContext, R.color.myblack))
        }
    }
    
    fun getPosition(rec: Recording): Int
    {
        return data.indexOf(rec)
    }
    
    override fun getItemId(position: Int): Long
    {
        return position.toLong()
    }
    
    fun removeRecording(record: Recording)
    {
        data.remove(record)
        notifyDataSetChanged()
    }
}
