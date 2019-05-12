package com.example.nminhanh.spacesharing;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nminhanh.spacesharing.Model.Space;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;
import org.imperiumlabs.geofirestore.GeoQueryDataEventListener;

import java.util.HashMap;
import java.util.Map;

public class MapsSearchActivity extends FragmentActivity implements
        OnMapReadyCallback, GetLatLngAsyncTask.TranslateCompleteListener,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private static final int REQUEST_LOCATION_PERMISSION_CODE = 1;
    private static final float DEFAULT_ZOOM = 15;
    private static final String TAG = "MA:MapSearchActivity";
    private GoogleMap mMap;
    private LatLng mDefaultLatlng;
    private Location mCurrentLocation;

    private boolean isLocationPermissionGranted = false;
    FusedLocationProviderClient mFusedLocationProviderClient;

    ImageButton mBtnBack;
    FloatingActionButton mBtnLocation;
    ImageButton mBtnSearch;
    EditText mEditSearch;
    EditText mEditResultNumber;
    EditText mEditRadius;
    TextView mBtnFilter;
    RelativeLayout mLayoutFooter;

    RelativeLayout mLayoutInfo;
    ImageView mImageInfo;
    TextView mTextViewInfoTitle;
    TextView mTextViewInfoShortAddress;
    TextView mBtnGetDetail;

    String mCurrentAddressStr;
    int nearbyItemCount;
    Map<Marker, Space> markerInfoList;
    boolean isInfoLayoutVisible = false;
    FirebaseStorage mFirebaseStorage;
    GeoQuery geoQuery;
    Space currentMarkerSpace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_search);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFirebaseStorage = FirebaseStorage.getInstance();
        initialize();
        markerInfoList = new HashMap<>();

        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mBtnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });

        mBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mEditSearch.getText().toString().isEmpty()) {
                    new GetLatLngAsyncTask(MapsSearchActivity.this).execute(mCurrentAddressStr);
                }
            }
        });

        mEditSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    mCurrentAddressStr = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mEditResultNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    mEditResultNumber.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mEditRadius.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    mEditRadius.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mBtnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkForEmptyEditTextError()) {
                    resetFilterResult();
                    int resultNumber = Integer.valueOf(mEditResultNumber.getText().toString());
                    double radius = Double.valueOf(mEditRadius.getText().toString());
                    getFilterResultList(resultNumber, radius);

                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(mCurrentLocation.getLatitude(),
                                    mCurrentLocation.getLongitude())));
                    CircleOptions circleOptions = new CircleOptions()
                            .fillColor(R.color.map_circle_fill_color)
                            .strokeColor(R.color.map_circle_stroke_color)
                            .center(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))
                            .radius(radius * 1000);
                    Circle circle = mMap.addCircle(circleOptions);
                }
            }
        });

        mBtnGetDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsSearchActivity.this, SpaceDetailActivity.class);
                intent.putExtra("current space", currentMarkerSpace);
                startActivity(intent);
            }
        });
    }

    private void resetFilterResult() {
        mMap.clear();
        markerInfoList = new HashMap<>();
    }

    public void initialize() {
        mBtnBack = findViewById(R.id.map_search_btn_back);
        mBtnLocation = findViewById(R.id.map_btn_location);
        mBtnSearch = findViewById(R.id.map_btn_search);
        mEditResultNumber = findViewById(R.id.map_edit_result_number);
        mEditRadius = findViewById(R.id.map_edit_radius);
        mEditSearch = findViewById(R.id.map_edit_text_search);
        mBtnFilter = findViewById(R.id.map_btn_filter);
        mLayoutFooter = findViewById(R.id.map_footer_layout);
        mLayoutFooter.setVisibility(View.VISIBLE);

        mLayoutInfo = findViewById(R.id.map_marker_info_layout);
        mLayoutInfo.setVisibility(View.INVISIBLE);
        mImageInfo = findViewById(R.id.map_info_item_image);
        mTextViewInfoTitle = findViewById(R.id.map_info_item_title);
        mTextViewInfoShortAddress = findViewById(R.id.map_info_item_address);
        mBtnGetDetail = findViewById(R.id.map_info_item_get_detail);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(40, 255, 40, 255);

        // Add a marker in Sydney and move the camera
        mDefaultLatlng = new LatLng(21.0221485, 105.8017759);
        mMap.addMarker(new MarkerOptions().position(mDefaultLatlng));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLatlng, DEFAULT_ZOOM));

        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);
        requestForLocationPermission();
        getDeviceLocation();
    }

    private void requestForLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,

                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION_CODE);
        } else {
            isLocationPermissionGranted = true;
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // re-request perrmission to set up the location layout for map
                requestForLocationPermission();
            }
