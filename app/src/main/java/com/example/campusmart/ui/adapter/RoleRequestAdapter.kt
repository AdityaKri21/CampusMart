package com.example.campusmart.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmart.data.model.RoleRequest
import com.example.campusmart.databinding.ItemRoleRequestBinding

class RoleRequestAdapter(
    private val requests: MutableList<RoleRequest>,
    private val onApprove: (RoleRequest) -> Unit,
    private val onReject: (RoleRequest) -> Unit
) : RecyclerView.Adapter<RoleRequestAdapter.RequestViewHolder>() {

    inner class RequestViewHolder(val binding: ItemRoleRequestBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemRoleRequestBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]
        holder.binding.apply {
            reqShopName.text = request.shopName
            reqCategoryLocation.text = "${request.category} • ${request.location}"
            approveButton.setOnClickListener { onApprove(request) }
            rejectButton.setOnClickListener { onReject(request) }
        }
    }

    override fun getItemCount() = requests.size

    fun removeItem(request: RoleRequest) {
        val index = requests.indexOf(request)
        if (index != -1) {
            requests.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}