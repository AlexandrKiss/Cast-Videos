package com.google.sample.cast.refplayer.mediaplayer

import com.google.sample.cast.refplayer.R

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Point
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import com.android.volley.toolbox.ImageLoader
import com.google.sample.cast.refplayer.databinding.PlayerActivityBinding
import com.google.sample.cast.refplayer.settings.CastPreference
import com.google.sample.cast.refplayer.utils.*
import java.util.*

class LocalPlayerActivity: AppCompatActivity() {

    private val binding by lazy { PlayerActivityBinding.inflate(layoutInflater) }

    private var mSeekbarTimer: Timer? = null
    private var mControllersTimer: Timer? = null
    private var mPlaybackState: PlaybackState? = null
    private val mHandler = Handler()
    private val mAspectRatio = 72f / 128
    private var mSelectedMedia: MediaItem? = null
    private var mControllersVisible = false
    private var mDuration = 0
    private var mLocation: PlaybackLocation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        loadViews()
        setupControlsCallbacks()
        // see what we need to play and where
        val bundle = intent.extras
        if (bundle != null) {
            mSelectedMedia = MediaItem.fromBundle(intent.getBundleExtra("media"))
            setupActionBar()
            val shouldStartPlayback = bundle.getBoolean("shouldStart")
            val startPosition = bundle.getInt("startPosition", 0)
            binding.videoView1.setVideoURI(Uri.parse(mSelectedMedia?.url))
            Log.d(TAG, "Setting url of the VideoView to: " + mSelectedMedia?.url)
            if (shouldStartPlayback) {
                // this will be the case only if we are coming from the
                // CastControllerActivity by disconnecting from a device
                mPlaybackState = PlaybackState.PLAYING
                updatePlaybackLocation(PlaybackLocation.LOCAL)
                updatePlayButton(mPlaybackState)
                if (startPosition > 0) {
                    binding.videoView1.seekTo(startPosition)
                }
                binding.videoView1.start()
                startControllersTimer()
            } else {
                // we should load the video but pause it
                // and show the album art.
                updatePlaybackLocation(PlaybackLocation.LOCAL)
                mPlaybackState = PlaybackState.IDLE
                updatePlayButton(mPlaybackState)
            }
        }
        updateMetadata(true)
    }

    private fun updatePlaybackLocation(location: PlaybackLocation) {
        mLocation = location
        if (location == PlaybackLocation.LOCAL) {
            if (mPlaybackState == PlaybackState.PLAYING
                || mPlaybackState == PlaybackState.BUFFERING
            ) {
                setCoverArtStatus(null)
                startControllersTimer()
            } else {
                stopControllersTimer()
                setCoverArtStatus(mSelectedMedia!!.getImage(0))
            }
        } else {
            stopControllersTimer()
            setCoverArtStatus(mSelectedMedia!!.getImage(0))
            updateControllersVisibility(false)
        }
    }

    private fun play(position: Int) {
        startControllersTimer()
        when (mLocation) {
            PlaybackLocation.LOCAL -> {
                binding.videoView1.seekTo(position)
                binding.videoView1.start()
            }
            PlaybackLocation.REMOTE -> {
                mPlaybackState = PlaybackState.BUFFERING
                updatePlayButton(mPlaybackState)
            }
            else -> {}
        }
        restartTrickplayTimer()
    }

    private fun togglePlayback() {
        stopControllersTimer()
        when (mPlaybackState) {
            PlaybackState.PAUSED -> when (mLocation) {
                PlaybackLocation.LOCAL -> {
                    binding.videoView1.start()
                    Log.d(TAG, "Playing locally...")
                    mPlaybackState = PlaybackState.PLAYING
                    startControllersTimer()
                    restartTrickplayTimer()
                    updatePlaybackLocation(PlaybackLocation.LOCAL)
                }
                PlaybackLocation.REMOTE -> finish()
                else -> {}
            }
            PlaybackState.PLAYING -> {
                mPlaybackState = PlaybackState.PAUSED
                binding.videoView1.pause()
            }
            PlaybackState.IDLE -> when (mLocation) {
                PlaybackLocation.LOCAL -> {
                    binding.videoView1.setVideoURI(Uri.parse(mSelectedMedia!!.url))
                    binding.videoView1.seekTo(0)
                    binding.videoView1.start()
                    mPlaybackState = PlaybackState.PLAYING
                    restartTrickplayTimer()
                    updatePlaybackLocation(PlaybackLocation.LOCAL)
                }
                PlaybackLocation.REMOTE -> {}
                else -> {}
            }
            else -> {}
        }
        updatePlayButton(mPlaybackState)
    }

    private fun setCoverArtStatus(url: String?) {
        if (url != null) {
            val mImageLoader = CustomVolleyRequest.getInstance(this.applicationContext)
                .imageLoader
            mImageLoader[url, ImageLoader.getImageListener(binding.coverArtView, 0, 0)]
            binding.coverArtView.setImageUrl(url, mImageLoader)
            binding.coverArtView.visibility = View.VISIBLE
            binding.videoView1.visibility = View.INVISIBLE
        } else {
            binding.coverArtView.visibility = View.GONE
            binding.videoView1.visibility = View.VISIBLE
        }
    }

    private fun stopTrickplayTimer() {
        Log.d(TAG, "Stopped TrickPlay Timer")
        mSeekbarTimer?.cancel()
    }

    private fun restartTrickplayTimer() {
        stopTrickplayTimer()
        mSeekbarTimer = Timer()
        mSeekbarTimer!!.scheduleAtFixedRate(UpdateSeekbarTask(), 100, 1000)
        Log.d(TAG, "Restarted TrickPlay Timer")
    }

    private fun stopControllersTimer() {
        mControllersTimer?.cancel()
    }

    private fun startControllersTimer() {
        mControllersTimer?.cancel()
        if (mLocation == PlaybackLocation.REMOTE) {
            return
        }
        mControllersTimer = Timer()
        mControllersTimer!!.schedule(HideControllersTask(), 5000)
    }

    // should be called from the main thread
    private fun updateControllersVisibility(show: Boolean) {
        if (show) {
            supportActionBar!!.show()
            binding.controllers.visibility = View.VISIBLE
        } else {
            if (!Utils.isOrientationPortrait(this)) {
                supportActionBar!!.hide()
            }
            binding.controllers.visibility = View.INVISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() was called")
        if (mLocation == PlaybackLocation.LOCAL) {
            if (mSeekbarTimer != null) {
                mSeekbarTimer!!.cancel()
                mSeekbarTimer = null
            }
            if (mControllersTimer != null) {
                mControllersTimer!!.cancel()
            }
            // since we are playing locally, we need to stop the playback of
            // video (if user is not watching, pause it!)
            binding.videoView1.pause()
            mPlaybackState = PlaybackState.PAUSED
            updatePlayButton(PlaybackState.PAUSED)
        }
    }

    override fun onStop() {
        Log.d(TAG, "onStop() was called")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy() is called")
        stopControllersTimer()
        stopTrickplayTimer()
        super.onDestroy()
    }

    override fun onStart() {
        Log.d(TAG, "onStart was called")
        super.onStart()
    }

    override fun onResume() {
        Log.d(TAG, "onResume() was called")
        updatePlaybackLocation(PlaybackLocation.LOCAL)
        super.onResume()
    }

    inner class UpdateSeekbarTask : TimerTask() {
        override fun run() {
            mHandler.post {
                if (mLocation == PlaybackLocation.LOCAL) {
                    val currentPos: Int = binding.videoView1.currentPosition
                    updateSeekbar(currentPos, mDuration)
                }
            }
        }
    }

    inner class HideControllersTask : TimerTask() {
        override fun run() {
            mHandler.post {
                updateControllersVisibility(false)
                mControllersVisible = false
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupControlsCallbacks() {
        binding.videoView1.setOnErrorListener { mp, what, extra ->
            Log.e(
                TAG, "OnErrorListener.onError(): VideoView encountered an "
                        + "error, what: " + what + ", extra: " + extra
            )
            val msg: String = when {
                extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT ->
                    getString(R.string.video_error_media_load_timeout)
                what == MediaPlayer.MEDIA_ERROR_SERVER_DIED ->
                    getString(R.string.video_error_server_unaccessible)
                else -> getString(R.string.video_error_unknown_error)
            }
            Utils.showErrorDialog(this@LocalPlayerActivity, msg)
            binding.videoView1.stopPlayback()
            mPlaybackState = PlaybackState.IDLE
            updatePlayButton(mPlaybackState)
            true
        }
        binding.videoView1.setOnPreparedListener { mp ->
            Log.d(TAG, "onPrepared is reached")
            mDuration = mp.duration
            binding.endText.text = mDuration.formatMillis()
            binding.seekBar1.max = mDuration
            restartTrickplayTimer()
        }
        binding.videoView1.setOnCompletionListener {
            stopTrickplayTimer()
            Log.d(TAG, "setOnCompletionListener()")
            mPlaybackState = PlaybackState.IDLE
            updatePlayButton(mPlaybackState!!)
        }
        binding.videoView1.setOnTouchListener { _, _ ->
            if (!mControllersVisible) {
                updateControllersVisibility(true)
            }
            startControllersTimer()
            false
        }
        binding.seekBar1.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (mPlaybackState == PlaybackState.PLAYING) {
                    play(seekBar.progress)
                } else if (mPlaybackState != PlaybackState.IDLE) {
                    binding.videoView1.seekTo(seekBar.progress)
                }
                startControllersTimer()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                stopTrickplayTimer()
                binding.videoView1.pause()
                stopControllersTimer()
            }

            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                binding.startText.text = progress.formatMillis()
            }
        })
        binding.playPause.setOnClickListener {
            if (mLocation == PlaybackLocation.LOCAL) {
                togglePlayback()
            }
        }
    }

    private fun updateSeekbar(position: Int, duration: Int) {
        binding.seekBar1.progress = position
        binding.seekBar1.max = duration
        binding.startText.text = position.formatMillis()
        binding.endText.text = duration.formatMillis()
    }

    private fun updatePlayButton(state: PlaybackState?) {
        Log.d(
            TAG,
            "Controls: PlayBackState: $state"
        )
        val isConnected = false
        binding.controllers.setVisibility(!isConnected)
        binding.playCircle.setVisibility(!isConnected)
        when (state) {
            PlaybackState.PLAYING -> {
                binding.progressBar1.visibility = View.INVISIBLE
                binding.playPause.visibility = View.VISIBLE
                binding.playPause.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_av_pause_dark, null)
                )
                binding.playCircle.setVisibility(isConnected)
            }
            PlaybackState.IDLE -> {
                binding.playCircle.visibility = View.VISIBLE
                binding.controllers.visibility = View.GONE
                binding.coverArtView.visibility = View.VISIBLE
                binding.videoView1.visibility = View.INVISIBLE
            }
            PlaybackState.PAUSED -> {
                binding.progressBar1.visibility = View.INVISIBLE
                binding.playPause.visibility = View.VISIBLE
                binding.playPause.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_av_play_dark, null)
                )
                binding.playCircle.setVisibility(isConnected)
            }
            PlaybackState.BUFFERING -> {
                binding.playCircle.visibility = View.INVISIBLE
                binding.progressBar1.visibility = View.VISIBLE
            }
            else -> {}
        }
    }

    @SuppressLint("NewApi")
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        supportActionBar!!.show()
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE
            }
            updateMetadata(false)
            binding.container.setBackgroundColor(resources.getColor(R.color.black))
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
            )
            window.clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
            updateMetadata(true)
            binding.container.setBackgroundColor(resources.getColor(R.color.white))
        }
    }

    private fun updateMetadata(visible: Boolean) {
        val displaySize: Point
        if (!visible) {
            binding.textView2.visibility = View.GONE
            binding.textView1.visibility = View.GONE
            binding.textView3.visibility = View.GONE
            displaySize = Utils.getDisplaySize(this)
            val lp = RelativeLayout.LayoutParams(
                displaySize.x,
                displaySize.y + supportActionBar!!.height
            )
            lp.addRule(RelativeLayout.CENTER_IN_PARENT)
            binding.videoView1.layoutParams = lp
            binding.videoView1.invalidate()
        } else {
            binding.textView2.text = mSelectedMedia!!.subTitle
            binding.textView1.text = mSelectedMedia!!.title
            binding.textView3.text = mSelectedMedia!!.studio
            binding.textView2.visibility = View.VISIBLE
            binding.textView1.visibility = View.VISIBLE
            binding.textView3.visibility = View.VISIBLE
            displaySize = Utils.getDisplaySize(this)
            val lp = RelativeLayout.LayoutParams(
                displaySize.x,
                (displaySize.x * mAspectRatio).toInt()
            )
            lp.addRule(RelativeLayout.BELOW, R.id.toolbar)
            binding.videoView1.layoutParams = lp
            binding.videoView1.invalidate()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.browse, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent: Intent
        if (item.itemId == R.id.action_settings) {
            intent = Intent(this@LocalPlayerActivity, CastPreference::class.java)
            startActivity(intent)
        } else if (item.itemId == R.id.home) {
            ActivityCompat.finishAfterTransition(this)
        }
        return true
    }

    private fun setupActionBar() {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        toolbar.title = mSelectedMedia!!.title
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun loadViews() {
        binding.textView2.movementMethod = ScrollingMovementMethod()
        binding.startText.text = 0.formatMillis()
        ViewCompat.setTransitionName(binding.coverArtView, getString(R.string.transition_image))
        binding.playCircle.setOnClickListener { togglePlayback() }
    }

    companion object {
        private const val TAG = "LocalPlayerActivity"
    }
}