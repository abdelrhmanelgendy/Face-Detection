package net.gamal.faceapprecon.savedFaces.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import net.gamal.faceapprecon.databinding.FragmentSavedFacesBinding
import net.gamal.faceapprecon.detection.presentation.mvi.FaceDetectionContract
import net.gamal.faceapprecon.detection.presentation.mvi.FaceDetectionViewModel
import net.gamal.faceapprecon.savedFaces.rec_adapter.SavedFacesAdapter

@AndroidEntryPoint
class SavedFacesFragment : Fragment() {

    private lateinit var binding: FragmentSavedFacesBinding
    private val faceDetectionViewModel by viewModels<FaceDetectionViewModel>()
    private val adapter by lazy {
        SavedFacesAdapter(requireContext()){
            faceDetectionViewModel.processIntent(FaceDetectionContract.FaceDetectionAction.DeleteFaceDataByID(it))
            faceDetectionViewModel.processIntent(FaceDetectionContract.FaceDetectionAction.FetchListOfFaceDetections(true))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSavedFacesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observerOnVMState()
        observerOnVMEvent()
        binding.savedFacesRecyclerView.adapter = adapter
        binding.savedFacesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        faceDetectionViewModel.processIntent(
            FaceDetectionContract.FaceDetectionAction.FetchListOfFaceDetections(
                true
            )
        )
    }

    private fun observerOnVMState() {
        lifecycleScope.launch {
            faceDetectionViewModel.viewState.collectLatest { event ->
                if (event.exception != null) {

                }
            }
        }
    }

    private fun observerOnVMEvent() {
        lifecycleScope.launch {
            faceDetectionViewModel.singleEvent.collect { event ->
                when (event) {
                    is FaceDetectionContract.FaceDetectionEvent.FetchedListOfFaces -> {
                        adapter.setSavedFaces(event.faces)
                        adapter.notifyDataSetChanged()
                    }

                    else -> {}

                }
            }
        }
    }
}