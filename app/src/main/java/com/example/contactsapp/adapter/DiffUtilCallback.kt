package com.example.contactsapp.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.contactsapp.model.User

object DiffUtilCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}
