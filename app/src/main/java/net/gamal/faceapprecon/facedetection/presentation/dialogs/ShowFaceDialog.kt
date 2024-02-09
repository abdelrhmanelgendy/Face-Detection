package net.gamal.faceapprecon.facedetection.presentation.dialogs

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import net.gamal.faceapprecon.databinding.ShowFaceDialogBinding


class ShowFaceDialog() : DialogFragment() {
    private lateinit var showFaceDialogBinding: ShowFaceDialogBinding
    private var faceBitmap: Bitmap? = null
    private var name: String = ""

    fun setData(faceBitmap: Bitmap, name: String) {
        this.faceBitmap = faceBitmap
        this.name = name
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        showFaceDialogBinding = ShowFaceDialogBinding.inflate(inflater)
        return showFaceDialogBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showFaceDialogBinding.apply {
            faceName.text = name
            faceImage.setImageBitmap(faceBitmap)
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window!!.setLayout(width, height)
        }
    }
}
