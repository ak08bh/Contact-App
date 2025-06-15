package com.example.contactsapp.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contactsapp.databinding.UserContactListBinding
import com.example.contactsapp.model.User

class ContactDetailAdapter(private val context: Context) :
    ListAdapter<User,ContactDetailAdapter.ContactDetailAdapterViewHolder>(DiffUtilCallback)  {

    private lateinit var onClickListener: onEditClickListener

    fun setOnClickListener(listener: onEditClickListener) {
        this.onClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactDetailAdapterViewHolder {
        return ContactDetailAdapterViewHolder(UserContactListBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun onBindViewHolder(holder: ContactDetailAdapterViewHolder, position: Int) {

        val data = getItem(position)

        holder.binding.email.text = data.email
        holder.binding.name.text = data.fullName
        holder.binding.mobile.text = data.phone
        holder.binding.profession.text = data.course


        val random = java.util.Random()
        val color = Color.rgb(
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        )

        // Tint the existing drawable background to preserve radius
        val drawable = holder.binding.main.background.mutate()
        drawable.setTint(color)

        holder.binding.edit.setOnClickListener {
            onClickListener.onClick(data.email, data.fullName, data.phone, position)
        }

    }

    class ContactDetailAdapterViewHolder(val binding: UserContactListBinding) : RecyclerView.ViewHolder(binding.root) {

    }

}

interface onEditClickListener {
     fun onClick(email: String, fullName: String, phone: String, position: Int)
}