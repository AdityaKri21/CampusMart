package com.example.campusmart.ui.marketplace

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusmart.R
import com.example.campusmart.data.model.MarketplaceListing
import com.example.campusmart.data.repository.AuthRepository
import com.example.campusmart.data.repository.MarketplaceRepository
import com.example.campusmart.databinding.FragmentMarketplaceBinding
import com.example.campusmart.ui.adapter.ListingAdapter
import kotlinx.coroutines.launch

class MarketplaceFragment : Fragment() {

    private var _binding: FragmentMarketplaceBinding? = null
    private val binding get() = _binding!!

    private val marketplaceRepository = MarketplaceRepository()
    private val authRepository = AuthRepository()
    private var showingMyListings = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarketplaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.marketplaceRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.addListingButton.setOnClickListener { showAddListingDialog() }
        binding.myListingsButton.setOnClickListener {
            showingMyListings = !showingMyListings
            binding.myListingsButton.text = if (showingMyListings) "All Listings" else "My Listings"
            loadListings()
        }

        loadListings()
    }

    private fun loadListings() {
        lifecycleScope.launch {
            val uid = authRepository.getCurrentUserId() ?: return@launch

            val listings = if (showingMyListings) {
                marketplaceRepository.getMyListings(uid).toMutableList()
            } else {
                marketplaceRepository.getAllListings().toMutableList()
            }

            binding.marketplaceRecyclerView.adapter = ListingAdapter(
                listings,
                showDelete = showingMyListings,
                onDelete = { listing -> deleteListing(listing) }
            )
        }
    }

    private fun deleteListing(listing: MarketplaceListing) {
        lifecycleScope.launch {
            val result = marketplaceRepository.deleteListing(listing.listingId)
            if (result.isSuccess) {
                Toast.makeText(requireContext(), "Listing removed", Toast.LENGTH_SHORT).show()
                loadListings()
            } else {
                Toast.makeText(requireContext(), "Failed to remove", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddListingDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_listing, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.listingTitleInput)
        val descInput = dialogView.findViewById<EditText>(R.id.listingDescInput)
        val priceInput = dialogView.findViewById<EditText>(R.id.listingPriceInput)
        val categoryInput = dialogView.findViewById<EditText>(R.id.listingCategoryInput)
        val imageUrlInput = dialogView.findViewById<EditText>(R.id.listingImageUrlInput)
        val contactInput = dialogView.findViewById<EditText>(R.id.listingContactInput)
        val conditionSpinner = dialogView.findViewById<Spinner>(R.id.conditionSpinner)

        val conditions = listOf("new", "like_new", "used", "worn")
        conditionSpinner.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, conditions
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Sell an Item")
            .setView(dialogView)
            .setPositiveButton("Post") { _, _ ->
                val title = titleInput.text.toString().trim()
                val desc = descInput.text.toString().trim()
                val priceText = priceInput.text.toString().trim()
                val category = categoryInput.text.toString().trim()
                val imageUrl = imageUrlInput.text.toString().trim()
                val contactInfo = contactInput.text.toString().trim()
                val condition = conditionSpinner.selectedItem.toString()

                if (title.isEmpty() || priceText.isEmpty()) {
                    Toast.makeText(requireContext(), "Title and price are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val price = priceText.toDoubleOrNull()
                if (price == null) {
                    Toast.makeText(requireContext(), "Invalid price", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    val user = authRepository.getCurrentUser()
                    if (user == null) return@launch

                    val listing = MarketplaceListing(
                        sellerUid = user.uid,
                        sellerName = user.name,
                        title = title,
                        description = desc,
                        price = price,
                        category = category,
                        condition = condition,
                        imageUrl = imageUrl,
                        contactInfo = contactInfo
                    )

                    val result = marketplaceRepository.addListing(listing)
                    if (result.isSuccess) {
                        Toast.makeText(requireContext(), "Listing posted!", Toast.LENGTH_SHORT).show()
                        loadListings()
                    } else {
                        Toast.makeText(requireContext(), "Failed to post", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}