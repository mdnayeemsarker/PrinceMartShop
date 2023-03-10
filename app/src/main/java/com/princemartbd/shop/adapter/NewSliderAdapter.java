package com.princemartbd.shop.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.princemartbd.shop.R;
import com.princemartbd.shop.model.SliderData;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.ArrayList;

public class NewSliderAdapter extends SliderViewAdapter<NewSliderAdapter.SliderAdapterVH> {

    private final Activity activity;
    private final ArrayList<SliderData> mSliderItems;

    public NewSliderAdapter(Activity activity, ArrayList<SliderData> mSliderItems) {
        this.activity = activity;
        this.mSliderItems = mSliderItems;
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        @SuppressLint("InflateParams")
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.lyt_image_slider, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH holder, int position) {
        SliderData dataModel = mSliderItems.get(position);

        Glide.with(activity).load(dataModel.getImgUrl()).placeholder(R.mipmap.ic_launcher).into(holder.imageView);

    }

    @Override
    public int getCount() {
        return mSliderItems.size();
    }

    static class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        private final ImageView imageView;

        public SliderAdapterVH(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.sliderImage);
        }
    }
}