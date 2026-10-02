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
        val margin = (8 * density).toInt()
        val minWidth = (150 * density).toInt()
        val maxWidth = (screenWidth * 0.82f).toInt()
        val width = item.sourceBounds.width().coerceIn(minWidth, maxWidth)
        val overlap = (5 * density).toInt()

        val background = GradientDrawable().apply {
            setColor(Color.argb(238, 255, 255, 255))
            cornerRadius = 10 * density
            setStroke((1 * density).toInt().coerceAtLeast(1), Color.argb(38, 0, 0, 0))
        }

        val label = TextView(service).apply {
            text = item.text
            textSize = 13f
            setTextColor(Color.rgb(35, 35, 38))
            setLineSpacing(0f, 1.08f)
            setPadding(
                (8 * density).toInt(),
                (4 * density).toInt(),
                (8 * density).toInt(),
                (4 * density).toInt()
            )
            this.background = background
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
        }

        val x = item.sourceBounds.left.coerceIn(margin, (screenWidth - width - margin).coerceAtLeast(margin))
        val y = (item.sourceBounds.bottom - overlap).coerceAtLeast(margin)
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
