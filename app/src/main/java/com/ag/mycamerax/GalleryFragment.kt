package com.ag.mycamerax

import android.media.MediaScannerConnection
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import java.io.File


class GalleryFragment : Fragment() {
    private val args: GalleryFragmentArgs by navArgs()
    private lateinit var mediaList: MutableList<File>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rootDir = File(args.rootDirectory)
        mediaList = rootDir.listFiles()?.sortedDescending()?.toMutableList() ?: mutableListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_gallery, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mediaPager = view.findViewById<ViewPager>(R.id.photo_view_pager).apply {
            offscreenPageLimit = 2
            adapter = MediaPagerAdapter(childFragmentManager)
        }

        //Handle back button
        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigateUp()
        }


        //Handle delete button
        view.findViewById<ImageButton>(R.id.delete_button).setOnClickListener {
            mediaList.getOrNull(mediaPager.currentItem)?.let { mediaFile ->
                AlertDialog.Builder(requireContext(), R.style.Theme_MaterialComponents_Dialog)
                    .setTitle("Confirm")
                    .setMessage("Delete current Photo?")
                    .setIcon(R.drawable.ic_action_warning)
                    .setPositiveButton(android.R.string.yes) { _, _ ->

                        //delete current photo
                        mediaFile.delete()

                        //Send relevant broadcast to notify other apps of deletion
                        MediaScannerConnection.scanFile(
                            view.context,
                            arrayOf(mediaFile.absolutePath),
                            null,
                            null
                        )

                        //Notify our viewPager
                        mediaList.removeAt(mediaPager.currentItem)
                        mediaPager.adapter?.notifyDataSetChanged()
                        if (mediaPager.adapter == null) {
                            Log.d("cameraXX", "null adapter")
                        }

                        if (mediaList.isEmpty()) {
                            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                                .navigateUp()
                        }
                    }
                    .setNegativeButton(android.R.string.no, null)
                    .create()
                    .show()
            }
        }
    }

    inner class MediaPagerAdapter(fm: FragmentManager) :
        FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItemPosition(`object`: Any): Int {
            return PagerAdapter.POSITION_NONE
        }

        override fun getItem(position: Int): Fragment =
            PhotoFrameFragment.create(mediaList[position])

        override fun getCount(): Int = mediaList.size
    }
}
