package com.example.nminhanh.spacesharing;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.nminhanh.spacesharing.Model.Space;
import com.example.nminhanh.spacesharing.Utils.AddressUtils;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Locale;

public class SpaceManagementActivity extends AppCompatActivity {
    public static final String SPACE_CHILD = "space";
    private static final String TAG = "MA:SpaceManagement";

    Toolbar mToobar;
    ImageButton mBtnBack;
    RecyclerView spaceManagementRecycleView;
    RelativeLayout mEmptyLayout;
    ImageView mImageEmpty;
    Button mBtnAdd;
    Button mBtnEmptyAdd;

    AddressUtils mAddressUtils;

    FirestoreRecyclerAdapter<Space, SpaceViewHolder> mFirestoreRecyclerAdapter;
    FirebaseAuth mFirebaseAuth;
    FirebaseFirestore db;
    FirebaseStorage mFirebaseStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_management);

        mFirebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        initialize();
    }

    private void initialize() {
        mToobar = findViewById(R.id.space_management_toolbar);
        setSupportActionBar(mToobar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mBtnBack = findViewById(R.id.space_management_btn_back);
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mBtnAdd = findViewById(R.id.space_management_btn_add);
        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SpaceManagementActivity.this, AddSpaceActivity.class);
                intent.putExtra("command", "add space");
                startActivity(intent);
            }
        });

        mEmptyLayout = findViewById(R.id.empty_layout);
        mImageEmpty = findViewById(R.id.space_management_empty_image);
        Glide.with(this)
                .asBitmap()
                .load(R.drawable.empty_space)
                .into(mImageEmpty);
        mBtnEmptyAdd = findViewById(R.id.space_management_empty_btn_add);
        mBtnEmptyAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SpaceManagementActivity.this, AddSpaceActivity.class);
                intent.putExtra("command", "add space");
                startActivity(intent);
            }
        });

        spaceManagementRecycleView = findViewById(R.id.space_management_recycle_view);
        setUpRecyclerViewData();

    }

    private void setUpRecyclerViewData() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        spaceManagementRecycleView.setLayoutManager(layoutManager);

        mAddressUtils = new AddressUtils(this);

        if (mFirebaseAuth.getCurrentUser() == null) {
            mEmptyLayout.setVisibility(View.VISIBLE);
            spaceManagementRecycleView.setVisibility(View.GONE);
        } else {
            CollectionReference mSpacesCollection = db.collection(SPACE_CHILD);
            Query baseQuery = mSpacesCollection.whereEqualTo(
                    "idChu", mFirebaseAuth.getCurrentUser().getUid())
                    .orderBy("timeAdded", Query.Direction.DESCENDING);

            FirestoreRecyclerOptions<Space> options = new FirestoreRecyclerOptions.Builder<Space>()
                    .setQuery(baseQuery, Space.class)
                    .build();

            mFirestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Space, SpaceViewHolder>(options) {
                @Override
                public void onError(@NonNull FirebaseFirestoreException e) {
                    super.onError(e);
                    Log.d(TAG, e.getMessage());
                }

                @Override
                public void onDataChanged() {
                    super.onDataChanged();
                    if (getItemCount() == 0) {
                        mEmptyLayout.setVisibility(View.VISIBLE);
                        spaceManagementRecycleView.setVisibility(View.GONE);
                        mBtnAdd.setVisibility(View.GONE);
                    } else {
                        mEmptyLayout.setVisibility(View.GONE);
                        spaceManagementRecycleView.setVisibility(View.VISIBLE);
                        mBtnAdd.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                protected void onBindViewHolder(@NonNull SpaceViewHolder holder, int position, @NonNull final Space model) {
                    holder.mTextViewType.setText(model.getLoai());
                    holder.mTextViewSpaceTitle.setText(model.getTieuDe());
                    holder.mTextViewAddress.setText(model.getDiaChiDayDu());
                    holder.mTextViewPrice.setText((int) model.getDienTich() + "");
                    holder.mTextViewPrice.append(Html.fromHtml("m<sup><small>2</small></sup>"));
                    holder.mTextViewPrice.append(" - ");
                    double price = model.getGia();
                    String priceStr = String.valueOf(price);
                    if (priceStr.length() >= 10) {
                        price /= 1000000000.0;
                        holder.mTextViewPrice.append(String.format("%.1f tỉ đồng", price));
                    } else if (priceStr.length() >= 7) {
                        price /= 1000000.0;
                        holder.mTextViewPrice.append(String.format("%.1f triệu đồng", price));
                    } else if (priceStr.length() >= 4) {
                        price /= 1000.0;
                        holder.mTextViewPrice.append(price + " nghìn đồng");
                    } else {
                        holder.mTextViewPrice.append(price + " đồng");
                    }

                    holder.mImageView.setImageResource(R.drawable.loading2);
                    StorageReference mFirtImageRef = mFirebaseStorage
                            .getReference(model.getIdChu())
                            .child(model.getId())
                            .child(1 + "");

                    Glide.with(holder.mImageView.getContext())
                            .load(mFirtImageRef)
                            .into(holder.mImageView);

                    holder.setViewOnClickLister(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(SpaceManagementActivity.this, SpaceDetailActivity.class);
                            intent.putExtra("current space", model);
                            startActivity(intent);
                        }
                    });
                }

                @NonNull
                @Override
                public SpaceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                    View currentView = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.search_recycleview_item_layout, viewGroup, false);
                    return new SpaceViewHolder(currentView);
                }
            };
            spaceManagementRecycleView.setAdapter(mFirestoreRecyclerAdapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mFirebaseAuth.getCurrentUser() != null) {
            mEmptyLayout.setVisibility(View.GONE);
            spaceManagementRecycleView.setVisibility(View.VISIBLE);
            mFirestoreRecyclerAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mFirebaseAuth.getCurrentUser() != null) {
            mFirestoreRecyclerAdapter.stopListening();
        }
    }

    private String formatMoney(int gia) {
        return String.format(Locale.getDefault(), "%,d", gia);
    }

    public class SpaceViewHolder extends RecyclerView.ViewHolder {
        TextView mTextViewType;
        TextView mTextViewSpaceTitle;
        TextView mTextViewAddress;
        ImageView mImageView;
        TextView mTextViewPrice;


        public SpaceViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextViewType = itemView.findViewById(R.id.item_type);
            mTextViewSpaceTitle = itemView.findViewById(R.id.item_title);
            mTextViewAddress = itemView.findViewById(R.id.item_address);
            mImageView = itemView.findViewById(R.id.item_image);
            mTextViewPrice = itemView.findViewById(R.id.item_textview_price);
        }

        public void setViewOnClickLister(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);
        }
    }

}
