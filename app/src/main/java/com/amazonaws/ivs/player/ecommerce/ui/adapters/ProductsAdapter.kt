package com.amazonaws.ivs.player.ecommerce.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.ivs.player.ecommerce.databinding.RowStreamItemBinding
import com.amazonaws.ivs.player.ecommerce.models.ProductModel
import kotlin.properties.Delegates

class ProductsAdapter : RecyclerView.Adapter<ProductsAdapter.ViewHolder>() {

    var products: List<ProductModel> by Delegates.observable(emptyList()) { _, old, new ->
        DiffUtil.calculateDiff(StreamDiff(old, new)).dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowStreamItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = products.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        holder.binding.product = product
    }

    inner class ViewHolder(val binding: RowStreamItemBinding) : RecyclerView.ViewHolder(binding.root)

    inner class StreamDiff(
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
