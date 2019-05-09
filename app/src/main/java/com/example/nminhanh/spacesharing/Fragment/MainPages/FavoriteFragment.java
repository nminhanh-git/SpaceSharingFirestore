package com.example.nminhanh.spacesharing.Fragment.MainPages;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.signature.ObjectKey;
import com.example.nminhanh.spacesharing.GlideApp;
import com.example.nminhanh.spacesharing.Model.Space;
import com.example.nminhanh.spacesharing.Model.UserFavoriteSpace;
import com.example.nminhanh.spacesharing.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


/**
 * A simple {@link Fragment} subclass.
 */
public class FavoriteFragment extends Fragment {

    private static final String TAG = "MA:FavoriteFragment";
    View view;

    FirebaseFirestore mFirestore;
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mCurrentUser;

    RecyclerView mFavorRecyclerView;
    FirestoreRecyclerAdapter<UserFavoriteSpace, FavorSpaceViewHolder> mFirestoreRecyclerAdapter;


    public FavoriteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        view = inflater.inflate(R.layout.fragment_favorite, container, false);

        mFirestore = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();

        initializeView();
        if (mCurrentUser != null) {
            mFavorRecyclerView.setVisibility(View.VISIBLE);
            setUpRecyclerViewData();
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mFirestoreRecyclerAdapter.startListening();
    }

    private void setUpRecyclerViewData() {
        CollectionReference mUserFavoriteCollRef = mFirestore.collection("user_data").document(mCurrentUser.getUid()).collection("favorite_space");
        Query query = mUserFavoriteCollRef.orderBy("timeAdded", Query.Direction.DESCENDING);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "get favorite successful");
                }else{
                    Log.d(TAG, task.getException().getMessage());
                }
            }
        });
        FirestoreRecyclerOptions<UserFavoriteSpace> options = new FirestoreRecyclerOptions.Builder<UserFavoriteSpace>()
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
                if(getItemCount() == 0){

                }
            }

            @Override
            protected void onBindViewHolder(@NonNull FavorSpaceViewHolder holder, int position, @NonNull UserFavoriteSpace model) {
                String title = model.getTitle();
                String address = model.getAddress();
                String id = model.getId();

                holder.mTextViewTitle.setText(title);
                holder.mTextViewAddress.setText(address);

                StorageReference mImageRef = FirebaseStorage.getInstance()
                        .getReference(model.getOwnerId())
                        .child(id)
                        .child("1");

                GlideApp.with(getContext())
                        .load(mImageRef)
                        .signature(new ObjectKey(model.getTimeAdded()))
                        .into(holder.mImageView);
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
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mFavorRecyclerView.setLayoutManager(mLayoutManager);
    }

    private void initializeView() {
        mFavorRecyclerView = view.findViewById(R.id.favor_recycle_view);
    }

    @Override
    public void onStop() {
        super.onStop();
        mFirestoreRecyclerAdapter.stopListening();
    }

    public class FavorSpaceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView mImageView;
        TextView mTextViewTitle;
        TextView mTextViewAddress;

        public FavorSpaceViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.favor_item_image);
            mTextViewTitle = itemView.findViewById(R.id.favor_item_title);
            mTextViewAddress = itemView.findViewById(R.id.favor_item_address);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