//          else{
//                if(shouldShowRequestPermissionRationale())
//            }
        }
    }

    private void getDeviceLocation() {
        if (isLocationPermissionGranted) {
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        mMap.clear();
                        mCurrentLocation = task.getResult();
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), DEFAULT_ZOOM
                        ));
                        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                        mMap.clear();
                        mMap.addMarker(markerOptions);
                    } else {
                        Log.d(TAG, "current location is null. Using default");
                        Log.d(TAG, "exception: " + task.getException().getMessage());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLatlng, DEFAULT_ZOOM));
                        mMap.clear();
                        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                        mMap.addMarker(markerOptions);
                    }
                }
            });
        } else {
            requestForLocationPermission();
        }
    }

    private boolean checkForEmptyEditTextError() {
        if (!mEditRadius.getText().toString().isEmpty() && mEditRadius.getError() == null
                && !mEditResultNumber.getText().toString().isEmpty() && mEditResultNumber.getError() == null
                && mCurrentLocation != null) {
            return false;
        }
        if (mCurrentLocation == null) {
            Toast.makeText(this, "Bạn chưa chọn vị trí để phân loại", Toast.LENGTH_SHORT).show();
        }
        if (mEditResultNumber.getText().toString().isEmpty()) {
            mEditResultNumber.setError("Bạn chưa nhập giới hạn số lượng kết quả");
        }
        if (mEditRadius.getText().toString().isEmpty()) {
            mEditRadius.setError("Bạn chưa nhập giới hạn bán kính");
        }
        return true;
    }

    private void getFilterResultList(final int resultNumber, double radius) {
        nearbyItemCount = 0;
        CollectionReference collectionReference = FirebaseFirestore
                .getInstance().collection("space");
        GeoPoint currentNearbyGeoPoint = new GeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        GeoFirestore geoFirestore = new GeoFirestore(collectionReference);
        geoQuery = geoFirestore.queryAtLocation(currentNearbyGeoPoint, radius);
        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
            @Override
            public void onDocumentEntered(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {
                if (nearbyItemCount < resultNumber) {
                    Log.d(TAG, "onDocEntered");
                    nearbyItemCount++;
                    Space currentSpace = documentSnapshot.toObject(Space.class);
                    LatLng currentLatLng = new LatLng(currentSpace.getL().get(0), currentSpace.getL().get(1));

                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(currentLatLng)
                            .title(currentSpace.getDiaChiDayDu());
                    if (currentSpace.getLoai().equalsIgnoreCase(
                            getResources().getStringArray(R.array.type_array)[1]
                    )) {
                        markerOptions.icon(
                                BitmapDescriptorFactory
                                        .fromBitmap(getBitmapFromResource(R.drawable.ic_house_marker)));
                    } else if (currentSpace.getLoai().equalsIgnoreCase(
                            getResources().getStringArray(R.array.type_array)[2])) {
                        markerOptions.icon(
                                BitmapDescriptorFactory
                                        .fromBitmap(getBitmapFromResource(R.drawable.ic_shop_marker)));
                    } else {
                        markerOptions.icon(
                                BitmapDescriptorFactory
                                        .fromBitmap(getBitmapFromResource(R.drawable.ic_house_marker)));
                    }

                    final Marker currentMarker = mMap.addMarker(markerOptions);
                    markerInfoList.put(currentMarker, currentSpace);
                } else {
                    geoQuery.removeAllListeners();
                    return;
                }
            }

            @Override
            public void onDocumentExited(DocumentSnapshot documentSnapshot) {
                Log.d(TAG, "onDocExited");

            }

            @Override
            public void onDocumentMoved(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {
                Log.d(TAG, "onDocMoved");

            }

            @Override
            public void onDocumentChanged(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {
                Log.d(TAG, "onDocChanged");
            }

            @Override
            public void onGeoQueryReady() {
                Log.d(TAG, "onGeoQueryReady");
            }

            @Override
            public void onGeoQueryError(Exception e) {
                Log.d(TAG, "onGeoQueryError" + e.getMessage());
            }
        });
    }

    @Override
    public void onTranslateComplete(LatLng latLng) {
        if (latLng == null) {
            Toast.makeText(this, "Không tìm được địa chỉ bạn vừa nhập, xin hãy thử lại", Toast.LENGTH_SHORT).show();
        } else {
            mCurrentLocation.setLatitude(latLng.latitude);
            mCurrentLocation.setLongitude(latLng.longitude);
            mMap.clear();
            mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                            new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()),
                            DEFAULT_ZOOM)
            );
            MarkerOptions options = new MarkerOptions().position(latLng)
                    .title("vị trí hiện tại");
            mMap.addMarker(options);
        }
    }

    public Bitmap getBitmapFromResource(int resourcePath) {
        Drawable drawable = ContextCompat.getDrawable(this, resourcePath);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth()
                , drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Space mSpace = markerInfoList.get(marker);
        Intent detailIntent = new Intent(MapsSearchActivity.this, SpaceDetailActivity.class);
        detailIntent.putExtra("current space", mSpace);
        startActivity(detailIntent);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (!markerInfoList.containsKey(marker)) {
            return false;
        } else {
            marker.showInfoWindow();
            if (!isInfoLayoutVisible) {
                isInfoLayoutVisible = true;
                Space currentSpace = markerInfoList.get(marker);

                StorageReference imageRef = mFirebaseStorage
                        .getReference(currentSpace.getIdChu())
                        .child(currentSpace.getId())
                        .child(1 + "");
                GlideApp.with(this)
                        .load(imageRef)
                        .into(mImageInfo);
                mTextViewInfoTitle.setText(currentSpace.getTieuDe());
                String[] addresArr = currentSpace.getDiaChiDayDu().split(",");
                StringBuilder addressBuilder = new StringBuilder();
                for (int i = 1; i < addresArr.length; i++) {
                    addressBuilder.append(addresArr[i]);
                    if (i != addresArr.length - 1) {
                        addressBuilder.append(",");
                    }
                }
                mTextViewInfoShortAddress.setText(addressBuilder.toString().trim());


                slideUp(mLayoutInfo);
                slideDown(mLayoutFooter);
                mMap.setPadding(40, 290, 40, 290);
                currentMarkerSpace = currentSpace;
                return true;
            }
        }
        return false;
    }

    public void slideUp(RelativeLayout layout) {
        layout.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,
                0,
                layout.getHeight()
                , 0);
        animate.setDuration(500);
        animate.setFillAfter(true);
        layout.startAnimation(animate);
    }

    public void slideDown(RelativeLayout layout) {
        TranslateAnimation animate = new TranslateAnimation(
                0,
                0,
                0,
                layout.getHeight() + 100);
        animate.setDuration(500);
        animate.setFillAfter(true);
        layout.startAnimation(animate);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (isInfoLayoutVisible) {
            isInfoLayoutVisible = false;
            slideDown(mLayoutInfo);
            slideUp(mLayoutFooter);
            mMap.setPadding(40, 255, 40, 255);
        }
    }
}
