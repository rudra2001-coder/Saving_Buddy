package com.rudra.savingbuddy.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================
// PREMIUM GLASS CARD
// ============================================

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderColor: Color = BorderLight.copy(alpha = 0.3f),
    glowColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .then(if (onClick != null) Modifier.clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ) else Modifier),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = BackgroundCardGlass
        ),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        // Optional glow effect
        if (glowColor != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
        
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

// ============================================
// PREMIUM CARD WITH GRADIENT
// ============================================

@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = greenGradient,
    cornerRadius: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 4.dp else 12.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = gradientColors.first().copy(alpha = 0.3f),
                spotColor = gradientColors.last().copy(alpha = 0.3f)
            )
            .then(if (onClick != null) Modifier.clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ) else Modifier),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(colors = gradientColors)
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                content = content
            )
        }
    }
}

// ============================================
// PREMIUM BUTTON
// ============================================

@Composable
fun PremiumButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    isLoading: Boolean = false,
    gradientColors: List<Color> = greenGradient
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = when {
            isLoading -> 0.95f
            isPressed -> 0.96f
            !enabled -> 0.98f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )
    
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .scale(scale)
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = PrimaryGreen.copy(alpha = 0.5f),
            contentColor = Color.White,
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (enabled && !isLoading) 
                        Brush.linearGradient(colors = gradientColors)
                    else 
                        Brush.linearGradient(colors = listOf(
                            gradientColors.first().copy(alpha = 0.5f),
                            gradientColors.last().copy(alpha = 0.5f)
                        )),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ============================================
// PREMIUM OUTLINE BUTTON
// ============================================

@Composable
fun PremiumOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    accentColor: Color = PrimaryGreen
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "outlineScale"
    )
    
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = accentColor.copy(alpha = 0.1f),
            contentColor = accentColor,
            disabledContainerColor = SurfaceMedium.copy(alpha = 0.2f),
            disabledContentColor = SurfaceMedium
        ),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (enabled) accentColor else SurfaceMedium
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ============================================
// PREMIUM INPUT FIELD
// ============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = if (label.isNotEmpty()) {{ Text(label) }} else null,
        placeholder = if (placeholder.isNotEmpty()) {{ Text(placeholder, color = TextTertiary) }} else null,
        leadingIcon = if (leadingIcon != null) {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
        } else null,
        trailingIcon = if (trailingIcon != null) {
            {
                IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = if (isError) ExpenseRed else TextSecondary
                    )
                }
            }
        } else null,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryGreen,
            unfocusedBorderColor = BorderLight,
            focusedContainerColor = BackgroundCardGlass,
            unfocusedContainerColor = BackgroundCardGlass,
            errorBorderColor = ExpenseRed,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = PrimaryGreen
        ),
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

// ============================================
// PREMIUM EMPTY STATE
// ============================================

@Composable
fun EmptyStateView(
    icon: ImageVector = Icons.Default.Receipt,
    title: String,
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(PrimaryGreen.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = PrimaryGreen.copy(alpha = 0.6f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            PremiumButton(
                text = actionText,
                onClick = onAction,
                icon = Icons.Default.Add
            )
        }
    }
}

// ============================================
// LOADING SKELETON
// ============================================

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(SurfaceMedium.copy(alpha = alpha))
    )
}

// ============================================
// AMOUNT DISPLAY ANIMATION
// ============================================

@Composable
fun AnimatedAmount(
    amount: Double,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineMedium,
    prefix: String = "",
    suffix: String = ""
) {
    var displayAmount by remember { mutableStateOf(amount) }
    
    LaunchedEffect(amount) {
        displayAmount = amount
    }
    
    val animatedAmount by animateFloatAsState(
        targetValue = displayAmount.toFloat(),
        animationSpec = tween(1000),
        label = "amountAnimation"
    )
    
    Text(
        text = "$prefix${String.format("%,.0f", animatedAmount)}$suffix",
        style = style,
        fontWeight = FontWeight.Bold
    )
}

// ============================================
// TRUST BADGE
// ============================================

@Composable
fun TrustBadge(
    lastSynced: String = "Just now",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(BackgroundCardGlass)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Secured",
            modifier = Modifier.size(14.dp),
            tint = PrimaryGreen
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "AES-256",
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "•",
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = lastSynced,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary
        )
    }
}

// ============================================
// SUCCESS ANIMATION
// ============================================

@Composable
fun SuccessAnimation(
    modifier: Modifier = Modifier,
    onComplete: () -> Unit = {}
) {
    var showCheck by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        showCheck = true
        kotlinx.coroutines.delay(1500)
        onComplete()
    }
    
    Box(
        modifier = modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        if (showCheck) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreen),
                tint = Color.White
            )
        }
    }
}