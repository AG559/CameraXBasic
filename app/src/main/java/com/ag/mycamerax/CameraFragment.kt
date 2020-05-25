package com.ag.mycamerax

import android.annotation.SuppressLint
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Button
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraFragment : Fragment() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var container: ConstraintLayout
    private lateinit var viewFinder: PreviewView
    private var imageCapture: ImageCapture? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private lateinit var outPutFileDir: File

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        container = view as ConstraintLayout
        viewFinder = container.findViewById(R.id.preview_view)
        cameraExecutor = Executors.newSingleThreadExecutor()
        outPutFileDir = requireActivity ().externalMediaDirs.first()
        updateCameraUi()
        bindCameraUseCases()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }

    @SuppressLint("ResourceAsColor")
    private fun updateCameraUi() {
        container.findViewById<ConstraintLayout>(R.id.camera_ui_container)?.let {
            container.removeView(it)
        }
        val control = View.inflate(requireContext(), R.layout.camera_ui_container, container)
        control.findViewById<Button>(R.id.camera_ui_container_btn_capture).setOnClickListener {
            imageCapture?.let { imageCapture ->
                val file = File(outPutFileDir, "${System.currentTimeMillis()}.jpg")
                val outputOption = ImageCapture.OutputFileOptions.Builder(file).build()
                imageCapture.takePicture(
                    outputOption,
                    cameraExecutor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            Log.d("CameraXX", file.absolutePath.toString())
                            val savedUri = outputFileResults.savedUri ?: Uri.fromFile(file)
                            val mimeType = MimeTypeMap.getSingleton()
                                .getMimeTypeFromExtension(savedUri.toFile().extension)
                            MediaScannerConnection.scanFile(
                                context,
                                arrayOf(savedUri.toFile().absolutePath),
                                arrayOf(mimeType)
                            ) { _, uri ->
                                Log.d("CameraXX", "Image capture scanned into media store: $uri")
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {

                        }

                    })
            }
        }

        control.findViewById<Button>(R.id.camera_switch_btn).setOnClickListener {
            lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            bindCameraUseCases()
        }

        control.findViewById<Button>(R.id.gallery_btn).setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                .navigate(
                    CameraFragmentDirections.actionCameraFragmentToGalleryFragment(outPutFileDir.absolutePath)
                )
        }
    }

    private fun bindCameraUseCases() {
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(lensFacing).build()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            imageCapture =
                ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
            cameraProvider.unbindAll()
            try {
                val camera =
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                preview.setSurfaceProvider(viewFinder.createSurfaceProvider(camera.cameraInfo))
            } catch (exc: Exception) {
                Log.d("CameraXX", exc.toString())
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

}
