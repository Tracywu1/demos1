package com.cc.weversetranslator

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

class OverlayController(private val service: AccessibilityService) {
    private val windowManager = service.getSystemService(WindowManager::class.java)
    private var root: LinearLayout? = null
    private var textView: TextView? = null

    fun show(text: String) {
        if (root == null) attach()
        textView?.text = text
        root?.visibility = View.VISIBLE
    }

    fun hide() {
        root?.visibility = View.GONE
    }

    fun destroy() {
        root?.let { view -> runCatching { windowManager.removeView(view) } }
        root = null
        textView = null
    }

    private fun attach() {
        val density = service.resources.displayMetrics.density
        val padding = (14 * density).toInt()
        val radius = 16 * density

        val background = GradientDrawable().apply {
            setColor(Color.argb(236, 255, 255, 255))
            cornerRadius = radius
            setStroke((1 * density).toInt().coerceAtLeast(1), Color.argb(40, 0, 0, 0))
        }

        val label = TextView(service).apply {
            setTextColor(Color.rgb(28, 28, 30))
            textSize = 16f
            setLineSpacing(0f, 1.15f)
            setTextIsSelectable(true)
        }

        val container = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding, padding, padding)
            this.background = background
            addView(
                label,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }

        val horizontalMargin = (12 * density).toInt()
        val bottomMargin = (36 * density).toInt()

        val params = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            format = android.graphics.PixelFormat.TRANSLUCENT
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            x = 0
            y = bottomMargin
        }

        container.setPadding(padding + horizontalMargin, padding, padding + horizontalMargin, padding)
        windowManager.addView(container, params)
        root = container
        textView = label
    }
}
