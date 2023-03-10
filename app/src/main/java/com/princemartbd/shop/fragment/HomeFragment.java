package com.princemartbd.shop.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.princemartbd.shop.R;
import com.princemartbd.shop.activity.MainActivity;
import com.princemartbd.shop.adapter.CategoryAdapter;
import com.princemartbd.shop.adapter.OfferAdapter;
import com.princemartbd.shop.adapter.ProductHomeAdapter;
import com.princemartbd.shop.adapter.SectionAdapter;
import com.princemartbd.shop.adapter.SellerAdapter;
import com.princemartbd.shop.adapter.SliderAdapter;
import com.princemartbd.shop.helper.ApiConfig;
import com.princemartbd.shop.helper.Constant;
import com.princemartbd.shop.helper.Session;
import com.princemartbd.shop.model.Category;
import com.princemartbd.shop.model.Product;
import com.princemartbd.shop.model.Seller;
import com.princemartbd.shop.model.Slider;

public class HomeFragment extends Fragment {

    public Session session;
    public static ArrayList<Category> categoryArrayList, sectionList;
    public static ArrayList<Seller> sellerArrayList;
    ArrayList<Slider> sliderArrayList;
    Activity activity;
    NestedScrollView nestedScrollView;
//    NestedScrollView nestedScrollViewHomeProduct;
    SwipeRefreshLayout swipeLayout;
    View root;
    int timerDelay = 0, timerWaiting = 0;
    EditText searchView;
    RecyclerView categoryRecyclerView, recyclerView, sectionView, offerView, sellerRecyclerView;
    ViewPager mPager;
    LinearLayout mMarkersLayout;
    int size;
    Timer swipeTimer;
    Handler handler;
    Runnable Update;
    int currentPage = 0;
    RelativeLayout lytCategory, lytSeller;
    LinearLayout lytSearchView;
    Menu menu;
    TextView tvMore, tvMoreSeller;
    boolean searchVisible = false;
    private ShimmerFrameLayout mShimmerViewContainer;
    @SuppressLint("StaticFieldLeak")
    public static TextView tvLocation;
    public TextView tvTitleLocation;
    public static SwipeRefreshLayout.OnRefreshListener refreshListener;

    public static ArrayList<Product> productArrayList;
    @SuppressLint("StaticFieldLeak")
    public static ProductHomeAdapter mAdapter;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_home, container, false);
        activity = getActivity();
        session = new Session(activity);

        timerDelay = 3000;
        timerWaiting = 3000;
        setHasOptionsMenu(true);

        swipeLayout = root.findViewById(R.id.swipeLayout);
        categoryRecyclerView = root.findViewById(R.id.categoryRecycleView);

        sectionView = root.findViewById(R.id.sectionView);
        sectionView.setLayoutManager(new LinearLayoutManager(activity));
        sectionView.setNestedScrollingEnabled(false);

        offerView = root.findViewById(R.id.offerView);
        offerView.setLayoutManager(new LinearLayoutManager(activity));
        offerView.setNestedScrollingEnabled(false);

        nestedScrollView = root.findViewById(R.id.nestedScrollView);
//        nestedScrollViewHomeProduct = root.findViewById(R.id.nestedScrollViewHomeProduct);
        mMarkersLayout = root.findViewById(R.id.layout_markers);
        lytCategory = root.findViewById(R.id.lytCategory);
        lytSeller = root.findViewById(R.id.lytSeller);
        lytSearchView = root.findViewById(R.id.lytSearchView);
        sellerRecyclerView = root.findViewById(R.id.sellerRecyclerView);
        tvMore = root.findViewById(R.id.tvMore);
        tvMoreSeller = root.findViewById(R.id.tvMoreSeller);
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer);
        tvTitleLocation = root.findViewById(R.id.tvTitleLocation);
        tvLocation = root.findViewById(R.id.tvLocation);

        searchView = root.findViewById(R.id.searchView);

        tvLocation.setText(session.getData(Constant.GET_SELECTED_PINCODE_NAME));

