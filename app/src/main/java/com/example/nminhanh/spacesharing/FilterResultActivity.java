package com.example.nminhanh.spacesharing;

import android.arch.paging.PagedList;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
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
import android.widget.ImageButton;
import android.widget.Toast;

import com.adroitandroid.chipcloud.ChipCloud;
import com.bumptech.glide.Glide;
import com.example.nminhanh.spacesharing.Fragment.MainPages.SearchFragment;
import com.example.nminhanh.spacesharing.Model.City;
import com.example.nminhanh.spacesharing.Model.District;
import com.example.nminhanh.spacesharing.Model.Space;
import com.example.nminhanh.spacesharing.Model.Ward;
import com.example.nminhanh.spacesharing.Utils.AddressUtils;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

import javax.annotation.Nullable;

public class FilterResultActivity extends AppCompatActivity {

    static final String IMAGE_STORAGE_BASE_URL = "gs://spacesharing-298d6.appspot.com";
    static final String LOADING_PLACEHOLDER_IMAGE = "https://media.giphy.com/media/6036p0cTnjUrNFpAlr/giphy.gif";
    private static final String TAG = "MA:filterResult";

    Toolbar mToolbar;
    ImageButton mBtnBack;
    ChipCloud mChipCloud;

    RecyclerView mRecyclerViewResult;
    RecyclerView.LayoutManager mLinearLayoutManager;
    AddressUtils mAddressUtils;

    FirebaseFirestore db;
    FirestorePagingAdapter<Space, SpaceViewHolder> mFilterResultPagingAdapter;

    HashMap<String, String> mFilters;

    City currentCity;
    District currentDistrict;
    Ward currentWard;

