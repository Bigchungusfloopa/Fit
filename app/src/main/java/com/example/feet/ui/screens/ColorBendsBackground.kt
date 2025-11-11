package com.example.feet.ui.screens

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import com.example.feet.ui.theme.AGSL_SHADER_STRING
import kotlin.math.cos
import kotlin.math.sin

// 1. Create the Composable, matching the props from the React component
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun ColorBendsBackground(
    modifier: Modifier = Modifier,
    rotation: Float = 45f,
    speed: Float = 0.2f,
    colors: List<Color> = listOf(
        Color(0xFFFF5E0E),
        Color(0xFFFFC0CB),
        Color(0xFF8A2BE2),
        Color(0xFF00FFFF)
    ),
    transparent: Boolean = true,
    scale: Float = 1.2f,
    frequency: Float = 1.0f,
    warpStrength: Float = 1.0f,
    mouseInfluence: Float = 0.8f,
    parallax: Float = 0.5f,
    noise: Float = 0.1f
) {
    // 2. Compile the shader string. This is fast.
    val shader = remember { RuntimeShader(AGSL_SHADER_STRING) }

    // --- FIX ---
    // Create a list of 8 colors, padding with Black if the list is shorter
    val paddedColors = remember(colors) {
        val maxColors = 8
        val baseColors = colors.take(maxColors)
        baseColors + List(maxColors - baseColors.size) { Color.Black }
    }
    // --- END FIX ---


    // 4. Set up states for animated uniforms
    var canvasSize by remember { mutableStateOf(Offset(1f, 1f)) }
    var pointerTarget by remember { mutableStateOf(Offset.Zero) }

    // This fix was from before, and is still correct
    var pointerCurrent by remember { mutableStateOf(Offset.Zero) } // For smoothing

    // Use withFrameNanos for a high-res time uniform, like in the original
    val time by produceState(0f) {
        val startTime = withFrameNanos { it }
        while (true) {
            withFrameNanos {
                value = (it - startTime) / 1_000_000_000f // time in seconds
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                canvasSize = Offset(it.width.toFloat(), it.height.toFloat())
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        // Get pointer position
                        pointerTarget = awaitPointerEvent().changes.first().position
                    }
                }
            }
            .drawWithCache {
                // 5. Create the brush inside drawWithCache
                val brush = ShaderBrush(shader)

                // 6. Smooth the pointer, just like the original React code's 'lerp'
                val target = pointerTarget
                val current = pointerCurrent
                val newCurrent = Offset(
                    x = current.x + (target.x - current.x) * 0.1f, // lerp
                    y = current.y + (target.y - current.y) * 0.1f
                )

                pointerCurrent = newCurrent

                // 7. Convert pointer to NDC coordinates [-1, 1]
                val ndcX = (newCurrent.x / canvasSize.x) * 2f - 1f
                val ndcY = -((newCurrent.y / canvasSize.y) * 2f - 1f) // Y is inverted

                // 8. Calculate rotation
                val rad = (rotation * Math.PI) / 180
                val c = cos(rad).toFloat()
                val s = sin(rad).toFloat()

                // 9. Set all uniforms every frame
                shader.setFloatUniform("uResolution", canvasSize.x, canvasSize.y)
                shader.setFloatUniform("uTime", time)
                shader.setFloatUniform("uSpeed", speed)
                shader.setFloatUniform("uRot", c, s)
                shader.setIntUniform("uColorCount", colors.size.coerceAtMost(8))

                // --- FIX ---
                // Set each of the 8 color uniforms individually
                paddedColors.forEachIndexed { index, color ->
                    val argb = color.toArgb()
                    val r = android.graphics.Color.red(argb) / 255f
                    val g = android.graphics.Color.green(argb) / 255f
                    val b = android.graphics.Color.blue(argb) / 255f
                    shader.setFloatUniform("uColor$index", r, g, b)
                }
                // --- END FIX ---

                shader.setIntUniform("uTransparent", if (transparent) 1 else 0)
                shader.setFloatUniform("uScale", scale)
                shader.setFloatUniform("uFrequency", frequency)
                shader.setFloatUniform("uWarpStrength", warpStrength)
                shader.setFloatUniform("uPointer", ndcX, ndcY)
                shader.setFloatUniform("uMouseInfluence", mouseInfluence)
                shader.setFloatUniform("uParallax", parallax)
                shader.setFloatUniform("uNoise", noise)

                onDrawBehind {
                    // 10. Draw the shader
                    drawRect(brush)
                }
            }
    )
}