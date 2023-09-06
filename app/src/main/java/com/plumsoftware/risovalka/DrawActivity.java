package com.plumsoftware.risovalka;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.kyanogen.signatureview.SignatureView;
import com.yandex.mobile.ads.banner.BannerAdEventListener;
import com.yandex.mobile.ads.banner.BannerAdSize;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdError;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.AdRequestConfiguration;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.common.InitializationListener;
import com.yandex.mobile.ads.common.MobileAds;
import com.yandex.mobile.ads.interstitial.InterstitialAd;
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener;
import com.yandex.mobile.ads.rewarded.Reward;
import com.yandex.mobile.ads.rewarded.RewardedAd;
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener;
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener;
import com.yandex.mobile.ads.rewarded.RewardedAdLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import yuku.ambilwarna.AmbilWarnaDialog;

public class DrawActivity extends AppCompatActivity {
    private int defaultColor;
    private final File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Рисовалка");
    private SignatureView signatureView;
    private String date;
    private int amount;
    private LinearLayout linearLayout;
    //    private RewardedAd mRewardedAd;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Nullable
    private RewardedAd mRewardedAd = null;
    @Nullable
    private RewardedAdLoader mRewardedAdLoader = null;

    private ProgressDialog progressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        //Data
        defaultColor = ContextCompat.getColor(DrawActivity.this, R.color.black);

        //Ads
        MobileAds.initialize(this, () -> {

        });

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Создание экземпляра mBannerAdView.
        BannerAdView mBannerAdView = (BannerAdView) findViewById(R.id.adView);
        mBannerAdView.setAdUnitId("R-M-2522647-1");
        mBannerAdView.setAdSize(BannerAdSize.inlineSize(this, 300, 50));


        progressDialog = new ProgressDialog(DrawActivity.this);

        //progressDialog.showDialog();
        // Создание объекта таргетирования рекламы.
        final AdRequest adRequest = new AdRequest.Builder().build();

        // Регистрация слушателя для отслеживания событий, происходящих в баннерной рекламе.
        mBannerAdView.setBannerAdEventListener(new BannerAdEventListener() {
            @Override
            public void onAdLoaded() {
                //progressDialog.dismiss();
            }

            @Override
            public void onAdFailedToLoad(@NonNull AdRequestError adRequestError) {
                //progressDialog.dismiss();
            }

            @Override
            public void onAdClicked() {

            }

            @Override
            public void onLeftApplication() {
                //progressDialog.dismiss();
            }

            @Override
            public void onReturnedToApplication() {

            }

            @Override
            public void onImpression(@Nullable ImpressionData impressionData) {

            }
        });

        // Загрузка объявления.
        mBannerAdView.loadAd(adRequest);

        //Rewarded
        mRewardedAdLoader = new RewardedAdLoader(DrawActivity.this);

        mRewardedAdLoader.setAdLoadListener(new RewardedAdLoadListener() {
            @Override
            public void onAdLoaded(@NonNull final RewardedAd rewardedAd) {
                mRewardedAd = rewardedAd;
                progressDialog.dismiss();

                mRewardedAd.setAdEventListener(new RewardedAdEventListener() {
                    @Override
                    public void onAdShown() {

                    }

                    @Override
                    public void onAdFailedToShow(@NonNull AdError adError) {

                    }

                    @Override
                    public void onAdDismissed() {

                    }

                    @Override
                    public void onAdClicked() {

                    }

                    @Override
                    public void onAdImpression(@Nullable ImpressionData impressionData) {

                    }

                    @Override
                    public void onRewarded(@NonNull Reward reward) {
//                            if (!signatureView.isBitmapEmpty()) {
                        try {
                            saveImage(signatureView.getSignatureBitmap(), date);
                            Snackbar.make(linearLayout, "Сохранено!", Snackbar.LENGTH_SHORT).setTextColor(Color.WHITE).setBackgroundTint(Color.parseColor("#95D61D")).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Snackbar.make(linearLayout, e.toString(), Snackbar.LENGTH_SHORT).setTextColor(Color.WHITE).setBackgroundTint(getColor(R.color.warning)).show();
                        }
//                            }
                    }
                });

                mRewardedAd.show(DrawActivity.this);
            }

            @Override
            public void onAdFailedToLoad(@NonNull final AdRequestError adRequestError) {
                progressDialog.dismiss();
                Toast.makeText(DrawActivity.this, "Не удалось загрузить реакламу:(\nПопробуйте позже", Toast.LENGTH_SHORT).show();
            }
        });

//        mRewardedAd = new RewardedAd(this);
//        mRewardedAd.setAdUnitId("R-M-2522647-3");

