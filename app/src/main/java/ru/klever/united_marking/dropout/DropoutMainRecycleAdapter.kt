package ru.klever.united_marking.dropout

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dropout_main_recycleview_item.view.*
import ru.klever.united_marking.R
import ru.klever.united_marking.TAG


interface OnDropoutItemClickListener{
    fun onItemClicked(code: String)
}

class DropoutHolder(itemView: View): RecyclerView.ViewHolder(itemView)
{
    private val dataCode=itemView.data_code

    fun bind(code:String, clickListenerDropoutMain: OnDropoutItemClickListener){
        dataCode.text=code
        Log.d(TAG,code)
        itemView.setOnClickListener {
            clickListenerDropoutMain.onItemClicked(code)
        }
    }
}

class DropoutMainRecycleAdapter(private val codes: MutableList<String>, val itemClickListener: OnDropoutItemClickListener):
    RecyclerView.Adapter<DropoutHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DropoutHolder {
        val itemView= LayoutInflater.from(parent.context).inflate(
            R.layout.dropout_main_recycleview_item,
            parent,
            false
        )
        return DropoutHolder(itemView)
    }

    override fun onBindViewHolder(holder: DropoutHolder, position: Int) {
        holder.bind(codes[position],itemClickListener)
    }

    override fun getItemCount(): Int {
        return codes.size
    }
}
