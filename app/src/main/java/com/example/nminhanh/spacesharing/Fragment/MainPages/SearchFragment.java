package com.example.nminhanh.spacesharing.Fragment.MainPages;


import android.arch.paging.PagedList;
import android.icu.text.DecimalFormat;
import android.icu.text.NumberFormat;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.nminhanh.spacesharing.SpaceViewHolder;
import com.example.nminhanh.spacesharing.Utils.AddressUtils;
import com.example.nminhanh.spacesharing.Model.Space;
import com.example.nminhanh.spacesharing.R;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Locale;


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

    FirestorePagingAdapter<Space, SpaceViewHolder> firestorePagingAdapter;

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
                .setPrefetchDistance(3)
                .setPageSize(6)
                .build();

        FirestorePagingOptions<Space> options = new FirestorePagingOptions.Builder<Space>()
                .setLifecycleOwner(this)
                .setQuery(baseQuery, config, Space.class)
                .build();

        // Firestore adapter
        firestorePagingAdapter = new FirestorePagingAdapter<Space, SpaceViewHolder>(options) {
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

                holder.mTextViewPrice.setText((int) model.getDienTich() + Html.fromHtml("m<super>2</super>").toString() + " - " + formattedMoney((int) model.getGia()) + " đồng");
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

        mRecycleView.setAdapter(firestorePagingAdapter);
        // Inflate the layout for this fragment
        return view;
    }

    private String formattedMoney(int gia) {
        return String.format(Locale.getDefault(), "%,d", gia);
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
