package com.example.exoplayerdemoapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.exoplayerdemoapp.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import java.io.File
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var player: ExoPlayer
    private lateinit var binding: ActivityMainBinding
    private lateinit var demoIntValue: String

    private val storage = Firebase.storage
    private val listRef = storage.reference.child("files/")
    private lateinit var adapter: ArrayAdapter<String>
    private var videoListStorage: ArrayList<StorageReference> = arrayListOf()
    private var videoList: List<String> = listOf()
    private var mSelectedItem: Int = -1
    private lateinit var fileName: String
    private lateinit var fileUri: Uri
    private var tempFile: File? = null


    var videoURL: Uri? = null

    //        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    var firstVideoUri = ""

    //        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
    var secondVideoUri = ""
//        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"


//        "https://videocdn.bodybuilding.com/video/mp4/62000/62792m.mp4"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        if (this::demoIntValue.isInitialized) {
            Log.d("TAG", "onCreate: $demoIntValue")
        }

        player = ExoPlayer.Builder(this).build()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, view, position, _ ->
                val selectedPdf = videoListStorage[position].path
                mSelectedItem = position
                fileName = selectedPdf

//                val gsReference = storage.getReferenceFromUrl(videoListStorage[position].path)

                storage.reference.child(selectedPdf).downloadUrl.addOnSuccessListener {
                    Log.d("TAG", "fetchListFromFirebaseStorage: $it")
                    playVideoFromUri(it)
                    // Got the download URL for 'users/me/profile.png'
                }.addOnFailureListener {
                    Log.d("TAG", "fetchListFromFirebaseStorage: $it")
                    // Handle any errors
                }
                Log.d("TAG", "onCreate: ${videoListStorage[position].path}")
                if (mSelectedItem == position) {
                    view.setBackgroundColor(Color.GRAY)
                } else {
                    view.setBackgroundColor(Color.WHITE)
                }

                adapter.notifyDataSetChanged()
            }

        binding.playerView.player = player
        checkUserPermission()


    }

    private fun setAdapter() {
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, videoList)
        binding.listView.adapter = adapter

//        val selectedPdf = videoList[0]
//        mSelectedItem = 0
//        fileName = selectedPdf
//        fileUri = Uri.fromFile(File("assets/$fileName"))

//        tempFile = copyFileFromAssetsToCache(fileName)

//        Log.d("TAG", "File name: $fileName $fileUri")

        adapter.notifyDataSetChanged()
    }

    private fun checkUserPermission() {
        when {
            (ContextCompat.checkSelfPermission(
                this, Manifest.permission.INTERNET
            ) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_NETWORK_STATE
            ) == PackageManager.PERMISSION_GRANTED) -> {
//                playVideoFromUri()

//                playMultipleVideoFromUri()
                fetchListFromFirebaseStorage()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.INTERNET)
            }
        }

    }

    private fun fetchListFromFirebaseStorage() {
        listRef.listAll().addOnSuccessListener {
            if (it.items.size > 0) {
                videoListStorage.addAll(it.items)
                videoList = videoListStorage.map { it.name }
                setAdapter()
//                    val localFile = File(it.items[0].path)
//                    listRef.getFile(localFile).addOnSuccessListener {
//                            Log.d("TAG", "Get File Success: $it")
//                        }.addOnFailureListener {
//                            Log.d("TAG", "Get File Fail: $it")
//                        }
            } else {
                Log.d("TAG", "fetchListFromFirebaseStorage:${it.items.size} ")
            }
        }.addOnFailureListener {
            // Uh-oh, an error occurred!
        }

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

    private fun playVideoFromUri(videoURL: Uri?) {
        // Build the media item.
        val mediaItem = MediaItem.fromUri(videoURL!!)
        // Set the media item to be played.
        player.setMediaItem(mediaItem)
        // Prepare the player.
        player.prepare()
        // Start the playback.
        player.play()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchListFromFirebaseStorage()
//            playMultipleVideoFromUri()
//            playVideoFromUri()
        } else {
            checkUserPermission()
        }
    }

}