    String priceSortDirection = "";
    int sizeStart;
    int sizeEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_result);

        db = FirebaseFirestore.getInstance();
        mAddressUtils = new AddressUtils(this);

        mChipCloud = findViewById(R.id.filter_result_chipcloud);
        getFilterInput();
        initializeView();
    }

    private void initializeView() {
        mToolbar = findViewById(R.id.filter_result_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mChipCloud = findViewById(R.id.filter_result_chipcloud);

        mBtnBack = findViewById(R.id.filter_result_btn_back);
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        initializeRecyclerView();
    }

    private void getFilterInput() {
        Intent mFilterIntent = getIntent();
        mFilters = (HashMap<String, String>) mFilterIntent.getSerializableExtra("filters");

        currentCity = (City) mFilterIntent.getSerializableExtra("city");
        currentDistrict = (District) mFilterIntent.getSerializableExtra("district");
        currentWard = (Ward) mFilterIntent.getSerializableExtra("ward");

        sizeStart = mFilterIntent.getIntExtra("size start", 0);
        sizeEnd = mFilterIntent.getIntExtra("size end", 0);
        Toast.makeText(this, sizeStart +"-"+sizeEnd, Toast.LENGTH_SHORT).show();

        if (!currentCity.getId().equals("-1")) {
            if (!currentCity.getName().equalsIgnoreCase("TP. Hồ Chí Minh")) {
                mChipCloud.addChip("Thành phố " + currentCity.getName());
            } else {
                mChipCloud.addChip(currentCity.getName());
            }
        }

        if (!currentDistrict.getId().equals("-1")) {
            mChipCloud.addChip(currentDistrict.getName());
        }

        if (!currentWard.getId().equals("-1")) {
            mChipCloud.addChip(currentWard.getName());
        }

        if (mFilters.containsKey("size")) {
            sizeStart = mFilterIntent.getIntExtra("size start", 0);
            sizeEnd = mFilterIntent.getIntExtra("size end", 0);
            mChipCloud.addChip("từ " + sizeStart + " đến " + sizeEnd + " m2");
        }

        if (mFilters.containsKey("price")) {
            priceSortDirection = mFilters.get("price");
            mChipCloud.addChip("giá tiền " + priceSortDirection);
        }
    }

    private void initializeRecyclerView() {
        mRecyclerViewResult = findViewById(R.id.filter_result_recycler_view);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerViewResult.setLayoutManager(mLinearLayoutManager);

        CollectionReference mSpacesCollection = db.collection("space");
        Query baseQuery = mSpacesCollection;
        if (currentCity != null && !currentCity.getId().equals("-1")) {
            baseQuery = baseQuery.whereEqualTo("thanhPhoId", currentCity.getId());
        }
        if (currentDistrict != null && !currentDistrict.getId().equals("-1")) {
            baseQuery = baseQuery.whereEqualTo("quanId", currentDistrict.getId());
        }
        if (currentWard != null && !currentWard.getId().equals("-1")) {
            baseQuery = baseQuery.whereEqualTo("phuongId", currentWard.getId());
        }

        if (mFilters.containsKey("size")) {
            baseQuery = baseQuery.whereGreaterThanOrEqualTo("dienTich", sizeStart)
                    .whereLessThanOrEqualTo("dienTich", sizeEnd)
                    .orderBy("dienTich", Query.Direction.ASCENDING);
        }

        switch (priceSortDirection.toLowerCase()) {
            case "tăng dần":
                baseQuery = baseQuery.orderBy("gia", Query.Direction.ASCENDING);
                break;
            case "giảm dần":
                baseQuery = baseQuery.orderBy("gia", Query.Direction.DESCENDING);
                break;
            default:
                baseQuery = baseQuery.orderBy("timeAdded", Query.Direction.DESCENDING);
                break;
        }


        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(3)
                .setPageSize(6)
                .build();

        FirestorePagingOptions<Space> options = new FirestorePagingOptions.Builder<Space>()
                .setLifecycleOwner(this)
                .setQuery(baseQuery, config, Space.class)
                .build();

        // Firestore adapter
        mFilterResultPagingAdapter = new FirestorePagingAdapter<Space, SpaceViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final SpaceViewHolder holder, int position, @NonNull Space model) {
                holder.mTextViewSpaceTitle.setText(model.getTieuDe());
                String thanhPho = "";
                switch (model.getThanhPhoId()) {
                    case "01":
                        thanhPho = "Hà Nội";
                        break;
                    case "48":
                        thanhPho = "Đà Nẵng";
                        break;
                    case "79":
                        thanhPho = "TP.Hồ Chí Minh";
                        break;
                }
                String quan = mAddressUtils.getDistrictName(model.getThanhPhoId(), model.getQuanId());
                String phuong = mAddressUtils.getWardName(model.getQuanId(), model.getPhuongId());
                holder.mTextViewAddress.setText(model.getDiaChiPho() + ", " + phuong + ", " + quan + ", " + thanhPho);
                holder.mTextViewPrice.setText(model.getDienTich() + Html.fromHtml("m<sup>2</sup>").toString() + " - " + model.getGia() + "đồng");
                if (!model.getFirstImagePath().equalsIgnoreCase("không có gì hết á!")) {
                    final String imageURl = IMAGE_STORAGE_BASE_URL + "/"
                            + model.getIdChu() + "/"
                            + model.getId() + "/"
                            + model.getFirstImagePath();

                    Glide.with(holder.mImageView.getContext())
                            .load(LOADING_PLACEHOLDER_IMAGE).into(holder.mImageView);
                    StorageReference storageRef = FirebaseStorage.getInstance()
                            .getReferenceFromUrl(imageURl);
                    storageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                String downloadUrl = task.getResult().toString();
                                Glide.with(holder.mImageView.getContext())
                                        .load(downloadUrl).into(holder.mImageView);
                            } else {
                                Toast.makeText(holder.mImageView.getContext(),
                                        "Error in loading image by Glide, or the image URL is invalid:" +
                                                imageURl,
                                        Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
            }

            @NonNull
            @Override
            public SpaceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new SpaceViewHolder(inflater.inflate(R.layout.search_recycleview_item_layout, viewGroup, false));
            }
        };

        mRecyclerViewResult.setAdapter(mFilterResultPagingAdapter);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mFilterResultPagingAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFilterResultPagingAdapter.stopListening();
    }
}
