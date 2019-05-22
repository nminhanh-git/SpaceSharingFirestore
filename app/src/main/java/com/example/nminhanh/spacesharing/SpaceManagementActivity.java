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

public class SpaceManagementActivity extends AppCompatActivity implements CustomFirestoreRecyclerAdapter.RecyclerViewDataChangedListener {
    public static final String SPACE_CHILD = "space";
    private static final String TAG = "MA:SpaceManagement";

    Toolbar mToobar;
    ImageButton mBtnBack;
    RecyclerView spaceManagementRecycleView;
    RelativeLayout mEmptyLayout;
    ImageView mImageEmpty;
    ImageButton mBtnAdd;
    Button mBtnEmptyAdd;

    CustomFirestoreRecyclerAdapter mFirestoreRecyclerAdapter;
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

            mFirestoreRecyclerAdapter = new CustomFirestoreRecyclerAdapter(options, this);
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

    @Override
    public void onRecyclerViewDataChanged(int itemCount) {
        if (itemCount == 0) {
            mEmptyLayout.setVisibility(View.VISIBLE);
            mBtnAdd.setVisibility(View.GONE);
        } else {
            mEmptyLayout.setVisibility(View.GONE);
            mBtnAdd.setVisibility(View.VISIBLE);

        }
    }
}
