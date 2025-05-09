package com.limsphere.pe

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.limsphere.pe.utils.AdManager
import com.limsphere.pe.utils.Utils
import java.io.File

class ShareActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var myImg: ImageView
    private lateinit var btnShare: ImageView
    private lateinit var btnDelete: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var file: File
    private lateinit var path: String
    private var isCreation: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        myImg = findViewById(R.id.myImg)
        isCreation = intent.extras?.getBoolean("isCreation") ?: false
        path = intent.extras?.getString("path") ?: ""
        file = File(path)

        Glide.with(this).load(Uri.fromFile(file)).into(myImg)

        btnBack = findViewById<ImageView>(R.id.btnBack).apply {
            setOnClickListener(this@ShareActivity)
        }
        btnShare = findViewById<ImageView>(R.id.btnShare).apply {
            setOnClickListener(this@ShareActivity)
        }
        btnDelete = findViewById<ImageView>(R.id.btnDelete).apply {
            setOnClickListener(this@ShareActivity)
        }

        val adContainer = findViewById<LinearLayout>(R.id.banner_container)

        // Commented out as in original
        /*
        if (!AdManager.isloadMAX) {
            AdManager.initAd(this)
            AdManager.adptiveBannerAd(this, adContainer)
            AdManager.loadInterAd(this)
        } else {
            AdManager.initMAX(this)
            AdManager.maxBannerAdaptive(this, adContainer)
            AdManager.maxInterstital(this)
        }
        */
    }

    override fun onBackPressed() {
        if (isCreation) {
            super.onBackPressed()
        } else {
            Utils.gotoMain(this)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnBack -> onBackPressed()
            R.id.btnShare -> {
                if (!AdManager.isloadMAX) {
                    AdManager.adCounter++
                    AdManager.showInterAd(this, null, 0)
                } else {
                    AdManager.adCounter++
                    AdManager.showMaxInterstitial(this, null, 0)
                }
                Utils.mShare(path, this)
            }
            R.id.btnDelete -> {
                if (!AdManager.isloadMAX) {
                    AdManager.adCounter++
                    AdManager.showInterAd(this, null, 0)
                } else {
                    AdManager.adCounter++
                    AdManager.showMaxInterstitial(this, null, 0)
                }
                mDelete(file)
            }
        }
    }

    private fun mDelete(mFile: File) {
        AlertDialog.Builder(this).apply {
            setTitle("Delete")
            setMessage("Are you Sure, You want to Delete this Image?")
            setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                if (mFile.exists()) {
                    mFile.delete()
                    onBackPressed()
                }
            }
            setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            show()
        }
    }
} 