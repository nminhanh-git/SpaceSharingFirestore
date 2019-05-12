package com.example.nminhanh.spacesharing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.imperiumlabs.geofirestore.GeoFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import github.chenupt.springindicator.SpringIndicator;

public class AddSpaceActivity extends AppCompatActivity implements
        AddAddressFragment.AddressReceiver, AddAddressFragment.AddressInflatedListener,
        AddDescriptionFragment.DescriptionReceiver, AddDescriptionFragment.DescriptionInflatedListener,
        AddOtherFragment.OtherReceiver, AddOtherFragment.OtherInflatedListener {

    private static final String TAG = "MA:AddSpaceActivity";
    Toolbar mToolbar;
    TextView mBtnCancel;
    TextView mBtnContinue;

    TextView mTextviewNote;

    NonSwipeViewPager mViewPagerAdd;
    SpringIndicator mIndicator;

    RelativeLayout mAddLayoutLoading;
    TextView mTextViewLoading;

    Space currentSpace;
    ArrayList<Bitmap> mImagePath;
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

    AddressOldDataReceiver addressOldDataReceiver;
    DescriptionOldDataReceiver descriptionOldDataReceiver;
    OtherOldDataReceiver otherOldDataReceiver;
    boolean isAddressOldDataSent = false;
    boolean isDescriptionOldDataSent = false;
    boolean isOtherOldDataSent = false;
    String command = "";

    AddCanceledListener cancelListener;


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

        Intent intent = getIntent();
        command = intent.getStringExtra("command");
        if (command.equalsIgnoreCase("edit space")) {
            currentSpace = (Space) intent.getSerializableExtra("current space");
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

        mAddLayoutLoading = findViewById(R.id.add_loading_layout);
        mTextViewLoading = findViewById(R.id.add_progress_text);

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (mViewPagerAdd.getCurrentItem()) {
                    case 0:
                        for (Fragment f : getSupportFragmentManager().getFragments()) {
                            if (f instanceof AddAddressFragment) {
                                cancelListener = (AddCanceledListener) f;
                                break;
                            }
                        }
                        cancelListener.onCanceled();
                        setResult(RESULT_CANCELED);
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
                            saveSpaceObject(currentSpace);
                            if (command.equalsIgnoreCase("edit space")) {
                                mTextViewLoading.setText("Đang cập nhật dữ liệu...");
                            }
                            mAddLayoutLoading.setVisibility(View.VISIBLE);

                        }
                        break;
                }
            }
        });
    }

    private void saveSpaceObject(final Space currentSpace) {
        final CollectionReference mSpacesCollRef = db.collection(SPACE_COLLECTION);
        Task saveTask;
        if (command.equalsIgnoreCase("edit space")) {
            saveTask = mSpacesCollRef.document(currentSpace.getId()).set(currentSpace);
            saveTask.addOnSuccessListener(new OnSuccessListener() {
                @Override
                public void onSuccess(Object o) {
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
        } else {
            saveTask = mSpacesCollRef.add(currentSpace);
            saveTask.addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
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
    }

    private void putImageToStorage(final String key) {
        for (int i = 1; i <= 5; i++) {
            if (!mImagePath.get(i - 1).equals(null)) {
                StorageReference storageRef = FirebaseStorage.getInstance()
                        .getReference(mFirebaseUser.getUid())
                        .child(key).child(String.valueOf(i));

                final int finalI = i;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Bitmap bitmap = mImagePath.get(i - 1);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] bitmapData = baos.toByteArray();

                storageRef.putBytes(bitmapData).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "successful upload image " + finalI);
                            Log.d(TAG, "update first image path successfully");
                            mAddLayoutLoading.setVisibility(View.GONE);
                            if (command.equalsIgnoreCase("edit space")) {
                                setResult(RESULT_OK);
                            }
                            finish();
                        } else {
                            Log.d(TAG, task.getException().getMessage());
                            Toast.makeText(AddSpaceActivity.this, "upload image " + (finalI + 1) + "fail", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }


    @Override
    public void onAddressReceived(String title, String addressNumber, String cityId, String
            districtId, String wardId, String fullAddress, ArrayList<Bitmap> imagePath) {
        if (title.isEmpty() || addressNumber.isEmpty() || cityId.equals("-1") || districtId.equals("-1") || wardId.equals("-1")
                || fullAddress.isEmpty() || imagePath.size() < 5) {
            canAddressContinue = false;
        } else {
            canAddressContinue = true;
            mImagePath = new ArrayList<>(imagePath);
            currentSpace.setIdChu(mFirebaseUser.getUid());
            currentSpace.setTieuDe(title);
            currentSpace.setDiaChiPho(addressNumber);
            currentSpace.setThanhPhoId(cityId);
            currentSpace.setQuanId(districtId);
            currentSpace.setPhuongId(wardId);
            currentSpace.setDiaChiDayDu(fullAddress);

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
    public void onDescriptionReceived(double size, int price, int prepaidMonth, String
            description) {
        if (size == 0 || price == 0 || description.isEmpty()) {
            canDescriptionContinue = false;
        } else {
            canDescriptionContinue = true;
            currentSpace.setDienTich(size);
            currentSpace.setGia(price);
            currentSpace.setMoTa(description);
            currentSpace.setThangCoc(prepaidMonth);
        }
    }


    @Override
    public void onOtherReceived(String type, String door, int bedRoom, int bathRoom,
                                int electricPrice, int waterPrice) {
        if (type.equalsIgnoreCase(getResources().getStringArray(R.array.type_array)[0])) {
            canOtherContinue = false;
        } else if (type.equalsIgnoreCase(getResources().getStringArray(R.array.type_array)[1])
                || type.equalsIgnoreCase(getResources().getStringArray(R.array.type_array)[2])
                ) {
            if (electricPrice == 0 || waterPrice == 0) {
                canOtherContinue = false;
            } else {
                canOtherContinue = true;
                currentSpace.setLoai(type);
                currentSpace.setHuongCua(door);
                currentSpace.setSoPhongVeSinh(bathRoom);
                currentSpace.setSoPhongNgu(bedRoom);
                currentSpace.setGiaDien(electricPrice);
                currentSpace.setGiaNuoc(waterPrice);
            }
        } else {
            canOtherContinue = true;
            currentSpace.setLoai(type);
        }

    }

    @Override
    public void onAddressInflated() {
        if (command.equalsIgnoreCase("edit space") && !isAddressOldDataSent) {
            for (Fragment f : getSupportFragmentManager().getFragments()) {
                if (f instanceof AddAddressFragment) {
                    addressOldDataReceiver = (AddressOldDataReceiver) f;
                    break;
                }
            }
            Bundle b = new Bundle();

            b.putString("id", currentSpace.getId());
            b.putString("idChu", currentSpace.getIdChu());
            b.putString("title", currentSpace.getTieuDe());
            b.putString("address number", currentSpace.getDiaChiPho());
            b.putString("cityId", currentSpace.getThanhPhoId());
            b.putString("districtId", currentSpace.getQuanId());
            b.putString("wardId", currentSpace.getPhuongId());
            b.putString("command", command);

            addressOldDataReceiver.onReceive(b);
            isAddressOldDataSent = true;
        }
    }

    @Override
    public void onDescriptionInflated() {
        if (command.equalsIgnoreCase("edit space") && !isDescriptionOldDataSent) {
            for (Fragment f : getSupportFragmentManager().getFragments()) {
                if (f instanceof AddDescriptionFragment) {
                    descriptionOldDataReceiver = (DescriptionOldDataReceiver) f;
                    break;
                }
            }
            Bundle b = new Bundle();

            b.putDouble("size", currentSpace.getDienTich());
            b.putInt("price", currentSpace.getGia());
            b.putInt("prepaid", currentSpace.getThangCoc());
            b.putString("description", currentSpace.getMoTa());

            descriptionOldDataReceiver.onReceive(b);
            isDescriptionOldDataSent = true;
        }
    }

    @Override
    public void onOtherInflated() {
        if (command.equalsIgnoreCase("edit space") && !isOtherOldDataSent) {
            for (Fragment f : getSupportFragmentManager().getFragments()) {
                if (f instanceof AddOtherFragment) {
                    otherOldDataReceiver = (OtherOldDataReceiver) f;
                    break;
                }
            }

            Bundle b = new Bundle();
            b.putInt("electric", currentSpace.getGiaDien());
            b.putInt("water", currentSpace.getGiaNuoc());
            b.putInt("bed", currentSpace.getSoPhongNgu());
            b.putInt("bath", currentSpace.getSoPhongVeSinh());
            b.putString("type", currentSpace.getLoai());
            b.putString("door", currentSpace.getHuongCua());

            otherOldDataReceiver.onReceive(b);
            isOtherOldDataSent = true;
        }
    }
}
