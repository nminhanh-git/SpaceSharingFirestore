package com.example.nminhanh.spacesharing;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import github.chenupt.springindicator.SpringIndicator;

public class AddSpaceActivity extends AppCompatActivity implements AddAddressFragment.AddressReceiver, AddDescriptionFragment.DescriptionReceiver, AddOtherFragment.OtherReceiver {

    private static final String TAG = "MA:AddSpaceActivity";
    Toolbar mToolbar;
    Button mBtnCancel;
    Button mBtnContinue;

    TextView mTextviewNote;

    NonSwipeViewPager mViewPagerAdd;
    SpringIndicator mIndicator;

    Space currentSpace;
    ArrayList<String> mImagePath;
    StepContinueListener listener;
    public static final String SPACE_CHILD = "space";

    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    FirebaseFirestore db;

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
        String text = "Những mục có dấu <font color=#e83841>*</font> là những mục bắt buộc";
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
                        mBtnCancel.setText("Trở về");
                        mBtnContinue.setText("Tiếp tục");
                        listener = (StepContinueListener) getSupportFragmentManager().getFragments().get(0);
                        listener.onContinue();
                        mViewPagerAdd.setCurrentItem(1);
                        break;
                    case 1:
                        mBtnCancel.setText("Trở về");
                        mBtnContinue.setText("Lưu");
                        listener = (StepContinueListener) getSupportFragmentManager().getFragments().get(1);
                        listener.onContinue();
                        mViewPagerAdd.setCurrentItem(2);
                        break;
                    case 2:
                        listener = (StepContinueListener) getSupportFragmentManager().getFragments().get(1);
                        listener.onContinue();
                        saveNewSpaceObject(currentSpace);
                        finish();
                        break;
                }
            }
        });
    }

    private void saveNewSpaceObject(final Space currentSpace) {
//        final DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference();
//        final String[] key = new String[1];
//        dbReference.child(SPACE_CHILD).push().setValue(currentSpace, new DatabaseReference.CompletionListener() {
//            @Override
//            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
//                if (databaseError == null) {
//                    key[0] = databaseReference.getKey();
//                    currentSpace.setId(key[0]);
//                    dbReference.child(SPACE_CHILD).child(key[0]).setValue(currentSpace);
//                    if (mImagePath != null && mImagePath.size() != 0) {
//                        putImageToStorage(key);
//                    }
//                    Toast.makeText(AddSpaceActivity.this, key[0], Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(AddSpaceActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
        final CollectionReference mSpacesCollRef = db.collection("space");
        mSpacesCollRef.add(currentSpace).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                currentSpace.setId(documentReference.getId());
                mSpacesCollRef.document(currentSpace.getId()).set(currentSpace);

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
    public void onAddressReceived(String title, String addressNumber, String cityId, String districtId, String wardId, ArrayList<String> imagePath) {
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
    }

    @Override
    public void onDescriptionReceived(double size, double price, String des) {
        currentSpace.setDienTich(size);
        currentSpace.setGia(price);
        currentSpace.setMoTa(des);
    }


    @Override
    public void onOtherReceived(String type, String door, int bedRoom, int bathRoom, String
            detail) {
        currentSpace.setLoai(type);
        currentSpace.setHuongCua(door);
        currentSpace.setSoPhongVeSinh(bathRoom);
        currentSpace.setSoPhongNgu(bedRoom);
        currentSpace.setThongTinPhapLy(detail);
    }
}
