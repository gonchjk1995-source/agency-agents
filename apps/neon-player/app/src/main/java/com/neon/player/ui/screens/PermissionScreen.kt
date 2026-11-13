package com.neon.player.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.neon.player.ui.components.CyberBackground
import com.neon.player.ui.components.glassPanel
import com.neon.player.ui.components.neonBorder
import com.neon.player.ui.theme.Hud
import com.neon.player.ui.theme.Neon
import com.neon.player.ui.theme.NeonIcons
import com.neon.player.ui.theme.glow

@Composable
fun PermissionScreen(openSettingsInstead: Boolean, onRequest: () -> Unit, onOpenSettings: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "lock")
    val pulse by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "pulse",
    )
    CyberBackground {
        Box(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .glassPanel(Neon.Chamfer)
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("ACCESS // REQUIRED", style = Hud.copy(color = Neon.Magenta).glow(Neon.Magenta, 10f))
                Spacer(Modifier.height(20.dp))
                Box(contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(96.dp)) {
                        drawCircle(Neon.Cyan.copy(alpha = 0.12f * pulse), radius = size.minDimension / 2)
                        drawCircle(Neon.Cyan.copy(alpha = pulse), radius = size.minDimension / 2 * pulse, style = Stroke(2f * density))
                    }
                    Icon(NeonIcons.Lock, contentDescription = null, tint = Neon.TextHi, modifier = Modifier.size(36.dp))
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    "Нужен доступ к музыке",
                    style = MaterialTheme.typography.titleLarge.copy(color = Neon.TextHi),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (openSettingsInstead) {
                        "Доступ отклонён. Откройте настройки приложения и разрешите «Музыка и аудио»."
                    } else {
                        "Плеер читает аудиофайлы на телефоне. Ничего не отправляется в сеть."
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(color = Neon.TextLo),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(Neon.Cyan, Neon.Violet, Neon.Magenta)), Neon.ChamferSmall)
                        .neonBorder(Neon.ChamferSmall, glow = 1f)
                        .clickable(role = Role.Button, onClick = if (openSettingsInstead) onOpenSettings else onRequest)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (openSettingsInstead) "ОТКРЫТЬ НАСТРОЙКИ" else "РАЗРЕШИТЬ ДОСТУП",
                        style = MaterialTheme.typography.labelLarge.copy(color = Neon.Void),
                    )
                }
            }
        }
    }
}
