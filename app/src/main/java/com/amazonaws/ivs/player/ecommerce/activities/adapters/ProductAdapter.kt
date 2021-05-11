package com.amazonaws.ivs.player.ecommerce.activities.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.ivs.player.ecommerce.databinding.PlayerProductItemBinding
import com.amazonaws.ivs.player.ecommerce.models.ProductModel
import kotlin.properties.Delegates

/**
 * Product adapter
 */
class ProductAdapter(private val onLearnMoreClick: (position: Int) -> Unit) :
    RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    var items: List<ProductModel> by Delegates.observable(emptyList()) { _, old, new ->
        DiffUtil.calculateDiff(SourceOptionDiff(old, new)).dispatchUpdatesTo(this)
    }

    fun getFeaturedPosition(): Int = items.indexOf(items.find { it.isFeatured })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            PlayerProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val items = items[position]
        holder.binding.apply {
            data = items
            first = position == 0
            last = position == itemCount - 1
        }

        holder.binding.btnLearnMore.setOnClickListener {
            onLearnMoreClick(position)
        }
    }

    inner class ViewHolder(val binding: PlayerProductItemBinding) : RecyclerView.ViewHolder(binding.root)

    inner class SourceOptionDiff(
        private val oldItems: List<ProductModel>,
        private val newItems: List<ProductModel>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldItems.size

        override fun getNewListSize() = newItems.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition].id == newItems[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition] == newItems[newItemPosition]
        }
    }
}
