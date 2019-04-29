package com.example.nminhanh.spacesharing;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.nminhanh.spacesharing.Fragment.MainPages.PagerAdapter;
import com.example.nminhanh.spacesharing.Fragment.MainPages.ShowFacebookLoadingListener;
import com.example.nminhanh.spacesharing.Fragment.MainPages.SignOutListener;
import com.example.nminhanh.spacesharing.Model.City;
import com.example.nminhanh.spacesharing.Model.District;
import com.example.nminhanh.spacesharing.Model.Ward;
import com.example.nminhanh.spacesharing.Utils.AddressUtils;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.apptik.widget.MultiSlider;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, SignOutListener, ShowFacebookLoadingListener {

    static final int REQUEST_ADD = 1;

    Toolbar mToolbar;
    ImageView mImageToolbarLogo;
    ViewPager mViewPager;
    BottomNavigationView mNavigationView;
    Menu mOptionMenu;
    RelativeLayout mLayoutFacebookLoading;
    ImageView mImageFacebookLoading;
    FirebaseAuth mFirebaseAuth;

    RelativeLayout mLayoutNearby;

    Spinner mFilterSpinnerCity;
    Spinner mFilterSpinnerDistrict;
    ArrayAdapter<String> mDistrictAdapter;
    ArrayAdapter<String> mWardAdapter;
    Spinner mFilterSpinnerWard;
    ArrayList<District> districtList;
    ArrayList<Ward> wardList;
    ArrayList<String> districtNameList;
    ArrayList<String> wardNameList;
    City currentCity;
    District currentDistrict;
    Ward currentWard;
    AddressUtils mAddressUtils;

    CheckBox mCheckBoxSize;
    TextView mTextViewSizeStart;
    TextView mTextViewSizeEnd;
    MultiSlider mMultiSliderSize;
    TextView mViewSizeUnselect;
    int sizeStart;
    int sizeEnd;

    CheckBox mCheckBoxPrice;
    Spinner mSpinnerPrice;
    HashMap<String, String> filters;
    String priceSortDirection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAddressUtils = new AddressUtils(this);
    }

    void initialize() {
        // Initialize toolbar
        mToolbar = findViewById(R.id.main_toolbar);
        this.setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mImageToolbarLogo = mToolbar.findViewById(R.id.main_image_toolbar_logo);
        Glide.with(this)
                .asBitmap()
                .load(R.drawable.logo)
                .into(mImageToolbarLogo);

        //Initialize ViewPager
        mViewPager = findViewById(R.id.main_pager);
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(adapter);

        mNavigationView = findViewById(R.id.navigation_view);
        mNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = 0;
                switch (menuItem.getItemId()) {
                    case R.id.action_search:
                        id = 0;

                        break;
                    case R.id.action_space:
                        id = 1;
                        break;
                    case R.id.action_favor:
                        id = 2;

                        break;
                    case R.id.action_account:
                        id = 3;
                        break;
                }
                invalidateOptionsMenu();
                mViewPager.setCurrentItem(id, true);
                return true;
            }
        });
        mViewPager.setOnPageChangeListener(this);

        mLayoutFacebookLoading = findViewById(R.id.account_facebook_loading);
        mImageFacebookLoading = findViewById(R.id.loading_image);
        Glide.with(this).load("https://media.giphy.com/media/eBb2W1OYVHou9l6W7N/giphy.gif").into(mImageFacebookLoading);
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        mNavigationView.getMenu().getItem(i).setChecked(true);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        mOptionMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem filterItem = menu.findItem(R.id.main_menu_item_filter);
        MenuItem addItem = menu.findItem(R.id.main_menu_item_add);
        switch (mViewPager.getCurrentItem()) {
            case 0:
                filterItem.setVisible(true);
                addItem.setVisible(false);
                break;
            case 1:
                filterItem.setVisible(false);
                addItem.setVisible(true);
                break;
            case 2:
            case 3:
                filterItem.setVisible(false);
                addItem.setVisible(false);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_menu_item_filter:
                showFilterDialog();
                break;
            case R.id.main_menu_item_add:
                if (mFirebaseAuth.getCurrentUser() != null) {
                    Intent intent = new Intent(MainActivity.this, AddSpaceActivity.class);
                    startActivityForResult(intent, REQUEST_ADD);
                } else {
                    Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                    startActivity(intent);
                    finish();
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final LayoutInflater inflater = getLayoutInflater();

        View mDialogView = inflater.inflate(R.layout.layout_dialog_filter_search, null);
        Button mButtonCancel = mDialogView.findViewById(R.id.filter_dialog_cancel);
        Button mButtonOK = mDialogView.findViewById(R.id.filter_dialog_ok);

        mLayoutNearby = mDialogView.findViewById(R.id.filter_nearby_layout);

        mFilterSpinnerCity = mDialogView.findViewById(R.id.filter_dialog_spinner_city);
        mFilterSpinnerDistrict = mDialogView.findViewById(R.id.filter_dialog_spinner_district);
        mFilterSpinnerWard = mDialogView.findViewById(R.id.filter_dialog_spinner_ward);

        mTextViewSizeStart = mDialogView.findViewById(R.id.size_start);
        mTextViewSizeEnd = mDialogView.findViewById(R.id.size_end);
        mMultiSliderSize = mDialogView.findViewById(R.id.slider_size);

        mCheckBoxSize = mDialogView.findViewById(R.id.filter_checkbox_size);
        mViewSizeUnselect = mDialogView.findViewById(R.id.filter_size_unselected);

        mCheckBoxPrice = mDialogView.findViewById(R.id.filter_checkbox_price);
        mSpinnerPrice = mDialogView.findViewById(R.id.filter_dialog_spinner_price);
        mSpinnerPrice.setEnabled(false);

        final AlertDialog mDialog = builder.setView(mDialogView).create();
        ColorDrawable dialogBackground = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(dialogBackground, 50);
        mDialog.getWindow().setBackgroundDrawable(inset);

        filters = new HashMap<>();
        initializeFilterAddressSpinnerData();
        initializeSliders();
        initializeCheckboxes();

        mLayoutNearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filters.put("nearby", "");
            }
        });
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mButtonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FilterResultActivity.class);
                intent.putExtra("filters", filters);
                intent.putExtra("city", currentCity);
                intent.putExtra("district", currentDistrict);
                intent.putExtra("ward", currentWard);
                intent.putExtra("size start", sizeStart);
                intent.putExtra("size end", sizeEnd);

                startActivity(intent);
                mDialog.dismiss();
            }
        });
        mDialog.show();
    }

    private void initializeFilterAddressSpinnerData() {
        districtList = new ArrayList<>();
        wardList = new ArrayList<>();
        districtNameList = new ArrayList<>();
        wardNameList = new ArrayList<>();

        currentCity = new City("-1", "Hãy chọn thành phố...");
        ArrayAdapter<CharSequence> mCityAdapter = ArrayAdapter.createFromResource(this, R.array.city_array, android.R.layout.simple_spinner_dropdown_item);
        mFilterSpinnerCity.setAdapter(mCityAdapter);
        mFilterSpinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        currentCity = new City("01", "Hà Nội");
                        break;
                    case 2:
                        currentCity = new City("48", "Đà Nẵng");
                        break;
                    case 3:
                        currentCity = new City("79", "TP.Hồ Chí Minh");
                        break;
                    default:
                        currentCity = new City("-1", "Hãy chọn thành phố...");
                        break;
                }
                refreshDistrictList();
                currentDistrict = districtList.get(0);
                refreshWardList();
                currentWard = wardList.get(0);
                mFilterSpinnerDistrict.setSelection(0, true);
                mFilterSpinnerWard.setSelection(0, true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentCity = new City("-1", "Hãy chọn thành phố...");
                refreshDistrictList();
                currentDistrict = districtList.get(0);
                refreshWardList();
                currentWard = wardList.get(0);
                mFilterSpinnerDistrict.setSelection(0, true);
                mFilterSpinnerWard.setSelection(0, true);
            }
        });
        refreshDistrictList();
        currentDistrict = districtList.get(0);
        mDistrictAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, districtNameList);
        mFilterSpinnerDistrict.setAdapter(mDistrictAdapter);
        mFilterSpinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentDistrict = districtList.get(position);
                refreshWardList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentDistrict = districtList.get(0);
                refreshWardList();
            }
        });

        refreshWardList();
        currentWard = wardList.get(0);
        mWardAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, wardNameList);
        mFilterSpinnerWard.setAdapter(mWardAdapter);
        mFilterSpinnerWard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentWard = wardList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentWard = wardList.get(0);
            }
        });

    }

    private void refreshDistrictList() {
        String districtListJSON = mAddressUtils.getJSONfromAssets("quan-huyen/" + currentCity.getId() + ".json");
        districtList.clear();
        districtList.addAll(mAddressUtils.getDistrictList(districtListJSON));

        districtNameList.clear();
        districtNameList.addAll(mAddressUtils.getDistrictNameList(districtList));

        if (mDistrictAdapter != null) {
            mDistrictAdapter.notifyDataSetChanged();
        }
    }

    private void refreshWardList() {
        String wardListJSON = mAddressUtils.getJSONfromAssets("xa-phuong/" + currentDistrict.getId() + ".json");
        wardList.clear();
        wardList.addAll(mAddressUtils.getWardList(wardListJSON));
        wardNameList.clear();
        wardNameList.addAll(mAddressUtils.getWardNameList(wardList));

        if (mWardAdapter != null) {
            mWardAdapter.notifyDataSetChanged();
        }
    }

    private void initializeSliders() {
        sizeStart = 0;
        sizeEnd = 100;
        mMultiSliderSize.setOnThumbValueChangeListener(new MultiSlider.OnThumbValueChangeListener() {
            @Override
            public void onValueChanged(MultiSlider multiSlider, MultiSlider.Thumb thumb, int thumbIndex, int value) {
                if (thumbIndex == 0) {
                    sizeStart = value;
                    mTextViewSizeStart.setText(sizeStart + "");
                } else {
                    sizeEnd = value;
                    mTextViewSizeEnd.setText(sizeEnd + "");
                }
            }
        });
    }

    private void initializeCheckboxes() {
        mCheckBoxSize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mViewSizeUnselect.setVisibility(View.GONE);
                    filters.put("size", "");
                } else {
                    mViewSizeUnselect.setVisibility(View.VISIBLE);
                    filters.remove("size");
                }
            }
        });
        final ArrayAdapter<CharSequence> mPriceAdapter = ArrayAdapter.createFromResource(this, R.array.price_order_array, android.R.layout.simple_spinner_dropdown_item);
        mSpinnerPrice.setAdapter(mPriceAdapter);
        mSpinnerPrice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    priceSortDirection = mPriceAdapter.getItem(position).toString();
                    filters.put("price", priceSortDirection);
                } else {
                    if (filters.containsKey("price")) {
                        filters.remove("price");
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mCheckBoxPrice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mSpinnerPrice.setEnabled(true);
                } else {
                    mSpinnerPrice.setSelection(0);
                    mSpinnerPrice.setEnabled(false);
                    filters.remove("price");
                }

            }
        });
    }

    @Override
    public void onSignOut() {
        Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
        mViewPager.setCurrentItem(0, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onShowingFacebookLoading() {
        mLayoutFacebookLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void onHidingFacebookLoading() {
        mLayoutFacebookLoading.setVisibility(View.GONE);
    }
}
