package com.example.nminhanh.spacesharing.Fragment.MainPages;


import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.nminhanh.spacesharing.Utils.AddressUtils;
import com.example.nminhanh.spacesharing.Model.Space;
import com.example.nminhanh.spacesharing.R;

import com.example.nminhanh.spacesharing.mCustomFirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {
    RecyclerView mRecycleView;
    RecyclerView.LayoutManager mLinearLayoutManager;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    AddressUtils mAddressUtils;

    static final String IMAGE_STORAGE_BASE_URL = "gs://spacesharing-298d6.appspot.com";
    static final String LOADING_PLACEHOLDER_IMAGE = "https://media.giphy.com/media/6036p0cTnjUrNFpAlr/giphy.gif";

    FirestorePagingAdapter<Space, mCustomFirestorePagingAdapter.SpaceViewHolder> firestorePagingAdapter;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = getLayoutInflater().inflate(R.layout.fragment_search, container, false);

        setHasOptionsMenu(true);

        mRecycleView = view.findViewById(R.id.recycleView);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mRecycleView.setLayoutManager(mLinearLayoutManager);

        mAddressUtils = new AddressUtils(getActivity());

        CollectionReference mSpacesCollection = db.collection("space");
        Query baseQuery = mSpacesCollection.orderBy("timeAdded", Query.Direction.DESCENDING);

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
        mRecycleView.setAdapter(firestorePagingAdapter);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        firestorePagingAdapter.startListening();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        firestorePagingAdapter.stopListening();
    }
}
