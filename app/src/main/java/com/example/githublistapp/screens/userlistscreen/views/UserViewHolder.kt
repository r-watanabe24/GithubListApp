package com.example.githublistapp.screens.userlistscreen.views

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.githublistapp.R
import com.example.githublistapp.screens.userlistscreen.models.UserUi

class UserViewHolder(root: ViewGroup) : RecyclerView.ViewHolder(root) {
    private val avatarImageView: ImageView = root.findViewById(R.id.imgAvatar)
    private val nameTextView: TextView = root.findViewById(R.id.txtName)
    private val urlTextView: TextView = root.findViewById(R.id.txtUrl)

    fun bind(item: UserUi) {
        nameTextView.text = item.nameText
        urlTextView.text = item.urlText
        avatarImageView.load(item.avatarUrl)
    }
}