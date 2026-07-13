package com.aesprt.foldgo.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aesprt.foldgo.ui.theme.FoldGoTheme
import com.aesprt.foldgo.ui.theme.DeepOceanBlue
import com.aesprt.foldgo.ui.theme.MintGreen

@Composable
fun FoldGoLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 48.dp,
    title: String = "Fold&Go",
    supportingText: String = "Freshly Managed",
    showText: Boolean = true,
    textColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Logo Icon
        Box(contentAlignment = Alignment.TopEnd) {
            Surface(
                modifier = Modifier.size(iconSize),
                shape = RoundedCornerShape(iconSize * 0.35f),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(DeepOceanBlue, MintGreen)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Layers, // Representing folded fabric
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(iconSize * 0.6f)
                    )
                }
            }
            
            // Sparkle overlay to signify "Fresh/Clean"
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = MintGreen,
                modifier = Modifier
                    .size(iconSize * 0.4f)
                    .offset(x = (iconSize * 0.15f), y = -(iconSize * 0.15f))
            )
        }

        if (showText) {
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = (iconSize.value * 0.5f).sp,
                        letterSpacing = (-0.5).sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FoldGoLogoPreview() {
    FoldGoTheme {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            FoldGoLogo(iconSize = 64.dp)
            FoldGoLogo(iconSize = 32.dp, textColor = Color.Black)
            FoldGoLogo(iconSize = 80.dp, showText = false)
        }
    }
}
