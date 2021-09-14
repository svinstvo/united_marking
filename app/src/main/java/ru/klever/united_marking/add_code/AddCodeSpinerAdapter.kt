package ru.klever.united_marking.add_code

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.MutableLiveData
import kotlinx.android.synthetic.main.add_code_select_batch_spinner_item.view.*
import ru.klever.united_marking.R

class AddCodeSpinerAdapter(context:Context, batchs: Batchs):ArrayAdapter<batchsItem>(context,0,batchs){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent);
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent);
    }

    fun createItemView(position: Int, recycledView: View?, parent: ViewGroup):View {
        val batch = getItem(position)

        val view = recycledView ?: LayoutInflater.from(context).inflate(
            R.layout.add_code_select_batch_spinner_item,
            parent,false
        )

        batch?.let {
            view.add_code_spiner_batch_id.text=it.batch_id.toString()
            view.add_code_spiner_batch_print_name.text=it.batch_print_name
            view.add_code_spiner_product_name.text=it.product_name
            when (it.batch_status) {
                0 -> view.add_code_spiner_color_status.setImageResource(android.R.drawable.presence_invisible)
                1 -> view.add_code_spiner_color_status.setImageResource(android.R.drawable.presence_away)
                2 -> view.add_code_spiner_color_status.setImageResource(android.R.drawable.presence_online)
            }

        }
        return view
    }


}