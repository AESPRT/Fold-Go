package com.aesprt.foldgo.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.presentation.components.FoldGoLogo
import com.aesprt.foldgo.presentation.components.ModernBackground
import com.aesprt.foldgo.ui.theme.FoldGoTheme
import org.koin.androidx.compose.koinViewModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToRegistration: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: SplashViewModel = koinViewModel()
) {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
        delay(500.milliseconds)
        
        when (viewModel.getNextDestination()) {
            SplashDestination.Onboarding -> onNavigateToOnboarding()
            SplashDestination.ShopRegistration -> onNavigateToRegistration()
            SplashDestination.Dashboard -> onNavigateToDashboard()
        }
    }

    ModernBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            FoldGoLogo(
                iconSize = 80.dp,
                modifier = Modifier.alpha(alpha.value)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    FoldGoTheme {
        SplashScreen(
            onNavigateToOnboarding = {},
            onNavigateToRegistration = {},
            onNavigateToDashboard = {}
        )
    }
}
