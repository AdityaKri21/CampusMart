package com.example.campusmart.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campusmart.data.model.MarketplaceListing
import com.example.campusmart.databinding.ItemListingBinding

class ListingAdapter(
    private val listings: MutableList<MarketplaceListing>,
    private val showDelete: Boolean = false,
    private val onDelete: ((MarketplaceListing) -> Unit)? = null
) : RecyclerView.Adapter<ListingAdapter.ListingViewHolder>() {

    inner class ListingViewHolder(val binding: ItemListingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
        val binding = ItemListingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ListingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        val listing = listings[position]
        holder.binding.apply {
            listingTitle.text = listing.title
            listingDescription.text = listing.description
            listingPrice.text = "₹${listing.price}"
            listingSeller.text = "by ${listing.sellerName}"
            listingCondition.text = listing.condition.replace("_", " ").replaceFirstChar { it.uppercase() }

            if (listing.imageUrl.isNotBlank()) {
                Glide.with(root.context)
                    .load(listing.imageUrl)
                    .centerCrop()
                    .into(listingImage)
            } else {
                listingImage.setImageDrawable(null)
            }

            // Reset state each bind since views are recycled
            contactInfoText.visibility = View.GONE
            showContactButton.text = "Show Contact"
            showContactButton.setOnClickListener {
                if (contactInfoText.visibility == View.VISIBLE) {
                    contactInfoText.visibility = View.GONE
                    showContactButton.text = "Show Contact"
                } else {
                    contactInfoText.text = if (listing.contactInfo.isNotBlank()) {
                        "Contact: ${listing.contactInfo}"
                    } else {
                        "No contact info provided"
                    }
                    contactInfoText.visibility = View.VISIBLE
                    showContactButton.text = "Hide Contact"
                }
            }

            if (showDelete) {
                deleteListingButton.visibility = View.VISIBLE
                deleteListingButton.setOnClickListener { onDelete?.invoke(listing) }
            } else {
                deleteListingButton.visibility = View.GONE
            }
        }
    }

    override fun getItemCount() = listings.size

    fun removeItem(listing: MarketplaceListing) {
        val index = listings.indexOf(listing)
        if (index != -1) {
            listings.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}