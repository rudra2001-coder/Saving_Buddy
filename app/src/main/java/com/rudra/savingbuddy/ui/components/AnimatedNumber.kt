package com.rudra.savingbuddy.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.rudra.savingbuddy.util.CurrencyFormatter

@Composable
fun AnimatedNumber(
    targetNumber: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    textColor: Color = Color.Unspecified,
    prefix: String = ""
) {
    val animatedNumber by animateFloatAsState(
        targetValue = targetNumber.toFloat(),
        animationSpec = tween(durationMillis = 1000),
        label = "number_animation"
    )
    
    val formattedNumber = remember(animatedNumber) {
        try {
            CurrencyFormatter.format(animatedNumber.toDouble())
        } catch (e: Exception) {
            CurrencyFormatter.format(targetNumber)
        }
    }
    
    Text(
        text = "$prefix$formattedNumber",
        modifier = modifier,
        style = style,
        color = textColor
    )
}