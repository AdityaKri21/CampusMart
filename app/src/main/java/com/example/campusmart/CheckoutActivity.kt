package com.example.campusmart

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.campusmart.data.cart.CartManager
import com.example.campusmart.data.model.Order
import com.example.campusmart.data.model.OrderItem
import com.example.campusmart.data.repository.AuthRepository
import com.example.campusmart.data.repository.OrderRepository
import com.example.campusmart.databinding.ActivityCheckoutBinding
import kotlinx.coroutines.launch

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private val orderRepository = OrderRepository()
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }

        displayOrderSummary()

        binding.orderTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            binding.deliveryAddressInput.visibility =
                if (checkedId == binding.deliveryRadio.id) View.VISIBLE else View.GONE
        }

        binding.placeOrderButton.setOnClickListener {
            placeOrder()
        }
    }

    private fun displayOrderSummary() {
        val items = CartManager.getItems()
        val summary = items.joinToString("\n") { "${it.product.name} x${it.quantity} — ₹${it.product.price * it.quantity}" }
        binding.orderSummaryText.text = summary
        binding.checkoutTotalText.text = "Total: ₹${CartManager.getTotalAmount()}"
    }

    private fun placeOrder() {
        val items = CartManager.getItems()
        if (items.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
            return
        }

        val isDelivery = binding.deliveryRadio.isChecked
        val address = binding.deliveryAddressInput.text.toString().trim()

        if (isDelivery && address.isEmpty()) {
            Toast.makeText(this, "Please enter a delivery address", Toast.LENGTH_SHORT).show()
            return
        }

        binding.placeOrderButton.isEnabled = false

        lifecycleScope.launch {
            val user = authRepository.getCurrentUser()
            if (user == null) {
                Toast.makeText(this@CheckoutActivity, "User not found", Toast.LENGTH_SHORT).show()
                binding.placeOrderButton.isEnabled = true
                return@launch
            }

            val orderItems = items.map {
                OrderItem(
                    productId = it.product.productId,
                    name = it.product.name,
                    price = it.product.price,
                    quantity = it.quantity
                )
            }

            val order = Order(
                studentUid = user.uid,
                studentName = user.name,
                vendorFirestoreId = CartManager.getVendorFirestoreId() ?: "",
                vendorName = items.first().vendorName,
                items = orderItems,
                totalAmount = CartManager.getTotalAmount(),
                orderType = if (isDelivery) "delivery" else "takeaway",
                deliveryAddress = if (isDelivery) address else "",
                status = "placed"
            )

            val result = orderRepository.placeOrder(order)

            binding.placeOrderButton.isEnabled = true

            if (result.isSuccess) {
                CartManager.clearCart()
                Toast.makeText(this@CheckoutActivity, "Order placed successfully!", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this@CheckoutActivity, "Order failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}