package com.example.campusmart.ui.home

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusmart.R
import com.example.campusmart.data.model.Vendor
import com.example.campusmart.data.repository.VendorRepository
import com.example.campusmart.databinding.FragmentHomeBinding
import com.example.campusmart.ui.adapter.VendorAdapter
import kotlinx.coroutines.launch
import com.example.campusmart.data.util.CategoryMapper

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val vendorRepository = VendorRepository()
    private var allVendors: List<Vendor> = emptyList()

    private var selectedCategory: String? = null
    private var currentSearchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vendorRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        setupSearch()
        setupCategoryTabs()
        loadVendors()
    }

    private fun setupSearch() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s.toString()
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupCategoryTabs() {
        binding.categoryFoodContainer.setOnClickListener { selectCategory("Food") }
        binding.categoryStationeryContainer.setOnClickListener { selectCategory("Stationery") }
        binding.categoryPrintingContainer.setOnClickListener { selectCategory("Printing") }
        binding.categoryDailyUseContainer.setOnClickListener { selectCategory("Daily Use") }
        binding.categoryMiscContainer.setOnClickListener { selectCategory("Misc") }

        binding.viewAllText.setOnClickListener {
            selectedCategory = null
            resetCategoryHighlights()
            applyFilters()
        }
    }

    private fun selectCategory(bucket: String) {
        selectedCategory = if (selectedCategory == bucket) null else bucket
        highlightSelectedCategory()
        applyFilters()
    }

    private fun highlightSelectedCategory() {
        resetCategoryHighlights()

        val (iconView, labelView) = when (selectedCategory) {
            "Food" -> binding.foodIcon to binding.foodLabel
            "Stationery" -> binding.stationeryIcon to binding.stationeryLabel
            "Printing" -> binding.printingIcon to binding.printingLabel
            "Daily Use" -> binding.dailyUseIcon to binding.dailyUseLabel
            "Misc" -> binding.miscIcon to binding.miscLabel
            else -> return
        }

        val primaryGreen = ContextCompat.getColor(requireContext(), R.color.primary_green)
        iconView.backgroundTintList = ColorStateList.valueOf(primaryGreen)
        labelView.setTextColor(primaryGreen)
    }

    private fun resetCategoryHighlights() {
        val icons = listOf(binding.foodIcon, binding.stationeryIcon, binding.printingIcon, binding.dailyUseIcon, binding.miscIcon)
        val labels = listOf(binding.foodLabel, binding.stationeryLabel, binding.printingLabel, binding.dailyUseLabel, binding.miscLabel)
        val textPrimary = ContextCompat.getColor(requireContext(), R.color.text_primary_light)

        icons.forEach { it.backgroundTintList = null } // back to default bg_category_circle color
        labels.forEach { it.setTextColor(textPrimary) }
    }


    private fun applyFilters() {
        var result = allVendors

        if (selectedCategory != null) {
            result = result.filter { CategoryMapper.mapToCategoryBucket(it.category) == selectedCategory }
        }

        if (currentSearchQuery.isNotBlank()) {
            result = result.filter {
                it.shopName.contains(currentSearchQuery, ignoreCase = true) ||
                        it.category.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        displayVendors(result)
    }

    private fun loadVendors() {
        lifecycleScope.launch {
            try {
                allVendors = vendorRepository.getAllVendors()
                applyFilters()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load vendors", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayVendors(vendors: List<Vendor>) {
        binding.vendorRecyclerView.adapter = VendorAdapter(vendors) { vendor ->
            val bundle = Bundle().apply {
                putString("vendorId", vendor.firestoreDocId)
                putString("vendorName", vendor.shopName)
                putString("vendorCategory", vendor.category)
                putString("deliveryTime", vendor.deliveryTimeText)
            }
            findNavController().navigate(R.id.vendorDetailFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}