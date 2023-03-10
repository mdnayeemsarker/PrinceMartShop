package com.princemartbd.shop.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aamarpay.library.AamarPay;
import com.aamarpay.library.DialogBuilder;
import com.princemartbd.shop.R;
import com.princemartbd.shop.helper.ApiConfig;
import com.princemartbd.shop.helper.Constant;
import com.princemartbd.shop.helper.Session;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PayNowActivity extends AppCompatActivity {
    @SuppressLint("StaticFieldLeak")
    public static Toolbar toolbar;
    private String storePhotoUrl = "";
    private String tran_id;
    private String status;
    private String shopNameStr;
    private String bonus_payment_enabled;
    private String getIntentData;
    private String slug;
    private String amountString;
    private int getSellerId;
    private TextView shopName;
    private TextView payableAmountText;
    private TextView shopEmail;

    private CheckBox chPrivacy, chBonusBalance;
    private Boolean isServer = false;

    private TextView shopPhone;
    private TextView tvBonusBalance;
    private LinearLayout bonusLL;

    private double cashback;

    private CardView shopPay, shopLogin;

    private ImageView shopPhoto;

    private Session session;
    private Activity activity;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_now);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

        getIntentData = getIntent().getStringExtra(Constant.ABMN_QR_RESULT);
        String getIntentDataTypeAmount = getIntent().getStringExtra(Constant.TYPE_AMOUNT);

        GetSellerData(getIntentData);
        activity = PayNowActivity.this;
        session = new Session(this);
        shopPay = findViewById(R.id.shopPayId);
        shopLogin = findViewById(R.id.shopLoginId);
        shopName = findViewById(R.id.shopNameId);
        shopEmail = findViewById(R.id.shopEmailId);
        shopPhone = findViewById(R.id.shopPhoneId);
        tvBonusBalance = findViewById(R.id.tvBonusBalance);
        EditText shopAmount = findViewById(R.id.shopAmountId);
        shopPhoto = findViewById(R.id.shopPhotoId);
        payableAmountText = findViewById(R.id.payableAmountTextId);
        chPrivacy = findViewById(R.id.chPrivacy);
        chBonusBalance = findViewById(R.id.chBonusBalance);
        bonusLL = findViewById(R.id.bonusLL);

        CardView cardViewHamburger = findViewById(R.id.cardViewHamburger);
        ImageView imageMenu = findViewById(R.id.imageMenu);
        ImageView imageHome = findViewById(R.id.imageHome);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);

        imageHome.setVisibility(View.GONE);
        imageMenu.setVisibility(View.VISIBLE);
        imageMenu.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_arrow_back));
        cardViewHamburger.setOnClickListener(view -> onBackPressed());

        TextView typeAmountText = findViewById(R.id.typeAmountTextId);

        tran_id = System.currentTimeMillis() + Constant.randomNumeric(3);
        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            getUserData();
            toolbarTitle.setText(getString(R.string.hi) + session.getData(Constant.NAME) + ".!");
        } else {
            tvBonusBalance.setVisibility(View.GONE);
            bonusLL.setVisibility(View.GONE);
            toolbarTitle.setText(getString(R.string.hi_user));
        }

        shopPay.setVisibility(View.GONE);

        if (getIntentDataTypeAmount.equals("0")) {
            typeAmountText.setVisibility(View.GONE);
            shopAmount.setVisibility(View.VISIBLE);
        }else {
            amountString = getIntentDataTypeAmount;
            shopAmount.setText(getIntentDataTypeAmount);
//            shopPay.setVisibility(View.VISIBLE);
            setPayBalance("Payable: " + amountString);
        }

        shopAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.equals("")) {
                    shopPay.setVisibility(View.GONE);
                    bonusLL.setVisibility(View.GONE);
                } else {
                    shopPay.setVisibility(View.VISIBLE);
                    amountString = s.toString();
                    setPayBalance("Payable: " + amountString);
                    if (session.getBoolean(Constant.IS_USER_LOGIN) && Double.parseDouble(session.getData("bonus_balance")) > 0 && bonus_payment_enabled.equals("1")) {
                        bonusLL.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        shopPay.setOnClickListener(this::pay);
        shopLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra(Constant.FROM, "physicalPay");
            intent.putExtra(Constant.ABMN_QR_RESULT, getIntentData);
            intent.putExtra(Constant.TYPE_AMOUNT, amountString);
            startActivity(intent);
        });
    }

    @SuppressLint("SetTextI18n")
    public void getUserData() {
        try {
            Map<String, String> params = new HashMap<>();
            params.put(Constant.GET_USER_DATA, Constant.GetVal);
            params.put(Constant.USER_ID, session.getData(Constant.ID));
            ApiConfig.RequestToVolley((result, response) -> {
                if (result) {
                    try {
                        JSONObject jsonObject_ = new JSONObject(response);
                        if (!jsonObject_.getBoolean(Constant.ERROR)) {
                            JSONObject jsonObject = jsonObject_.getJSONArray(Constant.DATA).getJSONObject(0);
                            Log.d("USER_DATA_URL", jsonObject.toString());
                            session.setData("bonus_balance", jsonObject.getString("bonus_balance"));

                            if (Double.parseDouble(session.getData("bonus_balance")) > 0) {
                                tvBonusBalance.setVisibility(View.VISIBLE);
                                tvBonusBalance.setText("Bonus Wallet: " + "৳" + session.getData("bonus_balance"));
                            } else {
                                tvBonusBalance.setVisibility(View.GONE);
                                bonusLL.setVisibility(View.GONE);
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, activity, Constant.USER_DATA_URL, params, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    private void setPayBalance(String payableAmount) {
        payableAmountText.setText(payableAmount);
    }

    @SuppressLint("SetTextI18n")
    private void pay(View v) {

        ApiConfig.hideKeyboard(activity, v);

        if (chPrivacy.isChecked()) {
            if (!session.getBoolean(Constant.IS_USER_LOGIN)) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
                alertDialog.setTitle("Attention");
                alertDialog.setMessage("You are not signed in user, Please login in first then start to go payment, thank you");
                alertDialog.setCancelable(false);
                alertDialog.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());

                Dialog dialog = alertDialog.create();
                dialog.show();

                shopLogin.setVisibility(View.VISIBLE);
                shopPay.setVisibility(View.GONE);
            } else {
                if (chBonusBalance.isChecked()) {
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
                    LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View dialogView = inflater.inflate(R.layout.lyt_pay_dialog, null);
                    alertDialog.setView(dialogView);
                    alertDialog.setCancelable(true);
                    final AlertDialog dialog = alertDialog.create();
                    Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    double amountInput = Double.parseDouble(amountString);
                    double bonusBalance = Double.parseDouble(session.getData("bonus_balance"));
                    double dueBal = 0;
                    dialog.show();
                    TextView tvPayableDialog = dialogView.findViewById(R.id.tvPayableDialog);
                    TextView tvDueBBDialog = dialogView.findViewById(R.id.tvDueBBDialog);
                    TextView tvDialogCancel = dialogView.findViewById(R.id.tvDialogCancel);
                    TextView tvDialogConfirm = dialogView.findViewById(R.id.tvDialogConfirm);
                    if (Integer.parseInt(amountString) > 0) {

                        if (amountInput == bonusBalance || amountInput < bonusBalance) {
                            isServer = true;
                            dueBal = bonusBalance - amountInput;
                            tvPayableDialog.setText("Payable৳0.0");
                            tvDueBBDialog.setText("Due Bonus: ৳" + dueBal);
                        } else {
                            isServer = false;
                            dueBal = amountInput - bonusBalance;
                            tvPayableDialog.setText("Payable৳" + dueBal);
                            tvDueBBDialog.setText("Due Bonus: ৳0.0");
                        }
                    } else {
                        Toast.makeText(activity, "Please input payable amount first", Toast.LENGTH_SHORT).show();
                    }

                    double finalDueBal = dueBal;
                    tvDialogConfirm.setOnClickListener(v1 -> {
                        dialog.dismiss();
                        if (isServer) {
                            Map<String, String> params = new HashMap<>();
                            params.put(Constant.SEND_REQUEST, Constant.GetVal);
                            params.put("user_id", session.getData(Constant.USER_ID));
                            params.put("seller_id", String.valueOf(getSellerId));
                            params.put("tran_id", tran_id);
                            params.put("status", "Successful");
                            params.put("cashback", "0");
                            params.put("amount", "0");
                            params.put("used_bonus", amountString);
                            putHistory(params);
                        } else {
                            payAndGet("" + finalDueBal, "" + bonusBalance);
                        }

                    });
                    tvDialogCancel.setOnClickListener(view -> dialog.dismiss());
                }
                else {
                    if (!amountString.equals("")) {
                        JSONArray offersArray = session.getJSONArray("offersArray");
                        for (int i = 0; i < offersArray.length(); i++) {
                            try {
                                JSONObject data = offersArray.getJSONObject(i);
                                String start = data.getString("start");
                                String end = data.getString("end");
                                String type = data.getString("type"); //flat/percentage
                                String amount = data.getString("amount");
                                String is_active = data.getString("is_active");
                                if (is_active.equals("1")) {
                                    Log.d("amount", Double.parseDouble(amountString) + "\n" + data);
//                                    raw with admin panel
//                                    if (Double.parseDouble(amountString) >= Double.parseDouble(start) &&
//                                            Double.parseDouble(amountString) <= Double.parseDouble(end)) {
//                                        double typeAmount = Double.parseDouble(amountString);
//                                        cashback = (typeAmount * Double.parseDouble(amount)) / 100;
//                                    }
//                                    startSslCommerzPayment();
//                                    custom logic with shahin vai
                                    if (Double.parseDouble(amountString) >= Double.parseDouble(start)) {
                                        if (Double.parseDouble(amountString) <= Double.parseDouble(end)){
                                            double typeAmount = Double.parseDouble(amountString);
                                            cashback = (typeAmount * Double.parseDouble(amount)) / 100;
                                        }else {
                                            cashback = (Double.parseDouble(end) * Double.parseDouble(amount)) / 100;
                                        }
                                    }
                                    startSslCommerzPayment();


                                }
                                setPayBalance("Payable: " + amountString);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("payOfferArray", e.getMessage());
                            }
                        }
                    }
                }
            }
        } else {
            Toast.makeText(activity, getResources().getText(R.string.alert_privacy_msg), Toast.LENGTH_SHORT).show();
        }
    }

    private void payAndGet(String payable, String used) {

        String user_id = session.getData(Constant.USER_ID);

        String seller_id = String.valueOf(getSellerId);

        status = "initialize";

        DialogBuilder dialogBuilder = new DialogBuilder(activity, alertDialog);
        AamarPay aamarPay = new AamarPay(activity, Constant.AAMRAPAY_STORE_ID, Constant.AAMRAPAY_SIGNATURE_KEY);
        aamarPay.testMode(Constant.TEST_MODE);
        aamarPay.autoGenerateTransactionID(true);

        String trxID = aamarPay.generate_trx_id();

        dialogBuilder.showLoading();

        aamarPay.setTransactionParameter(payable, "BDT", "purchase" + trxID + tran_id);
        aamarPay.setCustomerDetails(session.getData(Constant.NAME), "demo@gmail.com", session.getData(Constant.MOBILE),
                session.getData(Constant.ADDRESS), "Demo City", "Bangladesh");

        Log.d("onPaymentFailure", "customDetails " + payable + "\nBDT\n" + "purchase\n" + trxID + tran_id + "\n" + session.getData(Constant.NAME) + "\ndemo@gmail.com\n" + session.getData(Constant.MOBILE) + "\n" +
                session.getData(Constant.ADDRESS) + "\nDemo City" + "\nBangladesh");

        Map<String, String> params = new HashMap<>();
        params.put(Constant.SEND_REQUEST, Constant.GetVal);
        params.put("user_id", user_id);
        params.put("seller_id", seller_id);
        params.put("tran_id", trxID + tran_id);
        params.put("cashback", "0");
        params.put("amount", payable);
        params.put("used_bonus", used);
        aamarPay.initPGW(new AamarPay.onInitListener() {
            @Override
            public void onInitFailure(Boolean error, String message) {
                dialogBuilder.dismissDialog();
                Log.d("onPaymentFailure", "onInitFailure " + message);
                Toast.makeText(activity, "Payment Failed, Please try again latter.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPaymentSuccess(JSONObject jsonObject) {
                dialogBuilder.dismissDialog();
                Log.d("success", jsonObject.toString());

                try {
//                    String pg_txnid = jsonObject.getString("pg_txnid");
//                    String mer_txnid = jsonObject.getString("mer_txnid");
//                    String risk_title = jsonObject.getString("risk_title");
//                    String risk_level = jsonObject.getString("risk_level");
//                    String merchant_id = jsonObject.getString("merchant_id");
//                    String store_id = jsonObject.getString("store_id");
//                    String amount = jsonObject.getString("amount");
//                    String amount_bdt = jsonObject.getString("amount_bdt");
//                    String status_title = jsonObject.getString("status_title");
//                    String cardnumber = jsonObject.getString("cardnumber");
//                    String approval_code = jsonObject.getString("approval_code");
//                    String payment_processor = jsonObject.getString("payment_processor");
//                    String bank_trxid = jsonObject.getString("bank_trxid");
//                    String payment_type = jsonObject.getString("payment_type");
//                    String error_code = jsonObject.getString("error_code");
//                    String error_title = jsonObject.getString("error_title");
//                    String bin_country = jsonObject.getString("bin_country");
//                    String bin_issuer = jsonObject.getString("bin_issuer");
//                    String bin_cardtype = jsonObject.getString("bin_cardtype");
//                    String bin_cardcategory = jsonObject.getString("bin_cardcategory");
//                    String date = jsonObject.getString("date");
//                    String date_processed = jsonObject.getString("date_processed");
//                    String amount_currency = jsonObject.getString("amount_currency");
//                    String rec_amount = jsonObject.getString("rec_amount");
//                    String processing_ratio = jsonObject.getString("processing_ratio");
//                    String processing_charge = jsonObject.getString("processing_charge");
//                    String ip = jsonObject.getString("ip");
//                    String currency = jsonObject.getString("currency");
//                    String currency_merchant = jsonObject.getString("currency_merchant");
//                    String convertion_rate = jsonObject.getString("convertion_rate");
//                    String opt_a = jsonObject.getString("opt_a");
//                    String opt_b = jsonObject.getString("opt_b");
//                    String opt_c = jsonObject.getString("opt_c");
//                    String opt_d = jsonObject.getString("opt_d");
//                    String verify_status = jsonObject.getString("verify_status");
//                    String call_type = jsonObject.getString("call_type");
//                    String email_send = jsonObject.getString("email_send");
//                    String doc_recived = jsonObject.getString("doc_recived");
//                    String checkout_status = jsonObject.getString("checkout_status");
                    String pay_status = jsonObject.getString("pay_status");
                    String status_code = jsonObject.getString("status_code");

                    status = pay_status;
                    params.put("status", status);

                    if (status_code.equals("2") && status.equals("Successful")) {
                        putHistory2(params);
                    } else {
                        Toast.makeText(activity, "Payment Unsuccessful, Please try again latter.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("JSONException", e.toString());
                }
            }

            @Override
            public void onPaymentFailure(JSONObject jsonObject) {
                dialogBuilder.dismissDialog();
                Log.d("onPaymentFailure", "onPaymentFailure " + jsonObject.toString());
                Toast.makeText(activity, "Payment Failed, Please try again latter.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPaymentProcessingFailed(JSONObject jsonObject) {
                dialogBuilder.dismissDialog();
                Log.d("onPaymentFailure", "onPaymentProcessingFailed" + jsonObject.toString());
                Toast.makeText(activity, "Payment Processing Failed, Please try again latter.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPaymentCancel(JSONObject jsonObject) {
                dialogBuilder.dismissDialog();
                Log.d("onPaymentFailure", "onPaymentCancel" + jsonObject.toString());
                Toast.makeText(activity, "Payment Canceled, Please try again latter.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void putHistory2(Map<String, String> params) {
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject object = new JSONObject(response);
                    if (!object.getBoolean(Constant.ERROR)) {
                        startActivity(new Intent(activity, PayHistoryActivity.class));
//                        startActivity(new Intent(activity, MainActivity.class).putExtra(Constant.FROM, ""));
                        Toast.makeText(activity, "Payment Complete", Toast.LENGTH_SHORT).show();
                        Log.d("response", object.getString(Constant.MESSAGE));
                        session.setData("bonus_balance", object.getString("bonus_balance"));
                    }
                    Toast.makeText(activity, object.getString(Constant.MESSAGE), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("response", response);
            }
        }, activity, Constant.GET_PAY_REQUEST_URL, params, true);
    }

    //  <  >
    //        params.put("cashback", ""+bonusBalance);
    private AlertDialog alertDialog;

    public void startSslCommerzPayment() {
        String user_id = session.getData(Constant.USER_ID);

        String seller_id = String.valueOf(getSellerId);

        status = "initialize";

        DialogBuilder dialogBuilder = new DialogBuilder(activity, alertDialog);
        AamarPay aamarPay = new AamarPay(activity, Constant.AAMRAPAY_STORE_ID, Constant.AAMRAPAY_SIGNATURE_KEY);
        aamarPay.testMode(Constant.TEST_MODE);
        aamarPay.autoGenerateTransactionID(true);

        String trxID = aamarPay.generate_trx_id();

        dialogBuilder.showLoading();

        aamarPay.setTransactionParameter(amountString, "BDT", "purchase" + trxID + tran_id);
        aamarPay.setCustomerDetails(session.getData(Constant.NAME), "demo@gmail.com", session.getData(Constant.MOBILE),
                session.getData(Constant.ADDRESS), "Demo City", "Bangladesh");

        Log.d("onPaymentFailure", "customDetails " + amountString + "\nBDT\n" + "purchase\n" + trxID + tran_id + "\n" + session.getData(Constant.NAME) + "\ndemo@gmail.com\n" + session.getData(Constant.MOBILE) + "\n" +
                session.getData(Constant.ADDRESS) + "\nDemo City" + "\nBangladesh");

        Map<String, String> params = new HashMap<>();
        params.put(Constant.SEND_REQUEST, Constant.GetVal);
        params.put("user_id", user_id);
        params.put("seller_id", seller_id);
        params.put("tran_id", trxID + tran_id);
        params.put("cashback", "" + cashback);
        params.put("amount", amountString);
        aamarPay.initPGW(new AamarPay.onInitListener() {
            @Override
            public void onInitFailure(Boolean error, String message) {
                dialogBuilder.dismissDialog();
                Log.d("onPaymentFailure", "onInitFailure " + message);
                Toast.makeText(activity, "Payment Failed, Please try again latter.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPaymentSuccess(JSONObject jsonObject) {
                dialogBuilder.dismissDialog();
                Log.d("success", jsonObject.toString());

                try {
//                    String pg_txnid = jsonObject.getString("pg_txnid");
//                    String mer_txnid = jsonObject.getString("mer_txnid");
//                    String risk_title = jsonObject.getString("risk_title");
//                    String risk_level = jsonObject.getString("risk_level");
//                    String merchant_id = jsonObject.getString("merchant_id");
//                    String store_id = jsonObject.getString("store_id");
//                    String amount = jsonObject.getString("amount");
//                    String amount_bdt = jsonObject.getString("amount_bdt");
//                    String status_title = jsonObject.getString("status_title");
//                    String cardnumber = jsonObject.getString("cardnumber");
//                    String approval_code = jsonObject.getString("approval_code");
//                    String payment_processor = jsonObject.getString("payment_processor");
//                    String bank_trxid = jsonObject.getString("bank_trxid");
//                    String payment_type = jsonObject.getString("payment_type");
//                    String error_code = jsonObject.getString("error_code");
//                    String error_title = jsonObject.getString("error_title");
//                    String bin_country = jsonObject.getString("bin_country");
//                    String bin_issuer = jsonObject.getString("bin_issuer");
//                    String bin_cardtype = jsonObject.getString("bin_cardtype");
//                    String bin_cardcategory = jsonObject.getString("bin_cardcategory");
//                    String date = jsonObject.getString("date");
//                    String date_processed = jsonObject.getString("date_processed");
//                    String amount_currency = jsonObject.getString("amount_currency");
//                    String rec_amount = jsonObject.getString("rec_amount");
//                    String processing_ratio = jsonObject.getString("processing_ratio");
//                    String processing_charge = jsonObject.getString("processing_charge");
//                    String ip = jsonObject.getString("ip");
//                    String currency = jsonObject.getString("currency");
//                    String currency_merchant = jsonObject.getString("currency_merchant");
//                    String convertion_rate = jsonObject.getString("convertion_rate");
//                    String opt_a = jsonObject.getString("opt_a");
//                    String opt_b = jsonObject.getString("opt_b");
//                    String opt_c = jsonObject.getString("opt_c");
//                    String opt_d = jsonObject.getString("opt_d");
//                    String verify_status = jsonObject.getString("verify_status");
//                    String call_type = jsonObject.getString("call_type");
//                    String email_send = jsonObject.getString("email_send");
//                    String doc_recived = jsonObject.getString("doc_recived");
//                    String checkout_status = jsonObject.getString("checkout_status");
                    String pay_status = jsonObject.getString("pay_status");
                    String status_code = jsonObject.getString("status_code");

                    status = pay_status;
                    params.put("status", status);

                    if (status_code.equals("2") && status.equals("Successful")) {
                        putHistory(params);
                    } else {
                        Toast.makeText(activity, "Payment Unsuccessful, Please try again latter.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("JSONException", e.toString());
                }
            }

            @Override
            public void onPaymentFailure(JSONObject jsonObject) {
                dialogBuilder.dismissDialog();
                Log.d("onPaymentFailure", "onPaymentFailure " + jsonObject.toString());
                Toast.makeText(activity, "Payment Failed, Please try again latter.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPaymentProcessingFailed(JSONObject jsonObject) {
                dialogBuilder.dismissDialog();
                Log.d("onPaymentFailure", "onPaymentProcessingFailed" + jsonObject.toString());
                Toast.makeText(activity, "Payment Processing Failed, Please try again latter.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPaymentCancel(JSONObject jsonObject) {
                dialogBuilder.dismissDialog();
                Log.d("onPaymentFailure", "onPaymentCancel" + jsonObject.toString());
                Toast.makeText(activity, "Payment Canceled, Please try again latter.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void putHistory(Map<String, String> params) {

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject object = new JSONObject(response);
                    if (!object.getBoolean(Constant.ERROR)) {
                        startActivity(new Intent(activity, PayHistoryActivity.class));
//                        startActivity(new Intent(activity, MainActivity.class).putExtra(Constant.FROM, ""));
                        Toast.makeText(activity, "Payment Complete", Toast.LENGTH_SHORT).show();
                        Log.d("response", object.getString(Constant.MESSAGE));
                        session.setData("bonus_balance", object.getString("bonus_balance"));
                    }
                    Toast.makeText(activity, object.getString(Constant.MESSAGE), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("response", response);
            }
        }, activity, Constant.GET_PAY_REQUEST_URL, params, true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class).putExtra(Constant.FROM, ""));
    }

    @SuppressLint("SetTextI18n")
    public void GetSellerData(String getData) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_SELLER_DATA, Constant.GetVal);
        params.put(Constant.SELLER_SLUG, getData);

        Log.d("getData", getData);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject object = new JSONObject(response);

                    if (!object.getBoolean(Constant.ERROR)) {
                        JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                        boolean hasSeller = false;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Log.e("response", String.valueOf(jsonObject));
                            slug = jsonObject.getString("slug");
                            if (slug.equals(getData)) {

                                hasSeller = true;
                                getSellerId = jsonObject.getInt("id");

//                                String tmp = jsonObject.getString("discount");
//                                discountDouble = Double.parseDouble(tmp);

                                shopNameStr = jsonObject.getString("store_name");
                                bonus_payment_enabled = jsonObject.getString("bonus_payment_enabled");

                                shopName.setText(shopNameStr);
                                shopEmail.setText(jsonObject.getString("email"));
                                shopPhone.setText(jsonObject.getString("mobile"));
                                storePhotoUrl = jsonObject.getString("logo");
                                Picasso.get().load(storePhotoUrl).fit().centerInside().placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).into(shopPhoto);

//                                if (!getIntentDataTypeAmount.equals("0")) {
//                                    typeAmountText.setVisibility(View.VISIBLE);
//                                    shopAmount.setVisibility(View.GONE);
//                                    typeAmountText.setText(getIntentDataTypeAmount);
//                                    typeAmount = Double.parseDouble(getIntentDataTypeAmount);
//                                    discountAmount = (typeAmount * discountDouble) / 100;
//                                    setDisBalance("Discount: " + discountAmount);
//
//                                    payableAmount = typeAmount - discountAmount;
//                                    setPayBalance("Payable: " + payableAmount);
//                                    shopPay.setVisibility(View.VISIBLE);
//                                }
                            }
                        }
                        if (!hasSeller) {
                            getDialog();
                        }
                    }
                    Log.d("physical_pay", response);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(activity, "E: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, this, Constant.GET_SELLER_DATA_URL, params, true);
    }

    private void getDialog() {
        final androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(activity);
        alertDialog.setTitle("Attention");
        alertDialog.setMessage("Seller Not Found");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("OK", (dialog, which) -> {

            Intent i = new Intent(activity, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra(Constant.FROM, "");
            activity.startActivity(i);
            activity.finish();
        });
        alertDialog.show();
    }
}