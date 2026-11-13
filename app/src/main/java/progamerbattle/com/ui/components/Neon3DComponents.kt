package progamerbattle.com.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import progamerbattle.com.ui.theme.CarbonBackground
import progamerbattle.com.ui.theme.CarbonBorder
import progamerbattle.com.ui.theme.CarbonBorderHighlight
import progamerbattle.com.ui.theme.CarbonCard
import progamerbattle.com.ui.theme.CarbonCardElevated
import progamerbattle.com.ui.theme.CarbonSurface
import progamerbattle.com.ui.theme.CyberGold
import progamerbattle.com.ui.theme.CyberGoldDark
import progamerbattle.com.ui.theme.CyberGoldLight
import progamerbattle.com.ui.theme.DangerRed
import progamerbattle.com.ui.theme.DangerRedDark
import progamerbattle.com.ui.theme.ElectricPurple
import progamerbattle.com.ui.theme.ElectricPurpleDark
import progamerbattle.com.ui.theme.ElectricPurpleLight
import progamerbattle.com.ui.theme.NeonCyan
import progamerbattle.com.ui.theme.NeonCyanDark
import progamerbattle.com.ui.theme.NeonCyanLight
import progamerbattle.com.ui.theme.NeonGreen
import progamerbattle.com.ui.theme.NeonGreenDark
import progamerbattle.com.ui.theme.NeonGreenLight
import progamerbattle.com.ui.theme.NeonMagenta
import progamerbattle.com.ui.theme.NeonMagentaDark
import progamerbattle.com.ui.theme.NeonMagentaLight
import progamerbattle.com.ui.theme.SpecularHighlightBright
import progamerbattle.com.ui.theme.TextMuted
import progamerbattle.com.ui.theme.TextPrimary
import progamerbattle.com.ui.theme.TextSecondary

enum class Neon3DStyle {
    CYAN,
    MAGENTA,
    PURPLE,
    GOLD,
    GREEN,
    RED,
    DARK
}

data class NeonGradientBundle(
    val topLight: Color,
    val faceTop: Color,
    val faceBottom: Color,
    val extrudedBase: Color,
    val glowColor: Color,
    val textColor: Color
)

fun getNeonBundle(style: Neon3DStyle): NeonGradientBundle = when (style) {
    Neon3DStyle.CYAN -> NeonGradientBundle(
        topLight = NeonCyanLight,
        faceTop = NeonCyan,
        faceBottom = Color(0xFF009EB3),
        extrudedBase = Color(0xFF004953),
        glowColor = NeonCyan,
        textColor = Color(0xFF041017)
    )
    Neon3DStyle.MAGENTA -> NeonGradientBundle(
        topLight = NeonMagentaLight,
        faceTop = NeonMagenta,
        faceBottom = Color(0xFFB8005C),
        extrudedBase = Color(0xFF5A002C),
        glowColor = NeonMagenta,
        textColor = Color(0xFFFFFFFF)
    )
    Neon3DStyle.PURPLE -> NeonGradientBundle(
        topLight = ElectricPurpleLight,
        faceTop = ElectricPurple,
        faceBottom = Color(0xFF6F00B8),
        extrudedBase = Color(0xFF38005D),
        glowColor = ElectricPurple,
        textColor = Color(0xFFFFFFFF)
    )
    Neon3DStyle.GOLD -> NeonGradientBundle(
        topLight = CyberGoldLight,
        faceTop = CyberGold,
        faceBottom = Color(0xFFCC9300),
        extrudedBase = Color(0xFF6B4D00),
        glowColor = CyberGold,
        textColor = Color(0xFF1F1500)
    )
    Neon3DStyle.GREEN -> NeonGradientBundle(
        topLight = NeonGreenLight,
        faceTop = NeonGreen,
        faceBottom = Color(0xFF00B862),
        extrudedBase = Color(0xFF00582F),
        glowColor = NeonGreen,
        textColor = Color(0xFF02170D)
    )
    Neon3DStyle.RED -> NeonGradientBundle(
        topLight = Color(0xFFFF7A8F),
        faceTop = DangerRed,
        faceBottom = Color(0xFFC4122E),
        extrudedBase = DangerRedDark,
        glowColor = DangerRed,
        textColor = Color(0xFFFFFFFF)
    )
    Neon3DStyle.DARK -> NeonGradientBundle(
        topLight = Color(0xFF2C3E6B),
        faceTop = CarbonCardElevated,
        faceBottom = CarbonCard,
        extrudedBase = Color(0xFF070B14),
        glowColor = NeonCyan.copy(alpha = 0.5f),
        textColor = TextPrimary
    )
}

