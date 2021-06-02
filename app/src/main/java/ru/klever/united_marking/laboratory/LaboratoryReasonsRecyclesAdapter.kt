package ru.klever.united_marking.laboratory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.laboratory_reason_recycleview_item.view.*
import ru.klever.united_marking.R
import ru.klever.united_marking.Reasons

interface OnReasonItemClickListener{
    fun onItemClicked(reason: Reasons)
}

class MyHolder(itemView: View):RecyclerView.ViewHolder(itemView)
{
    val reason_text=itemView.reason_text
    val reason_id=itemView.reason_code

    fun bind(reason: Reasons, clickListenerReason: OnReasonItemClickListener){
        reason_text.text=reason.reason_text
        reason_id.text=reason.reason_id

        itemView.setOnClickListener {
            clickListenerReason.onItemClicked(reason)
        }
    }
}

class LaboratoryReasonRecycleAdapter(val reasons: MutableList<Reasons>, val reasonItemClickListener: OnReasonItemClickListener):
    RecyclerView.Adapter<MyHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val itemView=LayoutInflater.from(parent.context).inflate(
            R.layout.laboratory_reason_recycleview_item,
            parent,
            false
        )
        return MyHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val reason=reasons.get(position)
        holder.bind(reason,reasonItemClickListener)
    }

    override fun getItemCount(): Int {
        return reasons.size
    }
}


