package com.example.exoplayerdemoapp

import android.app.PictureInPictureParams
import android.app.PictureInPictureUiState
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.exoplayerdemoapp.databinding.ActivityMainBinding
import com.example.exoplayerdemoapp.databinding.ActivityVideoPlayBinding

class VideoPlayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoPlayBinding
    private lateinit var player: ExoPlayer
    var videoURL =
        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
    var firstVideoUri = ""
    var secondVideoUri = ""

    //
    //        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
    //        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    //        "https://videocdn.bodybuilding.com/video/mp4/62000/62792m.mp4"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVideoPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializePlayer()


        binding.imgPIP.setOnClickListener {
            enterPipMode()
        }
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player

        playVideoFromUri()
    }


    private fun playMultipleVideoFromUri() {
//        firstVideoUri = videoListStorage.map { it.storage }
        val firstItem = MediaItem.fromUri(firstVideoUri)
        val secondItem = MediaItem.fromUri(secondVideoUri)
        // Add the media items to be played.
        player.addMediaItem(firstItem)
        player.addMediaItem(secondItem)
        // Prepare the player.
        player.prepare()
        // Start the playback.
        player.play()
    }

    private fun playVideoFromUri(/*videoURL: Uri?*/) {
        // Build the media item.
        val mediaItem = MediaItem.fromUri(videoURL)
        // Set the media item to be played.
        player.setMediaItem(mediaItem)
        // Prepare the player.
        player.prepare()
        // Start the playback.
        player.play()
    }

    override fun onPictureInPictureUiStateChanged(pipState: PictureInPictureUiState) {
        super.onPictureInPictureUiStateChanged(pipState)
        if (isInPictureInPictureMode) {
            // Hide UI controls for PiP mode
        } else {
            // Restore UI when back to full-screen
        }
    }

    fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val aspectRatio = Rational(16, 9) // You can adjust the aspect ratio
            val pipBuilder = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
            enterPictureInPictureMode(pipBuilder.build())
        }
    }

}