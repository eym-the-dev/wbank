package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent

@Composable
fun TopBarProfileActions(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.padding(end = 6.dp)
    ) {
        // Notification Center Icon with subtle Red Dot
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF161618))
                .clickable { /* Toggle notification modal state */ },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🔔",
                fontSize = 15.sp,
                lineHeight = 15.sp
            )
            // Rigid high contrast subtle Red Dot
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(Color(0xFFFF3B30), CircleShape)
                    .align(Alignment.TopEnd)
            )
        }

        // Profile Avatar Monogram with Luxury Gold trimming
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF111112))
                .border(1.2.dp, GoldAccent, CircleShape)
                .clickable { onLogout() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "IA",
                color = GoldAccent,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}
