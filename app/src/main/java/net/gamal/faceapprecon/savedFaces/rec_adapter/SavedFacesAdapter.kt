package net.gamal.faceapprecon.savedFaces.rec_adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import net.gamal.faceapprecon.R
import net.gamal.faceapprecon.detection.domain.models.EncodedFaceInformation
import java.util.Calendar

class SavedFacesAdapter(private val contexts: Context,private val onDeleteClick:(Int)->Unit) :
    RecyclerView.Adapter<SavedFacesAdapter.SavedFacesViewHolder>() {

    private var savedFaces: List<EncodedFaceInformation> = listOf()

    @SuppressLint("NotifyDataSetChanged")
    fun setSavedFaces(savedFaces: List<EncodedFaceInformation>) {
        this.savedFaces=savedFaces
        println("SavedFacesAdapter: setSavedFaces: savedFaces: $savedFaces")
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedFacesViewHolder {
        val view = LayoutInflater.from(contexts).inflate(R.layout.saved_faces_rec_item, parent, false)
        return SavedFacesViewHolder(view,onDeleteClick)
    }

    override fun onBindViewHolder(holder: SavedFacesViewHolder, position: Int) {
        holder.bind(savedFaces[position])
    }

    override fun getItemCount(): Int {
        println("SavedFacesAdapter: getItemCount: savedFaces.size: ${savedFaces.size}")
        return savedFaces.size
    }

    class SavedFacesViewHolder(view: View,val onDeleteClick:(Int)->Unit) : RecyclerView.ViewHolder(view) {
        private val circleImageView: CircleImageView = view.findViewById(R.id.civ_saved_face_image)
        private val faceName: TextView = view.findViewById(R.id.tv_saved_face_name)
        private val faceAddedDate: TextView = view.findViewById(R.id.tv_saved_face_time_stamp)
        private val imageDelete: ImageView = view.findViewById(R.id.iv_delete_face)
        fun bind(encodedFaceInformation: EncodedFaceInformation) {
            println("SavedFacesAdapter: SavedFacesViewHolder: bind: encodedFaceInformation: $encodedFaceInformation")
            encodedFaceInformation.apply {

                faceName.text = name
                Calendar.getInstance().apply {
                    timeInMillis = addedAt
                    faceAddedDate.text =
                        "Added at: ${get(Calendar.HOUR)}:${get(Calendar.MINUTE)}:${get(Calendar.SECOND)}"
                }
                circleImageView.setImageBitmap(faceImage)
                imageDelete.setOnClickListener {
                    onDeleteClick(encodedFaceInformation.id)
                }
            }
        }
    }
}