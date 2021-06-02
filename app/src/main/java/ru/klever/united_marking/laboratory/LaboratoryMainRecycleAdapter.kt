package ru.klever.united_marking.laboratory

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.laboratory_main_recycleview_item.view.*
import ru.klever.united_marking.R
import ru.klever.united_marking.TAG


interface OnLaboratoryItemClickListener{
    fun onItemClicked(code: String)
}

class LaboratoryHolder(itemView: View): RecyclerView.ViewHolder(itemView)
{
    private val dataCode=itemView.data_code

    fun bind(code:String, clickListenerLaboratoryMain: OnLaboratoryItemClickListener){
        dataCode.text=code
        Log.d(TAG,code)
        itemView.setOnClickListener {
            clickListenerLaboratoryMain.onItemClicked(code)
        }
    }
}

class LaboratoryMainRecycleAdapter(private val codes: MutableList<String>, val ItemClickListener: OnLaboratoryItemClickListener):
    RecyclerView.Adapter<LaboratoryHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LaboratoryHolder {
        val itemView= LayoutInflater.from(parent.context).inflate(
            R.layout.laboratory_main_recycleview_item,
            parent,
            false
        )
        return LaboratoryHolder(itemView)
    }

    override fun onBindViewHolder(holder: LaboratoryHolder, position: Int) {
        holder.bind(codes[position],ItemClickListener)
    }

    override fun getItemCount(): Int {
        return codes.size
    }
}
