package com.example.campusmart.ui.vendordetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusmart.data.cart.CartManager
import com.example.campusmart.data.model.Order
import com.example.campusmart.data.repository.AuthRepository
import com.example.campusmart.data.repository.OrderRepository
import com.example.campusmart.data.repository.ProductRepository
import com.example.campusmart.data.util.CategoryMapper
import com.example.campusmart.databinding.FragmentVendorDetailBinding
import com.example.campusmart.databinding.LayoutPrintRequestBinding
import com.example.campusmart.ui.adapter.ProductAdapter
import kotlinx.coroutines.launch

class VendorDetailFragment : Fragment() {

    private var _binding: FragmentVendorDetailBinding? = null
    private val binding get() = _binding!!

    private val productRepository = ProductRepository()
    private val orderRepository = OrderRepository()
    private val authRepository = AuthRepository()

    private lateinit var vendorId: String
    private lateinit var vendorName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVendorDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vendorId = arguments?.getString("vendorId") ?: ""
        vendorName = arguments?.getString("vendorName") ?: ""
        val vendorCategory = arguments?.getString("vendorCategory") ?: ""
        val deliveryTime = arguments?.getString("deliveryTime") ?: ""

        binding.detailVendorName.text = vendorName
        binding.detailVendorInfo.text = "$vendorCategory • $deliveryTime"

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        val bucket = CategoryMapper.mapToCategoryBucket(vendorCategory)

        if (bucket == "Printing") {
            showPrintRequestForm()
        } else {
            showProductList()
        }
    }

    private fun showProductList() {
        binding.productRecyclerView.visibility = View.VISIBLE
        binding.printRequestContainer.visibility = View.GONE

        binding.productRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        lifecycleScope.launch {
            try {
                val products = productRepository.getProductsForVendor(vendorId).toMutableList()
                binding.productRecyclerView.adapter = ProductAdapter(
                    products,
                    onAddToCart = { product ->
                        val added = CartManager.addToCart(product, vendorId, vendorName)
                        if (added) {
                            Toast.makeText(requireContext(), "${product.name} added to cart", Toast.LENGTH_SHORT).show()
                        } else {
                            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                .setTitle("Start a new cart?")
                                .setMessage("Your cart has items from another vendor. Clear it and add this instead?")
                                .setPositiveButton("Yes") { _, _ ->
                                    CartManager.clearCart()
                                    CartManager.addToCart(product, vendorId, vendorName)
                                    Toast.makeText(requireContext(), "${product.name} added to cart", Toast.LENGTH_SHORT).show()
                                }
                                .setNegativeButton("Cancel", null)
                                .show()
                        }
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load products", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPrintRequestForm() {
        binding.productRecyclerView.visibility = View.GONE
        binding.printRequestContainer.visibility = View.VISIBLE

        val printBinding = LayoutPrintRequestBinding.bind(binding.printRequestContainer.getChildAt(0))

        printBinding.submitPrintRequestButton.setOnClickListener {
            submitPrintRequest(printBinding)
        }
    }

    private fun submitPrintRequest(printBinding: LayoutPrintRequestBinding) {
        val link = printBinding.documentLinkInput.text.toString().trim()
        val copiesText = printBinding.copiesInput.text.toString().trim()
        val specialRequest = printBinding.specialRequestInput.text.toString().trim()
        val colorMode = if (printBinding.colorRadio.isChecked) "Color" else "Black & White"

        if (link.isEmpty() || copiesText.isEmpty()) {
            Toast.makeText(requireContext(), "Document link and copies are required", Toast.LENGTH_SHORT).show()
            return
        }

        val copies = copiesText.toIntOrNull()
        if (copies == null || copies <= 0) {
            Toast.makeText(requireContext(), "Enter a valid number of copies", Toast.LENGTH_SHORT).show()
            return
        }

        printBinding.submitPrintRequestButton.isEnabled = false

        lifecycleScope.launch {
            val user = authRepository.getCurrentUser()
            if (user == null) {
                Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                printBinding.submitPrintRequestButton.isEnabled = true
                return@launch
            }

            val order = Order(
                studentUid = user.uid,
                studentName = user.name,
                vendorFirestoreId = vendorId,
                vendorName = vendorName,
                orderType = "takeaway",
                status = "placed",
                isPrintOrder = true,
                documentLink = link,
                copies = copies,
                colorMode = colorMode,
                specialRequest = specialRequest
            )

            val result = orderRepository.placeOrder(order)

            printBinding.submitPrintRequestButton.isEnabled = true

            if (result.isSuccess) {
                Toast.makeText(requireContext(), "Print request submitted!", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "Submission failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}