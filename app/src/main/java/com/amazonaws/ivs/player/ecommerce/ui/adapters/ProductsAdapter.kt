package com.amazonaws.ivs.player.ecommerce.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.ivs.player.ecommerce.databinding.RowStreamItemBinding
import com.amazonaws.ivs.player.ecommerce.models.ProductModel

private val productsDiff = object : DiffUtil.ItemCallback<ProductModel>() {
    override fun areItemsTheSame(oldItem: ProductModel, newItem: ProductModel) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ProductModel, newItem: ProductModel) = oldItem == newItem
}

class ProductsAdapter : ListAdapter<ProductModel, ProductsAdapter.ViewHolder>(productsDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowStreamItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = currentList[position]
        holder.binding.product = product
    }

    inner class ViewHolder(val binding: RowStreamItemBinding) : RecyclerView.ViewHolder(binding.root)
}
