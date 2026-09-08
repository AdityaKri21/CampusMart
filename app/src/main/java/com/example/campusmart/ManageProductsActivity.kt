package com.example.campusmart

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusmart.data.model.Product
import com.example.campusmart.data.repository.ProductRepository
import com.example.campusmart.databinding.ActivityManageProductsBinding
import com.example.campusmart.ui.adapter.ProductAdapter
import kotlinx.coroutines.launch

class ManageProductsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageProductsBinding
    private val productRepository = ProductRepository()
    private lateinit var adapter: ProductAdapter
    private var vendorFirestoreId: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vendorFirestoreId = intent.getStringExtra("vendorFirestoreId") ?: ""

        binding.backButton.setOnClickListener { finish() }
        binding.addProductButton.setOnClickListener { showAddProductDialog() }

        binding.myProductsRecyclerView.layoutManager = LinearLayoutManager(this)

        loadProducts()
    }

    private fun loadProducts() {
        lifecycleScope.launch {
            val products = productRepository.getProductsForVendor(vendorFirestoreId).toMutableList()
            adapter = ProductAdapter(
                products,
                showDelete = true,
                onDelete = { product -> deleteProduct(product) }
            )
            binding.myProductsRecyclerView.adapter = adapter
        }
    }

    private fun deleteProduct(product: Product) {
        lifecycleScope.launch {
            val result = productRepository.deleteProduct(vendorFirestoreId, product.productId)
            if (result.isSuccess) {
                adapter.removeItem(product)
                Toast.makeText(this@ManageProductsActivity, "Product deleted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@ManageProductsActivity, "Delete failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddProductDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_product, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.productNameInput)
        val priceInput = dialogView.findViewById<EditText>(R.id.productPriceInput)
        val descInput = dialogView.findViewById<EditText>(R.id.productDescInput)
        val categoryInput = dialogView.findViewById<EditText>(R.id.productCategoryInput)
        val imageUrlInput = dialogView.findViewById<EditText>(R.id.productImageUrlInput)

        AlertDialog.Builder(this)
            .setTitle("Add Product")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameInput.text.toString().trim()
                val priceText = priceInput.text.toString().trim()
                val desc = descInput.text.toString().trim()
                val category = categoryInput.text.toString().trim()
                val imageUrl = imageUrlInput.text.toString().trim()

                if (name.isEmpty() || priceText.isEmpty()) {
                    Toast.makeText(this, "Name and price are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val price = priceText.toDoubleOrNull()
                if (price == null) {
                    Toast.makeText(this, "Invalid price", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val product = Product(
                    name = name,
                    price = price,
                    description = desc,
                    category = category,
                    imageUrl = imageUrl,
                    isAvailable = true
                )

                lifecycleScope.launch {
                    val result = productRepository.addProduct(vendorFirestoreId, product)
                    if (result.isSuccess) {
                        Toast.makeText(this@ManageProductsActivity, "Product added", Toast.LENGTH_SHORT).show()
                        loadProducts() // refresh list
                    } else {
                        Toast.makeText(this@ManageProductsActivity, "Add failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}