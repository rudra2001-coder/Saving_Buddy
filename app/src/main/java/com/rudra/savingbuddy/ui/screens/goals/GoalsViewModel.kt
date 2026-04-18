package com.rudra.savingbuddy.ui.screens.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.Goal
import com.rudra.savingbuddy.domain.model.GoalCategory
import com.rudra.savingbuddy.domain.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalsUiState(
    val activeGoals: List<Goal> = emptyList(),
    val completedGoals: List<Goal> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val editingGoal: Goal? = null,
    val selectedTab: Int = 0
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    init {
        loadGoals()
    }

    private fun loadGoals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            combine(
                goalRepository.getActiveGoals(),
                goalRepository.getCompletedGoals()
            ) { active, completed ->
                GoalsUiState(
                    activeGoals = active,
                    completedGoals = completed,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.update { it.copy(
                    activeGoals = state.activeGoals,
                    completedGoals = state.completedGoals,
                    isLoading = state.isLoading
                ) }
            }
        }
    }

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true, editingGoal = null) }
    }

    fun showEditDialog(goal: Goal) {
        _uiState.update { it.copy(showAddDialog = true, editingGoal = goal) }
    }

    fun hideDialog() {
        _uiState.update { it.copy(showAddDialog = false, editingGoal = null) }
    }

    fun saveGoal(
        name: String,
        targetAmount: Double,
        category: GoalCategory,
        deadline: Long,
        autoAllocate: Boolean,
        allocationPercentage: Double
    ) {
        viewModelScope.launch {
            val goal = Goal(
                id = _uiState.value.editingGoal?.id ?: 0,
                name = name,
                targetAmount = targetAmount,
                currentAmount = _uiState.value.editingGoal?.currentAmount ?: 0.0,
                category = category,
                deadline = deadline,
                autoAllocate = autoAllocate,
                allocationPercentage = allocationPercentage
            )
            
            if (_uiState.value.editingGoal != null) {
                goalRepository.updateGoal(goal)
            } else {
                goalRepository.insertGoal(goal)
            }
            hideDialog()
        }
    }

    fun addToGoal(goalId: Long, amount: Double) {
        viewModelScope.launch {
            goalRepository.addToGoal(goalId, amount)
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            goalRepository.deleteGoal(goal)
        }
    }

    fun markComplete(goalId: Long) {
        viewModelScope.launch {
            goalRepository.markGoalComplete(goalId)
        }
    }
}