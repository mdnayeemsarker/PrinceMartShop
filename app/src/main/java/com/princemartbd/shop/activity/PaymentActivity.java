package com.princemartbd.shop.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aamarpay.library.AamarPay;
import com.aamarpay.library.DialogBuilder;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.princemartbd.shop.R;
import com.princemartbd.shop.adapter.DateAdapter;
import com.princemartbd.shop.adapter.SlotAdapter;
import com.princemartbd.shop.fragment.CheckoutFragment;
import com.princemartbd.shop.helper.ApiConfig;
import com.princemartbd.shop.helper.Constant;
import com.princemartbd.shop.helper.PaymentModelClass;
import com.princemartbd.shop.helper.Session;
import com.princemartbd.shop.model.BookingDate;
import com.princemartbd.shop.model.Slot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PaymentActivity extends AppCompatActivity {
    public static final String TAG = CheckoutFragment.class.getSimpleName();
    public static String customerId;
    public static String razorPayId;
    public static String paymentMethod = "";
    public static String deliveryTime = "";
    public static String deliveryDay = "";
    public static String pCode = "";
    RadioGroup lytPayment;
    public static Map<String, String> sendParams;
    public static RecyclerView recyclerView;
    @SuppressLint("StaticFieldLeak")
    public static SlotAdapter adapter;
    public LinearLayout paymentLyt, deliveryTimeLyt, lytPayOption, lytCLocation, processLyt;
    public ArrayList<String> variantIdList, qtyList, dateList;
    TextView tvSubTotal, tvTotalItems, tvSelectDeliveryDate, tvWltBalance, tvProceedOrder, tvConfirmOrder, tvPayment, tvDelivery;
    double subtotal = 0.0, usedBalance = 0.0, pCodeDiscount = 0.0;
    RadioButton rbCOD, rbPayU, rbPayPal, rbRazorPay, rbPayStack, rbFlutterWave, rbMidTrans, rbStripe, rbPayTm, rbSslCommerz, rbBankTransfer;
    ArrayList<BookingDate> bookingDates;
    RelativeLayout confirmLyt, lytWallet;
    RecyclerView recyclerViewDates;
    Calendar StartDate, EndDate;
    ScrollView scrollPaymentLyt;
    ArrayList<Slot> slotList;
    DateAdapter dateAdapter;
    int mYear, mMonth, mDay, isSmallBonus;
    String address = null;
    Activity activity;
    CheckBox chWallet;
    Button btnApply;
    Session session;
    double total;
    private ShimmerFrameLayout mShimmerViewContainer;
    public static ProgressDialog dialog_;
    CardView cardViewHamburger;
    TextView toolbarTitle;
    ImageView imageHome;
    ImageView imageMenu;
    Toolbar toolbar;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        activity = PaymentActivity.this;
        Constant.selectedDatePosition = 0;
        session = new Session(activity);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cardViewHamburger = findViewById(R.id.cardViewHamburger);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        imageMenu = findViewById(R.id.imageMenu);
        imageHome = findViewById(R.id.imageHome);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        toolbarTitle.setText(getString(R.string.payment));

        imageHome.setVisibility(View.GONE);

        imageMenu.setVisibility(View.VISIBLE);
        imageMenu.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_arrow_back));

        cardViewHamburger.setOnClickListener(view -> onBackPressed());

        getAllWidgets();

        total = getIntent().getDoubleExtra("total", 0);
        subtotal = getIntent().getDoubleExtra("subtotal", 0);

        Constant.SETTING_TAX = getIntent().getDoubleExtra("tax", 0);
        pCodeDiscount = getIntent().getDoubleExtra("pCodeDiscount", 0);
        pCode = getIntent().getStringExtra("pCode");
        address = getIntent().getStringExtra("address");
        variantIdList = getIntent().getStringArrayListExtra("variantIdList");
        qtyList = getIntent().getStringArrayListExtra("qtyList");

        tvSubTotal.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + subtotal));
        tvTotalItems.setText(Constant.TOTAL_CART_ITEM + (Constant.TOTAL_CART_ITEM > 1 ? activity.getString(R.string.items) : activity.getString(R.string.item)));

        ApiConfig.getWalletBalance(activity, session);

        GetPaymentConfig();
        chWallet.setTag("false");
        tvWltBalance.setText("Bonus Balance: ৳" + session.getData("bonus_balance"));
        if (Double.parseDouble(session.getData("bonus_balance")) == 0) {
            lytWallet.setVisibility(View.GONE);
        } else {
            lytWallet.setVisibility(View.VISIBLE);
        }

        tvProceedOrder.setOnClickListener(v -> PlaceOrderProcess(activity));

        chWallet.setOnClickListener(view -> {
            if (chWallet.getTag().equals("false")) {
                chWallet.setChecked(true);
                lytWallet.setVisibility(View.VISIBLE);
                paymentMethod = Constant.WALLET;
                if (Double.parseDouble(session.getData("bonus_balance")) >= subtotal) {
                    isSmallBonus = 0;
                    usedBalance = subtotal;
                    tvWltBalance.setText("Remaining bonus balance ৳" + (Double.parseDouble(session.getData("bonus_balance")) - usedBalance));
                    lytPayOption.setVisibility(View.GONE);
                } else {
                    isSmallBonus = 1;
                    usedBalance = Double.parseDouble(session.getData("bonus_balance"));
                    tvWltBalance.setText("Remaining bonus balance ৳0.00");
                    lytPayOption.setVisibility(View.VISIBLE);
                }
                subtotal = (subtotal - usedBalance);
                tvSubTotal.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + subtotal));
                chWallet.setTag("true");
            } else {
                walletUncheck();
            }

        });

        confirmLyt.setVisibility(View.VISIBLE);
        scrollPaymentLyt.setVisibility(View.VISIBLE);
        lytPayment.setOnCheckedChangeListener((group, checkedId) -> {
            try {
                RadioButton rb = findViewById(checkedId);
                paymentMethod = rb.getTag().toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void getAllWidgets() {
        recyclerView = findViewById(R.id.recyclerView);

        rbPayTm = findViewById(R.id.rbPayTm);
        rbSslCommerz = findViewById(R.id.rbSslCommerz);
        rbPayStack = findViewById(R.id.rbPayStack);
        rbFlutterWave = findViewById(R.id.rbFlutterWave);
        rbCOD = findViewById(R.id.rbCOD);
        lytPayment = findViewById(R.id.lytPayment);
        rbPayU = findViewById(R.id.rbPayU);
        rbPayPal = findViewById(R.id.rbPayPal);
        rbRazorPay = findViewById(R.id.rbRazorPay);
        rbMidTrans = findViewById(R.id.rbMidTrans);
        rbStripe = findViewById(R.id.rbStripe);
        rbBankTransfer = findViewById(R.id.rbBankTransfer);

        tvDelivery = findViewById(R.id.tvSummary);
        tvPayment = findViewById(R.id.tvPayment);
        chWallet = findViewById(R.id.chWallet);
        lytPayOption = findViewById(R.id.lytPayOption);
        lytCLocation = findViewById(R.id.lytCLocation);
        lytWallet = findViewById(R.id.lytWallet);
        paymentLyt = findViewById(R.id.paymentLyt);
        tvProceedOrder = findViewById(R.id.tvProceedOrder);
        tvConfirmOrder = findViewById(R.id.tvConfirmOrder);
        processLyt = findViewById(R.id.processLyt);
        tvSelectDeliveryDate = findViewById(R.id.tvSelectDeliveryDate);
        deliveryTimeLyt = findViewById(R.id.deliveryTimeLyt);
        recyclerViewDates = findViewById(R.id.recyclerViewDates);
        tvSubTotal = findViewById(R.id.tvSubTotal);
        tvTotalItems = findViewById(R.id.tvTotalItems);
        confirmLyt = findViewById(R.id.confirmLyt);
        scrollPaymentLyt = findViewById(R.id.scrollPaymentLyt);
        tvWltBalance = findViewById(R.id.tvWltBalance);
        btnApply = findViewById(R.id.btnApply);
        mShimmerViewContainer = findViewById(R.id.mShimmerViewContainer);
    }

    public void GetPaymentConfig() {
        recyclerView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.SETTINGS, Constant.GetVal);
        params.put(Constant.GET_PAYMENT_METHOD, Constant.GetVal);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        if (jsonObject.has(Constant.PAYMENT_METHODS)) {
                            JSONObject object = jsonObject.getJSONObject(Constant.PAYMENT_METHODS);
                            if (object.has(Constant.cod_payment_method)) {
                                Constant.COD = object.getString(Constant.cod_payment_method);
                                Constant.COD_MODE = object.getString(Constant.cod_mode);
                            }
                            if (object.has(Constant.ssl_commerce_payment_method)) {
                                Constant.SSLECOMMERZ = object.getString(Constant.ssl_commerce_payment_method);
                                Constant.SSLECOMMERZ_MODE = object.getString(Constant.ssl_commerece_mode);
                                Constant.SSLECOMMERZ_STORE_ID = object.getString(Constant.ssl_commerece_store_id);
                                Constant.SSLECOMMERZ_SECRET_KEY = object.getString(Constant.ssl_commerece_secret_key);
                            }
                            setPaymentMethod();
                        } else {
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            Toast.makeText(activity, getString(R.string.alert_payment_methods_blank), Toast.LENGTH_SHORT).show();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    mShimmerViewContainer.stopShimmer();
                    mShimmerViewContainer.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        }, activity, Constant.SETTING_URL, params, false);
    }

    public void setPaymentMethod() {
        if (subtotal > 0) {
            if (Constant.DIRECT_BANK_TRANSFER.equals("0") && Constant.FLUTTERWAVE.equals("0") && Constant.PAYPAL.equals("0") && Constant.PAYUMONEY.equals("0") && Constant.COD.equals("0") && Constant.RAZORPAY.equals("0") && Constant.PAYSTACK.equals("0") && Constant.MIDTRANS.equals("0") && Constant.STRIPE.equals("0") && Constant.PAYTM.equals("0") && Constant.SSLECOMMERZ.equals("0")) {
                lytPayOption.setVisibility(View.GONE);
            } else {
                lytPayOption.setVisibility(View.VISIBLE);

                if (Constant.COD.equals("1")) {
                    if (Constant.COD_MODE.equals(Constant.product) && Constant.isCODAllow) {
                        rbCOD.setVisibility(View.GONE);
                    } else {
                        rbCOD.setVisibility(View.VISIBLE);
                    }
                }
                if (Constant.SSLECOMMERZ.equals("1")) {
                    rbSslCommerz.setVisibility(View.VISIBLE);
                }
            }
            getTimeSlots();
        } else {
            lytWallet.setVisibility(View.GONE);
            lytPayOption.setVisibility(View.GONE);
            mShimmerViewContainer.stopShimmer();
            mShimmerViewContainer.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void walletUncheck() {
        paymentMethod = "";
        lytPayOption.setVisibility(View.VISIBLE);
        tvWltBalance.setText(getString(R.string.total) + session.getData(Constant.CURRENCY) + session.getData("bonus_balance"));
        subtotal = (subtotal + usedBalance);
        tvSubTotal.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + subtotal));
        chWallet.setChecked(false);
        chWallet.setTag("false");
    }

    public void getTimeSlots() {
        GetTimeSlotConfig(session, activity);
    }

    public void GetTimeSlotConfig(final Session session, Activity activity) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.SETTINGS, Constant.GetVal);
        params.put(Constant.GET_TIME_SLOT_CONFIG, Constant.GetVal);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject1 = new JSONObject(response);
                    if (!jsonObject1.getBoolean(Constant.ERROR)) {
                        JSONObject jsonObject = new JSONObject(jsonObject1.getJSONObject(Constant.TIME_SLOT_CONFIG).toString());

                        session.setData(Constant.IS_TIME_SLOTS_ENABLE, jsonObject.getString(Constant.IS_TIME_SLOTS_ENABLE));
                        session.setData(Constant.DELIVERY_STARTS_FROM, jsonObject.getString(Constant.DELIVERY_STARTS_FROM));
                        session.setData(Constant.ALLOWED_DAYS, jsonObject.getString(Constant.ALLOWED_DAYS));

                        if (session.getData(Constant.IS_TIME_SLOTS_ENABLE).equals(Constant.GetVal)) {
                            deliveryTimeLyt.setVisibility(View.VISIBLE);

                            StartDate = Calendar.getInstance();
                            EndDate = Calendar.getInstance();
                            mYear = StartDate.get(Calendar.YEAR);
                            mMonth = StartDate.get(Calendar.MONTH);
                            mDay = StartDate.get(Calendar.DAY_OF_MONTH);

                            int DeliveryStartFrom = Integer.parseInt(session.getData(Constant.DELIVERY_STARTS_FROM)) - 1;
                            int DeliveryAllowFrom = Integer.parseInt(session.getData(Constant.ALLOWED_DAYS));

                            StartDate.add(Calendar.DATE, DeliveryStartFrom);
                            EndDate.add(Calendar.DATE, (DeliveryStartFrom + DeliveryAllowFrom));

                            dateList = ApiConfig.getDates(StartDate.get(Calendar.DATE) + "-" + (StartDate.get(Calendar.MONTH) + 1) + "-" + StartDate.get(Calendar.YEAR), EndDate.get(Calendar.DATE) + "-" + (EndDate.get(Calendar.MONTH) + 1) + "-" + EndDate.get(Calendar.YEAR));
                            setDateList(dateList);

                            GetTimeSlots();

                        } else {
                            deliveryTimeLyt.setVisibility(View.GONE);
                            deliveryDay = "Date : N/A";
                            deliveryTime = "Time : N/A";

                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mShimmerViewContainer.stopShimmer();
                    mShimmerViewContainer.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        }, activity, Constant.SETTING_URL, params, false);
    }

    public void GetTimeSlots() {
        slotList = new ArrayList<>();
        Map<String, String> params = new HashMap<>();
        params.put("get_time_slots", Constant.GetVal);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject object = new JSONObject(response);

                    if (!object.getBoolean(Constant.ERROR)) {
                        JSONArray jsonArray = object.getJSONArray("time_slots");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object1 = jsonArray.getJSONObject(i);
                            slotList.add(new Slot(object1.getString(Constant.ID), object1.getString("title"), object1.getString("last_order_time")));
                        }

                        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

                        adapter = new SlotAdapter(deliveryTime, activity, slotList);
                        recyclerView.setAdapter(adapter);

                        mShimmerViewContainer.stopShimmer();
                        mShimmerViewContainer.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mShimmerViewContainer.stopShimmer();
                    mShimmerViewContainer.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        }, activity, Constant.SETTING_URL, params, true);
    }

    public void setDateList(ArrayList<String> datesList) {
        bookingDates = new ArrayList<>();
        for (int i = 0; i < datesList.size(); i++) {
            String[] date = datesList.get(i).split("-");

            BookingDate bookingDate1 = new BookingDate();
            bookingDate1.setDate(date[0]);
            bookingDate1.setMonth(date[1]);
            bookingDate1.setYear(date[2]);
            bookingDate1.setDay(date[3]);

            bookingDates.add(bookingDate1);
        }
        dateAdapter = new DateAdapter(activity, bookingDates);

        recyclerViewDates.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewDates.setAdapter(dateAdapter);
    }

    @SuppressLint("SetTextI18n")
    public void PlaceOrderProcess(Activity activity) {
        if (deliveryDay.length() == 0) {
            Toast.makeText(activity, getString(R.string.select_delivery_day), Toast.LENGTH_SHORT).show();
            return;
        } else if (deliveryTime.length() == 0) {
            Toast.makeText(activity, getString(R.string.select_delivery_time), Toast.LENGTH_SHORT).show();
            return;
        } else if (paymentMethod.isEmpty()) {
            Toast.makeText(activity, getString(R.string.select_payment_method), Toast.LENGTH_SHORT).show();
            return;
        }
        sendParams = new HashMap<>();
        sendParams.put(Constant.PLACE_ORDER, Constant.GetVal);
        sendParams.put(Constant.USER_ID, session.getData(Constant.ID));
        sendParams.put(Constant.PRODUCT_VARIANT_ID, String.valueOf(variantIdList));
        sendParams.put(Constant.QUANTITY, String.valueOf(qtyList));
        sendParams.put(Constant.TOTAL, "" + total);
        sendParams.put(Constant.DELIVERY_CHARGE, "" + Constant.SETTING_DELIVERY_CHARGE);
        sendParams.put(Constant.KEY_WALLET_BALANCE, String.valueOf(usedBalance));
        sendParams.put(Constant.KEY_WALLET_USED, chWallet.getTag().toString());
        sendParams.put(Constant.FINAL_TOTAL, "" + subtotal);
        sendParams.put(Constant.PAYMENT_METHOD, paymentMethod);
        if (!pCode.isEmpty()) {
            sendParams.put(Constant.PROMO_CODE, pCode);
            sendParams.put(Constant.PROMO_DISCOUNT, ApiConfig.StringFormat("" + pCodeDiscount));
        }
        sendParams.put(Constant.DELIVERY_TIME, (deliveryDay + " - " + deliveryTime));
        sendParams.put(Constant.ADDRESS_ID, Constant.selectedAddressId);

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.dialog_order_confirm, null);
        alertDialog.setView(dialogView);
        alertDialog.setCancelable(true);
        final AlertDialog dialog = alertDialog.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        TextView tvDialogCancel, tvDialogConfirm, tvDialogItemTotal, tvDialogDeliveryCharge, tvDialogTotal, tvDialogPCAmount, tvDialogWallet, tvDialogFinalTotal;
        LinearLayout lytDialogPromo, lytDialogWallet;
        EditText tvSpecialNote;

        lytDialogPromo = dialogView.findViewById(R.id.lytDialogPromo);
        lytDialogWallet = dialogView.findViewById(R.id.lytDialogWallet);
        tvDialogItemTotal = dialogView.findViewById(R.id.tvDialogItemTotal);
        tvDialogDeliveryCharge = dialogView.findViewById(R.id.tvDialogDeliveryCharge);
        tvDialogTotal = dialogView.findViewById(R.id.tvDialogTotal);
        tvDialogPCAmount = dialogView.findViewById(R.id.tvDialogPCAmount);
        tvDialogWallet = dialogView.findViewById(R.id.tvDialogWallet);
        tvDialogFinalTotal = dialogView.findViewById(R.id.tvDialogFinalTotal);
        tvDialogCancel = dialogView.findViewById(R.id.tvDialogCancel);
        tvDialogConfirm = dialogView.findViewById(R.id.tvDialogConfirm);
        tvSpecialNote = dialogView.findViewById(R.id.tvSpecialNote);

        if (pCodeDiscount > 0) {
            lytDialogPromo.setVisibility(View.VISIBLE);
            tvDialogPCAmount.setText("- " + session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + pCodeDiscount));
        } else {
            lytDialogPromo.setVisibility(View.GONE);
        }

        if (chWallet.getTag().toString().equals("true")) {
            lytDialogWallet.setVisibility(View.VISIBLE);
            tvDialogWallet.setText("- " + session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + usedBalance));
        } else {
            lytDialogWallet.setVisibility(View.GONE);
        }

        tvDialogItemTotal.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + total));
        tvDialogDeliveryCharge.setText(Constant.SETTING_DELIVERY_CHARGE > 0 ? session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + Constant.SETTING_DELIVERY_CHARGE) : getString(R.string.free));
        tvDialogTotal.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + (total + Constant.SETTING_DELIVERY_CHARGE)));
        tvDialogFinalTotal.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + subtotal));

        tvDialogConfirm.setOnClickListener(v -> {
            tvDialogConfirm.setClickable(false);
            tvDialogCancel.setClickable(false);
            Toast.makeText(activity, "Please wait while we are placing your order.!", Toast.LENGTH_SHORT).show();
            sendParams.put(Constant.ORDER_NOTE, tvSpecialNote.getText().toString().trim());
            if (paymentMethod.equals("cod")) {
                ApiConfig.RequestToVolley((result, response) -> {
                    if (result) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean(Constant.ERROR)) {
                                if (chWallet.getTag().toString().equals("true")) {
                                    ApiConfig.getWalletBalance(activity, session);
                                }
                                dialog.dismiss();

                                Intent intent = new Intent(activity, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra(Constant.FROM, "payment_success");
                                activity.startActivity(intent);
                            } else {
                                Toast.makeText(activity, object.getString(Constant.MESSAGE), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            PaymentActivity.dialog_.dismiss();
                        }
                    }
                }, activity, Constant.ORDER_PROCESS_URL, sendParams, true);
            } else if (paymentMethod.equals(Constant.WALLET)) {

                if (isSmallBonus == 0)
                    orderPlace(dialog);
                else
                    startSslCommerzPayment(activity, sendParams.get(Constant.FINAL_TOTAL), System.currentTimeMillis() + Constant.randomNumeric(3), sendParams);

            } else {
                sendParams.put(Constant.USER_NAME, session.getData(Constant.NAME));
                if (paymentMethod.equals("AAMRAPAY")) {
                    dialog.dismiss();
                    startSslCommerzPayment(activity, sendParams.get(Constant.FINAL_TOTAL), System.currentTimeMillis() + Constant.randomNumeric(3), sendParams);
                }
            }
        });

        tvDialogCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void orderPlace(AlertDialog dialog) {
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject object = new JSONObject(response);
                    if (!object.getBoolean(Constant.ERROR)) {
                        if (chWallet.getTag().toString().equals("true")) {
                            ApiConfig.getWalletBalance(activity, session);
                        }
                        dialog.dismiss();

                        Intent intent = new Intent(activity, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(Constant.FROM, "payment_success");
                        activity.startActivity(intent);
                    } else {
                        Toast.makeText(activity, object.getString(Constant.MESSAGE), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    PaymentActivity.dialog_.dismiss();
                }
            }
        }, activity, Constant.ORDER_PROCESS_URL, sendParams, true);
    }

    private AlertDialog alertDialog;

    public void startSslCommerzPayment(Activity activity, String amount, String transId, Map<String, String> sendParams) {
        DialogBuilder dialogBuilder = new DialogBuilder(activity, alertDialog);
        AamarPay aamarPay = new AamarPay(activity, Constant.AAMRAPAY_STORE_ID, Constant.AAMRAPAY_SIGNATURE_KEY);
        aamarPay.testMode(Constant.TEST_MODE);
        aamarPay.autoGenerateTransactionID(true);

        String trxID = aamarPay.generate_trx_id();

        dialogBuilder.showLoading();

        aamarPay.setTransactionParameter(amount, "BDT", "purchase" + trxID + transId);
        aamarPay.setCustomerDetails(session.getData(Constant.NAME), "demo@gmail.com", session.getData(Constant.MOBILE),
                session.getData(Constant.ADDRESS), "Demo City", "Bangladesh");
        aamarPay.initPGW(new AamarPay.onInitListener() {
            @Override
            public void onInitFailure(Boolean error, String message) {
                dialogBuilder.dismissDialog();
                Log.d("onPaymentFailure", message);
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPaymentSuccess(JSONObject jsonObject) {
                Log.d("success", jsonObject.toString());

                try {
                    String pg_txnid = jsonObject.getString("pg_txnid");
                    String mer_txnid = jsonObject.getString("mer_txnid");
                    String risk_title = jsonObject.getString("risk_title");
                    String risk_level = jsonObject.getString("risk_level");
                    String merchant_id = jsonObject.getString("merchant_id");
                    String store_id = jsonObject.getString("store_id");
                    String amount = jsonObject.getString("amount");
                    String amount_bdt = jsonObject.getString("amount_bdt");
                    String pay_status = jsonObject.getString("pay_status");
                    String status_code = jsonObject.getString("status_code");
                    String status_title = jsonObject.getString("status_title");
                    String cardnumber = jsonObject.getString("cardnumber");
                    String approval_code = jsonObject.getString("approval_code");
                    String payment_processor = jsonObject.getString("payment_processor");
                    String bank_trxid = jsonObject.getString("bank_trxid");
                    String payment_type = jsonObject.getString("payment_type");
                    String error_code = jsonObject.getString("error_code");
                    String error_title = jsonObject.getString("error_title");
                    String bin_country = jsonObject.getString("bin_country");
                    String bin_issuer = jsonObject.getString("bin_issuer");
                    String bin_cardtype = jsonObject.getString("bin_cardtype");
                    String bin_cardcategory = jsonObject.getString("bin_cardcategory");
                    String date = jsonObject.getString("date");
                    String date_processed = jsonObject.getString("date_processed");
                    String amount_currency = jsonObject.getString("amount_currency");
                    String rec_amount = jsonObject.getString("rec_amount");
                    String processing_ratio = jsonObject.getString("processing_ratio");
                    String processing_charge = jsonObject.getString("processing_charge");
                    String ip = jsonObject.getString("ip");
                    String currency = jsonObject.getString("currency");
                    String currency_merchant = jsonObject.getString("currency_merchant");
                    String convertion_rate = jsonObject.getString("convertion_rate");
                    String opt_a = jsonObject.getString("opt_a");
                    String opt_b = jsonObject.getString("opt_b");
                    String opt_c = jsonObject.getString("opt_c");
                    String opt_d = jsonObject.getString("opt_d");
                    String verify_status = jsonObject.getString("verify_status");
                    String call_type = jsonObject.getString("call_type");
                    String email_send = jsonObject.getString("email_send");
                    String doc_recived = jsonObject.getString("doc_recived");
                    String checkout_status = jsonObject.getString("checkout_status");

                    PlaceOrder(activity, "AAMRAPAY", pg_txnid, true, sendParams, "AAMRAPAY");

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("JSONException", e.toString());
                }

            }

            @Override
            public void onPaymentFailure(JSONObject jsonObject) {
                dialogBuilder.dismissDialog();
                Log.d("onPaymentFailure", jsonObject.toString());
                Toast.makeText(activity, "Payment Failed, Please try again latter.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPaymentProcessingFailed(JSONObject jsonObject) {
                dialogBuilder.dismissDialog();
                Log.d("onPaymentFailure", jsonObject.toString());
                Toast.makeText(activity, "Payment Processing Failed, Please try again latter.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPaymentCancel(JSONObject jsonObject) {
                dialogBuilder.dismissDialog();
                Log.d("onPaymentFailure", jsonObject.toString());
                Toast.makeText(activity, "Payment Canceled, Please try again latter.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void PlaceOrder(final Activity activity, final String paymentType, final String txnid, boolean issuccess, final Map<String, String> sendParams, final String payType) {
        DialogBuilder dialogBuilder = new DialogBuilder(activity, alertDialog);
        dialogBuilder.showLoading();
        if (issuccess) {
            ApiConfig.RequestToVolley((result, response) -> {
                if (result) {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (!object.getBoolean(Constant.ERROR)) {
                            sendParams.put(Constant.ORDER_ID, object.getString(Constant.ORDER_ID));
                            AddTransaction(activity, paymentType, txnid, payType, activity.getString(R.string.order_success), sendParams, dialogBuilder);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("exception", e.getMessage());
                    }
                }
            }, activity, Constant.ORDER_PROCESS_URL, sendParams, false);
        } else {
            AddTransaction(activity, "RazorPay", txnid, payType, getString(R.string.order_failed), sendParams, dialogBuilder);
        }
    }

    public void AddTransaction(Activity activity, String paymentType, String txnid, final String status, String message, Map<String, String> sendParams, DialogBuilder dialogBuilder) {
        Map<String, String> transparams = new HashMap<>();
        transparams.put(Constant.ADD_TRANSACTION, Constant.GetVal);
        transparams.put(Constant.USER_ID, sendParams.get(Constant.USER_ID));
        transparams.put(Constant.ORDER_ID, sendParams.get(Constant.ORDER_ID));
        transparams.put(Constant.TYPE, paymentType);
        transparams.put(Constant.TAX_PERCENT, "" + Constant.SETTING_TAX);
        transparams.put(Constant.TRANS_ID, txnid);
        transparams.put(Constant.AMOUNT, sendParams.get(Constant.FINAL_TOTAL));
        transparams.put(Constant.STATUS, status);
        transparams.put(Constant.MESSAGE, message);
        Date c = Calendar.getInstance().getTime();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        transparams.put("transaction_date", df.format(c));

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        if (!status.equals(Constant.FAILED)) {
                            try {
                                if (PaymentActivity.dialog_ != null) {
                                    PaymentActivity.dialog_.dismiss();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            dialogBuilder.dismissDialog();

                            Intent intent = new Intent(activity, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra(Constant.FROM, "payment_success");
                            activity.startActivity(intent);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    dialogBuilder.dismissDialog();
                    Log.d("exception", e.getMessage());
                }
            }
        }, activity, Constant.ORDER_PROCESS_URL, transparams, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null && data.getStringExtra("response") != null) {
            try {
                JSONObject details = new JSONObject(data.getStringExtra("response"));
                JSONObject jsonObject = details.getJSONObject(Constant.DATA);

                if (resultCode == RavePayActivity.RESULT_SUCCESS) {
                    Toast.makeText(activity, getString(R.string.order_placed1), Toast.LENGTH_LONG).show();
                    new PaymentModelClass(activity).PlaceOrder(activity, getString(R.string.flutterwave), jsonObject.getString("txRef"), true, sendParams, Constant.SUCCESS);
                } else if (resultCode == RavePayActivity.RESULT_ERROR) {
                    new PaymentModelClass(activity).PlaceOrder(activity, "", "", false, sendParams, Constant.PENDING);
                    Toast.makeText(activity, getString(R.string.order_error), Toast.LENGTH_LONG).show();
                } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                    new PaymentModelClass(activity).PlaceOrder(activity, "", "", false, sendParams, Constant.FAILED);
                    Toast.makeText(activity, getString(R.string.order_cancel), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            if (PaymentActivity.dialog_ != null) {
                PaymentActivity.dialog_.dismiss();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.payment);
        activity.invalidateOptionsMenu();
    }

    public void setAlertDialog(AlertDialog alertDialog) {
        this.alertDialog = alertDialog;
    }
}