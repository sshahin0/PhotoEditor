package com.limsphere.pe

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.limsphere.pe.adapter.MyCreationAdapter
import com.limsphere.pe.model.MyAlbumMediaFile
import com.limsphere.pe.utils.AdManager
import com.limsphere.pe.utils.ImageUtils
import org.apache.commons.io.comparator.LastModifiedFileComparator
import java.io.File

class CreationActivity : AppCompatActivity() {

    private lateinit var files: Array<File>
    private lateinit var creation_list: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var btnBack: ImageView
    private lateinit var emptyStatus: LinearLayout
    private lateinit var myCreationAdapter: MyCreationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creation)

        btnBack = findViewById<ImageView>(R.id.btnBack).apply {
            setOnClickListener { onBackPressed() }
        }
        emptyStatus = findViewById(R.id.emptyStatus)

        val adContainer = findViewById<LinearLayout>(R.id.banner_container)

        if (!AdManager.isloadMAX) {
            // admob
            AdManager.initAd(this)
            AdManager.loadBannerAd(this, adContainer)
            AdManager.loadInterAd(this)
        } else {
            // MAX + Fb Ads
            AdManager.initMAX(this)
            AdManager.maxBanner(this, adContainer)
            AdManager.maxInterstital(this)
        }
    }

    override fun onResume() {
        super.onResume()
        setRecycler()
    }

    private fun setRecycler() {
        myCreationAdapter = MyCreationAdapter(this, getMyCreation())

        creation_list = findViewById<RecyclerView>(R.id.creation_list).apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@CreationActivity, 2)
            itemAnimator = DefaultItemAnimator()
            adapter = myCreationAdapter
        }
        myCreationAdapter.notifyDataSetChanged()
    }

    private fun getMyCreation(): ArrayList<MyAlbumMediaFile> {
        val mediaList = ArrayList<MyAlbumMediaFile>()
        val targetPath = File(ImageUtils.OUTPUT_COLLAGE_FOLDER)

        files = targetPath.listFiles() ?: emptyArray()

        if (files.isNotEmpty()) {
            emptyStatus.visibility = View.GONE
            try {
                files.sortWith(LastModifiedFileComparator.LASTMODIFIED_REVERSE)
                mediaList.addAll(files.map { MyAlbumMediaFile(it) })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            emptyStatus.visibility = View.VISIBLE
        }
        return mediaList
    }
} 