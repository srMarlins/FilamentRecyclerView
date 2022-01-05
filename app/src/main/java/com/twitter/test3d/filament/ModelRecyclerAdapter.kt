package com.twitter.test3d.filament

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.twitter.test3d.R
import java.nio.ByteBuffer

class ModelRecyclerAdapter : RecyclerView.Adapter<ModelRecyclerAdapter.ModelViewHolder>() {

    var data: List<ByteBuffer> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        return ModelViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.model_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        holder.modelView.loadGlb(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val modelView = itemView as ModelTextureView
    }
}