package com.cc.weversetranslator

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.ArrayDeque
import java.util.LinkedHashMap
import java.util.concurrent.Executors

class WeverseAccessibilityService : AccessibilityService() {
    companion object {
        private const val WEVERSE_PACKAGE = "co.benx.weverse"
        private val HANGUL = Regex("[\\u1100-\\u11FF\\u3130-\\u318F\\uAC00-\\uD7A3]")
    }

    private data class ScreenMessage(
        val text: String,
        val bounds: Rect
    )

    private val mainHandler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var overlay: OverlayController

    private val recentContext = ArrayDeque<String>()
    private val translationCache = object : LinkedHashMap<String, String>(128, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean = size > 160
    }
    private var requestSerial = 0L
    private var requestInFlight = false

    private val scanRunnable = Runnable { scanAndTranslate() }
    private val visibilityWatchdog = object : Runnable {
        override fun run() {
            val pkg = rootInActiveWindow?.packageName?.toString()
            if (pkg != WEVERSE_PACKAGE) overlay.hideAll()
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
        mainHandler.postDelayed(scanRunnable, 260)
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
            overlay.hideAll()
            return
        }

        val collected = mutableListOf<ScreenMessage>()
        collectTexts(root, collected)
        val visible = filterMessages(collected)

        if (visible.isEmpty()) {
            overlay.renderTranslations(emptyList())
            return
        }

        renderCached(visible)
        if (requestInFlight) return

        val newMessages = visible.filter { it.text !in translationCache.keys }
        if (newMessages.isEmpty()) {
            overlay.hideStatus()
            visible.forEach { rememberContext(it.text) }
            return
        }

        if (!AppPrefs.hasPlanAccess(this)) {
            overlay.showStatus("请先回翻译器使用 ChatGPT 登录")
            return
        }

        val batch = newMessages.takeLast(8)
        val firstNewIndex = visible.indexOfFirst { candidate -> batch.any { it.text == candidate.text } }
        val beforeNew = if (firstNewIndex > 0) visible.take(firstNewIndex).map { it.text } else emptyList()
        val contextForRequest = (recentContext.toList() + beforeNew).distinct().takeLast(8)
        val serial = ++requestSerial
        requestInFlight = true
        overlay.showStatus("翻译中…")

        executor.execute {
            val result = runCatching {
                TranslationClient(this).translateLines(
                    recentContext = contextForRequest,
                    newMessages = batch.map { it.text }
                )
            }

            mainHandler.post {
                requestInFlight = false
                if (serial != requestSerial) return@post
                result.onSuccess { translations ->
                    batch.zip(translations).forEach { (message, translated) ->
                        translationCache[message.text] = translated
                        rememberContext(message.text)
                    }
                    visible.forEach { rememberContext(it.text) }
                    overlay.hideStatus()
                    renderCached(visible)
                    mainHandler.removeCallbacks(scanRunnable)
                    mainHandler.postDelayed(scanRunnable, 120)
                }.onFailure { error ->
                    overlay.showStatus("翻译失败：${error.message ?: error.javaClass.simpleName}")
                }
            }
        }
    }

    private fun renderCached(visible: List<ScreenMessage>) {
        val items = visible.mapNotNull { message ->
            val translated = translationCache[message.text] ?: return@mapNotNull null
            OverlayController.BubbleTranslation(
                sourceBounds = Rect(message.bounds),
                text = translated
            )
        }
        overlay.renderTranslations(items)
    }

    private fun collectTexts(node: AccessibilityNodeInfo, out: MutableList<ScreenMessage>) {
        val text = node.text?.toString()?.let(::normalize).orEmpty()
        if (text.isNotBlank() && HANGUL.containsMatchIn(text)) {
            val rect = Rect()
            node.getBoundsInScreen(rect)
            if (!rect.isEmpty) out += ScreenMessage(text, rect)
        }

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

    private fun filterMessages(raw: List<ScreenMessage>): List<ScreenMessage> {
        if (raw.isEmpty()) return emptyList()
        val density = resources.displayMetrics.density
        val screenHeight = resources.displayMetrics.heightPixels
        val headerCutoff = (screenHeight * 0.13f).toInt()
        val senderLabelMaxHeight = (34 * density).toInt()
        val senderLabelMaxWidth = (150 * density).toInt()

        val deduped = raw
            .filter { it.text.isNotBlank() && HANGUL.containsMatchIn(it.text) }
            .distinctBy {
                val r = it.bounds
                "${it.text}|${r.left / 4}|${r.top / 4}|${r.right / 4}|${r.bottom / 4}"
            }
            .sortedWith(compareBy<ScreenMessage> { it.bounds.top }.thenBy { it.bounds.left })

        val repeatedShortLabels = deduped
            .groupBy { it.text }
            .filter { (text, items) ->
                text.length <= 12 &&
                    text.none(Char::isWhitespace) &&
                    items.size >= 2 &&
                    items.any { it.bounds.top < (screenHeight * 0.22f).toInt() }
            }
            .keys

        return deduped.filter { item ->
            val r = item.bounds
            val looksLikeTopTitle = r.top < headerCutoff && item.text.length <= 20
            val looksLikeSenderLabel =
                item.text.length <= 12 &&
                    item.text.none(Char::isWhitespace) &&
                    r.height() <= senderLabelMaxHeight &&
                    r.width() <= senderLabelMaxWidth
            item.text !in repeatedShortLabels && !looksLikeTopTitle && !looksLikeSenderLabel
        }
    }

    private fun normalize(value: String): String =
        value.replace(Regex("\\s+"), " ").trim()

    private fun rememberContext(text: String) {
        if (recentContext.peekLast() == text) return
        recentContext.addLast(text)
        while (recentContext.size > 16) recentContext.removeFirst()
    }
}
