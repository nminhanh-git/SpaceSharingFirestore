package com.example.nminhanh.spacesharing;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
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
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.nminhanh.spacesharing.Fragment.MainPages.PagerAdapter;
import com.example.nminhanh.spacesharing.Fragment.MainPages.SearchFragment;
import com.example.nminhanh.spacesharing.Fragment.MainPages.ShowFacebookLoadingListener;
import com.example.nminhanh.spacesharing.Fragment.MainPages.SignOutListener;
import com.example.nminhanh.spacesharing.Model.City;
import com.example.nminhanh.spacesharing.Model.District;
import com.example.nminhanh.spacesharing.Model.Ward;
import com.example.nminhanh.spacesharing.Utils.AddressUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;

import io.apptik.widget.MultiSlider;

public class MainActivity extends AppCompatActivity
        implements ViewPager.OnPageChangeListener,
        SignOutListener,
        ShowFacebookLoadingListener,
        FetchAddressTask.onGetAddressCompletedListener {

    static final int REQUEST_ADD = 1;
    private static final int REQUEST_LOCATION_PERMISSION_CODE = 1;

    Toolbar mToolbar;
    ImageView mImageToolbarLogo;
    ViewPager mViewPager;
    BottomNavigationView mNavigationView;
    Menu mOptionMenu;
    RelativeLayout mLayoutFacebookLoading;
    ImageView mImageFacebookLoading;
    FirebaseAuth mFirebaseAuth;

    HashMap<String, String> filters;

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

    Spinner mFilterSpinnerType;
    String type;

    CheckBox mCheckBoxSize;
    TextView mTextViewSizeStart;
    TextView mTextViewSizeEnd;
    MultiSlider mMultiSliderSize;
    TextView mViewSizeUnselect;
    int sizeStart;
    int sizeEnd;

    CheckBox mCheckBoxPrice;
    Spinner mSpinnerPrice;
    String priceSortDirection;

    FusedLocationProviderClient mFusedLocationProviderClient;
    LocationCallback mLocationCallback;
    String mLatestAddress;
    boolean isTrackingLocation = false;

    ImageView mImageLocation;
    TextView mTextViewLocationAddress;
    TextView mTextViewLocationLoading;
    Button mBtnLocationStartLocate;
    Button mBtnLocationCancel;
    Button mBtnLocationFilter;
    AnimatorSet mAnimLocationImage;
    Location mLatestTranslatedLocation;

    GoToTopEventListener goToTopEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAddressUtils = new AddressUtils(this);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (isTrackingLocation) {
                    new FetchAddressTask(MainActivity.this)
                            .execute(locationResult.getLastLocation());
                }
            }
        };
    }

    void initialize() {
        // Initialize toolbar
        mToolbar = findViewById(R.id.main_toolbar);
        this.setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mImageToolbarLogo = mToolbar.findViewById(R.id.main_image_toolbar_logo);
        Glide.with(this)
                .asBitmap()
                .load(R.drawable.logo_2)
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
                        if (mViewPager.getCurrentItem() == 0) {
                            for (Fragment f : getSupportFragmentManager().getFragments()) {
                                if (f instanceof SearchFragment) {
                                    goToTopEventListener = (GoToTopEventListener) f;
                                    break;
                                }
                            }
                            goToTopEventListener.GoToTop();
                        }
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
                    intent.putExtra("command", "add space");
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

        View mDialogView = inflater.inflate(R.layout.dialog_filter_search_layout, null);
        Button mButtonCancel = mDialogView.findViewById(R.id.filter_dialog_cancel);
        Button mButtonOK = mDialogView.findViewById(R.id.filter_dialog_ok);

        mLayoutNearby = mDialogView.findViewById(R.id.filter_nearby_layout);

        mFilterSpinnerCity = mDialogView.findViewById(R.id.filter_dialog_spinner_city);
        mFilterSpinnerDistrict = mDialogView.findViewById(R.id.filter_dialog_spinner_district);
        mFilterSpinnerWard = mDialogView.findViewById(R.id.filter_dialog_spinner_ward);

        mFilterSpinnerType = mDialogView.findViewById(R.id.filter_dialog_spinner_type);

        mTextViewSizeStart = mDialogView.findViewById(R.id.size_start);
        mTextViewSizeEnd = mDialogView.findViewById(R.id.size_end);
        mMultiSliderSize = mDialogView.findViewById(R.id.slider_size);

        mCheckBoxSize = mDialogView.findViewById(R.id.filter_checkbox_size);
        mViewSizeUnselect = mDialogView.findViewById(R.id.filter_size_unselected);

        mCheckBoxPrice = mDialogView.findViewById(R.id.filter_checkbox_price);
        mSpinnerPrice = mDialogView.findViewById(R.id.filter_dialog_spinner_price);
        mSpinnerPrice.setEnabled(false);
        filters = new HashMap<>();

        final AlertDialog mDialog = builder.setView(mDialogView).create();
        ColorDrawable dialogBackground = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(dialogBackground, 40, 50, 40, 50);
        mDialog.getWindow().setBackgroundDrawable(inset);

        initializeFilterAddressSpinnerData();
        initializeSliders();
        initializeCheckboxes();

        ArrayAdapter<CharSequence> mTypeAdapter = ArrayAdapter.createFromResource(this, R.array.type_array, android.R.layout.simple_spinner_dropdown_item);
        mFilterSpinnerType.setAdapter(mTypeAdapter);
        mFilterSpinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    type = "";
                    if (filters.containsKey("type")) {
                        filters.remove("type");
                    }
                } else {
                    type = (String) mFilterSpinnerType.getItemAtPosition(position);
                    filters.put("type", type);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                type = "";
                if (filters.containsKey("type")) {
                    filters.remove("type");
                }
            }
        });

        mLayoutNearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showNearbyDialog();
                Intent MapIntent = new Intent(MainActivity.this, MapsSearchActivity.class);
                startActivity(MapIntent);
                mDialog.dismiss();
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

    private void showNearbyDialog() {
        AlertDialog.Builder mDialogBuilderLocation = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View mDialogView = inflater.inflate(R.layout.dialog_location_layout, null);

        mImageLocation = mDialogView.findViewById(R.id.dialog_location_image);
        mTextViewLocationAddress = mDialogView.findViewById(R.id.dialog_location_address);
        mTextViewLocationLoading = mDialogView.findViewById(R.id.dialog_location_text_view_loading);
        mBtnLocationStartLocate = mDialogView.findViewById(R.id.dialog_location_button_start_locate);
        mBtnLocationCancel = mDialogView.findViewById(R.id.dialog_location_btn_cancel);
        mBtnLocationFilter = mDialogView.findViewById(R.id.dialog_location_btn_filter);
        mAnimLocationImage = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.rotate);
        mAnimLocationImage.setTarget(mImageLocation);

        final AlertDialog mDialogLocation = mDialogBuilderLocation.setView(mDialogView).create();
        InsetDrawable insetDrawable = new InsetDrawable(new ColorDrawable(Color.TRANSPARENT), 50, 200, 50, 200);
        mDialogLocation.getWindow().setBackgroundDrawable(insetDrawable);

        mBtnLocationCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogLocation.dismiss();
            }
        });

        mBtnLocationStartLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isTrackingLocation) {
                    startTrackingLocation();
                } else {
                    stopTrackingLocation();
                }
            }
        });

        mBtnLocationFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nearbyIntent = new Intent(MainActivity.this, FilterResultActivity.class);
                filters.put("nearby", "");
                nearbyIntent.putExtra("filters", filters);
                nearbyIntent.putExtra("latitude", mLatestTranslatedLocation.getLatitude());
                nearbyIntent.putExtra("longitude", mLatestTranslatedLocation.getLongitude());
                nearbyIntent.putExtra("address", mLatestAddress);
                startActivity(nearbyIntent);
                mDialogLocation.dismiss();
            }
        });
        mDialogLocation.show();
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

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void startTrackingLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION_CODE);
        } else {
            mFusedLocationProviderClient.requestLocationUpdates(
                    getLocationRequest(), mLocationCallback, null);

            mBtnLocationStartLocate.setText("Ngừng định vị");
            mTextViewLocationLoading.setVisibility(View.VISIBLE);
            mAnimLocationImage.start();
            isTrackingLocation = true;
        }
    }

    private void stopTrackingLocation() {
        if (isTrackingLocation) {
            isTrackingLocation = false;
            mBtnLocationStartLocate.setText("Định vị");
            mAnimLocationImage.end();
            mTextViewLocationLoading.setVisibility(View.GONE);
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onGetAddressCompleted(String result, Location location) {
        mLatestAddress = result;
        mTextViewLocationAddress.setText(result);
        mLatestTranslatedLocation = location;
        mBtnLocationFilter.setBackgroundResource(R.drawable.button_sign_in_background);
        mBtnLocationFilter.setEnabled(true);
    }

    @Override
    public void onSignOut() {
        Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
        mViewPager.setCurrentItem(0, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOCATION_PERMISSION_CODE) {
            if (resultCode == RESULT_OK) {
                startTrackingLocation();
            }
        }
    }

    @Override
    protected void onStop() {
        if (isTrackingLocation) {
            stopTrackingLocation();
        }
        super.onStop();
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
