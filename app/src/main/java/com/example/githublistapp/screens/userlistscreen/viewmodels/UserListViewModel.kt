package com.example.githublistapp.screens.userlistscreen.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.githublistapp.repositories.UserEntity
import com.example.githublistapp.repositories.UserRepositoryProtocol
import com.example.githublistapp.screens.userlistscreen.models.UserUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface UserListEvent {
    data object OnCreate : UserListEvent
}
data class UserListUiState(
    val isLoading: Boolean = false,
    val users: List<UserUi> = emptyList()
)
interface UserListViewModelInput {
    fun onEvent(e: UserListEvent)
}
interface UserListViewModelOutput {
    val uiState: StateFlow<UserListUiState>
}

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val repo: UserRepositoryProtocol
) : ViewModel(), UserListViewModelInput, UserListViewModelOutput {
    private val _uiState = MutableStateFlow(UserListUiState())
    override val uiState: StateFlow<UserListUiState> = _uiState.asStateFlow()

    override fun onEvent(event: UserListEvent) {
        when (event) {
            is UserListEvent.OnCreate -> load()
        }
    }

    private fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        runCatching { repo.fetchUsers(0, 30) }
            .onSuccess { list ->
                val ui = list.map { it.toUi() }
                _uiState.update { it.copy(isLoading = false, users = ui) }
            }
            .onFailure {
                _uiState.update { it.copy(isLoading = false) }
            }
    }

    private fun UserEntity.toUi() = UserUi(
        nameText = name.ifBlank { "(no name)" },
        urlText = url,
        avatarUrl = avatar
    )
}