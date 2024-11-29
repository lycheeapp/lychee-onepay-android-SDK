package com.lychee.onepay

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentActivity

object LycheeAppUtils {
    private var lycheeLoaderDialog: LycheeLoaderDialogFragment? = null

    fun openLycheeAppOrWebsite(context: Context) {
        val websiteIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://lycheeapp.org/"))
        context.startActivity(websiteIntent)
    }

    @Synchronized
    fun showLycheeLoader(activity: FragmentActivity) {
        if (lycheeLoaderDialog != null && lycheeLoaderDialog!!.isAdded) {
            hideLycheeLoader()
        }
        lycheeLoaderDialog = LycheeLoaderDialogFragment.newInstance()
        lycheeLoaderDialog?.show(activity.supportFragmentManager, LycheeLoaderDialogFragment.TAG)
    }

    @Synchronized
    fun hideLycheeLoader() {
        lycheeLoaderDialog?.dismissAllowingStateLoss()
        lycheeLoaderDialog = null
    }
}