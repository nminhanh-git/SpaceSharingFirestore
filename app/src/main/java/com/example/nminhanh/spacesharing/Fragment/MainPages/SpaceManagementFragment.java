package com.example.nminhanh.spacesharing.Fragment.MainPages;


import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.nminhanh.spacesharing.Utils.AddressUtils;
import com.example.nminhanh.spacesharing.Model.Space;
import com.example.nminhanh.spacesharing.R;
import com.example.nminhanh.spacesharing.mCustomFirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class SpaceManagementFragment extends Fragment {
    private static final String USER_SPACE_CHILD = "user_space";

    public SpaceManagementFragment() {
        // Required empty public constructor
    }

    public static final String SPACE_CHILD = "space";

    RecyclerView spaceManagementRecycleView;
    RelativeLayout mEmptyLayout;
    ImageView mImageEmpty;
    View view;

    AddressUtils mAddressUtils;

    FirestorePagingAdapter<Space, mCustomFirestorePagingAdapter.SpaceViewHolder> firestorePagingAdapter;
    FirebaseAuth mFirebaseAuth;
    FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_space_management, container, false);
        mFirebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setHasOptionsMenu(true);
        initialize();
        return view;
    }

    private void initialize() {
        mEmptyLayout = view.findViewById(R.id.empty_layout);
        mImageEmpty = view.findViewById(R.id.space_management_empty_image);
        Glide.with(getContext())
                .asBitmap()
                .load(R.drawable.empty_space)
                .into(mImageEmpty);

        spaceManagementRecycleView = view.findViewById(R.id.space_management_recycle_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        spaceManagementRecycleView.setLayoutManager(layoutManager);

        mAddressUtils = new AddressUtils(getActivity());

        if (mFirebaseAuth.getCurrentUser() == null) {
            mEmptyLayout.setVisibility(View.VISIBLE);
            spaceManagementRecycleView.setVisibility(View.GONE);
        } else {
            CollectionReference mSpacesCollection = db.collection(SPACE_CHILD);
            Query baseQuery = mSpacesCollection.whereEqualTo("idChu", mFirebaseAuth.getCurrentUser().getUid()).orderBy("timeAdded", Query.Direction.DESCENDING);

            PagedList.Config config = new PagedList.Config.Builder()
                    .setEnablePlaceholders(false)
                    .setPrefetchDistance(5)
                    .setPageSize(10)
                    .build();

            FirestorePagingOptions<Space> options = new FirestorePagingOptions.Builder<Space>()
                    .setLifecycleOwner(this)
                    .setQuery(baseQuery, config, Space.class)
                    .build();

            // Firestore adapter
            firestorePagingAdapter = new mCustomFirestorePagingAdapter(options, this.getContext());
            spaceManagementRecycleView.setAdapter(firestorePagingAdapter);
        }
    }

    private String formatMoney(int gia) {
        return String.format(Locale.getDefault(), "%,d", gia);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        if (mFirebaseAuth.getCurrentUser() != null) {
            mEmptyLayout.setVisibility(View.GONE);
            spaceManagementRecycleView.setVisibility(View.VISIBLE);
            firestorePagingAdapter.startListening();
        }
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mFirebaseAuth.getCurrentUser() != null) {
            firestorePagingAdapter.stopListening();
        }
    }
}
