package com.limsphere.pe

import android.Manifest
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.limsphere.pe.Activities.ScrapBookActivity
import com.limsphere.pe.Activities.ThumbListActivity
import com.limsphere.pe.shape.BodyShapeEditor
import com.limsphere.pe.utils.AdManager
import com.limsphere.pe.utils.CustomGalleryPicker
import com.limsphere.pe.utils.DateTimeUtils
import com.limsphere.pe.utils.ImageUtils
import com.xinlan.imageeditlibrary.editimage.EditImageActivity
import java.io.File
import java.util.concurrent.Executors

class StartActivity : AppCompatActivity(), View.OnClickListener {
    private val perRequest = 1
    private lateinit var customGalleryPicker: CustomGalleryPicker
    private val ACTION_REQUEST_EDITIMAGE = 9
    private val shapeRequest = 101
    private var fileName: String? = null
    private var imageWidth = 0
    private var imageHeight = 0
    private var doubleBackToExitPressedOnce = false
    private var currentRequest = -1
    private var mPopupWindow: PopupWindow? = null

    private val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private lateinit var menuBtn: ImageView
    private lateinit var clgMakerBtn: LinearLayout
    private lateinit var pipMakerBtn: LinearLayout
    private lateinit var scrapBtn: LinearLayout
    private lateinit var albumBtn: LinearLayout
    private lateinit var photoEditBtn: LinearLayout
    private lateinit var shapeBtn: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        // Initialize gallery picker
        customGalleryPicker = CustomGalleryPicker(this, object : CustomGalleryPicker.GalleryResultCallback {
            override fun onGalleryResult(filePaths: ArrayList<String>) {
                handleGalleryResult(filePaths)
            }

            override fun onGalleryCanceled() {
                Toast.makeText(this@StartActivity, "Image selection canceled", Toast.LENGTH_SHORT).show()
            }
        })

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("isloadMAX")

        menuBtn = findViewById(R.id.menuBtn)
        menuBtn.setOnClickListener(this)

        clgMakerBtn = findViewById(R.id.clgMakerBtn)
        clgMakerBtn.setOnClickListener(this)

        photoEditBtn = findViewById(R.id.photoEditBtn)
        photoEditBtn.setOnClickListener(this)

        pipMakerBtn = findViewById(R.id.pipMakerBtn)
        pipMakerBtn.setOnClickListener(this)

        scrapBtn = findViewById(R.id.scrapBtn)
        scrapBtn.setOnClickListener(this)

        albumBtn = findViewById(R.id.albumBtn)
        albumBtn.setOnClickListener(this)

        shapeBtn = findViewById(R.id.shapeBtn)
        shapeBtn.setOnClickListener(this)

