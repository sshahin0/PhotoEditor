package com.limsphere.pe;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.limsphere.pe.Activities.ScrapBookActivity;
import com.limsphere.pe.Activities.ThumbListActivity;
import com.limsphere.pe.shape.BodyShapeEditor;
import com.limsphere.pe.utils.AdManager;
import com.limsphere.pe.utils.CustomGalleryPicker;
import com.limsphere.pe.utils.DateTimeUtils;
import com.limsphere.pe.utils.ImageUtils;
import com.limsphere.pe.utils.Utils;
import com.xinlan.imageeditlibrary.editimage.EditImageActivity;
import com.xinlan.imageeditlibrary.editimage.utils.BitmapUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {
    int perRequest = 1;
    private CustomGalleryPicker customGalleryPicker;
    private int ACTION_REQUEST_EDITIMAGE = 9;
    private int shapeRequest = 101;
    private String fileName;
    private int imageWidth, imageHeight;
    private boolean doubleBackToExitPressedOnce = false;
    private int currentRequest = -1;

    private PopupWindow mPopupWindow;

    String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    ImageView menuBtn;
    LinearLayout clgMakerBtn;
    LinearLayout pipMakerBtn;
    LinearLayout scrapBtn;
    LinearLayout albumBtn;
    LinearLayout photoEditBtn;
    LinearLayout shapeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Initialize gallery picker
        customGalleryPicker = new CustomGalleryPicker(this, new CustomGalleryPicker.GalleryResultCallback() {
            @Override
            public void onGalleryResult(ArrayList<String> filePaths) {
                handleGalleryResult(filePaths);
            }

            @Override
            public void onGalleryCanceled() {
                Toast.makeText(StartActivity.this, "Image selection canceled", Toast.LENGTH_SHORT).show();
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("isloadMAX");

        menuBtn = findViewById(R.id.menuBtn);
        menuBtn.setOnClickListener(this);

        clgMakerBtn = findViewById(R.id.clgMakerBtn);
        clgMakerBtn.setOnClickListener(this);

        photoEditBtn = findViewById(R.id.photoEditBtn);
        photoEditBtn.setOnClickListener(this);

        pipMakerBtn = findViewById(R.id.pipMakerBtn);
        pipMakerBtn.setOnClickListener(this);

        scrapBtn = findViewById(R.id.scrapBtn);
        scrapBtn.setOnClickListener(this);

        albumBtn = findViewById(R.id.albumBtn);
        albumBtn.setOnClickListener(this);

        shapeBtn = findViewById(R.id.shapeBtn);
        shapeBtn.setOnClickListener(this);

        if (!AdManager.isloadMAX) {
            AdManager.initAd(StartActivity.this);
            AdManager.loadInterAd(StartActivity.this);
        } else {
            AdManager.initMAX(StartActivity.this);
            AdManager.maxInterstital(StartActivity.this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menuBtn:
                showMenu();
                break;
            case R.id.clgMakerBtn:
                collageMaker();
                break;
            case R.id.photoEditBtn:
                photoEditor();
                break;
            case R.id.pipMakerBtn:
                pipMaker();
                break;
            case R.id.shapeBtn:
                shapeEditor();
                break;
            case R.id.scrapBtn:
                scrapBook();
                break;
            case R.id.albumBtn:
                gotoCreation();
                break;
            default:
                break;
        }
    }

    void showMenu() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
            return;
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.custom_popup, null);

        mPopupWindow = new PopupWindow(customView, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        if (Build.VERSION.SDK_INT >= 21) {
            mPopupWindow.setElevation(5.0f);
        }

        TextView rateTxt = customView.findViewById(R.id.rateTxt);
        rateTxt.setOnClickListener(v -> {
            mPopupWindow.dismiss();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        });

        TextView shareTxt = customView.findViewById(R.id.shareTxt);
        shareTxt.setOnClickListener(v -> {
            mPopupWindow.dismiss();
            Intent myapp = new Intent(Intent.ACTION_SEND);
            myapp.setType("text/plain");
            myapp.putExtra(Intent.EXTRA_TEXT, "Click here and check out this amazing app\n https://play.google.com/store/apps/details?id=" + getPackageName() + " \n");
            startActivity(myapp);
        });

        TextView moreTxt = customView.findViewById(R.id.moreTxt);
        moreTxt.setOnClickListener(v -> {
            mPopupWindow.dismiss();
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/dev?id=7081479513420377164&hl=en")));
        });

        TextView privacyTxt = customView.findViewById(R.id.privacyTxt);
        privacyTxt.setOnClickListener(v -> {
            mPopupWindow.dismiss();
            startActivity(new Intent(StartActivity.this, PrivacyActivity.class));
        });

        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);
        mPopupWindow.showAsDropDown(menuBtn, 0, 0);
    }

    public boolean hasPermissions(String... permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(StartActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
            }
        }
        return false;
    }

    public void photoEditor() {
        if (hasPermissions(permissions)) {
            ActivityCompat.requestPermissions(StartActivity.this, permissions, perRequest);
        } else {
            currentRequest = ACTION_REQUEST_EDITIMAGE; // <-- Track request
            customGalleryPicker.setLimits(1, 1);
            customGalleryPicker.launchSingle();
        }
    }

    private void handleGalleryResult(ArrayList<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) return;

        if (currentRequest == shapeRequest) {
            // Start Shape Editor
            loadImage(filePaths.get(0)); // Load the image and go to BodyShapeEditor
        } else if (currentRequest == ACTION_REQUEST_EDITIMAGE) {
            // Start Photo Editor
            fileName = DateTimeUtils.getCurrentDateTime().replaceAll(":", "-").concat(".png");
            File collageFolder = new File(ImageUtils.OUTPUT_COLLAGE_FOLDER);
            if (!collageFolder.exists()) {
                collageFolder.mkdirs();
            }
            File outputFile = new File(collageFolder, fileName);
            EditImageActivity.start(this, filePaths.get(0), outputFile.getAbsolutePath(), ACTION_REQUEST_EDITIMAGE);
        }
    }


    public void loadImage(String filepath) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        imageWidth = metrics.widthPixels / 2;
        imageHeight = metrics.heightPixels / 2;

        Dialog dialog = ProgressDialog.show(StartActivity.this, getString(R.string.app_name), "Loading...");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            // Background work
            Bitmap bitmap = BitmapUtils.getSampledBitmap(filepath, imageWidth, imageHeight);

            handler.post(() -> {
                // UI thread
                dialog.dismiss();
                Utils.mBitmap = bitmap;
                startActivity(new Intent(StartActivity.this, BodyShapeEditor.class));
            });
        });
    }

    public void collageMaker() {
        if (hasPermissions(permissions)) {
            ActivityCompat.requestPermissions(StartActivity.this, permissions, perRequest);
        } else {
            Intent intent = new Intent(StartActivity.this, ThumbListActivity.class);
            intent.putExtra(ThumbListActivity.EXTRA_IS_FRAME_IMAGE, true);
            startActivity(intent);
        }
    }

    public void pipMaker() {
        if (hasPermissions(permissions)) {
            ActivityCompat.requestPermissions(StartActivity.this, permissions, perRequest);
        } else {
            Intent intent = new Intent(StartActivity.this, ThumbListActivity.class);
            intent.putExtra(ScrapBookActivity.EXTRA_CREATED_METHOD_TYPE, ScrapBookActivity.FRAME_TYPE);
            startActivity(intent);
        }
    }

    public void shapeEditor() {
        if (hasPermissions(permissions)) {
            ActivityCompat.requestPermissions(StartActivity.this, permissions, perRequest);
        } else {
            currentRequest = shapeRequest; // <-- Track request
            customGalleryPicker.setLimits(1, 1);
            customGalleryPicker.launchSingle();
        }
    }


    public void scrapBook() {
        if (hasPermissions(permissions)) {
            ActivityCompat.requestPermissions(StartActivity.this, permissions, perRequest);
        } else {
            Intent intent = new Intent(StartActivity.this, ScrapBookActivity.class);
            intent.putExtra(ScrapBookActivity.EXTRA_CREATED_METHOD_TYPE, ScrapBookActivity.PHOTO_TYPE);
            startActivity(intent);
        }
    }

    public void gotoCreation() {
        if (hasPermissions(permissions)) {
            ActivityCompat.requestPermissions(StartActivity.this, permissions, perRequest);
        } else {
            Intent intent = new Intent(StartActivity.this, CreationActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        if (this.doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000L);
    }
}
