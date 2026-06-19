package com.plumsoftware.risovalka.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.plumsoftware.risovalka.ads.AdsConfig;
import com.plumsoftware.risovalka.dialog.ProgressDialog;
import com.plumsoftware.risovalka.R;
import com.yandex.mobile.ads.banner.BannerAdEventListener;
import com.yandex.mobile.ads.banner.BannerAdSize;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdError;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.rewarded.Reward;
import com.yandex.mobile.ads.rewarded.RewardedAd;
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener;
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener;
import com.yandex.mobile.ads.rewarded.RewardedAdLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import yuku.ambilwarna.AmbilWarnaDialog;

public class DrawActivity extends AppCompatActivity {
    private int defaultColor;
    private final File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Рисовалка");
    private SignaturePad signatureView;
    private String date;
    private LinearLayout linearLayout;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Nullable
    private BannerAdView mBannerAdView;
    @Nullable
    private RewardedAd mRewardedAd = null;
    @Nullable
    private RewardedAdLoader mRewardedAdLoader = null;

    private ProgressDialog progressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        linearLayout = findViewById(R.id.linearLayout);

        ViewCompat.setOnApplyWindowInsetsListener(linearLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });

        defaultColor = ContextCompat.getColor(DrawActivity.this, R.color.black);

        progressDialog = new ProgressDialog(DrawActivity.this);

        mRewardedAdLoader = new RewardedAdLoader(this);
        setupStickyBanner();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        ImageButton rubber = findViewById(R.id.imageButton);
        ImageButton paintPicker = findViewById(R.id.imageButton2);
        ImageButton save = findViewById(R.id.save);
        ImageButton back = findViewById(R.id.back);
        final TextView size = findViewById(R.id.textView);
        SeekBar paintSize = findViewById(R.id.seekBar);
        signatureView = findViewById(R.id.signatureView);
        signatureView.setSaveEnabled(false);

        paintPicker.setOnClickListener(view -> openColorPicker(signatureView));
        back.setOnClickListener(v -> finish());
        rubber.setOnClickListener(view -> signatureView.clear());

        paintSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                size.setText(Integer.toString(progress));
                signatureView.setMaxWidth(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        save.setOnClickListener(view -> {
            if (!signatureView.isEmpty()) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                date = simpleDateFormat.format(new Date());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startAdAndSaveFlow();
                } else {
                    if (ContextCompat.checkSelfPermission(DrawActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        startAdAndSaveFlow();
                    } else {
                        askPermissions();
                    }
                }
            } else {
                Toast.makeText(DrawActivity.this, "Невозможно сохранить пустой холст.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupStickyBanner() {
        final BannerAdView bannerAdView = findViewById(R.id.adView);
        bannerAdView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                bannerAdView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                loadStickyBanner(bannerAdView);
            }
        });
    }

    private void loadStickyBanner(@NonNull final BannerAdView bannerAdView) {
        mBannerAdView = bannerAdView;
        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int adWidthPixels = bannerAdView.getWidth();
        if (adWidthPixels == 0) {
            adWidthPixels = displayMetrics.widthPixels;
        }
        final int adWidth = Math.round(adWidthPixels / displayMetrics.density);

        bannerAdView.setAdSize(BannerAdSize.sticky(this, adWidth));
        bannerAdView.setBannerAdEventListener(new BannerAdEventListener() {
            @Override
            public void onAdLoaded() {
                if (isDestroyed() && mBannerAdView != null) {
                    mBannerAdView.destroy();
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull AdRequestError error) {}

            @Override
            public void onAdClicked() {}

            @Override
            public void onImpression(@Nullable ImpressionData data) {}
        });
        bannerAdView.loadAd(new AdRequest.Builder(AdsConfig.bannerAdsId).build());
    }

    private void startAdAndSaveFlow() {
        if (mRewardedAdLoader != null) {
            progressDialog.showDialog();
            final AdRequest adRequest = new AdRequest.Builder(AdsConfig.rewardedAdsId).build();
            mRewardedAdLoader.loadAd(adRequest, new RewardedAdLoadListener() {
                @Override
                public void onAdLoaded(@NonNull final RewardedAd rewardedAd) {
                    mRewardedAd = rewardedAd;
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    mRewardedAd.setAdEventListener(new RewardedAdEventListener() {
                        @Override public void onAdShown() {}

                        @Override
                        public void onAdFailedToShow(@NonNull AdError adError) {
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            destroyRewardedAd();
                            saveDrawing();
                        }

                        @Override
                        public void onAdDismissed() {
                            destroyRewardedAd();
                        }

                        @Override public void onAdClicked() {}

                        @Override public void onAdImpression(@Nullable ImpressionData impressionData) {}

                        @Override
                        public void onRewarded(@NonNull Reward reward) {
                            saveDrawing();
                        }
                    });

                    mRewardedAd.show(DrawActivity.this);
                }

                @Override
                public void onAdFailedToLoad(@NonNull final AdRequestError adRequestError) {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    saveDrawing();
                }
            });
        } else {
            saveDrawing();
        }
    }

    @Override
    protected void onDestroy() {
        if (mBannerAdView != null) {
            mBannerAdView.destroy();
            mBannerAdView = null;
        }
        mRewardedAdLoader = null;
        destroyRewardedAd();
        super.onDestroy();
    }

    private void destroyRewardedAd() {
        if (mRewardedAd != null) {
            mRewardedAd.setAdEventListener(null);
            mRewardedAd = null;
        }
    }

    private void askPermissions() {
        Dexter.withContext(DrawActivity.this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        startAdAndSaveFlow();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(DrawActivity.this, "Требуется разрешение для сохранения", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest request, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void openColorPicker(SignaturePad signatureView) {
        AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(DrawActivity.this,
                defaultColor,
                new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override public void onCancel(AmbilWarnaDialog dialog) {}
                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        defaultColor = color;
                        signatureView.setPenColor(color);
                    }
                });
        ambilWarnaDialog.show();
    }

    private void saveImage(Bitmap bitmap, String fileName) throws IOException {
        File internalPath = new File(DrawActivity.this.getFilesDir(), "Рисовалка" + File.separator + "images");
        if (!internalPath.exists()) {
            internalPath.mkdirs();
        }
        File outFile = new File(internalPath, fileName + ".jpeg");
        FileOutputStream fos = new FileOutputStream(outFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName + ".jpeg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Рисовалка");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);

            ContentResolver resolver = getContentResolver();
            Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            Uri itemUri = resolver.insert(collection, values);

            if (itemUri != null) {
                try (OutputStream out = resolver.openOutputStream(itemUri)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                }
                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                resolver.update(itemUri, values, null, null);
            }
        } else {
            MediaStore.Images.Media.insertImage(getContentResolver(), outFile.getAbsolutePath(), fileName, "");
        }

        addToList(outFile);
    }

    private void addToList(File filePath) throws IOException {
        String string = filePath.getPath() + "\n";
        FileOutputStream fileOutputStream = openFileOutput("images", MODE_APPEND);
        fileOutputStream.write(string.getBytes(StandardCharsets.UTF_8));
        fileOutputStream.flush();
        fileOutputStream.close();
    }

    private void saveDrawing() {
        try {
            saveImage(signatureView.getSignatureBitmap(), date);
            Snackbar.make(linearLayout, "Сохранено!", Snackbar.LENGTH_SHORT)
                    .setTextColor(Color.WHITE)
                    .setBackgroundTint(Color.parseColor("#95D61D"))
                    .show();
        } catch (IOException e) {
            e.printStackTrace();
            Snackbar.make(linearLayout, "Ошибка сохранения: " + e.getMessage(), Snackbar.LENGTH_SHORT)
                    .setTextColor(Color.WHITE)
                    .setBackgroundTint(Color.RED)
                    .show();
        }
    }
}
