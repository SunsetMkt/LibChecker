package com.absinthe.libchecker.ui.dialog

import android.animation.ValueAnimator
import android.content.Context
import android.os.SystemProperties
import android.view.SurfaceControl
import android.view.SurfaceControlTransactionHidden
import android.view.View
import android.view.ViewHidden
import android.view.Window
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AlertDialog
import com.absinthe.libchecker.utils.OsUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.rikka.tools.refine.Refine

class BaseDialogBuilder(context: Context) : MaterialAlertDialogBuilder(context) {
  private val supportBlur = SystemProperties.getBoolean(
    "ro.surface_flinger.supports_background_blur",
    false
  ) && !SystemProperties.getBoolean("persist.sys.sf.disable_blurs", false)

  override fun create(): AlertDialog {
    return super.create().also { dialog ->
      dialog.setOnShowListener {
        setBackgroundBlurRadius(dialog)
      }
    }
  }

  private fun setBackgroundBlurRadius(dialog: AlertDialog) {
    if (OsUtils.atLeastR()) {
      val animator = ValueAnimator.ofInt(1, 150).apply {
        interpolator = DecelerateInterpolator()
        duration = 150
      }
      val window: Window = dialog.window ?: return
      val view: View = window.decorView
      if (OsUtils.atLeastS()) {
        window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        animator.addUpdateListener { animation: ValueAnimator ->
          window.attributes.blurBehindRadius = animation.animatedValue as Int
        }
      } else if (supportBlur) {
        try {
          val viewRootImpl = Refine.unsafeCast<ViewHidden>(view).viewRootImpl ?: return
          animator.addUpdateListener { animation: ValueAnimator ->
            try {
              val transaction = SurfaceControl.Transaction()
              Refine.unsafeCast<SurfaceControlTransactionHidden>(transaction)
                .setBackgroundBlurRadius(
                  viewRootImpl.surfaceControl,
                  animation.animatedValue as Int
                )
              transaction.apply()
            } catch (t: Throwable) {
              t.printStackTrace()
            }
          }
        } catch (t: Throwable) {
          t.printStackTrace()
        }
      }
      view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View?) {}

        override fun onViewDetachedFromWindow(v: View?) {
          animator.cancel()
        }
      })
      animator.start()
    }
  }
}
