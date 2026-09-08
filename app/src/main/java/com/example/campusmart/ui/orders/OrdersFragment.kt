package com.example.campusmart.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusmart.data.repository.AuthRepository
import com.example.campusmart.data.repository.OrderRepository
import com.example.campusmart.data.repository.VendorRepository
import com.example.campusmart.databinding.FragmentOrdersBinding
import com.example.campusmart.ui.adapter.OrderAdapter
import kotlinx.coroutines.launch

class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!

    private val orderRepository = OrderRepository()
    private val authRepository = AuthRepository()
    private val vendorRepository = VendorRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.myOrdersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        loadMyOrders()
    }

    override fun onResume() {
        super.onResume()
        loadMyOrders() // refresh every time this tab is shown
    }

    private fun loadMyOrders() {
        lifecycleScope.launch {
            val uid = authRepository.getCurrentUserId() ?: return@launch
            val orders = orderRepository.getOrdersForStudent(uid)
                .sortedByDescending { it.createdAt }

            if (orders.isEmpty()) {
                binding.emptyOrdersText.visibility = View.VISIBLE
                binding.myOrdersRecyclerView.visibility = View.GONE
            } else {
                binding.emptyOrdersText.visibility = View.GONE
                binding.myOrdersRecyclerView.visibility = View.VISIBLE
                binding.myOrdersRecyclerView.adapter = OrderAdapter(
                    orders,
                    readOnly = true,
                    onRate = { order, stars ->
                        submitRating(order, stars)
                    }
                )
            }
        }
    }

    private fun submitRating(order: com.example.campusmart.data.model.Order, stars: Int) {
        lifecycleScope.launch {
            val result = orderRepository.submitRating(order, stars, vendorRepository)
            if (result.isSuccess) {
                Toast.makeText(requireContext(), "Thanks for rating!", Toast.LENGTH_SHORT).show()
                loadMyOrders() // refresh to show locked-in rating
            } else {
                Toast.makeText(requireContext(), "Failed to submit rating", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}