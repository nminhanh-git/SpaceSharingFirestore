package com.example.nminhanh.spacesharing;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.example.nminhanh.spacesharing.Model.Space;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;

public class AdminActivity extends AppCompatActivity implements CustomFirestoreRecyclerAdapter.RecyclerViewDataChangedListener {
    private static final String SPACE_CHILD = "space";
    Toolbar mToobar;
    ImageButton mBtnBack;
    RecyclerView mRecycleViewAdmin;
    RelativeLayout mEmptyLayout;
    ImageView mImageEmpty;

    CustomFirestoreRecyclerAdapter mFirestoreRecyclerAdapter;
    FirebaseAuth mFirebaseAuth;
    FirebaseFirestore db;
    FirebaseStorage mFirebaseStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        mFirebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        initialize();
    }

    public void initialize() {
        mToobar = findViewById(R.id.admin_toolbar);
        setSupportActionBar(mToobar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mBtnBack = findViewById(R.id.admin_btn_back);
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mEmptyLayout = findViewById(R.id.empty_layout);
        mImageEmpty = findViewById(R.id.admin_empty_image);
        Glide.with(this)
                .asBitmap()
                .load(R.drawable.empty_space)
                .into(mImageEmpty);

        mRecycleViewAdmin = findViewById(R.id.admin_recycle_view);
        setUpRecyclerViewData();
    }

    private void setUpRecyclerViewData() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecycleViewAdmin.setLayoutManager(layoutManager);

        if (mFirebaseAuth.getCurrentUser() == null) {
            mEmptyLayout.setVisibility(View.VISIBLE);
            mRecycleViewAdmin.setVisibility(View.GONE);
        } else {
            CollectionReference mSpacesCollection = db.collection(SPACE_CHILD);
            Query baseQuery = mSpacesCollection
                    .whereEqualTo("trangThai", "pending")
                    .orderBy("timeAdded", Query.Direction.DESCENDING);

            FirestoreRecyclerOptions<Space> options =
                    new FirestoreRecyclerOptions.Builder<Space>()
                            .setQuery(baseQuery, Space.class)
                            .build();

            mFirestoreRecyclerAdapter = new CustomFirestoreRecyclerAdapter(options, this);
            mRecycleViewAdmin.setAdapter(mFirestoreRecyclerAdapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mEmptyLayout.setVisibility(View.GONE);
        mRecycleViewAdmin.setVisibility(View.VISIBLE);
        mFirestoreRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mFirestoreRecyclerAdapter.stopListening();
    }

    @Override
    public void onRecyclerViewDataChanged(int itemCount) {
        if (itemCount == 0) {
            mEmptyLayout.setVisibility(View.VISIBLE);
        } else {
            mEmptyLayout.setVisibility(View.GONE);
        }
    }
}
