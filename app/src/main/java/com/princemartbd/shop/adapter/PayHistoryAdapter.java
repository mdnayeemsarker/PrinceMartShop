package com.princemartbd.shop.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.princemartbd.shop.R;
import com.princemartbd.shop.helper.ApiConfig;
import com.princemartbd.shop.helper.Constant;
import com.princemartbd.shop.model.PayHistoryModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PayHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    final Activity activity;
    private final ArrayList<PayHistoryModel> historyList;

    public PayHistoryAdapter(Activity activity, ArrayList<PayHistoryModel> historyList) {
        this.activity = activity;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(activity).inflate(R.layout.lyt_pay_history, viewGroup, false);
        return new PayHistoryAdapter.HolderItems(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final HolderItems holder = (HolderItems) viewHolder;

        double amount = Double.parseDouble(historyList.get(i).getAmount());
        double discount = Double.parseDouble(historyList.get(i).getDiscount());
        double payable = amount - discount;

        Log.d("test", "Amount: " + amount + "\nDiscount: " + discount + "\nPayable: " + payable  );

        holder.itemId.setText("ID. #"+historyList.get(i).getTran_id());
        holder.historyAmount.setText("Amount: "+historyList.get(i).getAmount());
        holder.historyPayable.setText("Payable: "+ historyList.get(i).getPayable());

        if (historyList.get(i).getDiscount().equals("0.00")){
            holder.historyDiscount.setText("Bonus: "+historyList.get(i).getCashback());
        }

//        holder.historyDiscount.setText("Discount: "+historyList.get(i).getDiscount());
        holder.historyDateCreate.setText("Date: "+historyList.get(i).getDate_created());

//        if (historyList.get(i).getLast_updated().equals("null")){
//            holder.historyLastUpdate.setVisibility(View.GONE);
//        }
//        else {
//            holder.historyLastUpdate.setText("update: "+historyList.get(i).getLast_updated());
//        }
//        Toast.makeText(activity, "update"+historyList.get(i).getLast_updated(), Toast.LENGTH_SHORT).show();

        holder.historyStatus.setText("Payment Status: " + historyList.get(i).getStatus());

        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_SELLER_DATA, Constant.GetVal);
        params.put(Constant.SELLER_ID, historyList.get(i).getSeller_id());

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject object = new JSONObject(response);

                    if (!object.getBoolean(Constant.ERROR)) {
                        JSONObject jsonObject = object.getJSONArray(Constant.DATA).getJSONObject(0);
                        holder.historySellerName.setText("Merchant: "+jsonObject.getString("store_name"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(activity, "E: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, activity, Constant.GET_SELLER_DATA_URL, params, true);

    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HolderItems extends RecyclerView.ViewHolder {

        final TextView itemId;
        final TextView historySellerName;
        final TextView historyAmount;
        final TextView historyDiscount;
        final TextView historyPayable;
        final TextView historyDateCreate;
//        final TextView historyLastUpdate;
        final TextView historyStatus;
        final TextView historyCashBackId;
        public HolderItems(@NonNull View itemView) {
            super(itemView);

            itemId = itemView.findViewById(R.id.itemIdId);
            historySellerName = itemView.findViewById(R.id.historySellerNameId);
            historyAmount = itemView.findViewById(R.id.historyAmountId);
            historyDiscount = itemView.findViewById(R.id.historyDiscountId);
            historyPayable = itemView.findViewById(R.id.historyPayableId);
            historyDateCreate = itemView.findViewById(R.id.historyDateCreateId);
//            historyLastUpdate = itemView.findViewById(R.id.historyLastUpdateId);
            historyStatus = itemView.findViewById(R.id.historyStatusId);
            historyCashBackId = itemView.findViewById(R.id.historyCashBackId);
        }
    }
}
