package com.princemartbd.shop.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.princemartbd.shop.R;
import com.princemartbd.shop.adapter.PayHistoryAdapter;
import com.princemartbd.shop.helper.ApiConfig;
import com.princemartbd.shop.helper.Constant;
import com.princemartbd.shop.helper.Session;
import com.princemartbd.shop.model.PayHistoryModel;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PayHistoryActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeLayout;

    private RecyclerView recyclerView;
    private PayHistoryAdapter historyAdapter;
    private ArrayList<PayHistoryModel> historyList;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_history);

        CardView cardViewHamburger = findViewById(R.id.cardViewHamburger);
        ImageView imageMenu = findViewById(R.id.imageMenu);
        ImageView imageHome = findViewById(R.id.imageHome);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        toolbarTitle.setText("Payment History");

        imageHome.setVisibility(View.GONE);
        imageMenu.setVisibility(View.VISIBLE);
        imageMenu.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_arrow_back));
        cardViewHamburger.setOnClickListener(view -> onBackPressed());

        swipeLayout = findViewById(R.id.swipeLayout);
        recyclerView = findViewById(R.id.recyclerView);

        historyList = new ArrayList<>();
        historyAdapter = new PayHistoryAdapter(this, historyList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        swipeLayout.setColorSchemeResources(R.color.colorPrimary);

        swipeLayout.setOnRefreshListener(() -> {
            swipeLayout.setRefreshing(true);
            setData();
        });

        setData();
    }

    private void setData() {

        Session session = new Session(this);
        historyList = new ArrayList<>();

        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_HISTORIES, Constant.GetVal);
        params.put("type", "user");
        params.put("type_id", session.getData(Constant.ID));

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject object = new JSONObject(response);
                    if (!object.getBoolean(Constant.ERROR)) {
                        JSONArray array = object.getJSONArray("data");

                        for (int i = 0; i < array.length(); i++) {
                            try {
                                JSONObject jweObject = array.getJSONObject(i);
                                if (jweObject != null){
                                    PayHistoryModel historyModel = new Gson().fromJson(jweObject.toString(), PayHistoryModel.class);
                                    historyList.add(historyModel);
                                }
                            }catch (Exception e){
                                Toast.makeText(this, "Error: " + e, Toast.LENGTH_SHORT).show();
                            }
                        }
                        historyAdapter = new PayHistoryAdapter(this, historyList);
                        recyclerView.setAdapter(historyAdapter);
                        swipeLayout.setRefreshing(false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("payHistoryJSONException", e.toString());
                }
                Log.d("response", response);
            }
        }, this, Constant.GET_PAY_REQUEST_URL, params, true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class).putExtra(Constant.FROM, ""));
    }
}