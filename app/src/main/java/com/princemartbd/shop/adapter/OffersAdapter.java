package com.princemartbd.shop.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.princemartbd.shop.R;
import com.princemartbd.shop.model.OffersModel;

import java.util.ArrayList;

public class OffersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final Activity activity;
    private final ArrayList<OffersModel> offersModelArrayList;

    public OffersAdapter(Activity activity, ArrayList<OffersModel> offersModelArrayList) {
        this.activity = activity;
        this.offersModelArrayList = offersModelArrayList;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(activity).inflate(R.layout.lyt_offers_item, viewGroup, false);
        return new HolderItems(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        OffersModel model = offersModelArrayList.get(position);

        final HolderItems holder = (HolderItems) viewHolder;

        holder.itemIdTV.setText("#" +model.getId());
        holder.nameTV.setText(model.getName());
        holder.offerStartEndTV.setText("Pay and get bonus: ৳" + model.getStart() + " - ৳" + model.getEnd());

        holder.typeAmountTV.setText(model.getType() + ": " + model.getAmount() + "%");

    }

    @Override
    public int getItemCount() {
        return offersModelArrayList.size();
    }


    static class HolderItems extends RecyclerView.ViewHolder {

        final TextView itemIdTV;
        final TextView nameTV;
        final TextView offerStartEndTV;
        final TextView typeAmountTV;
        public HolderItems(@NonNull View itemView) {
            super(itemView);

            itemIdTV = itemView.findViewById(R.id.itemIdTV);
            nameTV = itemView.findViewById(R.id.nameTV);
            offerStartEndTV = itemView.findViewById(R.id.offerStartEndTV);
            typeAmountTV = itemView.findViewById(R.id.typeAmountTV);
        }
    }
}
