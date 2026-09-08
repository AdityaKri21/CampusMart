package com.example.campusmart.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.campusmart.AdminDashboardActivity
import com.example.campusmart.LoginActivity
import com.example.campusmart.VendorDashboardActivity
import com.example.campusmart.data.repository.AuthRepository
import com.example.campusmart.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val authRepository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadProfile()

        binding.saveProfileButton.setOnClickListener {
            saveProfile()
        }

        binding.logoutButton.setOnClickListener {
            authRepository.logout()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
        binding.requestRoleButton.setOnClickListener {
            showVendorRequestDialog()
        }

        checkPendingRequest()

        binding.adminDashboardButton.setOnClickListener {
            startActivity(Intent(requireContext(), AdminDashboardActivity::class.java))
        }
        binding.vendorDashboardButton.setOnClickListener {
            startActivity(Intent(requireContext(), VendorDashboardActivity::class.java))
        }
    }

    private fun loadProfile() {
        lifecycleScope.launch {
            val user = authRepository.getCurrentUser()
            user?.let {
                binding.profileName.text = it.name
                binding.profileEmail.text = it.email
                binding.profileRole.text = "Role: ${it.role.replaceFirstChar { c -> c.uppercase() }}"
                binding.phoneInput.setText(it.phone)
                binding.roomNoInput.setText(it.roomNo)
                binding.hostelBlockInput.setText(it.hostelBlock)

                if (it.role == "admin") {
                    binding.adminDashboardButton.visibility = View.VISIBLE
                    binding.requestRoleButton.visibility = View.GONE
                } else if (it.role == "vendor") {
                    binding.vendorDashboardButton.visibility = View.VISIBLE
                    binding.requestRoleButton.visibility = View.GONE
                }
            }
        }
    }

    private fun saveProfile() {
        val phone = binding.phoneInput.text.toString().trim()
        val roomNo = binding.roomNoInput.text.toString().trim()
        val hostelBlock = binding.hostelBlockInput.text.toString().trim()

        lifecycleScope.launch {
            val result = authRepository.updateProfile(phone, roomNo, hostelBlock)
            if (result.isSuccess) {
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun checkPendingRequest() {
        lifecycleScope.launch {
            val pending = authRepository.getMyPendingRequest()
            if (pending != null) {
                binding.roleRequestStatus.visibility = View.VISIBLE
                binding.roleRequestStatus.text = "Your vendor request is pending admin approval"
                binding.requestRoleButton.visibility = View.GONE
            }
        }
    }

    private fun showVendorRequestDialog() {
        val dialogView = layoutInflater.inflate(
            com.example.campusmart.R.layout.dialog_vendor_request, null
        )
        val shopNameInput = dialogView.findViewById<android.widget.EditText>(
            com.example.campusmart.R.id.shopNameInput
        )
        val categoryInput = dialogView.findViewById<android.widget.EditText>(
            com.example.campusmart.R.id.shopCategoryInput
        )
        val locationInput = dialogView.findViewById<android.widget.EditText>(
            com.example.campusmart.R.id.shopLocationInput
        )

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Request Vendor Access")
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                val shopName = shopNameInput.text.toString().trim()
                val category = categoryInput.text.toString().trim()
                val location = locationInput.text.toString().trim()

                if (shopName.isEmpty() || category.isEmpty() || location.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    val result = authRepository.submitVendorRequest(shopName, category, location)
                    if (result.isSuccess) {
                        Toast.makeText(requireContext(), "Request submitted!", Toast.LENGTH_SHORT).show()
                        checkPendingRequest()
                    } else {
                        Toast.makeText(requireContext(), "Submission failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}