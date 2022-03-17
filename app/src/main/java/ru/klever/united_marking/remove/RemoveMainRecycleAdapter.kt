package ru.klever.united_marking.remove

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.remove_main_recycleview_item.view.*
import ru.klever.united_marking.R
import ru.klever.united_marking.TAG


interface OnRemoveItemClickListener{
    fun onItemClicked(code: String)
}

class RemoveHolder(itemView: View): RecyclerView.ViewHolder(itemView)
{
    private val dataCode=itemView.data_code

    fun bind(code:String, clickListenerRemoveMain: OnRemoveItemClickListener){
        dataCode.text=code
        Log.d(TAG,code)
        itemView.setOnClickListener {
            clickListenerRemoveMain.onItemClicked(code)
        }
    }
}

class RemoveMainRecycleAdapter(private val codes: MutableList<String>, val itemClickListener: OnRemoveItemClickListener):
    RecyclerView.Adapter<RemoveHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RemoveHolder {
        val itemView= LayoutInflater.from(parent.context).inflate(
            R.layout.remove_main_recycleview_item,
            parent,
            false
        )
        return RemoveHolder(itemView)
    }

    override fun onBindViewHolder(holder: RemoveHolder, position: Int) {
        holder.bind(codes[position],itemClickListener)
    }

    override fun getItemCount(): Int {
        return codes.size
    }
}
