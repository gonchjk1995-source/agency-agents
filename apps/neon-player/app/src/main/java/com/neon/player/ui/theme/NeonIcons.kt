package com.neon.player.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Thin geometric line icons drawn for this app (24×24 grid). */
object NeonIcons {
    val Play = icon("play", fill = listOf("M8,4.5 L19.5,12 L8,19.5 Z"))
    val Pause = icon("pause", fill = listOf("M6.5,4.5h3.6v15h-3.6z", "M13.9,4.5h3.6v15h-3.6z"))
    val Next = icon("next", fill = listOf("M5,5.5 L14,12 L5,18.5 Z", "M16,5.5h2.5v13h-2.5z"))
    val Previous = icon("prev", fill = listOf("M19,5.5 L10,12 L19,18.5 Z", "M5.5,5.5h2.5v13h-2.5z"))
    val Shuffle = icon(
        "shuffle",
        stroke = listOf(
            "M3,7h3c4.5,0 6.5,10 11,10h3",
            "M3,17h3c4.5,0 6.5,-10 11,-10h3",
            "M17.5,4l3,3l-3,3",
            "M17.5,14l3,3l-3,3",
        ),
    )
    val Repeat = icon(
        "repeat",
        stroke = listOf(
            "M4,11.5v-2a3,3 0 0,1 3,-3h13",
            "M17,3.5l3,3l-3,3",
            "M20,12.5v2a3,3 0 0,1 -3,3h-13",
            "M7,20.5l-3,-3l3,-3",
        ),
    )
    val RepeatOne = icon(
        "repeat_one",
        stroke = listOf(
            "M4,11.5v-2a3,3 0 0,1 3,-3h13",
            "M17,3.5l3,3l-3,3",
            "M20,12.5v2a3,3 0 0,1 -3,3h-13",
            "M7,20.5l-3,-3l3,-3",
            "M11,10.5l1.5,-1v5",
        ),
    )
    val Search = icon("search", stroke = listOf("M4,10.5a6.5,6.5 0 1,0 13,0a6.5,6.5 0 1,0 -13,0", "M15.5,15.5L20.5,20.5"))
    val Close = icon("close", stroke = listOf("M6,6L18,18", "M18,6L6,18"))
    val ChevronDown = icon("chevron_down", stroke = listOf("M5,9l7,7l7,-7"))
    val Note = icon(
        "note",
        stroke = listOf(
            "M9,17.5V5.5l11,-2v12",
            "M3.5,17.5a2.75,2.75 0 1,0 5.5,0a2.75,2.75 0 1,0 -5.5,0",
            "M14.5,15.5a2.75,2.75 0 1,0 5.5,0a2.75,2.75 0 1,0 -5.5,0",
        ),
    )
    val Lock = icon(
        "lock",
        stroke = listOf(
            "M5,11h14v9.5h-14z",
            "M8,11V8a4,4 0 0,1 8,0v3",
            "M12,14.5v2.5",
        ),
    )

    private fun icon(name: String, fill: List<String> = emptyList(), stroke: List<String> = emptyList()): ImageVector {
        val builder = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        fill.forEach { builder.addPath(pathData = addPathNodes(it), fill = SolidColor(Color.White)) }
        stroke.forEach {
            builder.addPath(
                pathData = addPathNodes(it),
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
        return builder.build()
    }
}
