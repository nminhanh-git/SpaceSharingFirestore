package com.example.nminhanh.spacesharing;

import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nminhanh.spacesharing.Fragment.AddSpacePages.AddAddressFragment;
import com.example.nminhanh.spacesharing.Fragment.AddSpacePages.AddDescriptionFragment;
import com.example.nminhanh.spacesharing.Fragment.AddSpacePages.AddOtherFragment;
import com.example.nminhanh.spacesharing.Fragment.AddSpacePages.AddPagerAdapter;
import com.example.nminhanh.spacesharing.Model.Space;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.imperiumlabs.geofirestore.GeoFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import github.chenupt.springindicator.SpringIndicator;

public class AddSpaceActivity extends AppCompatActivity implements AddAddressFragment.AddressReceiver, AddDescriptionFragment.DescriptionReceiver, AddOtherFragment.OtherReceiver {

    private static final String TAG = "MA:AddSpaceActivity";
    Toolbar mToolbar;
    TextView mBtnCancel;
    TextView mBtnContinue;

    TextView mTextviewNote;

    NonSwipeViewPager mViewPagerAdd;
    SpringIndicator mIndicator;

    Space currentSpace;
    ArrayList<String> mImagePath;
    StepContinueListener listener;
    public static final String SPACE_COLLECTION = "space";

    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    FirebaseFirestore db;

