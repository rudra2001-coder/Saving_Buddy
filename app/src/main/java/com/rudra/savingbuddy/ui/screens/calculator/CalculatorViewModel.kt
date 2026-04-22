package com.rudra.savingbuddy.ui.screens.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow

data class CalculatorUiState(
    val principal: Double = 0.0,
    val rate: Double = 0.0,
    val timeYears: Double = 0.0,
    val monthlyPayment: Double = 0.0,
    val totalInterest: Double = 0.0,
    val totalPayment: Double = 0.0,
    val resultText: String = "",
    val calculations: List<CalculationResult> = emptyList()
)

data class CalculationResult(
    val label: String,
    val value: Double,
    val description: String = ""
)

@HiltViewModel
class CalculatorViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    fun calculateLoan(
        principal: Double,
        annualRate: Double,
        years: Int
    ) {
        if (principal <= 0 || annualRate < 0 || years <= 0) {
            _uiState.value = _uiState.value.copy(resultText = "Please enter valid values")
            return
        }

        viewModelScope.launch {
            val monthlyRate = annualRate / 100 / 12
            val numPayments = years * 12

            val monthlyPayment = if (monthlyRate > 0) {
                principal * (monthlyRate * (1 + monthlyRate).pow(numPayments)) / 
                    ((1 + monthlyRate).pow(numPayments) - 1)
            } else {
                principal / numPayments
            }

            val totalPayment = monthlyPayment * numPayments
            val totalInterest = totalPayment - principal

            val results = listOf(
                CalculationResult("Monthly Payment", monthlyPayment, "Amount to pay each month"),
                CalculationResult("Total Payment", totalPayment, "Total amount over loan period"),
                CalculationResult("Total Interest", totalInterest, "Interest paid over loan period"),
                CalculationResult("Principal", principal, "Original loan amount"),
                CalculationResult("Interest Rate", annualRate.toDouble(), "Annual interest rate")
            )

            _uiState.value = _uiState.value.copy(
                principal = principal,
                rate = annualRate,
                timeYears = years.toDouble(),
                monthlyPayment = monthlyPayment,
                totalInterest = totalInterest,
                totalPayment = totalPayment,
                calculations = results,
                resultText = "Loan calculated successfully"
            )
        }
    }

    fun calculateCompoundInterest(
        principal: Double,
        annualRate: Double,
        years: Int,
        monthlyContribution: Double = 0.0
    ) {
        if (principal < 0 || annualRate < 0 || years <= 0) {
            _uiState.value = _uiState.value.copy(resultText = "Please enter valid values")
            return
        }

        viewModelScope.launch {
            val monthlyRate = annualRate / 100 / 12
            val numMonths = years * 12

            // Future value of principal
            val fvPrincipal = principal * (1 + monthlyRate).pow(numMonths)
            
            // Future value of monthly contributions
            val fvContributions = if (monthlyRate > 0 && monthlyContribution > 0) {
                monthlyContribution * (((1 + monthlyRate).pow(numMonths) - 1) / monthlyRate)
            } else {
                monthlyContribution * numMonths
            }

            val futureValue = fvPrincipal + fvContributions

            val results = listOf(
                CalculationResult("Future Value", futureValue, "Total after $years years"),
                CalculationResult("Total Contributions", principal + (monthlyContribution * numMonths), "Your total contributions"),
                CalculationResult("Interest Earned", futureValue - principal - (monthlyContribution * numMonths), "Interest earned"),
                CalculationResult("Principal", principal, "Starting amount"),
                CalculationResult("Monthly Add", monthlyContribution, "Monthly contribution")
            )

            _uiState.value = _uiState.value.copy(
                calculations = results,
                resultText = "Investment calculated: ${String.format("%.2f", futureValue)}"
            )
        }
    }

    fun calculateSavingsGoal(
        targetAmount: Double,
        months: Int,
        currentAmount: Double = 0.0
    ) {
        if (targetAmount <= 0 || months <= 0) {
            _uiState.value = _uiState.value.copy(resultText = "Please enter valid values")
            return
        }

        viewModelScope.launch {
            val remaining = targetAmount - currentAmount
            val monthlySavings = remaining / months

            val results = listOf(
                CalculationResult("Monthly Savings", monthlySavings, "Save this each month"),
                CalculationResult("Target Amount", targetAmount, "Goal amount"),
                CalculationResult("Current Amount", currentAmount, "Already saved"),
                CalculationResult("Remaining", remaining, "Amount left to save"),
                CalculationResult("Time Required", months.toDouble(), "Months to reach goal")
            )

            _uiState.value = _uiState.value.copy(
                calculations = results,
                resultText = "Save ${String.format("%.2f", monthlySavings)}/month to reach your goal"
            )
        }
    }

    fun calculateRetirement(
        currentAge: Int,
        retirementAge: Int,
        currentSavings: Double,
        monthlyContribution: Double,
        expectedReturn: Double
    ) {
        if (currentAge >= retirementAge || currentSavings < 0 || monthlyContribution < 0) {
            _uiState.value = _uiState.value.copy(resultText = "Please enter valid values")
            return
        }

        viewModelScope.launch {
            val yearsToRetirement = retirementAge - currentAge
            val months = yearsToRetirement * 12
            val monthlyRate = expectedReturn / 100 / 12

            // Future value of current savings
            val fvCurrent = currentSavings * (1 + monthlyRate).pow(months)

            // Future value of monthly contributions
            val fvContributions = if (monthlyRate > 0) {
                monthlyContribution * (((1 + monthlyRate).pow(months) - 1) / monthlyRate)
            } else {
                monthlyContribution * months
            }

            val totalAtRetirement = fvCurrent + fvContributions
            val monthlyRetirementIncome = totalAtRetirement / (12 * 25) // Assume 25 year retirement

            val results = listOf(
                CalculationResult("At Retirement", totalAtRetirement, "Total at age $retirementAge"),
                CalculationResult("Monthly Income", monthlyRetirementIncome, "Estimated monthly income"),
                CalculationResult("Current Savings", currentSavings, "Your current savings"),
                CalculationResult("Total Contributions", monthlyContribution * months, "Total you'll contribute"),
                CalculationResult("Interest Earned", totalAtRetirement - currentSavings - (monthlyContribution * months), "Investment growth")
            )

            _uiState.value = _uiState.value.copy(
                calculations = results,
                resultText = "At retirement: ${String.format("%.2f", totalAtRetirement)}"
            )
        }
    }

    fun calculateBudget(
        income: Double,
        savingsGoal: Double
    ) {
        if (income <= 0) {
            _uiState.value = _uiState.value.copy(resultText = "Please enter valid income")
            return
        }

        viewModelScope.launch {
            val remaining = income - savingsGoal
            // 50-30-20 rule: 50% needs, 30% wants, 20% savings
            val needs = remaining * 0.50
            val wants = remaining * 0.30
            val actualSavings = income * 0.20

            val results = listOf(
                CalculationResult("Needs", needs, "50% - Essentials"),
                CalculationResult("Wants", wants, "30% - Lifestyle"),
                CalculationResult("Savings", actualSavings, "20% - Savings goal"),
                CalculationResult("Budget Total", remaining, "Total monthly budget")
            )

            _uiState.value = _uiState.value.copy(
                calculations = results,
                resultText = "Based on 50-30-20 rule"
            )
        }
    }

    fun clearResults() {
        _uiState.value = CalculatorUiState()
    }
}