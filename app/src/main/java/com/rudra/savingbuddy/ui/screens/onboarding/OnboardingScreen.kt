package com.rudra.savingbuddy.ui.screens.onboarding

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.rudra.savingbuddy.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val gradient: List<Color>,
    val color: Color
)

private val onboardingPages = listOf(
    OnboardingPage(
        Icons.Default.AccountBalanceWallet,
        "Welcome to Saving Buddy",
        "Your all-in-one personal finance manager. Track expenses, manage budgets, and achieve your financial goals.",
        listOf(PrimaryGreen, AccentTeal),
        PrimaryGreen
    ),
    OnboardingPage(
        Icons.Default.TrendingUp,
        "Track Everything",
        "Log income, expenses, bills, and subscriptions. Scan receipts with your camera for effortless tracking.",
        listOf(AccentTeal, AccentCyan),
        AccentCyan
    ),
    OnboardingPage(
        Icons.Default.PieChart,
        "Smart Reports & Insights",
        "Get detailed analytics, generate PDF reports, and receive AI-powered insights about your spending habits.",
        listOf(AccentPurple, AccentCyan),
        AccentPurple
    ),
    OnboardingPage(
        Icons.Default.Language,
        "14 Languages Supported",
        "Use the app in your preferred language with full multi-currency support and global accessibility.",
        listOf(WarningOrange, PrimaryGreen),
        WarningOrange
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val coroutineScope = rememberCoroutineScope()
    var showSkip by remember { mutableStateOf(true) }

    LaunchedEffect(pagerState.currentPage) {
        showSkip = pagerState.currentPage < onboardingPages.size - 1
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundMain)
            .systemBarsPadding()
    ) {
        // Top skip button
        AnimatedVisibility(
            visible = showSkip,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    context.getSharedPreferences("onboarding", Context.MODE_PRIVATE).edit { putBoolean("completed", true) }
                    onComplete()
                }) {
                    Text("Skip", color = TextSecondary)
                }
            }
        }

        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val onboardingPage = onboardingPages[page]
            OnboardingPageContent(onboardingPage)
        }

        // Bottom section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                repeat(onboardingPages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 32.dp else 8.dp,
                        label = "indicator"
                    )
                    Box(
                        modifier = Modifier
                            .width(width)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isSelected) PrimaryGreen else BorderLight.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            // Action button
            if (pagerState.currentPage == onboardingPages.size - 1) {
                PremiumButton(
                    text = "Get Started",
                    onClick = {
                        context.getSharedPreferences("onboarding", Context.MODE_PRIVATE).edit { putBoolean("completed", true) }
                        onComplete()
                    },
                    icon = Icons.Default.Check,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                PremiumButton(
                    text = "Next",
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (pagerState.currentPage < onboardingPages.size - 1) {
                TextButton(onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(onboardingPages.size - 1)
                    }
                }) {
                    Text("I'm already familiar, take me to the app", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(colors = page.gradient.map { it.copy(alpha = 0.2f) })
                    )
                    .border(2.dp, page.color.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    page.icon,
                    null,
                    tint = page.color,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                page.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                page.description,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
