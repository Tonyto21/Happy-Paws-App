package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AmberTerracotta
import com.example.ui.theme.DeepCharcoal

/**
 * Official Happy Paws Liberia Rescue Center Emblem Logo
 * Displays the authentic uploaded brand asset (Happy-paws-logo-transparent1.png).
 */
@Composable
fun HappyPawsLogo(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    showTagline: Boolean = true,
    tintColor: Color = DeepCharcoal
) {
    Column(
        modifier = modifier.testTag("happy_paws_logo"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.happy_paws_logo),
                contentDescription = "Happy Paws Liberia Official Logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        if (showTagline && size >= 90.dp) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "HAPPY PAWS LIBERIA",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = tintColor
            )
            Text(
                text = "RESCUE CENTER & VET CLINIC",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp
                ),
                color = AmberTerracotta
            )
        }
    }
}
