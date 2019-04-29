package com.example.nminhanh.spacesharing.Fragment.MainPages;


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
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.nminhanh.spacesharing.Utils.AddressUtils;
import com.example.nminhanh.spacesharing.Model.Space;
import com.example.nminhanh.spacesharing.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 */
public class SpaceManagementFragment extends Fragment {
    private static final String USER_SPACE_CHILD = "user_space";

    public SpaceManagementFragment() {
        // Required empty public constructor
    }

    public class SpaceViewHolder extends RecyclerView.ViewHolder {
        TextView mTextViewSpaceTitle;
        TextView mTextViewAddress;
        ImageView mImageView;
        TextView mTextViewPrice;
        Switch mSwitchAvailable;

        public SpaceViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextViewSpaceTitle = itemView.findViewById(R.id.item_title);
            mTextViewAddress = itemView.findViewById(R.id.item_address);
            mImageView = itemView.findViewById(R.id.item_image);
            mTextViewPrice = itemView.findViewById(R.id.item_textview_price);
            mSwitchAvailable = itemView.findViewById(R.id.available_switch);
        }
    }

    FirebaseRecyclerAdapter<Space, SpaceViewHolder> mFirebaseRecyclerAdapter;

    public static final String SPACE_CHILD = "space";

    RecyclerView spaceManagementRecycleView;
    RelativeLayout mEmptyLayout;
    ImageView mImageEmpty;
    View view;

    AddressUtils mAddressUtils;
    DatabaseReference mFirbaseDbReference;
    FirebaseAuth mFirebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_space_management, container, false);
        mFirebaseAuth = FirebaseAuth.getInstance();
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

        mFirbaseDbReference = FirebaseDatabase.getInstance().getReference();
        SnapshotParser<Space> spaceSnapshotParser = new SnapshotParser<Space>() {
            @NonNull
            @Override
            public Space parseSnapshot(@NonNull DataSnapshot snapshot) {
                Space s = snapshot.getValue(Space.class);
                if (s != null) {
                    s.setId(snapshot.getKey());
                    return s;
                }
                return null;
            }
        };
        DatabaseReference mSpaceReference = mFirbaseDbReference.child(USER_SPACE_CHILD).child(SPACE_CHILD);
        FirebaseRecyclerOptions<Space> options =
                new FirebaseRecyclerOptions.Builder<Space>()
                        .setQuery(mSpaceReference, spaceSnapshotParser)
                        .build();

        mFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Space, SpaceViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull SpaceViewHolder holder, int position, @NonNull Space model) {
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
                holder.mTextViewAddress.setText(phuong + ", " + quan + ", " + thanhPho);
                holder.mTextViewPrice.setText(model.getDienTich() + Html.fromHtml("m<sup>2</sup>").toString() + " - " + model.getGia() + "đồng");
            }

            @NonNull
            @Override
            public SpaceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new SpaceViewHolder(
                        inflater.inflate(R.layout.space_management_recycleview_item_layout,
                                viewGroup,
                                false));
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                if (this.getItemCount() == 0) {
                    spaceManagementRecycleView.setVisibility(View.GONE);
                    mEmptyLayout.setVisibility(View.VISIBLE);
                } else {
                    spaceManagementRecycleView.setVisibility(View.VISIBLE);
                    mEmptyLayout.setVisibility(View.GONE);
                }
            }
        };
        spaceManagementRecycleView.setAdapter(mFirebaseRecyclerAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        if (mFirebaseAuth.getCurrentUser() == null) {
            mEmptyLayout.setVisibility(View.VISIBLE);
            spaceManagementRecycleView.setVisibility(View.GONE);
        } else {
            mEmptyLayout.setVisibility(View.GONE);
            spaceManagementRecycleView.setVisibility(View.VISIBLE);
            mFirebaseRecyclerAdapter.startListening();
        }
        super.onResume();
    }
}