//        if (!session.getBoolean(Constant.GET_SELECTED_PINCODE)) {
//            MainActivity.pinCodeFragment = new PinCodeFragment();
//            Bundle bundle = new Bundle();
//            bundle.putString(Constant.FROM, "home");
//            MainActivity.pinCodeFragment.setArguments(bundle);
//            MainActivity.pinCodeFragment.show(MainActivity.fm, null);
//        } else {
//            tvLocation.setText(session.getData(Constant.GET_SELECTED_PINCODE_NAME));
//        }
//
//        tvTitleLocation.setOnClickListener(v -> {
//            MainActivity.pinCodeFragment = new PinCodeFragment();
//            Bundle bundle = new Bundle();
//            bundle.putString(Constant.FROM, "home");
//            MainActivity.pinCodeFragment.setArguments(bundle);
//            MainActivity.pinCodeFragment.show(MainActivity.fm, null);
//        });
//
//        tvLocation.setOnClickListener(v -> {
//            MainActivity.pinCodeFragment = new PinCodeFragment();
//            Bundle bundle = new Bundle();
//            bundle.putString(Constant.FROM, "home");
//            MainActivity.pinCodeFragment.setArguments(bundle);
//            MainActivity.pinCodeFragment.show(MainActivity.fm, null);
//        });


        if (nestedScrollView != null) {
            nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                Rect scrollBounds = new Rect();
                nestedScrollView.getHitRect(scrollBounds);
                if (!lytSearchView.getLocalVisibleRect(scrollBounds) || scrollBounds.height() < lytSearchView.getHeight()) {
                    searchVisible = true;
                    menu.findItem(R.id.toolbar_search).setVisible(true);
                } else {
                    searchVisible = false;
                    menu.findItem(R.id.toolbar_search).setVisible(false);
                }
                activity.invalidateOptionsMenu();
            });
        }

        tvMore.setOnClickListener(v -> {
            if (!MainActivity.categoryClicked) {
                MainActivity.fm.beginTransaction().add(R.id.container, MainActivity.categoryFragment).show(MainActivity.categoryFragment).hide(MainActivity.active).commit();
                MainActivity.categoryClicked = true;
            } else {
                MainActivity.fm.beginTransaction().show(MainActivity.categoryFragment).hide(MainActivity.active).commit();
            }
            MainActivity.bottomNavigationView.setSelectedItemId(R.id.navCategory);
            MainActivity.active = MainActivity.categoryFragment;
        });

        tvMoreSeller.setOnClickListener(v -> MainActivity.fm.beginTransaction().add(R.id.container, new SellerListFragment()).addToBackStack(null).commit());

        searchView.setOnTouchListener((View v, MotionEvent event) -> {
            Fragment fragment = new ProductListFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constant.FROM, "search");
            bundle.putString(Constant.NAME, activity.getString(R.string.search));
            bundle.putString(Constant.ID, "");
            fragment.setArguments(bundle);
            MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
            return false;
        });

        lytSearchView.setOnClickListener(v -> {
            Fragment fragment = new ProductListFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constant.FROM, "search");
            bundle.putString(Constant.NAME, activity.getString(R.string.search));
            bundle.putString(Constant.ID, "");
            fragment.setArguments(bundle);
            MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
        });

        mPager = root.findViewById(R.id.pager);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int position) {
                ApiConfig.addMarkers(position, sliderArrayList, mMarkersLayout, activity);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        refreshListener = () -> {
            if (swipeTimer != null) {
                swipeTimer.cancel();
            }
            timerDelay = 3000;
            timerWaiting = 3000;
            ApiConfig.getWalletBalance(activity, session);
            if (new Session(activity).getBoolean(Constant.IS_USER_LOGIN)) {
                ApiConfig.getWalletBalance(activity, new Session(activity));
            }
            GetHomeData();
        };

        swipeLayout.setOnRefreshListener(() -> {
            swipeLayout.setRefreshing(false);
            refreshListener.onRefresh();
        });

        GetHomeData();
        GetData();

        if (new Session(activity).getBoolean(Constant.IS_USER_LOGIN)) {
            ApiConfig.getWalletBalance(activity, new Session(activity));
        }

        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(activity, 2));
//        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        return root;
    }

    int total;
    int offset = 0;
    boolean isLoadMore = false;
    void GetData() {
        productArrayList = new ArrayList<>();
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.LIMIT, ""+4000);
        params.put(Constant.GET_ALL_PRODUCTS, Constant.GetVal);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
//                        Toast.makeText(activity, jsonObject.getString("total"), Toast.LENGTH_SHORT).show();
                        productArrayList = new ArrayList<>();
                        JSONObject object = new JSONObject(response);
                        JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                        try {
                            productArrayList.addAll(ApiConfig.GetProductList(jsonArray));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mAdapter = new ProductHomeAdapter(activity, productArrayList, R.layout.lyt_home_product);
                        mAdapter.setHasStableIds(true);
                        recyclerView.setAdapter(mAdapter);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("all_product", response);
            }
        }, activity, Constant.GET_PRODUCTS_URL, params, false);

