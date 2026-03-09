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
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
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
import com.yandex.mobile.ads.common.AdRequestConfiguration;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.common.MobileAds;
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
    private RewardedAd mRewardedAd = null;
    @Nullable
    private RewardedAdLoader mRewardedAdLoader = null;

    private ProgressDialog progressDialog = null;

    private final double TABLET_SCREEN_SIZE_THRESHOLD = 7.0;

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

        //Data
        defaultColor = ContextCompat.getColor(DrawActivity.this, R.color.black);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        double screenInches = Math.sqrt(Math.pow(screenWidth / displayMetrics.xdpi, 2) +
                Math.pow(screenHeight / displayMetrics.ydpi, 2));

        int bannerHeight = (screenInches >= TABLET_SCREEN_SIZE_THRESHOLD) ?
                (int) (screenHeight * 0.08) : (int) (screenHeight * 0.036);

        // Инициализация диалога загрузки
        progressDialog = new ProgressDialog(DrawActivity.this);

        //Ads
        MobileAds.initialize(this, () -> {
            BannerAdView mBannerAdView = findViewById(R.id.adView);
            mBannerAdView.setAdUnitId(AdsConfig.bannerAdsId); // Убедитесь, что AdsConfig берется правильно
            mBannerAdView.setAdSize(BannerAdSize.inlineSize(this, screenWidth, bannerHeight));

            final AdRequest adRequest = new AdRequest.Builder().build();
            mBannerAdView.setBannerAdEventListener(new BannerAdEventListener() {
                @Override public void onAdLoaded() {}
                @Override public void onAdFailedToLoad(@NonNull AdRequestError error) {}
                @Override public void onAdClicked() {}
                @Override public void onLeftApplication() {}
                @Override public void onReturnedToApplication() {}
                @Override public void onImpression(@Nullable ImpressionData data) {}
            });
            mBannerAdView.loadAd(adRequest);

            //Rewarded Loader
            mRewardedAdLoader = new RewardedAdLoader(DrawActivity.this);
            mRewardedAdLoader.setAdLoadListener(new RewardedAdLoadListener() {
                @Override
                public void onAdLoaded(@NonNull final RewardedAd rewardedAd) {
                    mRewardedAd = rewardedAd;
                    if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();

                    mRewardedAd.setAdEventListener(new RewardedAdEventListener() {
                        @Override public void onAdShown() {}
                        @Override
                        public void onAdFailedToShow(@NonNull AdError adError) {
                            if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
                            saveDrawing();
                        }
                        @Override
                        public void onAdDismissed() {}
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
                    if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
                    saveDrawing(); // Сохраняем без рекламы, если не загрузилась
                }
            });
        });

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // FVBI
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
                // Обновляем имя файла прямо перед сохранением
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                date = simpleDateFormat.format(new Date());

                // На Android 10+ (API 29+) WRITE_EXTERNAL_STORAGE не нужен для записи в Галерею
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startAdAndSaveFlow();
                } else {
                    // Для старых версий запрашиваем разрешение
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

    private void startAdAndSaveFlow() {
        if (mRewardedAdLoader != null) {
            progressDialog.showDialog(); // Показываем загрузку, пока ждем рекламу
            final AdRequestConfiguration adRequestConfiguration =
                    new AdRequestConfiguration.Builder(AdsConfig.rewardedAdsId).build();
            mRewardedAdLoader.loadAd(adRequestConfiguration);
        } else {
            saveDrawing();
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
        // 1. Сохранение во внутреннюю память приложения (как было у вас, не требует разрешений)
        File internalPath = new File(DrawActivity.this.getFilesDir(), "Рисовалка" + File.separator + "images");
        if (!internalPath.exists()) {
            internalPath.mkdirs();
        }
        File outFile = new File(internalPath, fileName + ".jpeg");
        FileOutputStream fos = new FileOutputStream(outFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();

        // 2. Современное сохранение в публичную Галерею (MediaStore)
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
            // Устаревший метод для Android 9 и ниже
            MediaStore.Images.Media.insertImage(getContentResolver(), outFile.getAbsolutePath(), fileName, "");
        }

        // 3. Сохраняем в ваш текстовый список
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