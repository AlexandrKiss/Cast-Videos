package com.google.sample.cast.refplayer

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.sample.cast.refplayer.databinding.VideoBrowserBinding
import com.google.sample.cast.refplayer.settings.CastPreference

class VideoBrowserActivity: AppCompatActivity() {

    private val mIsHoneyCombOrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
    private val binding by lazy { VideoBrowserBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupActionBar()
    }

    private fun setupActionBar() {
        val toolbar: Toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        toolbar.setTitle(R.string.app_name)
        setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.browse, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = when (item.itemId) {
            R.id.action_settings ->
                Intent(this@VideoBrowserActivity, CastPreference::class.java)
            else -> Intent()
        }
        startActivity(intent)
        return true
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy is called")
        super.onDestroy()
    }

    companion object {
        const val TAG = "VideoBrowserActivity"
    }
}