/**
 * High-end 3D Tactile Neon Button with an extruded 3D base,
 * dynamic press physics, specular highlight, and glowing ambient drop shadow.
 */
@Composable
fun Neon3DButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: Neon3DStyle = Neon3DStyle.CYAN,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    elevationDepth: Dp = 4.dp,
    shape: Shape = RoundedCornerShape(14.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 13.dp),
    testTag: String = "neon_3d_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val bundle = getNeonBundle(style)

    val currentElevation by animateDpAsState(
        targetValue = if (isPressed && enabled) 1.dp else elevationDepth,
        animationSpec = tween(durationMillis = 80),
        label = "button_elevation"
    )
    val pressOffset by animateDpAsState(
        targetValue = if (isPressed && enabled) elevationDepth - 1.dp else 0.dp,
        animationSpec = tween(durationMillis = 80),
        label = "button_press_offset"
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .height(52.dp + elevationDepth)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        // 1. Extruded Base layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .offset(y = elevationDepth)
                .clip(shape)
                .background(
                    if (enabled) bundle.extrudedBase else Color(0xFF1E2638)
                )
        )

        // 2. Tactile Front Surface Layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .offset(y = pressOffset)
                .shadow(
                    elevation = if (enabled) currentElevation else 0.dp,
                    shape = shape,
                    ambientColor = bundle.glowColor,
                    spotColor = bundle.glowColor
                )
                .clip(shape)
                .background(
                    if (enabled) {
                        Brush.verticalGradient(
                            listOf(
                                bundle.faceTop,
                                bundle.faceBottom
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF2E3A52),
                                Color(0xFF1F2839)
                            )
                        )
                    }
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            if (enabled) SpecularHighlightBright else Color(0x20FFFFFF),
                            if (enabled) bundle.glowColor.copy(alpha = 0.4f) else Color.Transparent
                        )
                    ),
                    shape = shape
                )
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) bundle.textColor else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    color = if (enabled) bundle.textColor else TextMuted,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 0.8.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 3D Cyber Card with elevated surface, specular rim light,
 * neon accent borders, and ambient drop glow.
 */
@Composable
fun Neon3DCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    accentColor: Color = NeonCyan,
    elevation: Dp = 8.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = accentColor.copy(alpha = 0.3f),
                spotColor = Color.Black
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        CarbonCardElevated,
                        CarbonCard,
                        CarbonSurface
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        accentColor.copy(alpha = 0.8f),
                        CarbonBorder,
                        CarbonBorder.copy(alpha = 0.3f)
                    )
                ),
                shape = shape
            )
            .then(clickableModifier),
        content = content
    )
}

/**
 * 3D Neon Filter Chip with extruded tactile state for tabs & filters.
 */
@Composable
fun Neon3DChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = NeonCyan,
    icon: ImageVector? = null,
    badgeText: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressOffset = if (isPressed) 2.dp else 0.dp

    Box(
        modifier = modifier
            .offset(y = pressOffset)
            .height(40.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) {
                    Brush.verticalGradient(
                        listOf(
                            activeColor.copy(alpha = 0.28f),
                            activeColor.copy(alpha = 0.12f)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            CarbonCardElevated,
                            CarbonSurface
                        )
                    )
                }
            )
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                brush = if (selected) {
                    Brush.verticalGradient(
                        listOf(
                            activeColor,
                            activeColor.copy(alpha = 0.4f)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            CarbonBorder,
                            CarbonBorder.copy(alpha = 0.3f)
                        )
                    )
                },
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) activeColor else TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = if (selected) TextPrimary else TextSecondary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp,
                maxLines = 1,
                softWrap = false
            )
            if (badgeText != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (selected) activeColor else CarbonBorder)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = if (selected) CarbonBackground else TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

/**
 * 3D Glowing Cyber Badge / Pill
 */
@Composable
fun Neon3DPill(
    text: String,
    color: Color = NeonCyan,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * 3D Stat Cube for Dashboard & Profile
 */
@Composable
fun Neon3DStatCube(
    title: String,
    value: String,
    icon: ImageVector,
    neonColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = neonColor.copy(alpha = 0.35f),
                spotColor = Color.Black
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        CarbonCardElevated,
                        CarbonCard
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        neonColor.copy(alpha = 0.7f),
                        CarbonBorder
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(neonColor.copy(alpha = 0.16f))
                        .border(1.dp, neonColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = neonColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = neonColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
