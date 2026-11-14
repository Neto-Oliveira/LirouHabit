package com.lirouhabit.app.monitor

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

/** LifecycleOwner mínimo para hospedar ComposeView dentro de uma janela de overlay. */
private class OverlayLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry

    fun onAttach() {
        savedStateController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun onDetach() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }
}

class OverlayManager(private val context: Context) {

    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    var isShowing = false
        private set

    private var overlayView: ComposeView? = null
    private var owner: OverlayLifecycleOwner? = null

    fun show(
        phrase: String,
        appName: String,
        dream: String,
        elapsedMinutes: Int,
        onResult: (victory: Boolean) -> Unit
    ) {
        mainHandler.post {
            if (isShowing || !Settings.canDrawOverlays(context)) return@post
            isShowing = true

            val lifecycleOwner = OverlayLifecycleOwner().also { it.onAttach() }
            owner = lifecycleOwner

            val view = ComposeView(context).apply {
                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeViewModelStoreOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(lifecycleOwner)
                setContent {
                    BlockingOverlay(
                        phrase = phrase,
                        appName = appName,
                        dream = dream,
                        elapsedMinutes = elapsedMinutes,
                        onVictory = {
                            hide()
                            onResult(true)
                        },
                        onDefeat = {
                            hide()
                            onResult(false)
                        }
                    )
                }
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply { gravity = Gravity.CENTER }

            runCatching { windowManager.addView(view, params) }
            overlayView = view
        }
    }

    fun hide() {
        mainHandler.post {
            overlayView?.let { runCatching { windowManager.removeView(it) } }
            overlayView = null
            owner?.onDetach()
            owner = null
            isShowing = false
        }
    }
}
