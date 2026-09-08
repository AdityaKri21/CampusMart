package com.example.campusmart.ui.cart

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusmart.CheckoutActivity
import com.example.campusmart.data.cart.CartManager
import com.example.campusmart.databinding.FragmentCartBinding
import com.example.campusmart.ui.adapter.CartAdapter

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.checkoutButton.setOnClickListener {
            if (CartManager.getItems().isEmpty()) {
                Toast.makeText(requireContext(), "Your cart is empty", Toast.LENGTH_SHORT).show()
            } else {
                binding.checkoutButton.setOnClickListener {
                    if (CartManager.getItems().isEmpty()) {
                        Toast.makeText(requireContext(), "Your cart is empty", Toast.LENGTH_SHORT).show()
                    } else {
                        startActivity(Intent(requireContext(), CheckoutActivity::class.java))
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshCart() // refresh every time this tab becomes visible
    }

    private fun refreshCart() {
        val items = CartManager.getItems()

        if (items.isEmpty()) {
            binding.emptyCartText.visibility = View.VISIBLE
            binding.cartRecyclerView.visibility = View.GONE
            binding.checkoutBar.visibility = View.GONE
        } else {
            binding.emptyCartText.visibility = View.GONE
            binding.cartRecyclerView.visibility = View.VISIBLE
            binding.checkoutBar.visibility = View.VISIBLE

            binding.cartRecyclerView.adapter = CartAdapter(
                items,
                onIncrease = { item ->
                    CartManager.updateQuantity(item.product.productId, item.quantity + 1)
                    refreshCart()
                },
                onDecrease = { item ->
                    CartManager.updateQuantity(item.product.productId, item.quantity - 1)
                    refreshCart()
                }
            )
            binding.cartTotalText.text = "Total: ₹${CartManager.getTotalAmount()}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}