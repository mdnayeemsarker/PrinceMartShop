package com.princemartbd.shop.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.princemartbd.shop.R;
import com.princemartbd.shop.activity.MainActivity;
import com.princemartbd.shop.adapter.ImageAdapter;
import com.princemartbd.shop.adapter.ItemsAdapter;
import com.princemartbd.shop.adapter.ProductImagesAdapter;
import com.princemartbd.shop.helper.ApiConfig;
import com.princemartbd.shop.helper.Constant;
import com.princemartbd.shop.helper.Session;
import com.princemartbd.shop.helper.album.Album;
import com.princemartbd.shop.helper.album.AlbumFile;
import com.princemartbd.shop.helper.album.api.widget.Widget;
import com.princemartbd.shop.helper.album.widget.divider.Api21ItemDivider;
import com.princemartbd.shop.helper.album.widget.divider.Divider;
import com.princemartbd.shop.model.OrderTracker;

import static android.content.Context.INPUT_METHOD_SERVICE;

@SuppressLint("NotifyDataSetChanged")
public class TrackerDetailFragment extends Fragment {
    public Button btnReorder, btnInvoice;
    View root;
    OrderTracker order;
    public ProgressBar progressBar;
    TextView tvOrderOTP, tvItemTotal, tvDeliveryCharge, tvTotal, tvPromoCode, tvPCAmount, tvWallet, tvFinalTotal, tvDPercent, tvDAmount;
    TextView tvOtherDetail, tvOrderId, tvOrderDate,btnOtherImages, btnSubmit,tvReceiptStatus, tvReceiptStatusReason, tvBankDetail;
    RecyclerView recyclerView,recyclerViewImageGallery, recyclerViewReceiptImages;
    RelativeLayout relativeLyt;
    LinearLayout lytPromo, lytWallet, lytPriceDetail, lytOTP;
    double totalAfterTax = 0.0;
    Activity activity;
    String id;
    Session session;
    HashMap<String, String> hashMap;
    private ShimmerFrameLayout mShimmerViewContainer;
    ScrollView scrollView;

