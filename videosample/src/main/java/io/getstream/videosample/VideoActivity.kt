package io.getstream.videosample

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import io.getstream.videosample.pager.PagerAdapter

class VideoActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        configPlayer()
        configPager()
    }

    private fun configPlayer() {
        val playerView = findViewById<PlayerView>(R.id.playerView)

        player = ExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                playerView?.player = exoPlayer
            }

        val mediaItem =
            MediaItem.fromUri(Uri.parse("asset:///speaker.mp4"))

        player?.setMediaItem(mediaItem)

        player?.playWhenReady = true
        player?.prepare()
    }

    private fun configPager() {
        val pager = findViewById<ViewPager2>(R.id.pager)
        pager.adapter = PagerAdapter(supportFragmentManager, lifecycle)
    }
}
