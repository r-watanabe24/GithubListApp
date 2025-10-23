package com.example.githublistapp.screens.userlistscreen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.githublistapp.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserListFragment : Fragment() {

    private val viewModel: UserListViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private val userAdapter = UserAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_user_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = userAdapter

        viewModel.onEvent(UserListEvent.OnCreate)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    showLoading(view, state.isLoading)
                    userAdapter.submit(state.users)
                }
            }
        }
    }

    private fun showLoading(root: View, show: Boolean) {
        val loadingView = root.findViewById<View>(R.id.loading)
        if (show) {
            loadingView.alpha = 0f
            loadingView.isVisible = true
            loadingView.animate().alpha(1f).setDuration(120).start()
        } else {
            loadingView.animate().alpha(0f).setDuration(120)
                .withEndAction { loadingView.isVisible = false }
                .start()
        }
    }

    inner class UserAdapter : RecyclerView.Adapter<UserViewHolder>() {
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

    inner class UserViewHolder(root: ViewGroup) : RecyclerView.ViewHolder(root) {
        private val avatarImageView: ImageView = root.findViewById(R.id.imgAvatar)
        private val nameTextView: TextView = root.findViewById(R.id.txtName)
        private val urlTextView: TextView = root.findViewById(R.id.txtUrl)

        fun bind(item: UserUi) {
            nameTextView.text = item.nameText
            urlTextView.text = item.urlText
            avatarImageView.load(item.avatarUrl)
        }
    }
}