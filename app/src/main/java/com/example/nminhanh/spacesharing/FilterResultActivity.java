package com.example.nminhanh.spacesharing;

import android.arch.paging.PagedList;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nminhanh.spacesharing.Model.City;
import com.example.nminhanh.spacesharing.Model.District;
import com.example.nminhanh.spacesharing.Model.Space;
import com.example.nminhanh.spacesharing.Model.Ward;
import com.example.nminhanh.spacesharing.Utils.AddressUtils;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;
import org.imperiumlabs.geofirestore.GeoQueryDataEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class FilterResultActivity extends AppCompatActivity {

    private static final String TAG = "MA:filterResult";

    Toolbar mToolbar;
    ImageButton mBtnBack;

    RecyclerView mRecyclerViewTag;

    public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {

        ArrayList<String> tagList;

        public TagAdapter(ArrayList<String> list) {
            this.tagList = new ArrayList<>(list);
        }

        public class TagViewHolder extends RecyclerView.ViewHolder {
            TextView mTextViewTag;

            public TagViewHolder(@NonNull View itemView) {
                super(itemView);
                mTextViewTag = itemView.findViewById(R.id.filter_result_tag_item);
            }
        }

        @NonNull
        @Override
        public TagViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View currentView = LayoutInflater.from(FilterResultActivity.this)
                    .inflate(R.layout.filter_list_item_layout, viewGroup, false);
            return new TagViewHolder(currentView);
        }

        @Override
        public void onBindViewHolder(@NonNull TagViewHolder tagViewHolder, int i) {
            tagViewHolder.mTextViewTag.setText(tagList.get(i));
        }

        @Override
        public int getItemCount() {
            return tagList.size();
        }


    }

    RecyclerView mRecyclerViewResult;
    RecyclerView.LayoutManager mLinearLayoutManager;
    AddressUtils mAddressUtils;

    FirebaseFirestore db;
    CustomFirestorePagingAdapter customFirestorePagingAdapter;

    HashMap<String, String> mFilters;
    ArrayList<String> mFilterArrayList;

    City currentCity;
    District currentDistrict;
    Ward currentWard;

    String priceSortDirection = "";
    int sizeStart;
    int sizeEnd;

    String type;

    GeoPoint currentNearbyGeoPoint;
    String currentNearbyAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_result);

        db = FirebaseFirestore.getInstance();
        mAddressUtils = new AddressUtils(this);

        initializeView();
    }

    private void initializeView() {
        mToolbar = findViewById(R.id.filter_result_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mBtnBack = findViewById(R.id.filter_result_btn_back);
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mRecyclerViewTag = findViewById(R.id.filter_tag_recycler_view);

        getFilterInput();
        initTagRecyclerView();
        initializeResultRecyclerView();
    }

    private void getFilterInput() {
        Intent mFilterIntent = getIntent();
        mFilters = (HashMap<String, String>) mFilterIntent.getSerializableExtra("filters");
        mFilterArrayList = new ArrayList<>();
        currentCity = (City) mFilterIntent.getSerializableExtra("city");
        currentDistrict = (District) mFilterIntent.getSerializableExtra("district");
        currentWard = (Ward) mFilterIntent.getSerializableExtra("ward");

        sizeStart = mFilterIntent.getIntExtra("size start", 0);
        sizeEnd = mFilterIntent.getIntExtra("size end", 0);
        Toast.makeText(this, sizeStart + "-" + sizeEnd, Toast.LENGTH_SHORT).show();

        if (!mFilters.containsKey("nearby")) {
            if (!currentCity.getId().equals("-1")) {
                if (!currentCity.getName().equalsIgnoreCase("TP. Hồ Chí Minh")) {
                    mFilterArrayList.add("Thành phố " + currentCity.getName());
                } else {
                    mFilterArrayList.add(currentCity.getName());
                }
            }

            if (!currentDistrict.getId().equals("-1")) {
                mFilterArrayList.add(currentDistrict.getName());
            }

            if (!currentWard.getId().equals("-1")) {
                mFilterArrayList.add(currentWard.getName());
            }

            if (mFilters.containsKey("size")) {
                sizeStart = mFilterIntent.getIntExtra("size start", 0);
                sizeEnd = mFilterIntent.getIntExtra("size end", 0);
                mFilterArrayList.add("từ " + sizeStart + " đến " + sizeEnd + "m2");
            }

            if (mFilters.containsKey("price")) {
                priceSortDirection = mFilters.get("price");
                mFilterArrayList.add("giá tiền " + priceSortDirection.toLowerCase());
            }

            if (mFilters.containsKey("type")) {
                type = mFilters.get("type");
                mFilterArrayList.add(type);
            }
        } else {
            double longitude = mFilterIntent.getDoubleExtra("longitude", 0.0);
            double latitude = mFilterIntent.getDoubleExtra("latitude", 0.0);
            currentNearbyGeoPoint = new GeoPoint(latitude, longitude);
            currentNearbyAddress = mFilterIntent.getStringExtra("address");

            mFilterArrayList.add("Bán kính 2km");
            mFilterArrayList.add("10 điểm gần nhất");
            mFilterArrayList.add(currentNearbyAddress);
        }
        initTagRecyclerView();
    }

    private void initTagRecyclerView() {
        TagAdapter mAdapter = new TagAdapter(mFilterArrayList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewTag.setLayoutManager(mLayoutManager);
        mRecyclerViewTag.setAdapter(mAdapter);
    }

    private void initializeResultRecyclerView() {
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

        if (mFilters.containsKey("type")) {
            baseQuery = baseQuery.whereEqualTo("loai", type);
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
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        FirestorePagingOptions<Space> options = new FirestorePagingOptions.Builder<Space>()
                .setQuery(baseQuery, config, Space.class)
                .build();

        customFirestorePagingAdapter = new CustomFirestorePagingAdapter(options,this);
        mRecyclerViewResult.setAdapter(customFirestorePagingAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        customFirestorePagingAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        customFirestorePagingAdapter.stopListening();
    }
}