        if (!AdManager.isloadMAX) {
            AdManager.initAd(this)
            AdManager.loadInterAd(this)
        } else {
            AdManager.initMAX(this)
            AdManager.maxInterstital(this)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.menuBtn -> showMenu()
            R.id.clgMakerBtn -> collageMaker()
            R.id.photoEditBtn -> photoEditor()
            R.id.pipMakerBtn -> pipMaker()
            R.id.shapeBtn -> shapeEditor()
            R.id.scrapBtn -> scrapBook()
            R.id.albumBtn -> gotoCreation()
        }
    }

    private fun showMenu() {
        if (mPopupWindow?.isShowing == true) {
            mPopupWindow?.dismiss()
            return
        }

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customView = inflater.inflate(R.layout.custom_popup, null)

        mPopupWindow = PopupWindow(
            customView,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            if (Build.VERSION.SDK_INT >= 21) {
                elevation = 5.0f
            }
            isOutsideTouchable = true
            isFocusable = true
        }

        customView.findViewById<TextView>(R.id.rateTxt).setOnClickListener {
            mPopupWindow?.dismiss()
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
            }
        }

        customView.findViewById<TextView>(R.id.shareTxt).setOnClickListener {
            mPopupWindow?.dismiss()
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Click here and check out this amazing app\nhttps://play.google.com/store/apps/details?id=$packageName\n")
                startActivity(this)
            }
        }

        customView.findViewById<TextView>(R.id.moreTxt).setOnClickListener {
            mPopupWindow?.dismiss()
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/dev?id=7081479513420377164&hl=en")))
        }

        customView.findViewById<TextView>(R.id.privacyTxt).setOnClickListener {
            mPopupWindow?.dismiss()
            startActivity(Intent(this, PrivacyActivity::class.java))
        }

        mPopupWindow?.showAsDropDown(menuBtn, 0, 0)
    }

    private fun hasPermissions(vararg permissions: String): Boolean {
        return permissions.any { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }
    }

    private fun photoEditor() {
        if (hasPermissions(*permissions)) {
            ActivityCompat.requestPermissions(this, permissions, perRequest)
        } else {
            currentRequest = ACTION_REQUEST_EDITIMAGE
            customGalleryPicker.setLimits(1, 1)
            customGalleryPicker.launchSingle()
        }
    }

    private fun handleGalleryResult(filePaths: ArrayList<String>) {
        if (filePaths.isEmpty()) return

        when (currentRequest) {
            shapeRequest -> loadImage(filePaths[0])
            ACTION_REQUEST_EDITIMAGE -> {
                fileName = DateTimeUtils.getCurrentDateTime().replace(":", "-").plus(".png")
                val collageFolder = File(ImageUtils.OUTPUT_COLLAGE_FOLDER).apply {
                    if (!exists()) {
                        mkdirs()
                    }
                }
                val outputFile = File(collageFolder, fileName!!)
                EditImageActivity.start(this, filePaths[0], outputFile.absolutePath, ACTION_REQUEST_EDITIMAGE)
            }
        }
    }

    private fun showLoadingDialog(message: String): Dialog {
        return AlertDialog.Builder(this)
            .setView(layoutInflater.inflate(R.layout.dialog_loading, null).apply {
                findViewById<TextView>(R.id.message).text = message
            })
            .setCancelable(false)
            .create()
            .apply {
                window?.setBackgroundDrawableResource(android.R.color.transparent)
            }
    }

    private fun loadImage(imagePath: String) {
        val dialog = showLoadingDialog(getString(R.string.loading))
        dialog.show()

        Executors.newSingleThreadExecutor().execute {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imagePath, options)
            imageWidth = options.outWidth
            imageHeight = options.outHeight

            Handler(Looper.getMainLooper()).post {
                dialog.dismiss()
                val intent = Intent(this@StartActivity, BodyShapeEditor::class.java)
                intent.putExtra("imagePath", imagePath)
                startActivity(intent)
            }
        }
    }

    fun collageMaker() {
        if (hasPermissions(*permissions)) {
            ActivityCompat.requestPermissions(this, permissions, perRequest)
        } else {
            val intent = Intent(this, ThumbListActivity::class.java)
            intent.putExtra("is_frame_images", true)  // This indicates photo collage
            startActivity(intent)
        }
    }

    fun pipMaker() {
        if (hasPermissions(*permissions)) {
            ActivityCompat.requestPermissions(this, permissions, perRequest)
        } else {
            val intent = Intent(this, ThumbListActivity::class.java)
            intent.putExtra("is_frame_images", false)  // This indicates pip collage
            startActivity(intent)
        }
    }

    fun shapeEditor() {
        if (hasPermissions(*permissions)) {
            ActivityCompat.requestPermissions(this, permissions, perRequest)
        } else {
            currentRequest = shapeRequest
            customGalleryPicker.setLimits(1, 1)
            customGalleryPicker.launchSingle()
        }
    }

    fun scrapBook() {
        if (hasPermissions(*permissions)) {
            ActivityCompat.requestPermissions(this, permissions, perRequest)
        } else {
            startActivity(Intent(this, ScrapBookActivity::class.java))
        }
    }

    fun gotoCreation() {
        startActivity(Intent(this, CreationActivity::class.java))
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }
} 