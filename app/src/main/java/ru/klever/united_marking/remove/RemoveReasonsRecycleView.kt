package ru.klever.united_marking.remove

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.klever.united_marking.R
import ru.klever.united_marking.Reasons
import ru.klever.united_marking.Settings

class RemoveReasonsRecycleView: AppCompatActivity(), OnReasonItemClickListener {
    val TAG = "debuu"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dropout_reason_recycle_view)
        val settings: Settings = Settings(this)
        val recyclerView:RecyclerView=findViewById(R.id.dropout_reason_recycle_view)
        recyclerView.layoutManager=LinearLayoutManager(this)
        recyclerView.adapter=
            LaboratoryReasonRecycleAdapter(settings.removeGetAllReasonsAndId(),this)
    }


    override fun onItemClicked(reason: Reasons) {
        val data=Intent()
        data.putExtra("reason_id",reason.reason_id)
        data.putExtra("reason_text",reason.reason_text)
        setResult(Activity.RESULT_OK,data)
        finish()
    }
}