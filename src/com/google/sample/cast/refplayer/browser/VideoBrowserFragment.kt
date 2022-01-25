package com.google.sample.cast.refplayer.browser

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.sample.cast.refplayer.R
import com.google.sample.cast.refplayer.databinding.VideoBrowserFragmentBinding
import com.google.sample.cast.refplayer.mediaplayer.LocalPlayerActivity
import com.google.sample.cast.refplayer.utils.MediaItem

/**
 * A fragment to host a list view of the video catalog.
 */
class VideoBrowserFragment: Fragment(), ItemClickListener,
    LoaderManager.LoaderCallbacks<MutableList<MediaItem>> {

    private val binding by lazy { VideoBrowserFragmentBinding.inflate(layoutInflater) }
    private val adapter by lazy { VideoListAdapter(this, requireContext()) }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, si: Bundle?): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.list.layoutManager = layoutManager
        binding.list.adapter = adapter
        loaderManager.initLoader(0, null, this)
    }

    override fun itemClicked(v: View?, item: MediaItem?, position: Int) {
        val transitionName = getString(R.string.transition_image)
        val viewHolder =
            binding.list.findViewHolderForPosition(position) as VideoListAdapter.ViewHolder
        val imagePair: Pair<View, String> = Pair.create(viewHolder.imgView as View, transitionName)
        if (item != null) {
            activity?.let { thisActivity ->
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(thisActivity, imagePair)
                val intent = Intent(thisActivity, LocalPlayerActivity::class.java)
                intent.putExtra("media", item.toBundle())
                intent.putExtra("shouldStart", false)
                ActivityCompat.startActivity(thisActivity, intent, options.toBundle())
            }
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<MutableList<MediaItem>> =
        VideoItemLoader(requireActivity(), CATALOG_URL)

    override fun onLoadFinished(
        loader: Loader<MutableList<MediaItem>>,
        data: MutableList<MediaItem>?
    ) {
        adapter.setData(data)
        binding.progressIndicator.visibility = View.GONE
        binding.emptyView.visibility =
            if (null == data || data.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onLoaderReset(loader: Loader<MutableList<MediaItem>>) {
        adapter.setData(null)
    }

    companion object {
        private const val TAG = "VideoBrowserFragment"
        private const val CATALOG_URL =
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/f.json"
    }
}