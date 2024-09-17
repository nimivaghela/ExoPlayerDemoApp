package com.example.exoplayerdemoapp

import android.Manifest
import android.content.Intent
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

    private lateinit var binding: ActivityMainBinding
    private lateinit var demoIntValue: String

    private val storage = Firebase.storage
    private val listRef = storage.reference.child("files/")
    private lateinit var adapter: ArrayAdapter<String>
    private var videoListStorage: ArrayList<StorageReference> = arrayListOf()
    private var videoList: List<String> = listOf()
    private var mSelectedItem: Int = -1
    private lateinit var fileName: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        if (this::demoIntValue.isInitialized) {
            Log.d("TAG", "onCreate: $demoIntValue")
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, view, position, _ ->
                val selectedPdf = videoListStorage[position].path

                navigateToNewScreen()
                storage.reference.child(selectedPdf).downloadUrl.addOnSuccessListener {
                    Log.d("TAG", "fetchListFromFirebaseStorage: $it")
//                    playVideoFromUri(it)
                }.addOnFailureListener {
                    Log.d("TAG", "fetchListFromFirebaseStorage: $it")
                }
                Log.d("TAG", "onCreate: ${videoListStorage[position].path}")
                if (mSelectedItem == position) {
                    view.setBackgroundColor(Color.GRAY)
                } else {
                    view.setBackgroundColor(Color.WHITE)
                }

                adapter.notifyDataSetChanged()
            }

        checkUserPermission()

    }

    private fun navigateToNewScreen() {
        var intent = Intent(this@MainActivity, VideoPlayActivity::class.java)
        startActivity(intent)
//        intent.putExtras()
    }

    private fun setAdapter() {
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, videoList)
        binding.listView.adapter = adapter

        adapter.notifyDataSetChanged()
    }

    private fun checkUserPermission() {
        when {
            (ContextCompat.checkSelfPermission(
                this, Manifest.permission.INTERNET
            ) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_NETWORK_STATE
            ) == PackageManager.PERMISSION_GRANTED) -> {
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
            } else {
                Log.d("TAG", "fetchListFromFirebaseStorage:${it.items.size} ")
            }
        }.addOnFailureListener {
            Log.d("TAG", "fetchListFromFirebaseStorage:$it ")
        }

    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchListFromFirebaseStorage()
        } else {
            checkUserPermission()
        }
    }

}