        // Создание объекта таргетирования рекламы.
//        final AdRequest adRequestR = new AdRequest.Builder().build();

        //File name
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        date = simpleDateFormat.format(new Date());

        //File
        String fileName = path + "/" + date + ".png";

        //FVBI
        ImageButton rubber = (ImageButton) findViewById(R.id.imageButton);
        ImageButton paintPicker = (ImageButton) findViewById(R.id.imageButton2);
        ImageButton save = (ImageButton) findViewById(R.id.save);
        ImageButton back = (ImageButton) findViewById(R.id.back);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        ImageView paint = (ImageView) findViewById(R.id.imageView);
        final TextView size = (TextView) findViewById(R.id.textView);
        SeekBar paintSize = (SeekBar) findViewById(R.id.seekBar);
        signatureView = (SignatureView) findViewById(R.id.signatureView);

        //Clickers
        paintPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openColorPicker(signatureView);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        paintSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                size.setText(Integer.toString(progress));
                signatureView.setPenSize(progress);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        rubber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signatureView.clearCanvas();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askPermissions();
                progressDialog.showDialog();
            }
        });
    }

    private void askPermissions() {
        Dexter.withContext(DrawActivity.this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        //Ads
//                        if (!signatureView.isBitmapEmpty()) {
//                            try {
//                                saveImage(signatureView.getSignatureBitmap(), date);
//                                Snackbar.make(linearLayout, "Сохранено!", Snackbar.LENGTH_SHORT).setTextColor(Color.WHITE).setBackgroundTint(Color.parseColor("#089303")).show();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                                Snackbar.make(linearLayout, e.toString(), Snackbar.LENGTH_SHORT).setTextColor(Color.WHITE).setBackgroundTint(Color.parseColor("#9F0505")).show();
//                            }
//                        }

                        if (mRewardedAdLoader != null) {
                            final AdRequestConfiguration adRequestConfiguration =
                                    new AdRequestConfiguration.Builder("R-M-2522647-3").build();
                            mRewardedAdLoader.loadAd(adRequestConfiguration);
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();
    }

    private void openColorPicker(SignatureView signatureView) {
        AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(DrawActivity.this,
                ContextCompat.getColor(DrawActivity.this, R.color.white),
                new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {

                    }

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        defaultColor = color;
                        signatureView.setPenColor(color);
                    }
                });
        ambilWarnaDialog.show();
    }

    private void saveImage(Bitmap bitmap, String fileName) throws IOException {
        File path = new File(DrawActivity.this.getFilesDir(), "Рисовалка" + File.separator + "images");
        if (!path.exists()) {
            path.mkdirs();
        }
        File outFile = new File(path, fileName + ".jpeg");
        FileOutputStream fos = new FileOutputStream(outFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();
        MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, fileName, "");

        addToList(outFile);
    }

    private void addToList(File filePath) throws IOException {
        String string = filePath.getPath();
        string = string + "\n";
        FileOutputStream fileOutputStream = openFileOutput("images", MODE_APPEND);
        fileOutputStream.write(string.getBytes(StandardCharsets.UTF_8));
        fileOutputStream.flush();
        fileOutputStream.close();
    }

}