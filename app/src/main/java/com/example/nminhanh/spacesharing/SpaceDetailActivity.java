package com.example.nminhanh.spacesharing;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nminhanh.spacesharing.Fragment.AddSpacePages.DetailImageAdapter;
import com.example.nminhanh.spacesharing.Model.Conversation;
import com.example.nminhanh.spacesharing.Model.Message;
import com.example.nminhanh.spacesharing.Model.Space;
import com.example.nminhanh.spacesharing.Service.MyNotiService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SpaceDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_EDIT_SPACE = 1;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final String TAG = "MA:SpaceDetailActivity";
    Toolbar mToolbar;
    ImageButton mBtnBack;
    ImageButton mBtnFavorite;
    ImageButton mBtnEdit;
    ImageButton mBtnDelete;

    ImageButton mBtnCall;
    ImageButton mBtnChat;
    ImageButton mBtnEmail;
    ConstraintLayout mLayoutCommunication;

    Button mBtnAdminDisable;
    Button mBtnAdminEnable;

    TextView mTextViewType;
    TextView mTextViewTitle;
    TextView mTextViewDate;

    TextView mTextViewPrice;
    TextView mTextViewSize;
    TextView mTextViewPrePaid;

    CircleImageView mImageOwnerAva;
    TextView mTextViewOwnerName;
    TextView mTextViewOwnerEmail;
    TextView mTextViewOwnerPhone;

    TextView mTextViewAddress;
    TextView mTextViewDescription;

    TextView mTextViewOtherType;
    TextView mTextViewDoor;
    TextView mTextViewElectric;
    TextView mTextViewWater;
    TextView mTextViewBed;
    TextView mTextViewBath;

    LinearLayout mLayoutDoor;
    LinearLayout mLayoutElectric;
    LinearLayout mLayoutWater;
    LinearLayout mLayoutBed;
    LinearLayout mLayoutBath;
    LinearLayout mLayoutAdminBtn;

    ImageView mImageIndicator1;
    ImageView mImageIndicator2;
    ImageView mImageIndicator3;
    ImageView mImageIndicator4;
    ImageView mImageIndicator5;

    SwitchCompat mSwitchStatus;
    TextView mTextViewStatus;
    ImageView mImageStatus;

    MapView mAddressMapView;

    RecyclerView mRecyclerViewImage;
    ArrayList<String> mImageNameList;

    Space mCurrentSpace;
    FirebaseFirestore mFirestore;
    CollectionReference mSpaceCollRef;
    CollectionReference mUserDataCollRef;
    FirebaseStorage mFirebaseStorage;
    DocumentReference mCurrentSpaceRef;

    FirebaseAuth mFirebaseAuth;
    FirebaseUser mCurrentUser;
    boolean isFavorite = false;
    boolean isEdited;

    Intent parentIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_detail);

        mFirestore = FirebaseFirestore.getInstance();
        parentIntent = getIntent();
        mCurrentSpace = (Space) parentIntent.getSerializableExtra("current space");
        mSpaceCollRef = mFirestore.collection("space");
        mUserDataCollRef = mFirestore.collection("user_data");

        mFirebaseStorage = FirebaseStorage.getInstance();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        initializeView();
        setupInfo();
        setupOwnerInfo();
        setupButton();

        mAddressMapView.onCreate(mapViewBundle);
        mAddressMapView.getMapAsync(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            mapViewBundle.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
            mAddressMapView.onSaveInstanceState(mapViewBundle);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAddressMapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAddressMapView.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng currentLatLng = new LatLng(mCurrentSpace.getL().get(0), mCurrentSpace.getL().get(1));
        googleMap.addMarker(new MarkerOptions().position(currentLatLng));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
//        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//            @Override
//            public void onMapClick(LatLng latLng) {
//                Intent intent = new Intent(SpaceDetailActivity.this, MapsSearchActivity.class);
//                intent.putExtra("current latLng", latLng);
//                intent.putExtra("c")
//            }
//        });
    }

    private void initializeView() {
        mToolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        mBtnBack = mToolbar.findViewById(R.id.detail_btn_cancel);
        mBtnFavorite = mToolbar.findViewById(R.id.detail_btn_favorite);
        mBtnEdit = mToolbar.findViewById(R.id.detail_btn_edit);
        mBtnDelete = mToolbar.findViewById(R.id.detail_btn_delete);

        mBtnCall = findViewById(R.id.detail_btn_call);
        mBtnChat = findViewById(R.id.detail_btn_chat);
        mBtnEmail = findViewById(R.id.detail_btn_mail);
        mLayoutCommunication = findViewById(R.id.detail_communication_layout);

        mLayoutAdminBtn = findViewById(R.id.detail_admin_btn_layout);
        mBtnAdminDisable = findViewById(R.id.detail_admin_btn_disable);
        mBtnAdminEnable = findViewById(R.id.detail_admin_btn_enable);

        mTextViewType = findViewById(R.id.detail_text_view_type);
        mTextViewTitle = findViewById(R.id.detail_text_view_title);
        mTextViewDate = findViewById(R.id.detail_text_view_date);

        mTextViewPrice = findViewById(R.id.detail_text_view_price);
        mTextViewSize = findViewById(R.id.detail_text_view_size);
        mTextViewPrePaid = findViewById(R.id.detail_text_view_prepaid);

        mImageOwnerAva = findViewById(R.id.detail_account_image);
        mTextViewOwnerName = findViewById(R.id.detail_account_text_view_name);
        mTextViewOwnerEmail = findViewById(R.id.detail_account_text_view_email);
        mTextViewOwnerPhone = findViewById(R.id.detail_account_text_view_phone);

        mTextViewAddress = findViewById(R.id.detail_text_view_address_value);
        mTextViewDescription = findViewById(R.id.detail_text_view_description_value);

        mTextViewOtherType = findViewById(R.id.detail_text_view_other_type);
        mTextViewDoor = findViewById(R.id.detail_text_view_other_door);
        mTextViewElectric = findViewById(R.id.detail_text_view_other_electric);
        mTextViewWater = findViewById(R.id.detail_text_view_other_water);
        mTextViewBed = findViewById(R.id.detail_text_view_other_bed);
        mTextViewBath = findViewById(R.id.detail_text_view_other_bath);

        mLayoutDoor = findViewById(R.id.detail_layout_door);
        mLayoutElectric = findViewById(R.id.detail_layout_electric);
        mLayoutWater = findViewById(R.id.detail_layout_water);
        mLayoutBed = findViewById(R.id.detail_layout_bed);
        mLayoutBath = findViewById(R.id.detail_layout_bath);

        mRecyclerViewImage = findViewById(R.id.detail_recycle_view_image);
        mImageIndicator1 = findViewById(R.id.detail_indicator_1);
        mImageIndicator2 = findViewById(R.id.detail_indicator_2);
        mImageIndicator3 = findViewById(R.id.detail_indicator_3);
        mImageIndicator4 = findViewById(R.id.detail_indicator_4);
        mImageIndicator5 = findViewById(R.id.detail_indicator_5);

        mSwitchStatus = findViewById(R.id.detail_switch_status);
        mTextViewStatus = findViewById(R.id.detail_text_view_status);
        mImageStatus = findViewById(R.id.deatail_image_status);

        mAddressMapView = findViewById(R.id.map);
        setUpImageRecyclerView();
    }

    private void setUpImageRecyclerView() {
        RecyclerView.LayoutManager mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewImage.setLayoutManager(mLinearLayoutManager);

        mImageNameList = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            mImageNameList.add(i + "");
        }
        final DetailImageAdapter mImageAdapter = new DetailImageAdapter(this, mImageNameList, mCurrentSpace.getId(), mCurrentSpace.getIdChu());
        mRecyclerViewImage.setAdapter(mImageAdapter);

        SnapHelper mSnapHelper = new PagerSnapHelper();
        mSnapHelper.attachToRecyclerView(mRecyclerViewImage);

        mRecyclerViewImage.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int currentItemPosition = layoutManager.findFirstVisibleItemPosition();
                switch (currentItemPosition) {
                    case 0:
                        mImageIndicator1.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.colorPrimary));
                        mImageIndicator2.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.light_gray));
                        mImageIndicator3.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.light_gray));
                        mImageIndicator4.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.light_gray));
                        mImageIndicator5.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.light_gray));
                        break;
                    case 1:
                        mImageIndicator1.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.light_gray));
                        mImageIndicator2.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.colorPrimary));
                        mImageIndicator3.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.light_gray));
                        mImageIndicator4.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.light_gray));
                        mImageIndicator5.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.light_gray));
                        break;
                    case 2:
                        mImageIndicator1.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.light_gray));
                        mImageIndicator2.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.light_gray));
                        mImageIndicator3.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.colorPrimary));
                        mImageIndicator4.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.light_gray));
                        mImageIndicator5.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.light_gray));
                        break;
                    case 3:
                        mImageIndicator1.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.light_gray));
                        mImageIndicator2.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.light_gray));
                        mImageIndicator3.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.light_gray));
                        mImageIndicator4.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.colorPrimary));
                        mImageIndicator5.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.light_gray));
                        break;
                    case 4:
                        mImageIndicator1.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.light_gray));
                        mImageIndicator2.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.light_gray));
                        mImageIndicator3.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.light_gray));
                        mImageIndicator4.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.light_gray));
                        mImageIndicator5.setColorFilter(ContextCompat.getColor(SpaceDetailActivity.this, R.color.colorPrimary));
                        break;
                }

            }
        });
    }

    private void setupInfo() {
        //Title data
        mTextViewTitle.setText(mCurrentSpace.getTieuDe());

        //Date data
        DateFormat mDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date mCurrentDate = mCurrentSpace.getTimeAdded();
        mTextViewDate.setText("ngày đăng: " + mDateFormat.format(mCurrentDate));

        // Price data
        String priceStr = formatMoney((int) mCurrentSpace.getGia());
        mTextViewPrice.setText(priceStr + "\nđồng");


        // Prepaid data
        if (mCurrentSpace.getThangCoc() > 0 && mCurrentSpace.getThangCoc() <= 10) {
            mTextViewPrePaid.setText(mCurrentSpace.getThangCoc() + " tháng");
        } else if (mCurrentSpace.getThangCoc() > 10) {
            mTextViewPrePaid.setText(formatMoney(mCurrentSpace.getThangCoc()) + "\nđồng");
        } else {
            mTextViewPrePaid.setText("Không");
        }

        // Size data
        mTextViewSize.setText(mCurrentSpace.getDienTich() + "");
        mTextViewSize.append(Html.fromHtml("m<sup><small>2</small></sup>"));

        // Address data
        mTextViewAddress.setText(mCurrentSpace.getDiaChiDayDu());

        // Description data
        mTextViewDescription.setText(mCurrentSpace.getMoTa());

        // Type data
        mTextViewType.setText(mCurrentSpace.getLoai());

        // Type related data
        if (!mCurrentSpace.getLoai().equalsIgnoreCase(getResources().getStringArray(R.array.type_array)[3])) {
            if (!mCurrentSpace.getHuongCua().equalsIgnoreCase(
                    getResources().getStringArray(R.array.door_direction_array)[0])) {
                mTextViewDoor.setText("Hướng cửa chính phía " + mCurrentSpace.getHuongCua());
            } else {
                mLayoutDoor.setVisibility(View.GONE);
            }
            mTextViewBed.setText(mCurrentSpace.getSoPhongNgu() + " phòng ngủ");
            mTextViewBath.setText(mCurrentSpace.getSoPhongVeSinh() + " phòng vệ sinh");
            mTextViewWater.setText(formatMoney((int) mCurrentSpace.getGiaNuoc()) + " đồng/khối");
            mTextViewElectric.setText(formatMoney((int) mCurrentSpace.getGiaDien()) + " đồng/số");
        } else {
            mLayoutDoor.setVisibility(View.GONE);
            mLayoutBed.setVisibility(View.GONE);
            mLayoutBath.setVisibility(View.GONE);
            mLayoutWater.setVisibility(View.GONE);
            mLayoutElectric.setVisibility(View.GONE);
        }
        mTextViewOtherType.setText(mCurrentSpace.getLoai());
        String parent = parentIntent.getStringExtra("from");

        if (parent != null && (parent.equalsIgnoreCase(SpaceManagementActivity.class.getSimpleName())
                || parent.equalsIgnoreCase(MyNotiService.class.getSimpleName()))) {
            mImageStatus.setVisibility(View.VISIBLE);
            mTextViewStatus.setVisibility(View.VISIBLE);
            mSwitchStatus.setVisibility(View.VISIBLE);
        } else {
            mImageStatus.setVisibility(View.GONE);
            mTextViewStatus.setVisibility(View.GONE);
            mSwitchStatus.setVisibility(View.GONE);
        }
        // Status Data
        updateSpaceStatus();

        //Favorite data
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        if (mCurrentUser != null) {
            DocumentReference mUserFavoriteSpaceRef = mUserDataCollRef
                    .document(mCurrentUser.getUid())
                    .collection("favorite_space")
                    .document(mCurrentSpace.getId());
            mUserFavoriteSpaceRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        mBtnFavorite.setImageResource(R.drawable.ic_favorite);
                        isFavorite = true;
                    }
                }
            });
        }
    }

    private void updateSpaceStatus() {
        mCurrentSpaceRef = mSpaceCollRef
                .document(mCurrentSpace.getId());
        mCurrentSpaceRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d(TAG, e.getMessage());
                } else {
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.get("trangThai").toString();
                        switch (status) {
                            case "allow":
                                mImageStatus.setColorFilter(getResources().getColor(android.R.color.holo_green_light, null));
                                mTextViewStatus.setText(getString(R.string.space_detail_status_allow_string));
                                mTextViewStatus.setTextColor(getResources().getColor(android.R.color.holo_green_light, null));
                                mSwitchStatus.setChecked(true);
                                break;
                            case "pending":
                                mSwitchStatus.setChecked(true);
                                mImageStatus.setColorFilter(getColor(R.color.colorPrimary));
                                mTextViewStatus.setText(getString(R.string.space_detail_status_pending_string));
                                mTextViewStatus.setTextColor(getResources().getColor(R.color.colorPrimary, null));
                                break;
                            case "not_publish":
                                mSwitchStatus.setChecked(false);
                                mImageStatus.setColorFilter(getColor(R.color.dark_gray));
                                mTextViewStatus.setText(getString(R.string.space_detail_status_not_publish_string));
                                mTextViewStatus.setTextColor(getResources().getColor(R.color.dark_gray, null));
                                break;
                            case "not_allow":
                                mSwitchStatus.setChecked(false);
                                mImageStatus.setColorFilter(getColor(R.color.colorCancel));
                                mTextViewStatus.setText(getString(R.string.space_detail_status_not_allow_string));
                                mTextViewStatus.setTextColor(getResources().getColor(R.color.colorCancel, null));
                                break;
                        }
                    }
                }
            }
        });
    }

    private void setupButton() {
        // Button Back
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEdited) {
                    setResult(RESULT_OK);
                } else {
                    setResult(RESULT_CANCELED);
                }
                finish();
            }
        });

        // Button Favorite
        mBtnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentUser != null) {
                    if (!isFavorite) {
                        mBtnFavorite.setImageResource(R.drawable.ic_favorite);
                    } else {
                        mBtnFavorite.setImageResource(R.drawable.ic_favorite_unselected);
                    }
                    showAddFavoritePromtDialog();
                } else {
                    Intent intent = new Intent(SpaceDetailActivity.this, WelcomeActivity.class);
                    startActivity(intent);
                }
            }
        });

        // Button Favorite
        mBtnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SpaceDetailActivity.this, AddSpaceActivity.class);
                intent.putExtra("command", "edit space");
                intent.putExtra("current space", mCurrentSpace);
                startActivityForResult(intent, REQUEST_EDIT_SPACE);
            }
        });

        // Button Favorite, Edit, Delete 's Visibility
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        if (mCurrentUser != null) {
            if (mCurrentUser.getUid().equalsIgnoreCase(mCurrentSpace.getIdChu())) {
                mBtnFavorite.setVisibility(View.GONE);
                mBtnEdit.setVisibility(View.VISIBLE);
                mBtnDelete.setVisibility(View.VISIBLE);
                mLayoutCommunication.setVisibility(View.GONE);
            } else {
                mBtnFavorite.setVisibility(View.VISIBLE);
                mBtnEdit.setVisibility(View.GONE);
                mBtnDelete.setVisibility(View.GONE);
            }
            String parentClassName = parentIntent.getStringExtra("from");
            if (parentClassName != null
                    && parentClassName.equalsIgnoreCase(AdminActivity.class.getSimpleName())) {
                mBtnFavorite.setVisibility(View.GONE);
                mBtnDelete.setVisibility(View.GONE);
                mBtnEdit.setVisibility(View.GONE);
                mLayoutAdminBtn.setVisibility(View.VISIBLE);
            }
        } else {
            mBtnEdit.setVisibility(View.GONE);
            mBtnDelete.setVisibility(View.GONE);
        }

        // Button Delete
        mBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
            }
        });

        // Communication Buttons
        mBtnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneUri = "tel:" + mTextViewOwnerPhone.getText().toString();
                Intent intentCall = new Intent(Intent.ACTION_DIAL, Uri.parse(phoneUri));
                startActivity(intentCall);
            }
        });


        mBtnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentUser = mFirebaseAuth.getCurrentUser();
                if (mCurrentUser != null) {
                    Intent intentChat = new Intent(SpaceDetailActivity.this, ChatActivity.class);
                    intentChat.putExtra("conversation id", mCurrentSpace.getIdChu());
                    intentChat.putExtra("conversation name", mTextViewOwnerName.getText().toString());
                    intentChat.putExtra("conversation phone", mTextViewOwnerPhone.getText().toString());
                    startActivity(intentChat);
                } else {
                    Toast.makeText(SpaceDetailActivity.this, "Bạn cần đăng nhập để thực hiện tác vụ này", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBtnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailUri = "mailto:" + mTextViewOwnerEmail.getText().toString();
                Intent mailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse(emailUri));
                try {
                    startActivity(mailIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(SpaceDetailActivity.this, "đã xảy ra lỗi! Bạn không có ứng dụng email nào để hoàn thành tác vụ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Admin Buttons
        mBtnAdminEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAdminDialog(true);
            }
        });
        mBtnAdminDisable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAdminDialog(false);
            }
        });

        // Space switch status Button
        mSwitchStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    if (isChecked) {
                        showPublishRequestDialog(true);
                    } else {
                        showPublishRequestDialog(false);
                    }
                }
            }
        });
    }

    private void setupOwnerInfo() {
        mUserDataCollRef.document(mCurrentSpace.getIdChu()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String ownerName = documentSnapshot.get("name").toString();
                String ownerPhoneNumber = documentSnapshot.get("phone_number").toString();
                String ownerEmail = documentSnapshot.get("email").toString();

                StorageReference mAvaImageRef = mFirebaseStorage.getReference(mCurrentSpace.getIdChu()).child("avatar");
                GlideApp.with(SpaceDetailActivity.this).load(mAvaImageRef).into(mImageOwnerAva);

                mTextViewOwnerName.setText(ownerName);
                mTextViewOwnerPhone.setText(ownerPhoneNumber);
                mTextViewOwnerEmail.setText(ownerEmail);

            }
        });
    }

    private void updateUIwithNewData() {
        DocumentReference mUpdatedSpaceDataRef = mFirestore.collection("space")
                .document(mCurrentSpace.getId());
        mUpdatedSpaceDataRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            mCurrentSpace = documentSnapshot.toObject(Space.class);
                            setupInfo();
                        }
                    }
                });
    }

    private void showDeleteDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        final View mDialogView = inflater.inflate(R.layout.detail_delete_dialog_layout, null);
        TextView mBtnCancel = mDialogView.findViewById(R.id.dialog_delete_no);
        TextView mBtnOk = mDialogView.findViewById(R.id.dialog_delete_yes);

        final AlertDialog deleteDialog = builder.setView(mDialogView).create();
        ColorDrawable dialogBackground = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(dialogBackground, 40, 50, 40, 50);
        deleteDialog.getWindow().setBackgroundDrawable(inset);

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog.dismiss();
            }
        });
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = mCurrentSpace.getId();

                DocumentReference spaceDocRef = mFirestore
                        .collection("space")
                        .document(id);
                spaceDocRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        finish();
                        Toast.makeText(SpaceDetailActivity.this, "Xóa tin đăng thành công", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        deleteDialog.show();
    }

    private void showPublishRequestDialog(final boolean isPublishRequest) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        final View mDialogView = inflater.inflate(R.layout.detail_space_publish_dialog_layout, null);
        TextView mTextViewSubtitle = mDialogView.findViewById(R.id.dialog_space_publish_subtitle);
        TextView mBtnCancel = mDialogView.findViewById(R.id.dialog_space_publish_no);
        TextView mBtnOk = mDialogView.findViewById(R.id.dialog_space_publish_yes);
        ImageView mImageView = mDialogView.findViewById(R.id.dialog_space_publish_image);

        if (!isPublishRequest) {
            mTextViewSubtitle.setText("Xác nhận gỡ tin đăng này khỏi trang chính?");
            GlideApp.with(this)
                    .load(R.drawable.ic_space_publish_off)
                    .into(mImageView);
        }

        final AlertDialog spacePublishDialog = builder.setView(mDialogView).create();
        ColorDrawable dialogBackground = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(dialogBackground, 40, 50, 40, 50);
        spacePublishDialog.getWindow().setBackgroundDrawable(inset);

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSwitchStatus.isChecked()) {
                    mSwitchStatus.setChecked(false);
                } else {
                    mSwitchStatus.setChecked(true);
                }
                spacePublishDialog.dismiss();
            }
        });
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = mCurrentSpace.getId();
                DocumentReference spaceDocRef = mFirestore
                        .collection("space")
                        .document(id);
                Map<String, Object> updates = new HashMap<>();
                if (isPublishRequest) {
                    if (mCurrentSpace.isAllow()) {
                        updates.put("trangThai", "allow");
                    } else {
                        updates.put("trangThai", "pending");
                    }
                } else {
                    updates.put("trangThai", "not_publish");
                }
                spaceDocRef.update(updates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    spacePublishDialog.dismiss();
                                    if (isPublishRequest) {
                                        mSwitchStatus.setChecked(true);
                                    } else {
                                        mSwitchStatus.setChecked(false);
                                    }
                                } else {
                                    Log.d(TAG, task.getException().getMessage());
                                }
                            }
                        });
            }

        });
        spacePublishDialog.show();
    }

    private void showAdminDialog(final boolean isAllow) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        final View mDialogView = inflater.inflate(R.layout.detail_admin_btns_dialog_layout, null);
        TextView mTextViewSubtitle = mDialogView.findViewById(R.id.dialog_admin_btns_subtitle);
        TextView mBtnCancel = mDialogView.findViewById(R.id.dialog_admin_btns_no);
        TextView mBtnOk = mDialogView.findViewById(R.id.dialog_admin_btns_yes);
        final TextView mEditReason = mDialogView.findViewById(R.id.dialog_admin_edit_reason);

        if (isAllow) {
            mEditReason.setVisibility(View.GONE);
            mTextViewSubtitle.setText("Cho phép tin này được đăng lên?");
        }

        final AlertDialog adminDialog = builder.setView(mDialogView).create();
        ColorDrawable dialogBackground = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(dialogBackground, 40, 50, 40, 50);
        adminDialog.getWindow().setBackgroundDrawable(inset);

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adminDialog.dismiss();
            }
        });
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = mCurrentSpace.getId();
                DocumentReference spaceDocRef = mFirestore
                        .collection("space")
                        .document(id);
                if (isAllow) {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("trangThai", "allow");
                    updates.put("allow",true);
                    updates.put("timeAdded", new Date());
                    spaceDocRef.update(updates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "publish space " + mCurrentSpace.getId() + "successfully");
                                        Toast.makeText(SpaceDetailActivity.this, "Cho phép thành công", Toast.LENGTH_SHORT).show();
                                        adminDialog.dismiss();
                                        finish();
                                    } else {
                                        Log.d(TAG, task.getException().getMessage());
                                    }
                                }
                            });
                } else {
                    if (mEditReason.getText().toString().isEmpty()
                            || mEditReason.getError() != null) {
                        mEditReason.setError("Bạn chưa nhập phần lý do");
                        mEditReason.requestFocus();
                    } else if (mEditReason.getText().toString().length() <= 10) {
                        mEditReason.setError("Số kí tự không được nhỏ hơn 10");
                        mEditReason.requestFocus();
                    } else {
                        Map<String,Object> updates = new HashMap<>();
                        updates.put("trangThai", "not_allow");
                        updates.put("allow", false);
                        spaceDocRef.update(updates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        String message = "Tin đăng về không gian tại địa chỉ "
                                                + mCurrentSpace.getDiaChiDayDu()
                                                + " đã không qua được kiểm duyệt với lý do: "
                                                + mEditReason.getText().toString();
                                        sendMessage(message);
                                    }
                                });
                        Toast.makeText(SpaceDetailActivity.this, "không cho phép thành công", Toast.LENGTH_SHORT).show();
                        adminDialog.dismiss();
                        finish();
                    }
                }
            }
        });
        adminDialog.show();
    }

    private void sendMessage(String message) {
        final DocumentReference mConversationFriendDocRef = mFirestore
                .collection("user_data")
                .document(mCurrentSpace.getIdChu())
                .collection("conversation")
                .document(mCurrentUser.getUid());

        mConversationFriendDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.getResult().exists()) {
                    Conversation currentFriendConversation = new Conversation(mCurrentUser.getUid(), new Date());
                    mConversationFriendDocRef.set(currentFriendConversation);
                }
            }
        });

        final CollectionReference mMessageFriendColRef = mConversationFriendDocRef
                .collection("message");

        final DocumentReference mConversationUserDocRef = mFirestore
                .collection("user_data")
                .document(mCurrentUser.getUid())
                .collection("conversation")
                .document(mCurrentSpace.getIdChu());

        mConversationUserDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.getResult().exists()) {
                    Conversation currentUserConversation = new Conversation(mCurrentSpace.getIdChu(), new Date());
                    mConversationUserDocRef.set(currentUserConversation);
                }
            }
        });

        final CollectionReference mMessageUserColRef = mConversationUserDocRef
                .collection("message");

        final Message currentMessage = new Message(mCurrentUser.getUid(), "", message, System.currentTimeMillis(), false);

        //save message to Firebase
        Task messageUserTask = mMessageUserColRef.add(currentMessage);
        messageUserTask.addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                currentMessage.setId(documentReference.getId());
                mMessageUserColRef.document(currentMessage.getId())
                        .set(currentMessage);

                Map<String, Object> conversationUpdates = new HashMap<>();
                conversationUpdates.put("timeAdded", FieldValue.serverTimestamp());
                conversationUpdates.put("newMessageId", currentMessage.getId());
                mConversationUserDocRef.update(conversationUpdates);
                Log.d(TAG, "update user's conversation successfully");

                mMessageFriendColRef.document(currentMessage.getId())
                        .set(currentMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
//
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("newMessageId", currentMessage.getId());
                            updates.put("timeAdded", FieldValue.serverTimestamp());
                            mConversationFriendDocRef.update(updates);

                            Log.d(TAG, "update friend's conversation successfully");
                        } else {
                            Log.d(TAG, "update friend's conversation error: " + task.getException().getMessage());
                        }
                    }
                });
            }
        });
    }

    private void showAddFavoritePromtDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        final View mDialogView = inflater.inflate(R.layout.detail_favorite_dialog_layout, null);
        TextView mTextViewSubtile = mDialogView.findViewById(R.id.dialog_favor_subtitle);
        TextView mBtnCancel = mDialogView.findViewById(R.id.dialog_favor_no);
        TextView mBtnOk = mDialogView.findViewById(R.id.dialog_favor_yes);

        if (isFavorite) {
            mTextViewSubtile.setText(getString(R.string.favor_dialog_promt_unsave_string));
            mBtnOk.setText("Bỏ lưu");
        }

        final AlertDialog favorDialog = builder.setView(mDialogView).create();
        ColorDrawable dialogBackground = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(dialogBackground, 40, 50, 40, 50);
        favorDialog.getWindow().setBackgroundDrawable(inset);

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favorDialog.dismiss();
                if (!isFavorite) {
                    mBtnFavorite.setImageResource(R.drawable.ic_favorite_unselected);
                } else {
                    mBtnFavorite.setImageResource(R.drawable.ic_favorite);
                }
            }
        });
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = mCurrentSpace.getId();

                DocumentReference userFavorSpaceDocRef = mUserDataCollRef
                        .document(mCurrentUser.getUid())
                        .collection("favorite_space").document(id);
                if (!isFavorite) {
                    Map<String, Object> idField = new HashMap<>();
                    idField.put("id", id);
                    userFavorSpaceDocRef.set(idField).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                favorDialog.dismiss();
                                isFavorite = true;
                                Toast.makeText(SpaceDetailActivity.this, "Đã lưu vào mục yêu thích", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SpaceDetailActivity.this, "Đã có lỗi xảy ra, xin vui lòng thử lại sau", Toast.LENGTH_SHORT).show();
                                favorDialog.dismiss();
                                isFavorite = false;
                            }
                        }
                    });
                } else {
                    userFavorSpaceDocRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            favorDialog.dismiss();
                            isFavorite = false;
                            Toast.makeText(SpaceDetailActivity.this, "Đã bỏ lưu ở mục yêu thích", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        favorDialog.show();
    }

    private String formatMoney(int gia) {
        return String.format(Locale.getDefault(), "%,d", gia);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_SPACE) {
            if (resultCode == RESULT_OK) {
                isEdited = true;
                updateUIwithNewData();
            } else if (resultCode == RESULT_CANCELED) {
                isEdited = false;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAddressMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAddressMapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAddressMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mAddressMapView.onLowMemory();
    }
}
