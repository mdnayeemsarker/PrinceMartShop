package com.princemartbd.shop.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.princemartbd.shop.R;
import com.princemartbd.shop.helper.ApiConfig;
import com.princemartbd.shop.helper.Constant;
import com.princemartbd.shop.helper.DatabaseHelper;
import com.princemartbd.shop.helper.Session;
import com.princemartbd.shop.helper.Utils;
import com.princemartbd.shop.model.RanNumGen;
import com.princemartbd.shop.ui.PinView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    LinearLayout lytOTP;
    EditText edtResetPass, edtResetCPass, edtRefer, imgLoginPassword, edtLoginMobile, edtName, edtEmail, edtPassword, edtConfirmPassword, edtMobileVerify;
    Button btnVerify;
    PinView pinViewOTP;
    TextView tvMobile, tvWelcome, tvTimer, tvResend, tvForgotPass, tvPrivacyPolicy;
    ScrollView lytLogin, lytSignUp, lytVerify, lytResetPass;
    Session session;
    Toolbar toolbar;
    CheckBox chPrivacy;
    Animation animShow, animHide;

    String phoneNumber, otpFor = "";
    boolean resendOTP = false;

    DatabaseHelper databaseHelper;
    Activity activity;
    boolean timerOn;
    ImageView img;
    RelativeLayout lytWebView;
    WebView webView;
    String from, mobile, countryCode, abmn_qrResult, type_amount;
    boolean forMultipleCountryUse = true;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        activity = LoginActivity.this;
        toolbar = findViewById(R.id.toolbar);
        session = new Session(activity);
        databaseHelper = new DatabaseHelper(activity);

        toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        animShow = AnimationUtils.loadAnimation(this, R.anim.view_show);
        animHide = AnimationUtils.loadAnimation(this, R.anim.view_hide);

        from = getIntent().getStringExtra(Constant.FROM);
        abmn_qrResult = getIntent().getStringExtra(Constant.ABMN_QR_RESULT);
        type_amount = getIntent().getStringExtra(Constant.TYPE_AMOUNT);

        chPrivacy = findViewById(R.id.chPrivacy);
        tvWelcome = findViewById(R.id.tvWelcome);
        edtResetPass = findViewById(R.id.edtResetPass);
        edtResetCPass = findViewById(R.id.edtResetCPass);
        imgLoginPassword = findViewById(R.id.imgLoginPassword);
        edtLoginMobile = findViewById(R.id.edtLoginMobile);
        lytLogin = findViewById(R.id.lytLogin);
        lytResetPass = findViewById(R.id.lytResetPass);
        lytOTP = findViewById(R.id.lytOTP);
        pinViewOTP = findViewById(R.id.pinViewOTP);
        btnVerify = findViewById(R.id.btnVerify);
        edtMobileVerify = findViewById(R.id.edtMobileVerify);
        lytVerify = findViewById(R.id.lytVerify);
        lytSignUp = findViewById(R.id.lytSignUp);
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        tvMobile = findViewById(R.id.tvMobile);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtRefer = findViewById(R.id.edtRefer);
        tvResend = findViewById(R.id.tvResend);
        tvTimer = findViewById(R.id.tvTimer);
        tvForgotPass = findViewById(R.id.tvForgotPass);
        tvPrivacyPolicy = findViewById(R.id.tvPrivacy);
        img = findViewById(R.id.img);
        lytWebView = findViewById(R.id.lytWebView);
        webView = findViewById(R.id.webView);

        tvForgotPass.setText(underlineSpannable(getString(R.string.forgot_text)));
        edtLoginMobile.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_phone, 0, 0, 0);

        imgLoginPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pass, 0, R.drawable.ic_show, 0);
        edtPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pass, 0, R.drawable.ic_show, 0);
        edtConfirmPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pass, 0, R.drawable.ic_show, 0);
        edtResetPass.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pass, 0, R.drawable.ic_show, 0);
        edtResetCPass.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pass, 0, R.drawable.ic_show, 0);

        Utils.setHideShowPassword(edtPassword);
        Utils.setHideShowPassword(edtConfirmPassword);
        Utils.setHideShowPassword(imgLoginPassword);
        Utils.setHideShowPassword(edtResetPass);
        Utils.setHideShowPassword(edtResetCPass);

        lytResetPass.setVisibility(View.GONE);
        lytLogin.setVisibility(View.VISIBLE);
        lytVerify.setVisibility(View.GONE);
        lytSignUp.setVisibility(View.GONE);
        lytOTP.setVisibility(View.GONE);
        lytWebView.setVisibility(View.GONE);

        tvWelcome.setText(getString(R.string.welcome) + " " + getString(R.string.app_name));

        forMultipleCountryUse = false;

        if (from != null) {
            switch (from) {
                case "drawer":
                case "checkout":
                case "physicalPay":
                case "tracker":
                    lytLogin.setVisibility(View.VISIBLE);
                    lytLogin.startAnimation(animShow);
                    new Handler().postDelayed(() -> edtLoginMobile.requestFocus(), 1500);
                    break;
                case "refer":
                    otpFor = "new_user";
                    lytVerify.setVisibility(View.VISIBLE);
                    lytVerify.startAnimation(animShow);
                    new Handler().postDelayed(() -> edtMobileVerify.requestFocus(), 1500);
                    break;
                default:
                    lytVerify.setVisibility(View.GONE);
                    lytResetPass.setVisibility(View.GONE);
                    lytVerify.setVisibility(View.GONE);
                    lytLogin.setVisibility(View.GONE);
                    lytSignUp.setVisibility(View.VISIBLE);
                    tvMobile.setText(mobile);
                    edtRefer.setText(Constant.FRIEND_CODE);
                    break;
            }
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        } else {
            lytVerify.setVisibility(View.GONE);
            lytResetPass.setVisibility(View.GONE);
            lytVerify.setVisibility(View.GONE);
            lytLogin.setVisibility(View.VISIBLE);
            lytSignUp.setVisibility(View.GONE);
        }
        PrivacyPolicy();
    }

    public void checkNumber() {
        session.setData(Constant.COUNTRY_CODE, countryCode);
        Map<String, String> params = new HashMap<>();
        params.put(Constant.TYPE, Constant.VERIFY_USER);
        params.put(Constant.MOBILE, mobile);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject object = new JSONObject(response);
                    phoneNumber = ("+" + session.getData(Constant.COUNTRY_CODE) + mobile);
                    if (otpFor.equals("new_user")) {
                        if (!object.getBoolean(Constant.ERROR)) {
                            setSnackBar(getString(R.string.alert_register_num1) + " " + getString(R.string.app_name) + getString(R.string.alert_register_num2), getString(R.string.btn_ok), from);
                        } else {
                            generateOTP(phoneNumber);
                        }
                    } else if (otpFor.equals("exist_user")) {
                        generateOTP(phoneNumber);
                    }
                } catch (JSONException ignored) {

                }
            }
        }, activity, Constant.REGISTER_URL, params, true);
    }

    String generateOTP = RanNumGen.randomString(6);

    public void generateOTP(String oPhoneNumber) {
        session.setData(Constant.GENERATED_OTP, generateOTP);

        Map<String, String> params = new HashMap<>();
        if (otpFor.equals("new_user")) {
            params.put(Constant.TEXT, "আপনার একাউন্ট যাচাইকরণ OTP: " + session.getData(Constant.GENERATED_OTP));
        } else if (otpFor.equals("exist_user")) {
            params.put(Constant.TEXT, "প্রিয় গ্রাহক, পাসওয়ার্ড পরিবর্তনের জন্য আপনার OTP: " + session.getData(Constant.GENERATED_OTP));
        }
        params.put(Constant.MOBILE, oPhoneNumber);
        params.put(Constant.SEND_SMS, Constant.GetVal);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject object = new JSONObject(response);
                    if (!object.getBoolean(Constant.ERROR)) {
                        setSnackBar("প্রিয় গ্রাহক, আপনার মোবাইল নম্বর ভেরিফিকেশনের জন্য আপনার মোবাইল নাম্বারে আমরা একটি OTP পাঠিয়েছি। অনুগ্রহ পূর্বক আপনার মোবাইলের মেসেজ চেক করুন।", getString(R.string.btn_ok), "check");
                        edtMobileVerify.setEnabled(false);
                        btnVerify.setText(getString(R.string.verify_otp));
                        lytOTP.setVisibility(View.VISIBLE);
                        lytOTP.startAnimation(animShow);

                        if (resendOTP) {
                            Toast.makeText(activity, getString(R.string.otp_resend_alert), Toast.LENGTH_SHORT).show();
                        } else {
                            edtMobileVerify.setEnabled(false);
                            btnVerify.setText(getString(R.string.verify_otp));
                            lytOTP.setVisibility(View.VISIBLE);
                            lytOTP.startAnimation(animShow);
                            new CountDownTimer(120000, 1000) {
                                @SuppressLint("SetTextI18n")
                                public void onTick(long millisUntilFinished) {
                                    timerOn = true;
                                    // Used for formatting digit to be in 2 digits only
                                    NumberFormat f = new DecimalFormat("00");
                                    long min = (millisUntilFinished / 60000) % 60;
                                    long sec = (millisUntilFinished / 1000) % 60;
                                    tvTimer.setText(f.format(min) + ":" + f.format(sec));
                                }

                                public void onFinish() {
                                    resendOTP = false;
                                    timerOn = false;
                                    tvTimer.setVisibility(View.GONE);
                                    img.setColorFilter(ContextCompat.getColor(activity, R.color.colorPrimary));
                                    tvResend.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary));

                                    tvResend.setOnClickListener(v -> {

                                        Map<String, String> params = new HashMap<>();
                                        if (otpFor.equals("new_user")) {
                                            params.put(Constant.TEXT, "আপনার একাউন্ট যাচাইকরণ OTP: " + session.getData(Constant.GENERATED_OTP));
                                        } else if (otpFor.equals("exist_user")) {
                                            params.put(Constant.TEXT, "প্রিয় গ্রাহক, পাসওয়ার্ড পরিবর্তনের জন্য আপনার OTP: " + session.getData(Constant.GENERATED_OTP));
                                        }
                                        params.put(Constant.MOBILE, mobile);
                                        params.put(Constant.SEND_SMS, Constant.GetVal);
                                        ApiConfig.RequestToVolley((result, response) -> {
                                            if (result) {
                                                try {
                                                    JSONObject object = new JSONObject(response);
                                                    if (!object.getBoolean(Constant.ERROR)) {
                                                        setSnackBar("প্রিয় গ্রাহক, আপনার মোবাইল নম্বর ভেরিফিকেশনের জন্য আপনার মোবাইল নাম্বারে আমরা একটি OTP পাঠিয়েছি। অনুগ্রহ পূর্বক আপনার মোবাইলের মেসেজ চেক করুন।", getString(R.string.btn_ok), "check");
                                                    } else {
                                                        setSnackBar("Something went wrong, Please try again latter", "Retry", from);
                                                    }
                                                } catch (JSONException e) {
                                                    Log.d("ignored", e.getMessage());
                                                    Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }

                                                resendOTP = false;
                                                timerOn = false;
                                                tvTimer.setVisibility(View.GONE);
                                                img.setColorFilter(ContextCompat.getColor(activity, R.color.colorPrimary));
                                                tvResend.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary));
                                                tvResend.setClickable(false);
                                                Log.d("response", response);
                                            }
                                        }, activity, Constant.SMS_SEND_URL, params, true);
                                    });
                                }
                            }.start();
                        }
                    } else {
                        setSnackBar("Something went wrong, Please try again latter", "Retry", from);
                    }
                } catch (JSONException e) {
                    Log.d("ignored", e.getMessage());
                    Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Log.d("response", response);
            }
        }, activity, Constant.SMS_SEND_URL, params, true);
    }

    public void ForgotPassword() {
        String reset_psw = edtResetPass.getText().toString();
        String reset_c_psw = edtResetCPass.getText().toString();

        if (ApiConfig.CheckValidation(reset_psw, false, false)) {
            edtResetPass.requestFocus();
            edtResetPass.setError(getString(R.string.enter_new_pass));
        } else if (ApiConfig.CheckValidation(reset_c_psw, false, false)) {
            edtResetCPass.requestFocus();
            edtResetCPass.setError(getString(R.string.enter_confirm_pass));
        } else if (!reset_psw.equals(reset_c_psw)) {
            edtResetCPass.requestFocus();
            edtResetCPass.setError(getString(R.string.pass_not_match));
        } else {
            final Map<String, String> params = new HashMap<>();
            params.put(Constant.TYPE, Constant.FORGOT_PASSWORD_MOBILE);
            params.put(Constant.PASSWORD, reset_c_psw);
            params.put(Constant.MOBILE, mobile);
            ApiConfig.RequestToVolley((result, response) -> {
                if (result) {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (!object.getBoolean(Constant.ERROR)) {
                            session.setData(Constant.GENERATED_OTP, "");
                            setSnackBar(getString(R.string.msg_reset_pass_success), getString(R.string.btn_ok), "forgot_password");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, activity, Constant.REGISTER_URL, params, true);
        }
    }

    public void UserLogin(String mobile, String password) {

        Map<String, String> params = new HashMap<>();
        params.put(Constant.LOGIN, Constant.GetVal);
        params.put(Constant.MOBILE, mobile);
        params.put(Constant.PASSWORD, password);
        params.put(Constant.FCM_ID, "" + session.getData(Constant.FCM_ID));
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        StartMainActivity(jsonObject.getJSONArray(Constant.DATA).getJSONObject(0), password);
                    }
                    Toast.makeText(activity, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Log.d("rrr", response);
        }, activity, Constant.LOGIN_URL, params, true);
    }

    public void setSnackBar(String message, String action, final String type) {
        final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(action, view -> {
            if (type.equals("forgot_password")) {
                try {
                    lytResetPass.setVisibility(View.GONE);
                    lytResetPass.startAnimation(animHide);
                    Thread.sleep(500);
                    lytVerify.setVisibility(View.GONE);
                    lytVerify.startAnimation(animHide);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (type.equals("check")) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_MESSAGING);
                startActivity(intent);
            }
            snackbar.dismiss();
        });

        snackbar.setActionTextColor(Color.RED);
        View snackBarView = snackbar.getView();
        TextView textView = snackBarView.findViewById(R.id.snackbar_text);
        textView.setMaxLines(5);
        snackbar.show();
        new CountDownTimer(7000, 1000){

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                snackbar.dismiss();
            }
        }.start();
    }

    @SuppressLint("SetTextI18n")
    public void OTP_Varification(String otptext) {

        if (otptext.equals(session.getData(Constant.GENERATED_OTP))) {
            if (otpFor.equals("new_user")) {
                lytVerify.setVisibility(View.GONE);
                tvMobile.setText(mobile);
                lytSignUp.setVisibility(View.VISIBLE);
                lytSignUp.startAnimation(animShow);
            } else if (otpFor.equals("exist_user")) {
                lytVerify.setVisibility(View.GONE);
                lytResetPass.setVisibility(View.VISIBLE);
                lytResetPass.startAnimation(animShow);
            }
        } else {
            //verification unsuccessful.. display an error message
            String message = "Something is wrong, Please check your OTP";
            pinViewOTP.requestFocus();
            pinViewOTP.setError(message);
        }
    }

    public void UserSignUpSubmit(String name, String email, String password, String affiliater_code) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.TYPE, Constant.REGISTER);
        params.put(Constant.NAME, name);
        params.put(Constant.AFFILIATER_CODE, affiliater_code);
        params.put(Constant.EMAIL, email);
        params.put(Constant.MOBILE, mobile);
        params.put(Constant.PASSWORD, password);
        params.put(Constant.COUNTRY_CODE, session.getData(Constant.COUNTRY_CODE));
        params.put(Constant.FCM_ID, "" + session.getData(Constant.FCM_ID));
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        session.setData(Constant.GENERATED_OTP, "");
                        StartMainActivity(jsonObject, password);
                    }
                    Toast.makeText(activity, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Log.d("response", response);
            }
        }, activity, Constant.REGISTER_URL, params, true);
    }

    public void OnBtnClick(View view) {
        int id = view.getId();
        ApiConfig.hideKeyboard(activity, view);
        if (id == R.id.tvSignUp) {
            otpFor = "new_user";
            edtMobileVerify.setText("");
            edtMobileVerify.setEnabled(true);
            lytOTP.setVisibility(View.GONE);
            lytVerify.setVisibility(View.VISIBLE);
            lytVerify.startAnimation(animShow);
        } else if (id == R.id.tvForgotPass) {
            otpFor = "exist_user";
            edtMobileVerify.setText("");
            edtMobileVerify.setEnabled(true);
            lytOTP.setVisibility(View.GONE);
            lytVerify.setVisibility(View.VISIBLE);
            lytVerify.startAnimation(animShow);
        } else if (id == R.id.btnResetPass) {
            ApiConfig.hideKeyboard(activity, view);
            ForgotPassword();
        } else if (id == R.id.btnLogin) {
            mobile = edtLoginMobile.getText().toString();
            final String password = imgLoginPassword.getText().toString();

            if (ApiConfig.CheckValidation(mobile, false, false)) {
                edtLoginMobile.requestFocus();
                edtLoginMobile.setError(getString(R.string.enter_mobile_no));
            } else if (ApiConfig.CheckValidation(mobile, false, true)) {
                edtLoginMobile.requestFocus();
                edtLoginMobile.setError(getString(R.string.enter_valid_mobile_no));
            } else if (ApiConfig.CheckValidation(password, false, false)) {
                imgLoginPassword.requestFocus();
                imgLoginPassword.setError(getString(R.string.enter_pass));
            } else {
                UserLogin(mobile, password);
            }
        } else if (id == R.id.btnVerify) {
            if (lytOTP.getVisibility() == View.GONE) {
                ApiConfig.hideKeyboard(activity, view);
                mobile = edtMobileVerify.getText().toString().trim();
                if (ApiConfig.CheckValidation(mobile, false, false)) {
                    edtMobileVerify.requestFocus();
                    edtMobileVerify.setError(getString(R.string.enter_mobile_no));
                } else if (ApiConfig.CheckValidation(mobile, false, true)) {
                    edtMobileVerify.requestFocus();
                    edtMobileVerify.setError(getString(R.string.enter_valid_mobile_no));
                } else {
                    checkNumber(); //
                }
            } else {
                String otptext = Objects.requireNonNull(pinViewOTP.getText()).toString().trim();
                if (ApiConfig.CheckValidation(otptext, false, false)) {
                    pinViewOTP.requestFocus();
                    pinViewOTP.setError(getString(R.string.enter_otp));
                } else {
                    OTP_Varification(otptext); //
                }
            }
        } else if (id == R.id.btnRegister) {
            String name = edtName.getText().toString().trim();
            String email = "" + edtEmail.getText().toString().trim();
            final String password = edtPassword.getText().toString().trim();
            String cpassword = edtConfirmPassword.getText().toString().trim();
            String affiliate_code = edtRefer.getText().toString().trim();
            if (ApiConfig.CheckValidation(name, false, false)) {
                edtName.requestFocus();
                edtName.setError(getString(R.string.enter_name));
            } else if (ApiConfig.CheckValidation(email, false, false)) {
                edtEmail.requestFocus();
                edtEmail.setError(getString(R.string.enter_email));
            } else if (ApiConfig.CheckValidation(email, true, false)) {
                edtEmail.requestFocus();
                edtEmail.setError(getString(R.string.enter_valid_email));
            } else if (ApiConfig.CheckValidation(password, false, false)) {
                edtConfirmPassword.requestFocus();
                edtPassword.setError(getString(R.string.enter_pass));
            } else if (ApiConfig.CheckValidation(cpassword, false, false)) {
                edtConfirmPassword.requestFocus();
                edtConfirmPassword.setError(getString(R.string.enter_confirm_pass));
            } else if (!password.equals(cpassword)) {
                edtConfirmPassword.requestFocus();
                edtConfirmPassword.setError(getString(R.string.pass_not_match));
            } else if (!chPrivacy.isChecked()) {
                Toast.makeText(activity, getString(R.string.alert_privacy_msg), Toast.LENGTH_LONG).show();
            } else {
                UserSignUpSubmit(name, email, password, affiliate_code);
            }
        } else if (id == R.id.imgVerifyClose) {
            lytOTP.setVisibility(View.GONE);
            lytVerify.setVisibility(View.GONE);
            lytVerify.startAnimation(animHide);
            edtMobileVerify.setText("");
            edtMobileVerify.setEnabled(true);
            pinViewOTP.setText("");
        } else if (id == R.id.imgResetPasswordClose) {
            edtResetPass.setText("");
            edtResetCPass.setText("");
            lytResetPass.setVisibility(View.GONE);
            lytResetPass.startAnimation(animHide);
        } else if (id == R.id.imgSignUpClose) {
            lytSignUp.setVisibility(View.GONE);
            lytSignUp.startAnimation(animHide);
            tvMobile.setText("");
            edtName.setText("");
            edtEmail.setText("");
            edtPassword.setText("");
            edtConfirmPassword.setText("");
            edtRefer.setText("");
        } else if (id == R.id.imgWebViewClose) {
            lytWebView.setVisibility(View.GONE);
            lytWebView.startAnimation(animHide);
        }
    }

    public void StartMainActivity(JSONObject jsonObject, String password) {
        try {

            Log.d("loginjsonObject", jsonObject.toString());

            new Session(activity).createUserLoginSession(jsonObject.getString(Constant.PROFILE)
                    , session.getData(Constant.FCM_ID),
                    jsonObject.getString(Constant.USER_ID),
                    jsonObject.getString(Constant.NAME),
                    jsonObject.getString(Constant.EMAIL),
                    jsonObject.getString(Constant.MOBILE),
                    password,
                    jsonObject.getString(Constant.REFERRAL_CODE));

            ApiConfig.AddMultipleProductInCart(session, activity, databaseHelper.getCartData());
            ApiConfig.AddMultipleProductInSaveForLater(session, activity, databaseHelper.getSaveForLaterData());
            ApiConfig.getCartItemCount(activity, session);

            ArrayList<String> favorites = databaseHelper.getFavorite();
            for (int i = 0; i < favorites.size(); i++) {
                ApiConfig.AddOrRemoveFavorite(activity, session, favorites.get(i), true);
            }

            databaseHelper.DeleteAllFavoriteData();
            databaseHelper.ClearCart();
            databaseHelper.ClearSaveForLater();

            ApiConfig.getWalletBalance(activity, session);
            session.setData(Constant.COUNTRY_CODE, jsonObject.getString(Constant.COUNTRY_CODE));

            MainActivity.homeClicked = false;
            MainActivity.categoryClicked = false;
            MainActivity.favoriteClicked = false;
            MainActivity.drawerClicked = false;

            Intent intent = new Intent(activity, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Constant.FROM, from);

            if (from.equals("physicalPay")) {
                intent.putExtra(Constant.ABMN_QR_RESULT, abmn_qrResult);
                intent.putExtra(Constant.TYPE_AMOUNT, type_amount);
            }
            startActivity(intent);

            finish();
        } catch (JSONException e) {
            e.printStackTrace();
            e.printStackTrace();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    public SpannableString underlineSpannable(String text) {
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new UnderlineSpan(), 0, text.length(), 0);
        return spannableString;
    }

    public void GetContent(final String type, final String key) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.SETTINGS, Constant.GetVal);
        params.put(type, Constant.GetVal);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean(Constant.ERROR)) {

                        String privacyStr = obj.getString(key);
                        webView.setVerticalScrollBarEnabled(true);
                        webView.loadDataWithBaseURL("", privacyStr, "text/html", "UTF-8", "");
                    } else {
                        Toast.makeText(activity, obj.getString(Constant.MESSAGE), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }
        }, activity, Constant.SETTING_URL, params, false);
    }

    public void PrivacyPolicy() {
        tvPrivacyPolicy.setClickable(true);
        tvPrivacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());

        String message = getString(R.string.msg_privacy_terms);
        String s2 = getString(R.string.terms_conditions);
        String s1 = getString(R.string.privacy_policy);
        final Spannable wordtoSpan = new SpannableString(message);

        wordtoSpan.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                GetContent(Constant.GET_PRIVACY, "privacy");
                try {
                    Thread.sleep(500);
                    lytWebView.setVisibility(View.VISIBLE);
                    lytWebView.startAnimation(animShow);
                } catch (Exception ignored) {

                }
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(activity, R.color.colorPrimary));
                ds.isUnderlineText();
            }
        }, message.indexOf(s1), message.indexOf(s1) + s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        wordtoSpan.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                GetContent(Constant.GET_TERMS, "terms");
                try {
                    Thread.sleep(500);
                    lytWebView.setVisibility(View.VISIBLE);
                    lytWebView.startAnimation(animShow);
                } catch (Exception e) {
                    Log.d("exception", e.getMessage());
                }
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(activity, R.color.colorPrimary));
                ds.isUnderlineText();
            }

        }, message.indexOf(s2), message.indexOf(s2) + s2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPrivacyPolicy.setText(wordtoSpan);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}