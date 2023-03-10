package com.princemartbd.shop.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.princemartbd.shop.R;
import com.princemartbd.shop.fragment.ProductDetailFragment;
import com.princemartbd.shop.helper.ApiConfig;
import com.princemartbd.shop.helper.Constant;
import com.princemartbd.shop.helper.DatabaseHelper;
import com.princemartbd.shop.helper.Session;
import com.princemartbd.shop.model.PriceVariation;
import com.princemartbd.shop.model.Product;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProductHomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // for load more
    final int VIEW_TYPE_ITEM = 0;
    final int VIEW_TYPE_LOADING = 1;
    public int resource;
    public ArrayList<Product> mDataset;
    Activity activity;
    Session session;
    boolean isLogin;
    DatabaseHelper databaseHelper;
    public boolean isLoading;

    public ProductHomeAdapter(Activity activity, ArrayList<Product> productArrayList, int lyt_item_list) {
        this.activity = activity;
        this.mDataset = productArrayList;
        this.resource = lyt_item_list;

    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_ITEM) {
            view = LayoutInflater.from(activity).inflate(resource, parent, false);
            return new HolderItems(view);
        }
        throw new IllegalArgumentException("unexpected viewType: " + viewType);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holderParent, int position) {
//        if (holderParent instanceof ProductLoadMoreAdapter.HolderItems) {
        HolderItems holder = (HolderItems) holderParent;
        holder.setIsRecyclable(false);
        Product product = mDataset.get(position);

        ArrayList<PriceVariation> priceVariations = product.getPriceVariations();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.tvTitle.setText(Html.fromHtml(product.getName(), 0));
        } else {
            holder.tvTitle.setText(product.getName());
        }

//        if (priceVariations.get(0).getDiscounted_price() != null) {
//            holder.tvPrice.setText("৳: " + priceVariations.get(0).getDiscounted_price());
//        }
//        if (priceVariations.get(0).getDiscounted_price().equals("0")) {
//            holder.tvPrice.setText("৳: " + priceVariations.get(0).getPrice());
//        }
//        if (priceVariations.get(0).getDiscounted_price() == null) {
//            holder.tvPrice.setText("৳: " + priceVariations.get(0).getPrice());
//        }

        Picasso.get().
                load(product.getImage())
                .fit()
                .centerInside()
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(holder.thumbnail);

        double DiscountedPrice, OriginalPrice;
        String taxPercentage = "0";
        try {
            taxPercentage = (Double.parseDouble(product.getTax_percentage()) > 0 ? product.getTax_percentage() : "0");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (priceVariations.get(0).getDiscounted_price().equals("0") || priceVariations.get(0).getDiscounted_price() == null || priceVariations.get(0).getDiscounted_price().equals("")) {
            holder.tvOriginalPrice.setVisibility(View.GONE);
            holder.showDiscount.setVisibility(View.GONE);
            holder.tvPrice.setVisibility(View.VISIBLE);
            holder.tvPrice.setText("৳" + priceVariations.get(0).getPrice());
        } else {
            DiscountedPrice = ((Float.parseFloat(priceVariations.get(0).getDiscounted_price()) + ((Float.parseFloat(priceVariations.get(0).getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
            OriginalPrice = (Float.parseFloat(priceVariations.get(0).getPrice()) + ((Float.parseFloat(priceVariations.get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100));

            holder.showDiscount.setVisibility(View.VISIBLE);
            holder.tvPrice.setVisibility(View.VISIBLE);
            holder.tvOriginalPrice.setVisibility(View.VISIBLE);
            holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvPrice.setText("৳" + priceVariations.get(0).getDiscounted_price());
            holder.tvOriginalPrice.setText("৳" + priceVariations.get(0).getPrice());

            holder.showDiscount.setText("-" + ApiConfig.GetDiscount(OriginalPrice, DiscountedPrice));
        }

        holder.lytRootId.setOnClickListener(v -> {

            if (Constant.CartValues.size() > 0) {
                ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);
            }

            AppCompatActivity activity1 = (AppCompatActivity) activity;
            Fragment fragment = new ProductDetailFragment();
            Bundle bundle = new Bundle();
//                bundle.putInt(Constant.VARIANT_POSITION, priceVariations.size());
//                bundle.putString(Constant.ID, priceVariations.get(0).getProduct_id());
            bundle.putString(Constant.ID, product.getId());
            bundle.putString(Constant.FROM, "similar");
            bundle.putInt(Constant.LIST_POSITION, position);

            fragment.setArguments(bundle);

            activity1.getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
        });
//        } else if (holderParent instanceof ProductLoadMoreAdapter.ViewHolderLoading) {
//            ProductLoadMoreAdapter.ViewHolderLoading loadingViewHolder = (ProductLoadMoreAdapter.ViewHolderLoading) holderParent;
//            loadingViewHolder.progressBar.setIndeterminate(true);
//        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

//    @Override
//    public int getItemViewType(int position) {
//        return mDataset.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
//    }
//
//    @Override
//    public long getItemId(int position) {
//        Product product = mDataset.get(position);
//        if (product != null)
//            return Integer.parseInt(product.getPriceVariations().get(0).getProduct_id());
//        else
//            return position;
//    }
//
//    public void setLoaded() {
//        isLoading = false;
//    }

    public static class HolderItems extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView tvTitle;
        TextView tvPrice;
        TextView tvOriginalPrice;
        TextView showDiscount;
        CardView lytRootId;

        public HolderItems(View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            showDiscount = itemView.findViewById(R.id.showDiscount);
            lytRootId = itemView.findViewById(R.id.lytRootId);
        }
    }
}