    LinearLayout lytReceipt_;
    private ArrayList<AlbumFile> mAlbumFiles;
    private ImageAdapter mAdapter;
    ProductImagesAdapter productImagesAdapter;
    RelativeLayout lytReceipt;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_tracker_detail, container, false);
        activity = getActivity();
        session = new Session(activity);

        lytPriceDetail = root.findViewById(R.id.lytPriceDetail);
        lytPromo = root.findViewById(R.id.lytPromo);
        lytWallet = root.findViewById(R.id.lytWallet);
        tvItemTotal = root.findViewById(R.id.tvItemTotal);
        tvDeliveryCharge = root.findViewById(R.id.tvDeliveryCharge);
        tvDAmount = root.findViewById(R.id.tvDAmount);
        tvDPercent = root.findViewById(R.id.tvDPercent);
        tvTotal = root.findViewById(R.id.tvTotal);
        tvPromoCode = root.findViewById(R.id.tvPromoCode);
        tvPCAmount = root.findViewById(R.id.tvPCAmount);
        tvWallet = root.findViewById(R.id.tvWallet);
        tvFinalTotal = root.findViewById(R.id.tvFinalTotal);
        tvOrderId = root.findViewById(R.id.tvOrderId);
        btnOtherImages = root.findViewById(R.id.btnOtherImages);
        btnSubmit = root.findViewById(R.id.btnSubmit);
        recyclerViewImageGallery = root.findViewById(R.id.recyclerViewImageGallery);
        recyclerViewReceiptImages = root.findViewById(R.id.recyclerViewReceiptImages);
        tvBankDetail = root.findViewById(R.id.tvBankDetail);
        tvReceiptStatusReason = root.findViewById(R.id.tvReceiptStatusReason);
        tvReceiptStatus = root.findViewById(R.id.tvReceiptStatus);
        tvOrderDate = root.findViewById(R.id.tvOrderDate);
        relativeLyt = root.findViewById(R.id.relativeLyt);
        tvOtherDetail = root.findViewById(R.id.tvOtherDetail);
        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setNestedScrollingEnabled(false);
        btnReorder = root.findViewById(R.id.btnReorder);
        btnInvoice = root.findViewById(R.id.btnInvoice);
        tvOrderOTP = root.findViewById(R.id.tvOrderOTP);
        lytOTP = root.findViewById(R.id.lytOTP);
        lytReceipt = root.findViewById(R.id.lytReceipt);
        lytReceipt_ = root.findViewById(R.id.lytReceipt_);
        scrollView = root.findViewById(R.id.scrollView);
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer);
        progressBar = root.findViewById(R.id.progressBar);
        hashMap = new HashMap<>();

        id = requireArguments().getString(Constant.ID);
        if (id.equals("")) {
            order = (OrderTracker) getArguments().getSerializable("model");
            id = order.getId();
            SetData(order);
        } else {
            getOrderDetails(id);
        }

        setHasOptionsMenu(true);

        btnReorder.setOnClickListener(view -> new AlertDialog.Builder(requireActivity())
                .setTitle(getString(R.string.re_order))
                .setMessage(getString(R.string.reorder_msg))
                .setPositiveButton(getString(R.string.proceed), (dialog, which) -> {
                    if (activity != null) {
                        GetReOrderData();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss()).show());

        mAlbumFiles = new ArrayList<>();
        recyclerViewImageGallery.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewReceiptImages = root.findViewById(R.id.recyclerViewReceiptImages);

        recyclerViewReceiptImages.setLayoutManager(new GridLayoutManager(activity, 3));
        Divider divider = new Api21ItemDivider(Color.TRANSPARENT, 10, 10);
        recyclerViewReceiptImages.addItemDecoration(divider);
        recyclerViewReceiptImages.setNestedScrollingEnabled(false);
        mAdapter = new ImageAdapter(activity, (view, position) -> previewImage(position));
        recyclerViewReceiptImages.setAdapter(mAdapter);

        tvBankDetail.setOnClickListener(v -> openBankDetails());

        btnOtherImages.setOnClickListener(v -> {
            lytReceipt_.setVisibility(View.VISIBLE);
            SelectImage("multi");
        });

        btnSubmit.setOnClickListener(v -> {
            if (mAlbumFiles != null && mAlbumFiles.size() > 0) {
                progressBar.setVisibility(View.VISIBLE);
                submitReceipt();
            } else {
                Toast.makeText(activity, activity.getString(R.string.no_receipt_select_message), Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }


    public void openBankDetails() {
        {
            @SuppressLint("InflateParams") View sheetView = activity.getLayoutInflater().inflate(R.layout.dialog_bank_detail, null);
            ViewGroup parentViewGroup = (ViewGroup) sheetView.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeAllViews();
            }

            final Dialog mBottomSheetDialog = new Dialog(activity);
            mBottomSheetDialog.setContentView(sheetView);
            mBottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mBottomSheetDialog.show();

            TextView tvAccountName = sheetView.findViewById(R.id.tvAccountName);
            TextView tvAccountNumber = sheetView.findViewById(R.id.tvAccountNumber);
            TextView tvBankName = sheetView.findViewById(R.id.tvBankName);
            TextView tvIFSCCode = sheetView.findViewById(R.id.tvIFSCCode);
            TextView tvExtraNote = sheetView.findViewById(R.id.tvExtraNote);

            tvAccountName.setText(Constant.ACCOUNT_NAME);
            tvAccountNumber.setText(Constant.ACCOUNT_NUMBER);
            tvBankName.setText(Constant.BANK_NAME);
            tvIFSCCode.setText(Constant.BANK_CODE);
            tvExtraNote.setText(Constant.NOTES);

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    void submitReceipt() {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            builder.addFormDataPart(Constant.AccessKey, Constant.AccessKeyVal);
            builder.addFormDataPart(Constant.UPLOAD_BANK_TRANSFER_ATTACHMENT, Constant.GetVal);
            builder.addFormDataPart(Constant.ORDER_ID, order.getId());

            for (int i = 0; i < mAlbumFiles.size(); i++) {
                File file = new File(mAlbumFiles.get(i).getPath());
                builder.addFormDataPart(Constant.IMAGES, file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file));
            }

            RequestBody body = builder.build();

            Request request = new Request.Builder()
                    .url(Constant.ORDER_PROCESS_URL)
                    .method("POST", body)
                    .addHeader(Constant.AUTHORIZATION, "Bearer " + ApiConfig.createJWT("eKart", "eKart Authentication"))
                    .build();

            Response response = client.newCall(request).execute();
            Toast.makeText(activity, new JSONObject(Objects.requireNonNull(response.body()).string()).getString(Constant.MESSAGE), Toast.LENGTH_SHORT).show();

            progressBar.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
            progressBar.setVisibility(View.GONE);
            Toast.makeText(activity, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void SelectImage(String type) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            if (type.equals("multi")) {
                selectOtherImage();
            }


        }
    }

    private void previewImage(int position) {
        if (mAlbumFiles == null || mAlbumFiles.size() == 0) {
            Toast.makeText(activity, R.string.no_selected, Toast.LENGTH_LONG).show();
        } else {
            Album.galleryAlbum(activity)
                    .checkable(true)
                    .checkedList(mAlbumFiles)
                    .currentPosition(position)
                    .widget(Widget.newDarkBuilder(activity).build()
                    )
                    .onResult(result -> {
                        mAlbumFiles = result;
                        mAdapter.notifyDataSetChanged(mAlbumFiles);
                        //mTvMessage.setVisibility(result.size() > 0 ? View.VISIBLE : View.GONE);
                    })
                    .start();
        }
    }

    private void selectOtherImage() {
        Album.image(activity)
                .multipleChoice()
                .camera(true)
                .columnCount(2)
                .selectCount(6)
                .checkedList(mAlbumFiles)
                .widget(Widget.newDarkBuilder(activity)
                        .build()
                )
                .onResult(result -> {
                    mAlbumFiles = result;
                    mAdapter.notifyDataSetChanged(mAlbumFiles);
                    //mTvMessage.setVisibility(result.size() > 0 ? View.VISIBLE : View.GONE);
                })
                .onCancel(result -> {
                    //Toast.makeText(AddBusinessActivity.this, "Cancel", Toast.LENGTH_LONG).show();
                })
                .start();
    }

    public void GetReOrderData() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_REORDER_DATA, Constant.GetVal);
        params.put(Constant.ID, id);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONObject(Constant.DATA).getJSONArray(Constant.ITEMS);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        hashMap.put(jsonArray.getJSONObject(i).getString(Constant.PRODUCT_VARIANT_ID), jsonArray.getJSONObject(i).getString(Constant.QUANTITY));
                    }
                    ApiConfig.AddMultipleProductInCart(session, activity, hashMap);
                } catch (JSONException e) {
                        e.printStackTrace();
                }
            }
        }, activity, Constant.ORDER_PROCESS_URL, params, false);
    }

    public void getOrderDetails(String id) {
        scrollView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_ORDERS, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.ORDER_ID, id);

        //  System.out.println("=====params " + params.toString());
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject1 = new JSONObject(response);
                    if (!jsonObject1.getBoolean(Constant.ERROR)) {
                        JSONObject jsonObject = jsonObject1.getJSONArray(Constant.DATA).getJSONObject(0);
                        SetData(ApiConfig.OrderTracker(jsonObject));
                    } else {
                        scrollView.setVisibility(View.VISIBLE);
                        mShimmerViewContainer.setVisibility(View.GONE);
                        mShimmerViewContainer.stopShimmer();
                    }
                } catch (JSONException e) {
                        e.printStackTrace();
                    scrollView.setVisibility(View.VISIBLE);
                    mShimmerViewContainer.setVisibility(View.GONE);
                    mShimmerViewContainer.stopShimmer();
                }
            }
        }, activity, Constant.ORDER_PROCESS_URL, params, false);
    }

    @SuppressLint("SetTextI18n")
    public void SetData(OrderTracker order) {
        try {
            String[] date = order.getDate_added().split("\\s+");
            tvOrderId.setText(order.getId());
            if (order.getOtp().equals("0")) {
                lytOTP.setVisibility(View.GONE);
            } else {
                tvOrderOTP.setText(order.getOtp());
            }

            btnInvoice.setOnClickListener(v -> {
                Fragment fragment = new WebViewFragment();
                Bundle bundle = new Bundle();
                bundle.putString("type", activity.getString(R.string.order) + "#" + order.getId());
                fragment.setArguments(bundle);
                MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
            });

            tvReceiptStatus.setText(order.getBank_transfer_status().equalsIgnoreCase("0") ? getString(R.string.pending) : order.getBank_transfer_status().equalsIgnoreCase("1") ? getString(R.string.accepted) : getString(R.string.rejected));

            if (order.getBank_transfer_status().equalsIgnoreCase("2")) {
                tvReceiptStatusReason.setVisibility(View.VISIBLE);
                tvReceiptStatusReason.setText(order.getBank_transfer_message());
            }

            productImagesAdapter = new ProductImagesAdapter(activity, order.getAttachment(), "api", order.getId());
            recyclerViewImageGallery.setAdapter(productImagesAdapter);

            if(order.getPayment_method().equalsIgnoreCase("bank_transfer")){
                lytReceipt.setVisibility(View.VISIBLE);
            }

            tvOrderDate.setText(date[0]);
            tvOtherDetail.setText(getString(R.string.name_1) + order.getUsername() + getString(R.string.mobile_no_1) + order.getMobile() + getString(R.string.address_1) + order.getAddress());
            totalAfterTax = (Double.parseDouble(order.getTotal()) + Double.parseDouble(order.getDelivery_charge()));
            tvItemTotal.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat(order.getTotal()));
            tvDeliveryCharge.setText("+ " + session.getData(Constant.CURRENCY) + ApiConfig.StringFormat(order.getDelivery_charge()));
            tvDPercent.setText(getString(R.string.discount) + "(" + order.getdPercent() + "%) :");
            tvDAmount.setText("- " + session.getData(Constant.CURRENCY) + ApiConfig.StringFormat(order.getdAmount()));
            tvTotal.setText(session.getData(Constant.CURRENCY) + totalAfterTax);
            tvPCAmount.setText("- " + session.getData(Constant.CURRENCY) + ApiConfig.StringFormat(order.getPromoDiscount()));
            tvWallet.setText("- " + session.getData(Constant.CURRENCY) + ApiConfig.StringFormat(order.getWalletBalance()));
            tvFinalTotal.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat(order.getFinal_total()));

            scrollView.setVisibility(View.VISIBLE);
            mShimmerViewContainer.setVisibility(View.GONE);
            mShimmerViewContainer.stopShimmer();

            recyclerView.setAdapter(new ItemsAdapter(activity, order.getItemsList(), "detail"));
            relativeLyt.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.order_track_detail);
        activity.invalidateOptionsMenu();
        hideKeyboard();
    }

    public void hideKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.toolbar_cart).setVisible(true);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(true);
    }
}