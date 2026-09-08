package com.example.campusmart

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusmart.data.repository.AdminRepository
import com.example.campusmart.databinding.ActivityAdminDashboardBinding
import com.example.campusmart.ui.adapter.RoleRequestAdapter
import kotlinx.coroutines.launch

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private val adminRepository = AdminRepository()
    private lateinit var adapter: RoleRequestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }

        binding.requestsRecyclerView.layoutManager = LinearLayoutManager(this)

        loadRequests()
    }

    private fun loadRequests() {
        lifecycleScope.launch {
            val requests = adminRepository.getPendingRequests().toMutableList()
            adapter = RoleRequestAdapter(
                requests,
                onApprove = { request ->
                    lifecycleScope.launch {
                        val result = adminRepository.approveVendorRequest(request)
                        if (result.isSuccess) {
                            Toast.makeText(this@AdminDashboardActivity, "Vendor approved", Toast.LENGTH_SHORT).show()
                            adapter.removeItem(request)
                        } else {
                            Toast.makeText(this@AdminDashboardActivity, "Failed to approve", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onReject = { request ->
                    lifecycleScope.launch {
                        val result = adminRepository.rejectRequest(request.requestId)
                        if (result.isSuccess) {
                            Toast.makeText(this@AdminDashboardActivity, "Request rejected", Toast.LENGTH_SHORT).show()
                            adapter.removeItem(request)
                        } else {
                            Toast.makeText(this@AdminDashboardActivity, "Failed to reject", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
            binding.requestsRecyclerView.adapter = adapter
        }
    }
}