package com.example.campusmart

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusmart.data.model.Order
import com.example.campusmart.data.repository.OrderRepository
import com.example.campusmart.databinding.ActivityVendorOrdersBinding
import com.example.campusmart.ui.adapter.OrderAdapter
import kotlinx.coroutines.launch

class VendorOrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVendorOrdersBinding
    private val orderRepository = OrderRepository()
    private var vendorFirestoreId: String = ""
    private var showingOngoing = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVendorOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vendorFirestoreId = intent.getStringExtra("vendorFirestoreId") ?: ""

        binding.backButton.setOnClickListener { finish() }
        binding.ordersRecyclerView.layoutManager = LinearLayoutManager(this)

        binding.ongoingTab.setOnClickListener { switchTab(true) }
        binding.completedTab.setOnClickListener { switchTab(false) }

        loadOrders()
    }

    private fun switchTab(ongoing: Boolean) {
        showingOngoing = ongoing
        val activeColor = androidx.core.content.ContextCompat.getColor(this, R.color.primary_green_light)
        val activeText = androidx.core.content.ContextCompat.getColor(this, R.color.primary_green)
        val inactiveText = androidx.core.content.ContextCompat.getColor(this, R.color.text_secondary_light)

        if (ongoing) {
            binding.ongoingTab.setBackgroundColor(activeColor)
            binding.ongoingTab.setTextColor(activeText)
            binding.completedTab.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            binding.completedTab.setTextColor(inactiveText)
        } else {
            binding.completedTab.setBackgroundColor(activeColor)
            binding.completedTab.setTextColor(activeText)
            binding.ongoingTab.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            binding.ongoingTab.setTextColor(inactiveText)
        }
        loadOrders()
    }

    private fun loadOrders() {
        lifecycleScope.launch {
            val orders = orderRepository.getOrdersForVendorByStatus(vendorFirestoreId, showingOngoing)
            binding.ordersRecyclerView.adapter = OrderAdapter(orders, readOnly = false, onUpdateStatus = { order ->
                updateStatus(order)
            })
        }
    }

    private fun updateStatus(order: Order) {
        val nextStatus = when (order.status) {
            "placed" -> "accepted"
            "accepted" -> "preparing"
            "preparing" -> "ready"
            "ready" -> "completed"
            else -> return
        }

        lifecycleScope.launch {
            val result = orderRepository.updateOrderStatus(order.orderId, nextStatus)
            if (result.isSuccess) {
                Toast.makeText(this@VendorOrdersActivity, "Order marked as $nextStatus", Toast.LENGTH_SHORT).show()
                loadOrders() // refresh
            } else {
                Toast.makeText(this@VendorOrdersActivity, "Update failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}