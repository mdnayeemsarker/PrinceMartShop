package com.princemartbd.shop.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.princemartbd.shop.BuildConfig;
import com.princemartbd.shop.R;
import com.princemartbd.shop.fragment.AddressListFragment;
import com.princemartbd.shop.fragment.BWalletFragment;
import com.princemartbd.shop.fragment.CartFragment;
import com.princemartbd.shop.fragment.CategoryFragment;
import com.princemartbd.shop.fragment.DrawerFragment;
import com.princemartbd.shop.fragment.FavoriteFragment;
import com.princemartbd.shop.fragment.HomeFragment;
import com.princemartbd.shop.fragment.OrderPlacedFragment;
import com.princemartbd.shop.fragment.ProductDetailFragment;
import com.princemartbd.shop.fragment.ProductListFragment;
import com.princemartbd.shop.fragment.SubCategoryFragment;
import com.princemartbd.shop.fragment.TrackOrderFragment;
import com.princemartbd.shop.fragment.TrackerDetailFragment;
import com.princemartbd.shop.fragment.WalletTransactionFragment;
import com.princemartbd.shop.helper.ApiConfig;
import com.princemartbd.shop.helper.Constant;
import com.princemartbd.shop.helper.DatabaseHelper;
import com.princemartbd.shop.helper.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    static final String TAG = "MAIN ACTIVITY";
    @SuppressLint("StaticFieldLeak")
    public static Toolbar toolbar;
    public static BottomNavigationView bottomNavigationView;
    public static Fragment active;
    public static FragmentManager fm = null;
    public static Fragment b_walletFragment, homeFragment, categoryFragment, favoriteFragment, drawerFragment;
    public static boolean b_walletClicked = false, homeClicked = false, categoryClicked = false, favoriteClicked = false, drawerClicked = false;
    public Activity activity;
    public Session session;
    boolean doubleBackToExitPressedOnce = false;
    DatabaseHelper databaseHelper;
    String from, abmn_qrResult, type_amount;
    CardView cardViewHamburger;

    AppEventsLogger logger = AppEventsLogger.newLogger(this);

    TextView toolbarTitle;

    //    public static PinCodeFragment pinCodeFragment;
    ImageView imageMenu, imageHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

        cardViewHamburger = findViewById(R.id.cardViewHamburger);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        imageMenu = findViewById(R.id.imageMenu);
        imageHome = findViewById(R.id.imageHome);

        activity = MainActivity.this;
        session = new Session(activity);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        from = getIntent().getStringExtra(Constant.FROM);
        abmn_qrResult = getIntent().getStringExtra(Constant.ABMN_QR_RESULT);
        type_amount = getIntent().getStringExtra(Constant.TYPE_AMOUNT);
        databaseHelper = new DatabaseHelper(activity);

        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            ApiConfig.getCartItemCount(activity, session);
        } else {
            session.setData(Constant.STATUS, "1");
            databaseHelper.getTotalItemOfCart(activity);
        }

        getOffersData();

        setAppLocal("en"); //Change you language code here

        fm = getSupportFragmentManager();

        b_walletFragment = new BWalletFragment();
        homeFragment = new HomeFragment();
        categoryFragment = new CategoryFragment();
        favoriteFragment = new FavoriteFragment();
        drawerFragment = new DrawerFragment();

        Bundle bundle = new Bundle();
        bottomNavigationView.setSelectedItemId(R.id.navMain);
        active = b_walletFragment;

        b_walletClicked = true;
        homeClicked = false;
        drawerClicked = false;
        favoriteClicked = false;
        categoryClicked = false;
        try {
            if (!getIntent().getStringExtra("json").isEmpty()) {
                bundle.putString("json", getIntent().getStringExtra("json"));
            }
            b_walletFragment.setArguments(bundle);
            fm.beginTransaction().add(R.id.container, b_walletFragment).commit();
        } catch (Exception e) {
            fm.beginTransaction().add(R.id.container, b_walletFragment).commit();
        }

        @SuppressLint("ResourceType") ColorStateList iconColorStates = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[]{
                        Color.parseColor(getResources().getString(R.color.text_unselected)),
                        Color.parseColor(getResources().getString(R.color.colorSecondary))
                });

        bottomNavigationView.setItemIconTintList(iconColorStates);
        bottomNavigationView.setItemTextColor(iconColorStates);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            {
                if (item.getItemId() == R.id.navMain) {
                    if (active != b_walletFragment) {
                        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                            Constant.TOOLBAR_TITLE = getString(R.string.hi) + session.getData(Constant.NAME) + "!";
                        } else {
                            Constant.TOOLBAR_TITLE = getString(R.string.hi_user);
                        }
                        invalidateOptionsMenu();
                        bottomNavigationView.getMenu().findItem(item.getItemId()).setChecked(true);
                        if (!b_walletClicked) {
                            fm.beginTransaction().add(R.id.container, b_walletFragment).show(b_walletFragment).hide(active).commit();
                            b_walletClicked = true;
                        } else {
                            fm.beginTransaction().show(b_walletFragment).hide(active).commit();
                        }
                        active = b_walletFragment;
                    }
                } else if (item.getItemId() == R.id.navCategory) {
                    Constant.TOOLBAR_TITLE = getString(R.string.title_category);
                    invalidateOptionsMenu();
                    if (active != categoryFragment) {
                        bottomNavigationView.getMenu().findItem(item.getItemId()).setChecked(true);
                        if (!categoryClicked) {
                            fm.beginTransaction().add(R.id.container, categoryFragment).show(categoryFragment).hide(active).commit();
                            categoryClicked = true;
                        } else {
                            fm.beginTransaction().show(categoryFragment).hide(active).commit();
                        }
                        active = categoryFragment;
                    }
                }else if (item.getItemId() == R.id.navShopping) {
                    if (active != homeFragment) {
                        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                            Constant.TOOLBAR_TITLE = getString(R.string.hi) + session.getData(Constant.NAME) + "!";
                        } else {
                            Constant.TOOLBAR_TITLE = getString(R.string.hi_user);
                        }
                        invalidateOptionsMenu();
                        bottomNavigationView.getMenu().findItem(item.getItemId()).setChecked(true);
                        if (!homeClicked) {
                            fm.beginTransaction().add(R.id.container, homeFragment).show(homeFragment).hide(active).commit();
                            homeClicked = true;
                        } else {
                            fm.beginTransaction().show(homeFragment).hide(active).commit();
                        }
                        active = homeFragment;
                    }
                } else if (item.getItemId() == R.id.navWishList) {
                    Constant.TOOLBAR_TITLE = getString(R.string.title_fav);
                    invalidateOptionsMenu();
                    if (active != favoriteFragment) {
                        bottomNavigationView.getMenu().findItem(item.getItemId()).setChecked(true);
                        if (!favoriteClicked) {
                            fm.beginTransaction().add(R.id.container, favoriteFragment).show(favoriteFragment).hide(active).commit();
                            favoriteClicked = true;
                        } else {
                            fm.beginTransaction().show(favoriteFragment).hide(active).commit();
                        }
                        active = favoriteFragment;
                    }
                } else if (item.getItemId() == R.id.navProfile) {
                    Constant.TOOLBAR_TITLE = getString(R.string.title_profile);
                    invalidateOptionsMenu();
                    if (active != drawerFragment) {
                        bottomNavigationView.getMenu().findItem(item.getItemId()).setChecked(true);
                        if (!drawerClicked) {
                            fm.beginTransaction().add(R.id.container, drawerFragment).show(drawerFragment).hide(active).commit();
                            drawerClicked = true;
                        } else {
                            fm.beginTransaction().show(drawerFragment).hide(active).commit();
                        }
                        active = drawerFragment;
                    }
                }
            }
            return false;
        });

        switch (from) {
            case "checkout":
                bottomNavigationView.setVisibility(View.GONE);
                ApiConfig.getCartItemCount(activity, session);
                Fragment fragment = new AddressListFragment();
                bundle = new Bundle();
                bundle.putString(Constant.FROM, "process");
                bundle.putDouble("total", Double.parseDouble(ApiConfig.StringFormat("" + Constant.FLOAT_TOTAL_AMOUNT)));
                fragment.setArguments(bundle);
                fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
                break;
            case "share":
                Fragment fragment0 = new ProductDetailFragment();
                Bundle bundle0 = new Bundle();
                bundle0.putInt(Constant.VARIANT_POSITION, getIntent().getIntExtra(Constant.VARIANT_POSITION, 0));
                bundle0.putString(Constant.ID, getIntent().getStringExtra(Constant.ID));
                bundle0.putString(Constant.FROM, "share");
                fragment0.setArguments(bundle0);
                fm.beginTransaction().add(R.id.container, fragment0).addToBackStack(null).commit();
                break;
            case "product":
                Fragment fragment1 = new ProductDetailFragment();
                Bundle bundle1 = new Bundle();
                bundle1.putInt(Constant.VARIANT_POSITION, getIntent().getIntExtra(Constant.VARIANT_POSITION, 0));
                bundle1.putString(Constant.ID, getIntent().getStringExtra(Constant.ID));
                bundle1.putString(Constant.FROM, "product");
                fragment1.setArguments(bundle1);
                fm.beginTransaction().add(R.id.container, fragment1).addToBackStack(null).commit();
                break;
            case "category":
                Fragment fragment2 = new SubCategoryFragment();
                Bundle bundle2 = new Bundle();
                bundle2.putString(Constant.ID, getIntent().getStringExtra(Constant.ID));
                bundle2.putString("name", getIntent().getStringExtra("name"));
                bundle2.putString(Constant.FROM, "category");
                fragment2.setArguments(bundle2);
                fm.beginTransaction().add(R.id.container, fragment2).addToBackStack(null).commit();
                break;
            case "order":
                Fragment fragment3 = new TrackerDetailFragment();
                Bundle bundle3 = new Bundle();
                bundle3.putSerializable("model", "");
                bundle3.putString(Constant.ID, getIntent().getStringExtra(Constant.ID));
                fragment3.setArguments(bundle3);
                fm.beginTransaction().add(R.id.container, fragment3).addToBackStack(null).commit();
                break;
            case "payment_success":
                fm.beginTransaction().add(R.id.container, new OrderPlacedFragment()).addToBackStack(null).commit();
                break;
            case "tracker":
                fm.beginTransaction().add(R.id.container, new TrackOrderFragment()).addToBackStack(null).commit();
                break;
            case "wallet":
                fm.beginTransaction().add(R.id.container, new WalletTransactionFragment()).addToBackStack(null).commit();
                break;
            case "physicalPay":
                Intent intent = new Intent(activity, PayNowActivity.class);
                intent.putExtra(Constant.ABMN_QR_RESULT, abmn_qrResult);
                intent.putExtra(Constant.TYPE_AMOUNT, type_amount);
                startActivity(intent);
                break;
        }

        fm.addOnBackStackChangedListener(() ->

        {
            toolbar.setVisibility(View.VISIBLE);
            Fragment currentFragment = fm.findFragmentById(R.id.container);
            Objects.requireNonNull(currentFragment).onResume();
        });

        FirebaseMessaging.getInstance().
                getToken().
                addOnSuccessListener(token ->
                {
                    session.setData(Constant.FCM_ID, token);
                    Register_FCM(token);
                });
        GetProductsName();
        getUserData(activity, session);

        printHashKey();
        getOnlineVersion();
    }

    public void logBattleTheMonsterEvent () {
        logger.logEvent("BattleTheMonster");
    }

    private void printHashKey() {
        try {
            @SuppressLint("PackageManagerGetSignatures")
            PackageInfo info = getPackageManager().getPackageInfo(activity.getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", activity.getPackageName() +" : " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void getOffersData() {

        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_CASHBACKS, Constant.GetVal);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject object = new JSONObject(response);
                    if (!object.getBoolean(Constant.ERROR)) {
                        JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                        session.setJSONArray("offersArray", jsonArray);
                        Log.d("offer", jsonArray.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("response", "Catch: " + response);
                }
            }
        }, activity, Constant.GET_CASHBACKS_URL, params, true);
    }

    private void getOnlineVersion() {
        Map<String, String> params = new HashMap<>();
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++){
                        JSONObject data = jsonArray.getJSONObject(i);
                        String onlinePackageID = data.getString("packageID");
                        String onlineVersionName = data.getString("versionName");
                        if (activity.getPackageName().equals(onlinePackageID)){
                            String versionName = BuildConfig.VERSION_NAME;
                            if (!onlineVersionName.equals(versionName)){
                                ApiConfig.newUpdate(activity);
                            }
                        }
                    }
                } catch (JSONException e) {
                    Log.d("app_update", e.getMessage());
                }
            }
        }, activity, Constant.APP_UPDATE_URL, params, false);
    }

    public void setAppLocal(String languageCode) {
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(new Locale(languageCode.toLowerCase()));
        resources.updateConfiguration(configuration, dm);
    }

    public void Register_FCM(String token) {
        Map<String, String> params = new HashMap<>();
        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            params.put(Constant.USER_ID, session.getData(Constant.ID));
        }
        params.put(Constant.FCM_ID, token);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        session.setData(Constant.FCM_ID, token);
                    }
                } catch (JSONException ignored) {

                }

            }
        }, activity, Constant.REGISTER_DEVICE_URL, params, false);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        if (fm.getBackStackEntryCount() == 0) {
            Toast.makeText(this, getString(R.string.exit_msg), Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        }
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_cart:
                checkOut();
                break;
            case R.id.toolbar_search:
                Fragment fragment = new ProductListFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constant.FROM, "search");
                bundle.putString(Constant.NAME, activity.getString(R.string.search));
                bundle.putString(Constant.ID, "");
                fragment.setArguments(bundle);
                MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
                break;
            case R.id.toolbar_logout:
                session.logoutUserConfirmation(activity);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkOut() {
        MainActivity.fm.beginTransaction().add(R.id.container, new CartFragment()).addToBackStack(null).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.toolbar_cart).setVisible(true);
        menu.findItem(R.id.toolbar_search).setVisible(true);
        menu.findItem(R.id.toolbar_cart).setIcon(ApiConfig.buildCounterDrawable(Constant.TOTAL_CART_ITEM, activity));

        if (fm.getBackStackEntryCount() > 0) {
            toolbarTitle.setText(Constant.TOOLBAR_TITLE);
            bottomNavigationView.setVisibility(View.GONE);

            cardViewHamburger.setCardBackgroundColor(getColor(R.color.colorPrimaryLight));
            imageMenu.setOnClickListener(v -> fm.popBackStack());

            imageMenu.setVisibility(View.VISIBLE);
            imageHome.setVisibility(View.GONE);
        } else {
            if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                toolbarTitle.setText(getString(R.string.hi) + session.getData(Constant.NAME) + ".!");
            } else {
                toolbarTitle.setText(getString(R.string.hi_user));
            }
            bottomNavigationView.setVisibility(View.VISIBLE);
            cardViewHamburger.setCardBackgroundColor(getColor(R.color.colorPrimaryLight));
            imageMenu.setVisibility(View.GONE);
            imageHome.setVisibility(View.VISIBLE);
        }

        invalidateOptionsMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.clear();
        LatLng latLng = new LatLng(Double.parseDouble(session.getCoordinates(Constant.LATITUDE)), Double.parseDouble(session.getCoordinates(Constant.LONGITUDE)));
        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title("Current Location"));

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(19));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        Objects.requireNonNull(fragment).onActivityResult(requestCode, resultCode, data);

    }


    @SuppressLint("SetTextI18n")
    public static void getUserData(final Activity activity, Session session) {
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
                            session.setUserData(jsonObject.getString(Constant.USER_ID), jsonObject.getString(Constant.NAME), jsonObject.getString(Constant.EMAIL), jsonObject.getString(Constant.COUNTRY_CODE), jsonObject.getString(Constant.PROFILE), jsonObject.getString(Constant.MOBILE), jsonObject.getString(Constant.BALANCE), jsonObject.getString(Constant.REFERRAL_CODE), jsonObject.getString(Constant.FRIEND_CODE), jsonObject.getString(Constant.FCM_ID), jsonObject.getString(Constant.STATUS));
                            session.setData("bonus_balance", jsonObject.getString("bonus_balance"));
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

    public void GetProductsName() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_ALL_PRODUCTS_NAME, Constant.GetVal);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        session.setData(Constant.GET_ALL_PRODUCTS_NAME, jsonObject.getString(Constant.DATA));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.GET_PRODUCTS_URL, params, false);
    }

    @Override
    protected void onPause() {
        invalidateOptionsMenu();
        super.onPause();
    }

    @Override
    protected void onResume() {
        ApiConfig.getWalletBalance(activity, session);
        super.onResume();
    }
}