package com.example.campusmart.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmart.data.model.Order
import com.example.campusmart.databinding.ItemOrderBinding

class OrderAdapter(
    private val orders: List<Order>,
    private val readOnly: Boolean = false,
    private val onUpdateStatus: ((Order) -> Unit)? = null,
    private val onRate: ((Order, Int) -> Unit)? = null
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.binding.apply {
            orderStudentName.text = if (readOnly) order.vendorName else order.studentName
            orderType.text = order.orderType.replaceFirstChar { it.uppercase() } +
                    if (order.orderType == "delivery") " • ${order.deliveryAddress}" else ""
            orderTotal.text = "Total: ₹${order.totalAmount}"
            orderStatusBadge.text = order.status.replaceFirstChar { it.uppercase() }

            // Print orders show copies/color/notes instead of a product list
            orderItemsText.text = if (order.isPrintOrder) {
                "📄 ${order.copies} copies (${order.colorMode})" +
                        if (order.specialRequest.isNotBlank()) "\nNote: ${order.specialRequest}" else ""
            } else {
                order.items.joinToString("\n") { "${it.name} x${it.quantity}" }
            }

            if (readOnly) {
                updateStatusButton.visibility = View.GONE

                // Show rating UI only for completed, student-facing orders
                if (order.status == "completed") {
                    if (order.rating > 0) {
                        ratedText.visibility = View.VISIBLE
                        orderRatingBar.visibility = View.VISIBLE
                        orderRatingBar.isEnabled = false
                        orderRatingBar.rating = order.rating.toFloat()
                    } else {
                        orderRatingBar.visibility = View.VISIBLE
                        ratedText.visibility = View.GONE
                        orderRatingBar.rating = 0f
                        orderRatingBar.setOnRatingBarChangeListener { _, stars, fromUser ->
                            if (fromUser && stars > 0) {
                                onRate?.invoke(order, stars.toInt())
                            }
                        }
                    }
                } else {
                    orderRatingBar.visibility = View.GONE
                    ratedText.visibility = View.GONE
                }
            } else {
                orderRatingBar.visibility = View.GONE
                ratedText.visibility = View.GONE

                val nextStatus = getNextStatus(order.status)
                if (nextStatus == null) {
                    updateStatusButton.visibility = View.GONE
                } else {
                    updateStatusButton.visibility = View.VISIBLE
                    updateStatusButton.text = "Mark as ${nextStatus.replaceFirstChar { it.uppercase() }}"
                    updateStatusButton.setOnClickListener { onUpdateStatus?.invoke(order) }
                }
            }
        }
    }

    override fun getItemCount() = orders.size

    private fun getNextStatus(current: String): String? = when (current) {
        "placed" -> "accepted"
        "accepted" -> "preparing"
        "preparing" -> "ready"
        "ready" -> "completed"
        else -> null
    }
}