package com.example.vnnews.ui.home

import android.graphics.Typeface
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView

/** Isolate feed text from recycled Compose/GPU glyph layers; keep the rest of the UI accelerated. */
@Composable
internal fun StableArticleText(
    value: String,
    style: TextStyle,
    maxLines: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    overflow: TextOverflow = TextOverflow.Ellipsis
) {
    val density = LocalDensity.current
    val sizePx = with(density) { style.fontSize.toPx() }
    val heightPx = with(density) { style.lineHeight.toPx() }
    val weight = style.fontWeight?.weight ?: 400
    AndroidView(
        modifier = modifier.semantics { text = AnnotatedString(value) },
        factory = { context ->
            TextView(context).apply {
                includeFontPadding = true
                setPadding(0, 0, 0, 0)
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                isClickable = false
                isFocusable = false
            }
        },
        onReset = { it.text = "" },
        update = { view ->
            if (view.text.toString() != value) view.text = value
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizePx)
            view.setTextColor(color.toArgb())
            view.typeface = if (style.fontFamily == FontFamily.Serif) {
                Typeface.create("serif", if (weight >= 600) Typeface.BOLD else Typeface.NORMAL)
            } else Typeface.create(if (weight >= 500) "sans-serif-medium" else "sans-serif", Typeface.NORMAL)
            view.maxLines = maxLines
            view.ellipsize = if (overflow == TextOverflow.Ellipsis) TextUtils.TruncateAt.END else null
            view.setLineSpacing((heightPx - view.paint.fontSpacing).coerceAtLeast(0f), 1f)
        }
    )
}
