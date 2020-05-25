package com.ag.mycamerax

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import java.io.File


class PhotoFrameFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = ImageView(context)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments ?: return
        val resource =
            args.getString(FILE_NAME_KEY)?.let { File(it) } ?: R.drawable.ic_action_delete
        Glide.with(view).load(resource).into(view as ImageView)
    }

    companion object {
        private const val FILE_NAME_KEY = "file_name"
        fun create(image: File) = PhotoFrameFragment().apply {
            arguments = Bundle().apply {
                putString(FILE_NAME_KEY, image.absolutePath)
            }
        }
    }
}
