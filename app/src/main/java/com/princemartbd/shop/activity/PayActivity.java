package com.princemartbd.shop.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
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

public class PayActivity extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    public static Toolbar toolbar;
    private String storePhotoUrl = "";
    private String tran_id;
    private String status;
    private String shopNameStr;
    private String getIntentDataTypeAmount;
    private int getSellerId;
    private TextView shopName;
    private TextView payableAmountText;
    private TextView typeAmountText;
    private TextView discountText;
    private TextView shopEmail;

    private CheckBox chPrivacy;

    private TextView shopPhone;
    private EditText shopAmount;

    private double discountDouble = 0;
    private double typeAmount = 0;
    private double discountAmount;
    private double payableAmount;

    private CardView shopPay, shopLogin;

    private ImageView shopPhoto;

    private Session session;
    private final Activity activity = PayActivity.this;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

        session = new Session(this);

        String getIntentData = getIntent().getStringExtra(Constant.ABMN_QR_RESULT);
        getIntentDataTypeAmount = getIntent().getStringExtra(Constant.TYPE_AMOUNT);

        GetSellerData(getIntentData);

        shopPay = findViewById(R.id.shopPayId);
        shopLogin = findViewById(R.id.shopLoginId);
        shopName = findViewById(R.id.shopNameId);
        shopEmail = findViewById(R.id.shopEmailId);
        shopPhone = findViewById(R.id.shopPhoneId);
        shopAmount = findViewById(R.id.shopAmountId);
        shopPhoto = findViewById(R.id.shopPhotoId);
        payableAmountText = findViewById(R.id.payableAmountTextId);
        discountText = findViewById(R.id.discountTextId);
        chPrivacy = findViewById(R.id.chPrivacy);

        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        typeAmountText = findViewById(R.id.typeAmountTextId);

        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            toolbarTitle.setText(getString(R.string.hi) + session.getData(Constant.NAME) + ".!");
        } else {
            toolbarTitle.setText(getString(R.string.hi_user));
        }

        shopPay.setVisibility(View.GONE);

        if (getIntentDataTypeAmount.equals("0")) {
            typeAmountText.setVisibility(View.GONE);
            shopAmount.setVisibility(View.VISIBLE);
        }

        tran_id = System.currentTimeMillis() + Constant.randomNumeric(3);

        shopAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.equals("")) {

                    String amountString = s.toString();

                    if (!amountString.equals("")) {
                        typeAmount = Double.parseDouble(amountString);
                        discountAmount = (typeAmount * discountDouble) / 100;
                        discountText.setText(String.valueOf(discountAmount));

                        payableAmount = typeAmount - discountAmount;
                        payableAmountText.setText(String.valueOf(payableAmount));

                        if (payableAmount > 10) {
                            shopPay.setVisibility(View.VISIBLE);
                        } else {
                            shopPay.setVisibility(View.GONE);
                        }
                    } else {
                        discountText.setText("00.00");
                        payableAmountText.setText("00.00");
                    }
                } else {
                    shopPay.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        shopPay.setOnClickListener(v -> pay());
        shopLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra(Constant.FROM, "physicalPay");
            intent.putExtra(Constant.ABMN_QR_RESULT, getIntentData);
            intent.putExtra(Constant.TYPE_AMOUNT, String.valueOf(typeAmount));
            startActivity(intent);
        });
    }

    private void pay() {
        if (chPrivacy.isChecked()) {
            if (!session.getBoolean(Constant.IS_USER_LOGIN)) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(PayActivity.this);
                alertDialog.setTitle("Attention");
                alertDialog.setMessage("You are not signed in user, Please login in first then start to go payment, thank you");
                alertDialog.setCancelable(false);
                alertDialog.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());

                Dialog dialog = alertDialog.create();
                dialog.show();

                shopLogin.setVisibility(View.VISIBLE);
                shopPay.setVisibility(View.GONE);
            } else {
                startSslCommerzPayment();
            }
        }else {
            Toast.makeText(activity, getResources().getText(R.string.alert_privacy_msg), Toast.LENGTH_SHORT).show();
        }
    }

    public void startSslCommerzPayment() {
        String user_id = session.getData(Constant.USER_ID);

        String seller_id = String.valueOf(getSellerId);
        String discount = String.valueOf(discountAmount);
        String amount = String.valueOf(payableAmount);

        status = "initialize";

        Map<String, String> params = new HashMap<>();
        params.put(Constant.SEND_REQUEST, Constant.GetVal);
        params.put("user_id", user_id);
        params.put("seller_id", seller_id);
        params.put("tran_id", tran_id);
        params.put("discount", discount);
        params.put("amount", amount);

        DialogBuilder dialogBuilder = new DialogBuilder(activity, null);
        AamarPay aamarPay = new AamarPay(activity, Constant.AAMRAPAY_STORE_ID, Constant.AAMRAPAY_SIGNATURE_KEY);
        aamarPay.testMode(Constant.TEST_MODE);
        aamarPay.autoGenerateTransactionID(true);

        String trxID = aamarPay.generate_trx_id();

        dialogBuilder.showLoading();

        String transId = System.currentTimeMillis() + Constant.randomNumeric(3);
        aamarPay.setTransactionParameter(amount, "BDT", "purchase" + trxID + transId);
        aamarPay.setCustomerDetails(session.getData(Constant.NAME), "demo@gmail.com", session.getData(Constant.MOBILE),
                session.getData(Constant.ADDRESS), "Demo City", "Bangladesh");

//        DialogBuilder dialogBuilder = new DialogBuilder(activity, alertDialog);
//        AamarPay aamarPay = new AamarPay(activity, "aamarpay", "28c78bb1f45112f5d40b956fe104645a");
//        aamarPay.testMode(true);
//        // Auto generate Trx
//        aamarPay.autoGenerateTransactionID(true);
//
//        // Generate unique transaction id
//        String trxID = aamarPay.generate_trx_id();
//
//        dialogBuilder.showLoading();
//        aamarPay.setTransactionParameter(amount, "BDT", "pay" + trxID);
//        aamarPay.setCustomerDetails(session.getData(Constant.NAME), "nayeem@gmail.com", session.getData(Constant.MOBILE),
//                "session.getData(Constant.ADDRESS)", "Default", "Bangladesh");

        aamarPay.initPGW(new AamarPay.onInitListener() {
            @Override
            public void onInitFailure(Boolean error, String message) {
                dialogBuilder.dismissDialog();
                Log.d("onInitFailure", message);
                Toast.makeText(activity, "Payment Failed, Please try again latter.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPaymentSuccess(JSONObject jsonObject) {
                dialogBuilder.dismissDialog();
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

                    status = pay_status;
                    params.put("status", status);

                    if (status_code.equals("2") && status.equals("Successful")){
                        putStory(params);
                    }else {
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
                Log.d("onPaymentFailure", jsonObject.toString());
                Toast.makeText(activity, "Payment Failed, Please try again latter.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPaymentProcessingFailed(JSONObject jsonObject) {
                dialogBuilder.dismissDialog();
                Log.d("onPaymentProcessingFailed", jsonObject.toString());
                Toast.makeText(activity, "Payment Processing Failed, Please try again latter.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPaymentCancel(JSONObject jsonObject) {
                dialogBuilder.dismissDialog();
                Log.d("onPaymentCancel", jsonObject.toString());
                Toast.makeText(activity, "Payment Canceled, Please try again latter.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void putStory(Map<String, String> params) {

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject object = new JSONObject(response);
                    if (!object.getBoolean(Constant.ERROR)) {
                        startActivity(new Intent(PayActivity.this, PayHistoryActivity.class));
                        Log.d("response", object.getString(Constant.MESSAGE));
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

    public void GetSellerData(String getData) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_SELLER_DATA, Constant.GetVal);
        params.put(Constant.SELLER_SLUG, getData);

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
                            if (jsonObject.getString("slug").equals(getData)) {

                                hasSeller = true;
                                getSellerId = jsonObject.getInt("id");

                                String tmp = jsonObject.getString("discount");

                                discountDouble = Double.parseDouble(tmp);

                                shopNameStr = jsonObject.getString("store_name");

                                shopName.setText(shopNameStr);
                                shopEmail.setText(jsonObject.getString("email"));
                                shopPhone.setText(jsonObject.getString("mobile"));
                                storePhotoUrl = jsonObject.getString("logo");
                                Picasso.get().load(storePhotoUrl).fit().centerInside().placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).into(shopPhoto);

                                if (!getIntentDataTypeAmount.equals("0")) {
                                    typeAmountText.setVisibility(View.VISIBLE);
                                    shopAmount.setVisibility(View.GONE);
                                    typeAmountText.setText(getIntentDataTypeAmount);
                                    typeAmount = Double.parseDouble(getIntentDataTypeAmount);
                                    discountAmount = (typeAmount * discountDouble) / 100;
                                    discountText.setText(String.valueOf(discountAmount));

                                    payableAmount = typeAmount - discountAmount;
                                    payableAmountText.setText(String.valueOf(payableAmount));
                                    shopPay.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                        if (!hasSeller) {
                            getDialog();
                        }
                    }
                    Log.d("physical_pay", response);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(PayActivity.this, "E: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, this, Constant.GET_SELLER_DATA_URL, params, true);
    }

    private void getDialog() {
        final androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(PayActivity.this);
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