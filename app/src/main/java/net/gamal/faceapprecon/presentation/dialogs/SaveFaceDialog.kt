package net.gamal.faceapprecon.presentation.dialogs

import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import net.gamal.faceapprecon.databinding.SaveFaceDialogBinding


class SaveFaceDialog() : DialogFragment() {
    private lateinit var saveFaceDialogBinding: SaveFaceDialogBinding
    private var faceBitmap: Bitmap? = null
    private var onSaveFaceClicked: ((String,Bitmap?) -> Unit)? = null
    private var onDismiss: () -> Unit = {}

    fun setFaceBitmap(faceBitmap: Bitmap): SaveFaceDialog {
        this.faceBitmap = faceBitmap
        return this
    }

    fun setOnSaveFaceClicked(onSaveFaceClicked: (String,Bitmap?) -> Unit): SaveFaceDialog {
        this.onSaveFaceClicked = onSaveFaceClicked
        return this
    }
    fun setonDismiss(onDismiss:() -> Unit = {}): SaveFaceDialog {
        this.onDismiss = onDismiss
        return this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        saveFaceDialogBinding = SaveFaceDialogBinding.inflate(inflater)
        return saveFaceDialogBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        saveFaceDialogBinding.apply {
            faceImage.setImageBitmap(faceBitmap)
            saveFaceButton.setOnClickListener {
                if(faceName.text.toString().isNotEmpty()){
                    onSaveFaceClicked?.invoke(faceName.text.toString(),faceBitmap)
                    faceName.text?.clear()
                }else
                {
                    showSnackBar("Please enter a name for the face")
                }
            }
            cancelButton.setOnClickListener {
                dismiss()
            }
        }
    }
    private fun showSnackBar(message: String) {
        Snackbar.make(saveFaceDialogBinding.root, message, Snackbar.LENGTH_SHORT).show()
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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismiss.invoke()
    }

}
