package com.example.githublistapp.screens.userlistscreen.views

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.githublistapp.R
import com.example.githublistapp.screens.userlistscreen.models.UserUi

class UserAdapter : RecyclerView.Adapter<UserViewHolder>() {
    private val items = mutableListOf<UserUi>()

    @SuppressLint("NotifyDataSetChanged")
    fun submit(newItems: List<UserUi>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val root = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false) as ViewGroup
        return UserViewHolder(root)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}