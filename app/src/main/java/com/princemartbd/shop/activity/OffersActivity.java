package com.princemartbd.shop.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.princemartbd.shop.R;
import com.princemartbd.shop.adapter.OffersAdapter;
import com.princemartbd.shop.adapter.PayHistoryAdapter;
import com.princemartbd.shop.helper.ApiConfig;
import com.princemartbd.shop.helper.Constant;
import com.princemartbd.shop.helper.Session;
import com.princemartbd.shop.model.OffersModel;
import com.princemartbd.shop.model.PayHistoryModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OffersActivity extends AppCompatActivity {

    private Activity activity;
    private Session session;

    private RecyclerView offersRV;
    private OffersAdapter offersAdapter;
    private ArrayList<OffersModel> offersModelArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers);

        define();
        work();

    }

    private void define() {
        activity = this;
        session = new Session(activity);

        offersRV = findViewById(R.id.offersRV);

        offersModelArrayList = new ArrayList<>();
        offersAdapter = new OffersAdapter(activity, offersModelArrayList);

    }

    private void work() {
        getData();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        offersRV.setLayoutManager(layoutManager);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

    }

    private void getData() {

        JSONArray offersArray = session.getJSONArray("offersArray");
        for (int i = 0; i < offersArray.length(); i++) {
            try {
                JSONObject data = offersArray.getJSONObject(i);
                String id = data.getString("id");
                String name = data.getString("name");
                String start = data.getString("start");
                String end = data.getString("end");
                String type = data.getString("type"); //flat/percentage
                String amount = data.getString("amount");
                String is_active = data.getString("is_active");
                String date_created = data.getString("date_created");

                OffersModel offersModel = new OffersModel(id, name, start, end, type, amount, is_active, date_created);
                offersModelArrayList.add(offersModel);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("payOfferArray", e.getMessage());
            }
        }
        offersAdapter = new OffersAdapter(this, offersModelArrayList);
        offersRV.setAdapter(offersAdapter);
    }
}