    double latitude;
    double longitude;
    boolean canAddressContinue = true;
    boolean canDescriptionContinue = true;
    boolean canOtherContinue = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_space);
        currentSpace = new Space();

        db = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        if (mFirebaseAuth.getCurrentUser() != null) {
            mFirebaseUser = mFirebaseAuth.getCurrentUser();
        }

        initialize();
    }

    private void initialize() {
        mToolbar = findViewById(R.id.add_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mBtnCancel = findViewById(R.id.add_btn_cancel);
        mBtnContinue = findViewById(R.id.add_btn_continue);

        mTextviewNote = findViewById(R.id.add_textview_note);
        String text = "Những mục có dấu <font color=#FF9800>*</font> là những mục bắt buộc";
        mTextviewNote.setText(Html.fromHtml(text));

        mViewPagerAdd = findViewById(R.id.add_viewpager);
        AddPagerAdapter adapter = new AddPagerAdapter(this, getSupportFragmentManager());
        mViewPagerAdd.setAdapter(adapter);
        mViewPagerAdd.setSwipingEnabled(false);

        mIndicator = findViewById(R.id.add_indicator);
        mIndicator.setViewPager(mViewPagerAdd);

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (mViewPagerAdd.getCurrentItem()) {
                    case 0:
                        finish();
                        break;
                    case 1:
                        mBtnCancel.setText("Hủy");
                        mBtnContinue.setText("Tiếp tục");
                        mViewPagerAdd.setCurrentItem(0);
                        break;
                    case 2:
                        mBtnCancel.setText("Trở về");
                        mBtnContinue.setText("Tiếp tục");
                        mViewPagerAdd.setCurrentItem(1);
                        break;
                }
            }
        });

        mBtnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mViewPagerAdd.getCurrentItem()) {
                    case 0:
                        for (Fragment f : getSupportFragmentManager().getFragments()) {
                            if (f instanceof AddAddressFragment) {
                                listener = (StepContinueListener) f;
                                break;
                            }
                        }
                        listener.onContinue();
                        if (canAddressContinue) {
                            mBtnCancel.setText("Trở về");
                            mBtnContinue.setText("Tiếp tục");
                            mViewPagerAdd.setCurrentItem(1);
                        }
                        break;
                    case 1:
                        for (Fragment f : getSupportFragmentManager().getFragments()) {
                            if (f instanceof AddDescriptionFragment) {
                                listener = (StepContinueListener) f;
                                break;
                            }
                        }
                        listener.onContinue();
                        if (canDescriptionContinue) {
                            mBtnCancel.setText("Trở về");
                            mBtnContinue.setText("Lưu");
                            mViewPagerAdd.setCurrentItem(2);
                        }
                        break;
                    case 2:
                        for (Fragment f : getSupportFragmentManager().getFragments()) {
                            if (f instanceof AddOtherFragment) {
                                listener = (StepContinueListener) f;
                                break;
                            }
                        }
                        listener.onContinue();
                        if (canOtherContinue) {
                            saveNewSpaceObject(currentSpace);
                            finish();
                        }
                        break;
                }
            }
        });
    }

    private void saveNewSpaceObject(final Space currentSpace) {
        final CollectionReference mSpacesCollRef = db.collection(SPACE_COLLECTION);
        mSpacesCollRef.add(currentSpace).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                currentSpace.setId(documentReference.getId());
                mSpacesCollRef.document(currentSpace.getId()).set(currentSpace);

                GeoFirestore mGeoFirestore = new GeoFirestore(mSpacesCollRef);
                mGeoFirestore.setLocation(currentSpace.getId(), new GeoPoint(latitude, longitude)
                        , new GeoFirestore.CompletionListener() {
                            @Override
                            public void onComplete(Exception e) {
                                if (e == null) {
                                    Log.d(TAG, "add location for this document to cloud firestore successfully!");
                                } else {
                                    Log.d(TAG, e.getMessage());
                                }
                            }
                        });

                DocumentReference currentSpaceRef = mSpacesCollRef.document(currentSpace.getId());
                Map<String, Object> updates = new HashMap<>();
                updates.put("timeAdded", FieldValue.serverTimestamp());
                currentSpaceRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "successful update timestamp");
                            if (mImagePath != null && mImagePath.size() != 0) {
                                putImageToStorage(currentSpace.getId());
                            }
                        } else {
                            Log.d(TAG, task.getException().getMessage());
                        }
                    }
                });
            }
        });

    }

    private void putImageToStorage(String key) {
        for (int i = 0; i < mImagePath.size(); i++) {
            if (!mImagePath.get(i).isEmpty()) {
                Uri uriPath = Uri.parse(mImagePath.get(i));
                StorageReference storageRef = FirebaseStorage.getInstance()
                        .getReference(mFirebaseUser.getUid())
                        .child(key).child(uriPath.getLastPathSegment());
                storageRef.putFile(uriPath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "successful upload images");
                        } else {
                            Log.d(TAG, task.getException().getMessage());
                        }
                    }
                });
            }
        }
    }


    @Override
    public void onAddressReceived(String title, String addressNumber, String cityId, String districtId, String wardId, String fullAddress, ArrayList<String> imagePath) {
        if (title.isEmpty() || addressNumber.isEmpty() || cityId.equals("-1") || districtId.equals("-1") || wardId.equals("-1")
                || fullAddress.isEmpty() || imagePath.isEmpty()) {
            canAddressContinue = false;
        } else {
            canAddressContinue = true;
            mImagePath = new ArrayList<>(imagePath);
            if (mImagePath != null && mImagePath.size() != 0) {
                currentSpace.setFirstImagePath(Uri.parse(mImagePath.get(0)).getLastPathSegment());
            } else {
                currentSpace.setFirstImagePath("không có gì hết á!");
            }
            currentSpace.setIdChu(mFirebaseUser.getUid());
            currentSpace.setTieuDe(title);
            currentSpace.setDiaChiPho(addressNumber);
            currentSpace.setThanhPhoId(cityId);
            currentSpace.setQuanId(districtId);
            currentSpace.setPhuongId(wardId);

            Geocoder mGeoCoder = new Geocoder(this);
            List<Address> addressesList;
            try {
                addressesList = mGeoCoder.getFromLocationName(fullAddress, 1);
                if (addressesList.size() > 0) {
                    Address currentAddress = addressesList.get(0);
                    latitude = currentAddress.getLatitude();
                    longitude = currentAddress.getLongitude();
                }
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDescriptionReceived(double size, double price, String des) {
        if (size == 0 || price == 0 || des.isEmpty()) {
            canDescriptionContinue = false;
        } else {
            canDescriptionContinue = true;
            currentSpace.setDienTich(size);
            currentSpace.setGia(price);
            currentSpace.setMoTa(des);
        }
    }


    @Override
    public void onOtherReceived(String type, String door, int bedRoom, int bathRoom, String
            detail) {
        if (type.equalsIgnoreCase(getResources().getStringArray(R.array.type_array)[0])) {
            canOtherContinue = false;
        } else if (type.equalsIgnoreCase(getResources().getStringArray(R.array.type_array)[1])
                || type.equalsIgnoreCase(getResources().getStringArray(R.array.type_array)[2])
                ) {
            canOtherContinue = !door.equalsIgnoreCase
                    (getResources().getStringArray(R.array.door_direction_array)[0]);
        } else {
            canOtherContinue = true;
            currentSpace.setLoai(type);
            currentSpace.setHuongCua(door);
            currentSpace.setSoPhongVeSinh(bathRoom);
            currentSpace.setSoPhongNgu(bedRoom);
            currentSpace.setThongTinPhapLy(detail);
        }

    }
}
