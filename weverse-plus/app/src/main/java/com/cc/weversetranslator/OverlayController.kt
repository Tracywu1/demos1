package com.cc.weversetranslator

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView

class OverlayController(private val service: AccessibilityService) {
    data class BubbleTranslation(
        val sourceBounds: Rect,
        val text: String
    )

    private val windowManager = service.getSystemService(WindowManager::class.java)
    private val bubbleViews = mutableListOf<View>()
    private var statusView: TextView? = null

    fun renderTranslations(items: List<BubbleTranslation>) {
        clearBubbleViews()
        items.takeLast(6).forEach { item -> addBubbleTranslation(item) }
    }

    fun showStatus(text: String) {
        val view = statusView ?: createStatusView().also { statusView = it }
        view.text = text
        view.visibility = View.VISIBLE
    }

    fun hideStatus() {
        statusView?.visibility = View.GONE
    }

    fun hideAll() {
        clearBubbleViews()
        hideStatus()
    }

    fun destroy() {
        clearBubbleViews()
        statusView?.let { runCatching { windowManager.removeView(it) } }
        statusView = null
    }

    private fun addBubbleTranslation(item: BubbleTranslation) {
        val density = service.resources.displayMetrics.density
        val screenWidth = service.resources.displayMetrics.widthPixels
        val screenHeight = service.resources.displayMetrics.heightPixels
        val margin = (8 * density).toInt()
        val gap = (5 * density).toInt()
        val minWidth = (170 * density).toInt()
        val maxWidth = (screenWidth * 0.88f).toInt()

        // Long Chinese translations need substantially more width than the Korean source bubble.
        val preferredWidth = when {
            item.text.length >= 48 -> (screenWidth * 0.88f).toInt()
            item.text.length >= 26 -> (screenWidth * 0.80f).toInt()
            item.text.length >= 14 -> maxOf(item.sourceBounds.width(), (screenWidth * 0.62f).toInt())
            else -> maxOf(item.sourceBounds.width(), minWidth)
        }
        val width = preferredWidth.coerceIn(minWidth, maxWidth)

        val background = GradientDrawable().apply {
            setColor(Color.argb(244, 255, 255, 255))
            cornerRadius = 10 * density
            setStroke((1 * density).toInt().coerceAtLeast(1), Color.argb(42, 0, 0, 0))
        }

        val label = TextView(service).apply {
            text = item.text
            textSize = 13f
            setTextColor(Color.rgb(35, 35, 38))
            setLineSpacing(0f, 1.10f)
            setPadding(
                (9 * density).toInt(),
                (6 * density).toInt(),
                (9 * density).toInt(),
                (6 * density).toInt()
            )
            this.background = background
            isSingleLine = false
            maxLines = Int.MAX_VALUE
            ellipsize = null
        }

        // Measure before attaching so a full-height card can be kept on screen.
        val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        label.measure(widthSpec, heightSpec)
        val measuredHeight = label.measuredHeight.coerceAtLeast((34 * density).toInt())

        val x = item.sourceBounds.left
            .coerceIn(margin, (screenWidth - width - margin).coerceAtLeast(margin))

        val safeTop = (52 * density).toInt()
        val safeBottom = screenHeight - (88 * density).toInt()
        val belowY = item.sourceBounds.bottom + gap
        val aboveY = item.sourceBounds.top - measuredHeight - gap
        val y = when {
            belowY + measuredHeight <= safeBottom -> belowY
            aboveY >= safeTop -> aboveY
            else -> belowY.coerceIn(safeTop, (safeBottom - measuredHeight).coerceAtLeast(safeTop))
        }

        val params = WindowManager.LayoutParams().apply {
            this.width = width
            height = WindowManager.LayoutParams.WRAP_CONTENT
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            format = android.graphics.PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.START
            this.x = x
            this.y = y
        }

        runCatching { windowManager.addView(label, params) }
            .onSuccess { bubbleViews += label }
    }

    private fun createStatusView(): TextView {
        val density = service.resources.displayMetrics.density
        val background = GradientDrawable().apply {
            setColor(Color.argb(225, 32, 32, 36))
            cornerRadius = 14 * density
        }
        val view = TextView(service).apply {
            setTextColor(Color.WHITE)
            textSize = 13f
            setPadding(
                (12 * density).toInt(),
                (7 * density).toInt(),
                (12 * density).toInt(),
                (7 * density).toInt()
            )
            this.background = background
        }
        val params = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            format = android.graphics.PixelFormat.TRANSLUCENT
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = (28 * density).toInt()
        }
        windowManager.addView(view, params)
        return view
    }

    private fun clearBubbleViews() {
        bubbleViews.forEach { view -> runCatching { windowManager.removeView(view) } }
        bubbleViews.clear()
    }
}
