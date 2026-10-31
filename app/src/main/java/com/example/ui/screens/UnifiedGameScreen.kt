package com.example.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ads.AdMobBannerView
import com.example.ads.AdMobManager

/**
 * Unified Game Screen for Ben The Adventurer
 * Packages the exact 1,000-Level Adventure Game with Native AdMob SDK Bridge.
 * 100% Identical to the GitHub Live Web Game.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun UnifiedGameScreen(
    adMobManager: AdMobManager,
    activity: Activity,
    onOpenAdminSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val bannerId by adMobManager.bannerId.collectAsState()
    val bannerEnabled by adMobManager.bannerEnabled.collectAsState()
    var isBannerVisibleByGameState by remember { mutableStateOf(false) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    BackHandler {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            activity.moveTaskToBack(true)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.destroy()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // --- 1. FULL 1,000 LEVELS EMBEDDED ENGINE (HARDWARE ACCELERATED WEBVIEW) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setLayerType(View.LAYER_TYPE_HARDWARE, null)
                        setBackgroundColor(android.graphics.Color.parseColor("#0F172A"))

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            mediaPlaybackRequiresUserGesture = false
                            allowFileAccess = true
                            allowContentAccess = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                            setSupportZoom(false)
                        }

                        // Native AdMob Bridge to allow the 1,000-level game to trigger official AdMob Ads
                        addJavascriptInterface(
                            object {
                                @JavascriptInterface
                                fun showNativeInterstitial(trigger: String) {
                                    activity.runOnUiThread {
                                        adMobManager.showInterstitial(activity) {
                                            // Notify WebView of ad dismissal to resume game transition
                                            this@apply.post {
                                                this@apply.evaluateJavascript(
                                                    "if (typeof onNativeInterstitialClosed === 'function') onNativeInterstitialClosed('$trigger');",
                                                    null
                                                )
                                                this@apply.evaluateJavascript(
                                                    "if (typeof claimInterstitialReward === 'function') claimInterstitialReward();",
                                                    null
                                                )
                                            }
                                        }
                                    }
                                }

                                @JavascriptInterface
                                fun showBannerAd(show: Boolean) {
                                    activity.runOnUiThread {
                                        isBannerVisibleByGameState = show
                                    }
                                }

                                @JavascriptInterface
                                fun openNativeAdminSettings() {
                                    activity.runOnUiThread {
                                        onOpenAdminSettings?.invoke()
                                    }
                                }

                                @JavascriptInterface
                                fun isNativeApp(): Boolean = true
                            },
                            "AndroidBridge"
                        )

                        webChromeClient = WebChromeClient()
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                // Inform HTML game that it's running in native app
                                view?.evaluateJavascript("window.isAndroidNative = true;", null)
                            }

                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): WebResourceResponse? {
                                val urlStr = request?.url?.toString() ?: ""
                                if (urlStr.contains("app-icon.png") || urlStr.contains("favicon")) {
                                    try {
                                        val stream = ctx.assets.open("app-icon.png")
                                        return WebResourceResponse("image/png", "UTF-8", stream)
                                    } catch (_: Exception) {}
                                }
                                return super.shouldInterceptRequest(view, request)
                            }
                        }

                        // Load local offline 1,000 Levels Game from Android Assets
                        loadUrl("file:///android_asset/index.html")
                        webViewInstance = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // --- 2. BOTTOM NATIVE ADMOB BANNER (LALABAS LANG SA LEVEL SUCCESS SCREEN, MAWAWALA SA GAMEPLAY PARA SA FULL GAME ZONE) ---
        if (isBannerVisibleByGameState && bannerEnabled) {
            AdMobBannerView(
                adUnitId = bannerId,
                isEnabled = true,
                onStatusChange = { adMobManager.updateBannerStatus(it) }
            )
        }
    }
}
