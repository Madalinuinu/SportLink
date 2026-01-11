package com.example.sportlink.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Creates a shimmer brush effect for loading skeletons.
 * 
 * Uses infinite animation to create a smooth shimmer effect that loops continuously.
 * The alpha value animates between 0.3 and 0.7 to create a pulsing effect.
 * 
 * @return Brush with animated gradient for shimmer effect
 */
@Composable
fun ShimmerBrush(): Brush {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    
    return Brush.linearGradient(
        colors = listOf(
            Color.Gray.copy(alpha = shimmerAlpha),
            Color.Gray.copy(alpha = shimmerAlpha * 0.5f),
            Color.Gray.copy(alpha = shimmerAlpha)
        )
    )
}

/**
 * Loading skeleton component for a lobby item.
 * 
 * Displays a shimmer effect placeholder while lobby data is loading.
 * Used to improve perceived performance and provide visual feedback during loading states.
 * 
 * The skeleton matches the layout of [LobbyItem] to provide a smooth transition
 * when real data is loaded.
 * 
 * @param modifier Modifier to be applied to the skeleton card
 */
@Composable
fun LobbyItemSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sport name skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(20.dp)
                        .background(
                            brush = ShimmerBrush(),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
            
            // Location skeleton
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
                    .background(
                        brush = ShimmerBrush(),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            
            // Date skeleton
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(14.dp)
                    .background(
                        brush = ShimmerBrush(),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            
            // Players skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(14.dp)
                        .background(
                            brush = ShimmerBrush(),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}

