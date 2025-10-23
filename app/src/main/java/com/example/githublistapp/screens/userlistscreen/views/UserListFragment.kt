package com.example.githublistapp.screens.userlistscreen.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.githublistapp.R
import com.example.githublistapp.screens.userlistscreen.viewmodels.UserListEvent
import com.example.githublistapp.screens.userlistscreen.viewmodels.UserListViewModel
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
}