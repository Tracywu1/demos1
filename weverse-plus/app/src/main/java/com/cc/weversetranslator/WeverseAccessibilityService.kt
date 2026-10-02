package com.cc.weversetranslator

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.ArrayDeque
import java.util.concurrent.Executors

class WeverseAccessibilityService : AccessibilityService() {
    companion object {
        private const val WEVERSE_PACKAGE = "co.benx.weverse"
        private val HANGUL = Regex("[\\u1100-\\u11FF\\u3130-\\u318F\\uAC00-\\uD7A3]")
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var overlay: OverlayController

    private val recentContext = ArrayDeque<String>()
    private var lastVisibleSnapshot: List<String> = emptyList()
    private var requestSerial = 0L

    private val scanRunnable = Runnable { scanAndTranslate() }
    private val visibilityWatchdog = object : Runnable {
        override fun run() {
            val pkg = rootInActiveWindow?.packageName?.toString()
            if (pkg != WEVERSE_PACKAGE) overlay.hide()
            mainHandler.postDelayed(this, 900)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlay = OverlayController(this)
        mainHandler.post(visibilityWatchdog)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.packageName?.toString() != WEVERSE_PACKAGE) return
        mainHandler.removeCallbacks(scanRunnable)
        mainHandler.postDelayed(scanRunnable, 320)
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        executor.shutdownNow()
        if (::overlay.isInitialized) overlay.destroy()
        super.onDestroy()
    }

    private fun scanAndTranslate() {
        val root = rootInActiveWindow ?: return
        if (root.packageName?.toString() != WEVERSE_PACKAGE) {
            overlay.hide()
            return
        }

        val visible = linkedSetOf<String>()
        collectTexts(root, visible)

        val korean = visible
            .map(::normalize)
            .filter { it.length >= 2 && HANGUL.containsMatchIn(it) }
            .distinct()

        if (korean.isEmpty()) {
            lastVisibleSnapshot = emptyList()
            return
        }

        val previous = lastVisibleSnapshot.toSet()
        var newMessages = korean.filter { it !in previous }
        lastVisibleSnapshot = korean

        if (newMessages.isEmpty()) return
        newMessages = newMessages.takeLast(4)

        val contextForRequest = recentContext.toList().takeLast(8)
        newMessages.forEach(::rememberContext)

        if (!AppPrefs.hasPlanAccess(this)) {
            overlay.show("翻译器：请先打开 Weverse Translator，使用 ChatGPT 登录")
            return
        }

        val serial = ++requestSerial
        overlay.show("翻译中…")

        executor.execute {
            val result = runCatching {
                TranslationClient(this)
                    .translate(contextForRequest, newMessages)
            }

            mainHandler.post {
                if (serial != requestSerial) return@post
                result.onSuccess { translated ->
                    overlay.show(translated)
                }.onFailure { error ->
                    overlay.show("翻译失败：${error.message ?: error.javaClass.simpleName}")
                }
            }
        }
    }

    private fun collectTexts(node: AccessibilityNodeInfo, out: MutableSet<String>) {
        node.text?.toString()?.trim()?.takeIf { it.isNotBlank() }?.let(out::add)

        node.contentDescription?.toString()?.trim()
            ?.takeIf { it.isNotBlank() && HANGUL.containsMatchIn(it) }
            ?.let(out::add)

        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                try {
                    collectTexts(child, out)
                } finally {
                    child.recycle()
                }
            }
        }
    }

    private fun normalize(value: String): String =
        value.replace(Regex("\\s+"), " ").trim()

    private fun rememberContext(text: String) {
        if (recentContext.peekLast() == text) return
        recentContext.addLast(text)
        while (recentContext.size > 12) recentContext.removeFirst()
    }
}
