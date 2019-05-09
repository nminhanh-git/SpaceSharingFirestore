package com.example.nminhanh.spacesharing;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nminhanh.spacesharing.Fragment.AddSpacePages.DetailImageAdapter;
import com.example.nminhanh.spacesharing.Model.Space;
import com.example.nminhanh.spacesharing.Model.UserFavoriteSpace;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class SpaceDetailActivity extends AppCompatActivity {

    private static final int REQUEST_EDIT_SPACE = 1;
    Toolbar mToolbar;
    ImageButton mBtnBack;
    ImageButton mBtnFavorite;
    Button mBtnEdit;

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

    ImageView mImageIndicator1;
    ImageView mImageIndicator2;
    ImageView mImageIndicator3;
    ImageView mImageIndicator4;
    ImageView mImageIndicator5;

    RecyclerView mRecyclerViewImage;
    ArrayList<String> mImageNameList;

    Space mCurrentSpace;
    FirebaseFirestore mFirestore;
    CollectionReference mSpaceCollRef;
    CollectionReference mUserDataCollRef;
    FirebaseStorage mFirebaseStorage;

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

        initializeView();
        setupInfo();
        setupOwnerInfo();
        setupButton();
    }

    private void initializeView() {
        mToolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        mBtnBack = mToolbar.findViewById(R.id.detail_btn_cancel);
        mBtnFavorite = mToolbar.findViewById(R.id.detail_btn_favorite);
        mBtnEdit = mToolbar.findViewById(R.id.detail_btn_edit);

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
        double price = 0;
        String priceStr = String.valueOf(mCurrentSpace.getGia());
        if (priceStr.length() >= 10) {
            price = mCurrentSpace.getGia() / 1000000000;
            mTextViewPrice.setText(String.format("%.1f tỉ", price));
        } else if (priceStr.length() >= 7) {
            price = mCurrentSpace.getGia() / 1000000;
            mTextViewPrice.setText(String.format("%.1f triệu", price));
        } else if (priceStr.length() >= 4) {
            price = mCurrentSpace.getGia() / 1000;
            mTextViewPrice.setText((int) price + " nghìn");
        } else {
            mTextViewPrice.setText((int) price + " đồng");
        }

        // Prepaid data
        if (mCurrentSpace.getThangCoc() > 0) {
            mTextViewPrePaid.setText(mCurrentSpace.getThangCoc() + " tháng");
        } else {
            mTextViewPrePaid.setText("không");
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
            mTextViewDoor.setText("Hướng cửa chính phía " + mCurrentSpace.getHuongCua());
            mTextViewBed.setText(mCurrentSpace.getSoPhongNgu() + " phòng ngủ");
            mTextViewBath.setText(mCurrentSpace.getSoPhongVeSinh() + " phòng vệ sinh");
            mTextViewWater.setText(mCurrentSpace.getGiaNuoc() + " nghìn/khối");
            mTextViewElectric.setText(mCurrentSpace.getGiaDien() + " nghìn/số");
        } else {
            mLayoutDoor.setVisibility(View.GONE);
            mLayoutBed.setVisibility(View.GONE);
            mLayoutBath.setVisibility(View.GONE);
            mLayoutWater.setVisibility(View.GONE);
            mLayoutElectric.setVisibility(View.GONE);
        }
        mTextViewOtherType.setText(mCurrentSpace.getLoai());

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

    private void setupButton() {
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

        mBtnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SpaceDetailActivity.this, AddSpaceActivity.class);
                intent.putExtra("command", "edit space");
                intent.putExtra("current space", mCurrentSpace);
                startActivityForResult(intent, REQUEST_EDIT_SPACE);
            }
        });

        if (mCurrentUser != null) {
            if (mCurrentUser.getUid().equalsIgnoreCase(mCurrentSpace.getIdChu())) {
                mBtnFavorite.setVisibility(View.GONE);
                mBtnEdit.setVisibility(View.VISIBLE);
            } else {
                mBtnFavorite.setVisibility(View.VISIBLE);
                mBtnEdit.setVisibility(View.GONE);
            }
        }
    }

    private void showAddFavoritePromtDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        final View mDialogView = inflater.inflate(R.layout.detail_favorite_dialog_layout, null);
        TextView mTextViewSubtile = mDialogView.findViewById(R.id.dialog_favor_subtitle);
        TextView mBtnCancel = mDialogView.findViewById(R.id.dialog_favor_no);
        TextView mBtnOk = mDialogView.findViewById(R.id.dialog_favor_yes);

        if(isFavorite){
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
                if(!isFavorite) {
                    mBtnFavorite.setImageResource(R.drawable.ic_favorite_unselected);
                }else{
                    mBtnFavorite.setImageResource(R.drawable.ic_favorite);
                }
            }
        });
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = mCurrentSpace.getId();
                String ownerId = mCurrentSpace.getIdChu();
                String title = mCurrentSpace.getTieuDe();
                StringBuilder addressBuilder = new StringBuilder();
                String[] addressArray = mCurrentSpace.getDiaChiDayDu().split(",");
                for (int i = 1; i < addressArray.length; i++) {
                    addressBuilder.append(addressArray[i]);
                    if (i != addressArray.length - 1) {
                        addressBuilder.append(",");
                    }
                }
                String shortAddress = addressBuilder.toString();
                DocumentReference userFavorSpaceDocRef = mUserDataCollRef
                        .document(mCurrentUser.getUid())
                        .collection("favorite_space").document(mCurrentSpace.getId());
                if(!isFavorite) {
                    userFavorSpaceDocRef.set(new UserFavoriteSpace(id, ownerId, title, shortAddress))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    favorDialog.dismiss();
                                    isFavorite = true;
                                    Toast.makeText(SpaceDetailActivity.this, "Đã lưu vào mục yêu thích", Toast.LENGTH_SHORT).show();
                                }
                            });
                }else{
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

    private void setupOwnerInfo() {
        mUserDataCollRef.document(mCurrentSpace.getIdChu()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String ownerName = documentSnapshot.get("name").toString();
                String ownerPhoneNumber = documentSnapshot.get("phone_number").toString();
                String ownerAvatarName = documentSnapshot.get("avatar_name").toString();
                String ownerEmail = documentSnapshot.get("email").toString();

                if (!ownerAvatarName.equalsIgnoreCase("null")) {
                    StorageReference mAvaImageRef = mFirebaseStorage.getReference(mCurrentSpace.getIdChu()).child(ownerAvatarName);
                    GlideApp.with(SpaceDetailActivity.this).load(mAvaImageRef).into(mImageOwnerAva);
                }
                mTextViewOwnerName.setText(ownerName);
                mTextViewOwnerPhone.setText(ownerPhoneNumber);
                mTextViewOwnerEmail.setText(ownerEmail);

            }
        });
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
}