//        productArrayList = new ArrayList<>();
//        if (mAdapter != null)
//            mAdapter.notifyDataSetChanged();
//
//        Map<String, String> params = new HashMap<>();
//
//        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
//            params.put(Constant.USER_ID, session.getData(Constant.ID));
//        }
//        params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);
//        params.put(Constant.OFFSET, "" + offset);
//
//        ApiConfig.RequestToVolley((result, response) -> {
//            if (result) {
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    if (!jsonObject.getBoolean(Constant.ERROR)) {
//                        total = Integer.parseInt(jsonObject.getString(Constant.TOTAL));
//                        if (offset == 0) {
//                            productArrayList = new ArrayList<>();
//                        }
//                        JSONObject object = new JSONObject(response);
//                        JSONArray jsonArray = object.getJSONArray(Constant.DATA);
//                        try {
//                            productArrayList.addAll(ApiConfig.GetProductList(jsonArray));
//                        } catch (Exception e) {
//                            mShimmerViewContainer.stopShimmer();
//                            mShimmerViewContainer.setVisibility(View.GONE);
//                        }
//                        if (offset == 0) {
//                            mAdapter = new ProductHomeAdapter(activity, productArrayList, R.layout.lyt_home_product);
//                            mAdapter.setHasStableIds(true);
//                            recyclerView.setAdapter(mAdapter);
//
//                            nestedScrollViewHomeProduct.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
//
//                                // if (diff == 0) {
//                                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
//                                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
//                                    if (productArrayList.size() < total) {
//                                        if (!isLoadMore) {
//                                            if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == productArrayList.size() - 1) {
//                                                //bottom of list!
//                                                productArrayList.add(null);
//                                                mAdapter.notifyItemInserted(productArrayList.size() - 1);
//
//                                                offset += Integer.parseInt("" + Constant.LOAD_ITEM_LIMIT);
//                                                Map<String, String> params1 = new HashMap<>();
//
//                                                if (session.getBoolean(Constant.IS_USER_LOGIN)) {
//                                                    params1.put(Constant.USER_ID, session.getData(Constant.ID));
//                                                }
//                                                params1.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);
//                                                params1.put(Constant.OFFSET, "" + offset);
//
//                                                ApiConfig.RequestToVolley((result1, response1) -> {
//                                                    if (result1) {
//                                                        try {
//                                                            JSONObject jsonObject1 = new JSONObject(response1);
//                                                            if (!jsonObject1.getBoolean(Constant.ERROR)) {
//
//                                                                JSONObject object1 = new JSONObject(response1);
//                                                                JSONArray jsonArray1 = object1.getJSONArray(Constant.DATA);
//                                                                productArrayList.remove(productArrayList.size() - 1);
//                                                                mAdapter.notifyItemRemoved(productArrayList.size());
//                                                                try {
//                                                                    productArrayList.addAll(ApiConfig.GetProductList(jsonArray1));
//                                                                } catch (Exception e) {
//                                                                    e.printStackTrace();
//                                                                }
//                                                                mAdapter.notifyDataSetChanged();
//                                                                mAdapter.setLoaded();
//                                                                isLoadMore = false;
//                                                            }
//                                                        } catch (JSONException e) {
//                                                            e.printStackTrace();
//                                                        }
//                                                    }
//                                                }, activity, Constant.GET_ALL_PRODUCTS, params1, false);
//                                                isLoadMore = true;
//                                            }
//                                        }
//                                    }
//                                }
//                            });
//                        }
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, activity, Constant.GET_ALL_PRODUCTS, params, false);
    }

    public void GetHomeData() {
        nestedScrollView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();

        if (swipeTimer != null) {
            swipeTimer.cancel();
        }
        Map<String, String> params = new HashMap<>();
        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            params.put(Constant.USER_ID, session.getData(Constant.ID));
        }
        if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
            params.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
        }

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        GetOfferImage(jsonObject.getJSONArray(Constant.OFFER_IMAGES));
                        GetCategory(jsonObject);
                        SectionProductRequest(jsonObject.getJSONArray(Constant.SECTIONS));
                        GetSlider(jsonObject.getJSONArray(Constant.SLIDER_IMAGES));
                        if (Constant.SHOW_SELLERS_IN_HOME_PAGE) {
                            GetSeller(jsonObject.getJSONArray(Constant.SELLER));
                        } else {
                            lytSeller.setVisibility(View.GONE);
                        }
                    } else {
                        nestedScrollView.setVisibility(View.VISIBLE);
                        mShimmerViewContainer.setVisibility(View.GONE);
                        mShimmerViewContainer.stopShimmer();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    nestedScrollView.setVisibility(View.VISIBLE);
                    mShimmerViewContainer.setVisibility(View.GONE);
                    mShimmerViewContainer.stopShimmer();

                }
            }
        }, activity, Constant.GET_ALL_DATA_URL, params, false);
    }

    public void GetOfferImage(JSONArray jsonArray) {
        ArrayList<String> offerList = new ArrayList<>();
        try {
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    offerList.add(object.getString(Constant.IMAGE));
                }
                offerView.setAdapter(new OfferAdapter(offerList, R.layout.offer_lyt));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    void GetCategory(JSONObject object) {
        categoryArrayList = new ArrayList<>();
        try {
            int visible_count;
            int column_count;

            JSONArray jsonArray = object.getJSONArray(Constant.CATEGORIES);

            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Category category = new Gson().fromJson(jsonObject.toString(), Category.class);
                    categoryArrayList.add(category);
                }

                if (!object.getString("style").equals("")) {
                    if (object.getString("style").equals("style_1")) {
                        visible_count = Integer.parseInt(object.getString("visible_count"));
                        column_count = Integer.parseInt(object.getString("column_count"));
                        categoryRecyclerView.setLayoutManager(new GridLayoutManager(activity, column_count));
                        categoryRecyclerView.setAdapter(new CategoryAdapter(activity, categoryArrayList, R.layout.lyt_category_grid, "home", visible_count));
                    } else if (object.getString("style").equals("style_2")) {
                        visible_count = Integer.parseInt(object.getString("visible_count"));
                        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
                        categoryRecyclerView.setAdapter(new CategoryAdapter(activity, categoryArrayList, R.layout.lyt_category_list, "home", visible_count));
                    }
                } else {
                    categoryRecyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
                    categoryRecyclerView.setAdapter(new CategoryAdapter(activity, categoryArrayList, R.layout.lyt_category_list, "home", 6));
                }
            } else {
                lytCategory.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void SectionProductRequest(JSONArray jsonArray) {  //json request for product search
        sectionList = new ArrayList<>();
        try {
            for (int j = 0; j < jsonArray.length(); j++) {
                Category section = new Category();
                JSONObject jsonObject = jsonArray.getJSONObject(j);
                section.setName(jsonObject.getString(Constant.TITLE));
                section.setId(jsonObject.getString(Constant.ID));
                section.setStyle(jsonObject.getString(Constant.SECTION_STYLE));
                section.setSubtitle(jsonObject.getString(Constant.SHORT_DESC));
                JSONArray productArray = jsonObject.getJSONArray(Constant.PRODUCTS);
                section.setProductList(ApiConfig.GetProductList(productArray));
                sectionList.add(section);
            }
            sectionView.setVisibility(View.VISIBLE);
            SectionAdapter sectionAdapter = new SectionAdapter(activity, sectionList);
            sectionView.setAdapter(sectionAdapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    void GetSlider(JSONArray jsonArray) {
        sliderArrayList = new ArrayList<>();
        try {
            size = jsonArray.length();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Slider slider = new Gson().fromJson(jsonObject.toString(), Slider.class);
                sliderArrayList.add(slider);
            }
            mPager.setAdapter(new SliderAdapter(sliderArrayList, activity, R.layout.lyt_slider, "home"));
            ApiConfig.addMarkers(0, sliderArrayList, mMarkersLayout, activity);
            handler = new Handler();
            Update = () -> {
                if (currentPage == size) {
                    currentPage = 0;
                }
                try {
                    mPager.setCurrentItem(currentPage++, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            swipeTimer = new Timer();
            swipeTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(Update);
                }
            }, timerDelay, timerWaiting);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        nestedScrollView.setVisibility(View.VISIBLE);
        mShimmerViewContainer.setVisibility(View.GONE);
        mShimmerViewContainer.stopShimmer();
    }

    void GetSeller(JSONArray jsonArray) {
        try {
            sellerArrayList = new ArrayList<>();
            if (jsonArray.length() > 0) {
                lytSeller.setVisibility(View.VISIBLE);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Seller seller = new Gson().fromJson(jsonObject.toString(), Seller.class);
                    sellerArrayList.add(seller);
                }

                sellerRecyclerView.setLayoutManager(new GridLayoutManager(activity, Constant.GRID_COLUMN));
                sellerRecyclerView.setAdapter(new SellerAdapter(activity, sellerArrayList, R.layout.lyt_seller, "home", 6));
            } else {
                lytSeller.setVisibility(View.GONE);
            }
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.invalidateOptionsMenu();
        ApiConfig.GetSettings(activity);
        hideKeyboard();
    }

    public void hideKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        this.menu = menu;
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.toolbar_cart).setVisible(true);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(searchVisible);
    }

}