package com.google.sample.cast.refplayer.browser

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.NetworkImageView
import com.google.sample.cast.refplayer.databinding.BrowseRowBinding
import com.google.sample.cast.refplayer.utils.CustomVolleyRequest
import com.google.sample.cast.refplayer.utils.MediaItem

interface ItemClickListener {
    fun itemClicked(v: View?, item: MediaItem?, position: Int)
}

class VideoListAdapter(private val clickListener: ItemClickListener, private val context: Context):
    RecyclerView.Adapter<VideoListAdapter.ViewHolder>() {

    private var videos: List<MediaItem>? = null
    private var mClickListener: ItemClickListener? = null

    class ViewHolder(binding: BrowseRowBinding) : RecyclerView.ViewHolder(binding.root) {
        val imgView: NetworkImageView = binding.imageView1
        val titleView: TextView = binding.textView1
        val descriptionView: TextView = binding.textView2
        val textContainer: LinearLayout = binding.textContainer

        fun setImage(imgUrl: String, context: Context) {
            val mImageLoader = CustomVolleyRequest.getInstance(context)?.imageLoader
            mImageLoader?.get(imgUrl, ImageLoader.getImageListener(imgView, 0, 0))
            imgView.setImageUrl(imgUrl, mImageLoader)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BrowseRowBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = videos?.get(position)
        item?.let { video ->
            holder.apply {
                titleView.text = video.title
                descriptionView.text = video.studio
                video.getImage(0)?.let { img ->
                    setImage(img, context)
                }
                imgView.setOnClickListener { clickListener.itemClicked(it, item, position) }
                textContainer.setOnClickListener { clickListener.itemClicked(it, item, position) }
            }
        }
    }

    override fun getItemCount(): Int = videos?.size ?: 0

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<MediaItem>?) {
        videos = data
        notifyDataSetChanged()
    }
}