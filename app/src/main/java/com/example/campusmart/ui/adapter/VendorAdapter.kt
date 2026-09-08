package com.example.campusmart.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmart.data.model.Vendor
import com.example.campusmart.databinding.ItemVendorBinding

class VendorAdapter(
    private val vendors: List<Vendor>,
    private val onVendorClick: (Vendor) -> Unit
) : RecyclerView.Adapter<VendorAdapter.VendorViewHolder>() {

    inner class VendorViewHolder(val binding: ItemVendorBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VendorViewHolder {
        val binding = ItemVendorBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VendorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VendorViewHolder, position: Int) {
        val vendor = vendors[position]
        holder.binding.apply {
            vendorName.text = vendor.shopName
            vendorCategory.text = vendor.category
            vendorLocation.text = "📍 ${vendor.location}"
            vendorRating.text = "${vendor.rating} ★"
            vendorDeliveryTime.text = vendor.deliveryTimeText

            if (vendor.imageUrl.isNotBlank()) {
                com.bumptech.glide.Glide.with(root.context)
                    .load(vendor.imageUrl)
                    .centerCrop()
                    .into(vendorImage)
            }

            root.setOnClickListener { onVendorClick(vendor) }
        }
    }

    override fun getItemCount() = vendors.size
}