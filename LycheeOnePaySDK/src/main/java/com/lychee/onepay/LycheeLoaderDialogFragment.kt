package com.lychee.onepay

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class LycheeLoaderDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        // Set transparent background for the dialog
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Set the custom loader layout
        dialog.setContentView(R.layout.dialog_lychee_loader_dialog)

        return dialog
    }

    companion object {
        const val TAG = "LycheeLoaderDialog"

        fun newInstance(): LycheeLoaderDialogFragment {
            return LycheeLoaderDialogFragment()
        }
    }
}