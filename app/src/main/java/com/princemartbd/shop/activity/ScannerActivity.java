package com.princemartbd.shop.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.princemartbd.shop.R;
import com.princemartbd.shop.helper.Constant;
import com.google.zxing.Result;

import java.util.Objects;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    private ZXingScannerView mScannerView;

    private final String[] permission = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.CAMERA"
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        CardView cardViewHamburger = findViewById(R.id.cardViewHamburger);
        ImageView imageMenu = findViewById(R.id.imageMenu);
        ImageView imageHome = findViewById(R.id.imageHome);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        toolbarTitle.setText("Scanner");

        imageHome.setVisibility(View.GONE);
        imageMenu.setVisibility(View.VISIBLE);
        imageMenu.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_arrow_back));
        cardViewHamburger.setOnClickListener(view -> onBackPressed());

        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        requestPermissions(permission, 80);
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v("TAG", rawResult.getText()); // Prints scan results
        Log.v("TAG", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

        if (!rawResult.getText().equals("")){
            mScannerView.stopCamera();
            Intent intent = new Intent(ScannerActivity.this, PayNowActivity.class);
            intent.putExtra(Constant.ABMN_QR_RESULT, rawResult.getText());
            intent.putExtra(Constant.TYPE_AMOUNT, "0");
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != 80) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permission, 80);
            }
        }else {
            mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
            mScannerView.startCamera();          // Start camera on resume
            mScannerView.resumeCameraPreview(this);
            setContentView(mScannerView);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mScannerView.stopCamera();           // Stop camera on pause
        startActivity(new Intent(this, MainActivity.class).putExtra(Constant.FROM, ""));
    }
}