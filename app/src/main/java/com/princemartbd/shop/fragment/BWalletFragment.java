package com.princemartbd.shop.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.princemartbd.shop.R;
import com.princemartbd.shop.activity.MainActivity;
import com.princemartbd.shop.activity.ScannerActivity;
import com.princemartbd.shop.activity.SplashActivity;
import com.princemartbd.shop.adapter.NewSliderAdapter;
import com.princemartbd.shop.helper.ApiConfig;
import com.princemartbd.shop.helper.Constant;
import com.princemartbd.shop.helper.Session;
import com.princemartbd.shop.model.SliderData;
import com.smarteist.autoimageslider.SliderView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.internal.http2.Http2Reader;

public class BWalletFragment extends Fragment {

    View root;
    Activity activity;
    Session session;
    private SliderView imageSlider;
    TextView bonusBalanceTV;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_b_wallet, container, false);
        activity = getActivity();
        session = new Session(activity);

        bonusBalanceTV = root.findViewById(R.id.bonusBalanceTV);
        imageSlider = root.findViewById(R.id.imageSlider);

        if (session.getBoolean(Constant.IS_USER_LOGIN))
            bonusBalanceTV.setText("৳ " + session.getData("bonus_balance"));
        else
            bonusBalanceTV.setText("৳ 0.00");
//        setText("Bonus Balance" + "\t:\t" + "৳" + session.getData("bonus_balance"));

        checkBonusBalance();

        root.findViewById(R.id.payIV).setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                startActivity(new Intent(activity, ScannerActivity.class));
            else
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 80);
        });
        GetHomeData();
        return root;
    }

    @SuppressLint("SetTextI18n")
    private void checkBonusBalance() {
        new Handler().postDelayed(this::checkBonusBalanceHelper, 500);
            if (session.getBoolean(Constant.IS_USER_LOGIN))
                bonusBalanceTV.setText("৳ " + session.getData("bonus_balance"));
            else
                bonusBalanceTV.setText("৳ 0.00");
    }

    private void checkBonusBalanceHelper() {
        new Handler().postDelayed(this::checkBonusBalance, 500);
    }

    public void GetHomeData() {

        Map<String, String> params = new HashMap<>();
        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            params.put(Constant.USER_ID, session.getData(Constant.ID));
        }
//        if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
//            params.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
//        }

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        Log.d("GET_ALL_DATA_URL", jsonObject.toString());
                        GetOfferImage(jsonObject.getJSONArray(Constant.OFFER_IMAGES));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.GET_ALL_DATA_URL, params, false);

    }


    public void GetOfferImage(JSONArray jsonArray) {

        ArrayList<SliderData> sliderDataArrayList = new ArrayList<>();

        try {
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    sliderDataArrayList.add(new SliderData(object.getString(Constant.IMAGE)));
                }
                NewSliderAdapter adapter = new NewSliderAdapter(activity, sliderDataArrayList);
                imageSlider.setSliderAdapter(adapter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != 80) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 80);
            }
        } else {
            startActivity(new Intent(activity, ScannerActivity.class));
        }
    }

}