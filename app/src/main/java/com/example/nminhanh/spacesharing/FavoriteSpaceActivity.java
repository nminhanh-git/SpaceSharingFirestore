package com.example.nminhanh.spacesharing;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.signature.ObjectKey;
import com.example.nminhanh.spacesharing.Model.Space;
import com.example.nminhanh.spacesharing.Model.UserFavoriteSpace;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FavoriteSpaceActivity extends AppCompatActivity {

    private static final String TAG = "MA:ChatFragment";
    FirebaseFirestore mFirestore;
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mCurrentUser;

    RelativeLayout mLayoutEmpty;
    ImageButton mBtnBack;
    RecyclerView mFavorRecyclerView;
    FirestoreRecyclerAdapter<UserFavoriteSpace, FavorSpaceViewHolder> mFirestoreRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_space);

        mFirestore = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();

        initializeView();
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (mCurrentUser != null) {
            mFavorRecyclerView.setVisibility(View.VISIBLE);
            setUpRecyclerViewData();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mCurrentUser != null) {
            mFirestoreRecyclerAdapter.startListening();
        }
    }

    private void setUpRecyclerViewData() {
        CollectionReference mUserFavoriteCollRef = mFirestore.collection("user_data")
                .document(mCurrentUser.getUid()).collection("favorite_space");
        Query query = mUserFavoriteCollRef;


        FirestoreRecyclerOptions<UserFavoriteSpace> options =
                new FirestoreRecyclerOptions.Builder<UserFavoriteSpace>()
                        .setQuery(query, UserFavoriteSpace.class)
                        .build();

        mFirestoreRecyclerAdapter = new FirestoreRecyclerAdapter<UserFavoriteSpace, FavorSpaceViewHolder>(options) {
            @Override
            public void onError(@NonNull FirebaseFirestoreException e) {
                super.onError(e);
                Log.d(TAG, e.getMessage());
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                if (getItemCount() == 0) {
                    mLayoutEmpty.setVisibility(View.VISIBLE);
                }else{
                    mLayoutEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onBindViewHolder(@NonNull final FavorSpaceViewHolder holder, int position, @NonNull UserFavoriteSpace model) {
                final Space[] currentSpace = new Space[1];
                DocumentReference mSpaceColRef = mFirestore.collection("space").document(model.getId());
                mSpaceColRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            currentSpace[0] = task.getResult().toObject(Space.class);
                            String title = currentSpace[0].getTieuDe();
                            String address = currentSpace[0].getDiaChiDayDu();

                            String[] addressArr = address.split(",");
                            StringBuilder addressBuilder = new StringBuilder();
                            for (int i = 1; i < addressArr.length; i++) {
                                addressBuilder.append(addressArr[i]);
                                if (i != addressArr.length - 1) {
                                    addressBuilder.append(",");
                                }
                            }
                            address = addressBuilder.toString().trim();

                            holder.mTextViewTitle.setText(title);
                            holder.mTextViewAddress.setText(address.trim());

                            StorageReference mImageRef = FirebaseStorage.getInstance()
                                    .getReference(currentSpace[0].getIdChu())
                                    .child(currentSpace[0].getId())
                                    .child("1");

                            GlideApp.with(FavoriteSpaceActivity.this)
                                    .load(mImageRef)
                                    .signature(new ObjectKey(currentSpace[0].getTimeAdded()))
                                    .into(holder.mImageView);

                            holder.setOnItemClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(FavoriteSpaceActivity.this, SpaceDetailActivity.class);
                                    intent.putExtra("current space", currentSpace[0]);
                                    startActivity(intent);
                                }
                            });
                        }
                    }
                });
            }

            @NonNull
            @Override
            public FavorSpaceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View currentView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.favorite_recycler_view_item_layout, viewGroup, false);
                return new FavorSpaceViewHolder(currentView);
            }
        };
        mFavorRecyclerView.setAdapter(mFirestoreRecyclerAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mFavorRecyclerView.setLayoutManager(mLayoutManager);
    }

    private void initializeView() {
        mFavorRecyclerView = findViewById(R.id.favor_recycle_view);
        mBtnBack = findViewById(R.id.favor_btn_back);
        mLayoutEmpty = findViewById(R.id.favor_empty_layout);
    }

    @Override
    protected void onStop() {
        if (mCurrentUser != null) {
            mFirestoreRecyclerAdapter.stopListening();
        }
        super.onStop();
    }

    public class FavorSpaceViewHolder extends RecyclerView.ViewHolder {
        ImageView mImageView;
        TextView mTextViewTitle;
        TextView mTextViewAddress;

        public FavorSpaceViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.favor_item_image);
            mTextViewTitle = itemView.findViewById(R.id.favor_item_title);
            mTextViewAddress = itemView.findViewById(R.id.favor_item_address);
        }

        public void setOnItemClickListener(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);
        }

    }
}
