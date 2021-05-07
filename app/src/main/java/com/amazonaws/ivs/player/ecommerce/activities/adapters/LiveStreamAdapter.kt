package com.amazonaws.ivs.player.ecommerce.activities.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.ivs.player.ecommerce.databinding.StreamItemBinding
import com.amazonaws.ivs.player.ecommerce.models.LiveStreamItem
import kotlin.properties.Delegates

/**
 * Live stream option selection adapter
 */
class LiveStreamAdapter(private val onAnswerClicked: (link: String) -> Unit) :
    RecyclerView.Adapter<LiveStreamAdapter.ViewHolder>() {

    var items: List<LiveStreamItem> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            StreamItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            data = items[position]
            first = position == 0
            last = position == itemCount - 1
            liveStreamItem.setOnClickListener {
                onAnswerClicked(items[position].link)
            }
        }
    }

    inner class ViewHolder(val binding: StreamItemBinding) : RecyclerView.ViewHolder(binding.root)

}
