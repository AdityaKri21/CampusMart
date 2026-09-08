package com.example.campusmart

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.campusmart.data.model.Vendor
import com.example.campusmart.data.repository.AuthRepository
import com.example.campusmart.data.repository.VendorRepository
import com.example.campusmart.databinding.ActivityVendorDashboardBinding
import kotlinx.coroutines.launch

class VendorDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVendorDashboardBinding
    private val authRepository = AuthRepository()
    private val vendorRepository = VendorRepository()

    private var myVendorFirestoreId: String = ""
    private var currentVendor: Vendor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVendorDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }

        loadMyShop()

        binding.manageProductsButton.setOnClickListener {
            val intent = Intent(this, ManageProductsActivity::class.java)
            intent.putExtra("vendorFirestoreId", myVendorFirestoreId)
            startActivity(intent)
        }

        binding.manageOrdersButton.setOnClickListener {
            val intent = Intent(this, VendorOrdersActivity::class.java)
            intent.putExtra("vendorFirestoreId", myVendorFirestoreId)
            startActivity(intent)
        }

        binding.editShopDetailsButton.setOnClickListener {
            showEditShopDialog()
        }
    }

    private fun loadMyShop() {
        lifecycleScope.launch {
            val user = authRepository.getCurrentUser()
            val vendor = user?.vendorId?.let { vendorRepository.getVendorByVendorId(it) }
            if (vendor != null) {
                currentVendor = vendor
                myVendorFirestoreId = vendor.firestoreDocId
                binding.vendorShopNameHeader.text = vendor.shopName
            }
        }
    }

    private fun showEditShopDialog() {
        val vendor = currentVendor
        if (vendor == null) {
            Toast.makeText(this, "Shop details not loaded yet", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_vendor, null)
        val shopNameInput = dialogView.findViewById<EditText>(R.id.editShopNameInput)
        val categoryInput = dialogView.findViewById<EditText>(R.id.editCategoryInput)
        val locationInput = dialogView.findViewById<EditText>(R.id.editLocationInput)
        val imageUrlInput = dialogView.findViewById<EditText>(R.id.editImageUrlInput)

        // Pre-fill with current values
        shopNameInput.setText(vendor.shopName)
        categoryInput.setText(vendor.category)
        locationInput.setText(vendor.location)
        imageUrlInput.setText(vendor.imageUrl)

        AlertDialog.Builder(this)
            .setTitle("Edit Shop Details")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val shopName = shopNameInput.text.toString().trim()
                val category = categoryInput.text.toString().trim()
                val location = locationInput.text.toString().trim()
                val imageUrl = imageUrlInput.text.toString().trim()

                if (shopName.isEmpty() || category.isEmpty() || location.isEmpty()) {
                    Toast.makeText(this, "Shop name, category, and location are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    val result = vendorRepository.updateVendorDetails(
                        myVendorFirestoreId, shopName, category, location, imageUrl
                    )
                    if (result.isSuccess) {
                        Toast.makeText(this@VendorDashboardActivity, "Shop details updated", Toast.LENGTH_SHORT).show()
                        loadMyShop() // refresh header + cached vendor data
                    } else {
                        Toast.makeText(this@VendorDashboardActivity, "Update failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}