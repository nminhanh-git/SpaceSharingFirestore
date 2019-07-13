package com.example.nminhanh.spacesharing.Fragment.MainPages;


import android.app.Activity;
import android.arch.paging.PagedList;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.nminhanh.spacesharing.AddSpaceActivity;
import com.example.nminhanh.spacesharing.Interface.GoToTopEventListener;
import com.example.nminhanh.spacesharing.Utils.AddressUtils;
import com.example.nminhanh.spacesharing.Model.Space;
import com.example.nminhanh.spacesharing.R;

import com.example.nminhanh.spacesharing.CustomFirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener,
        GoToTopEventListener {

    private static final String TAG = "MA:SearchFragment";
    RecyclerView mRecycleView;
    RecyclerView.LayoutManager mLinearLayoutManager;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mFirebaseAuth;

    SwipeRefreshLayout mSwipeRefreshLayout;
    FloatingActionButton mBtnAddSpace;

    AddressUtils mAddressUtils;

    FirestorePagingAdapter<Space, CustomFirestorePagingAdapter.SpaceViewHolder> firestorePagingAdapter;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = getLayoutInflater().inflate(R.layout.fragment_search, container, false);

        setHasOptionsMenu(true);


        mRecycleView = view.findViewById(R.id.recycleView);

        mSwipeRefreshLayout = view.findViewById(R.id.search_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_blue_dark);

        mAddressUtils = new AddressUtils(getActivity());

        createNewFirestorePagingAdapterInstance();
        mRecycleView.setAdapter(firestorePagingAdapter);
        // Inflate the layout for this fragment

        mBtnAddSpace = view.findViewById(R.id.search_btn_add);
        mBtnAddSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newSpaceIntent = new Intent(getActivity(), AddSpaceActivity.class);
                newSpaceIntent.putExtra("command", "add space");
                startActivity(newSpaceIntent);
            }
        });

        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mCurrentUser = mFirebaseAuth.getCurrentUser();
        if (mCurrentUser != null) {
            mBtnAddSpace.show();
        }else{
            mBtnAddSpace.hide();
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        firestorePagingAdapter.startListening();

        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 5000);


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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CustomFirestorePagingAdapter.REQUEST_DETAIL_SPACE) {
            if (resultCode == ((Activity) getContext()).RESULT_OK) {
                onRefresh();
            }
        }
    }

    public void createNewFirestorePagingAdapterInstance() {
        CollectionReference mSpacesCollection = db.collection("space");
        Query baseQuery = mSpacesCollection
                .whereEqualTo("trangThai", "allow")
                .orderBy("timeAdded", Query.Direction.DESCENDING);

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
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mRecycleView.setLayoutManager(mLinearLayoutManager);
        firestorePagingAdapter = new CustomFirestorePagingAdapter(options, getActivity());
    }

    @Override
    public void onRefresh() {
        firestorePagingAdapter.stopListening();
        createNewFirestorePagingAdapterInstance();
        mRecycleView.setAdapter(firestorePagingAdapter);
        firestorePagingAdapter.startListening();
        firestorePagingAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 4000);
    }

    @Override
    public void GoToTop() {
        mRecycleView.smoothScrollToPosition(0);
    }
}
