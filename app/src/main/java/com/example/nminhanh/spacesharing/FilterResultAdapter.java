package com.example.nminhanh.spacesharing;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.nminhanh.spacesharing.Model.Space;
import com.example.nminhanh.spacesharing.Utils.AddressUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class FilterResultAdapter extends RecyclerView.Adapter<FilterResultAdapter.SpaceViewHolder> {

    static final String IMAGE_STORAGE_BASE_URL = "gs://spacesharing-298d6.appspot.com";
    static final String LOADING_PLACEHOLDER_IMAGE = "https://media.giphy.com/media/6036p0cTnjUrNFpAlr/giphy.gif";
    Context context;
    ArrayList<Space> spacesList;
    AddressUtils mAddressUtils;

    public FilterResultAdapter(Context context, ArrayList<Space> spacesList) {
        this.context = context;
        this.spacesList = spacesList;
        mAddressUtils = new AddressUtils(context);
    }

    @NonNull
    @Override
    public SpaceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new SpaceViewHolder(inflater.inflate(R.layout.search_recycleview_item_layout, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final SpaceViewHolder holder, int i) {
        Space currentSpace = spacesList.get(i);
        holder.mTextViewSpaceTitle.setText(currentSpace.getTieuDe());
        String thanhPho = "";
        switch (currentSpace.getThanhPhoId()) {
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
        String quan = mAddressUtils.getDistrictName(currentSpace.getThanhPhoId(), currentSpace.getQuanId());
        String phuong = mAddressUtils.getWardName(currentSpace.getQuanId(), currentSpace.getPhuongId());
        holder.mTextViewAddress.setText(currentSpace.getDiaChiPho() + ", " + phuong + ", " + quan + ", " + thanhPho);
        holder.mTextViewPrice.setText(currentSpace.getDienTich() + Html.fromHtml("m<sup>2</sup>").toString() + " - " + currentSpace.getGia() + "đồng");

        final String imageURl = IMAGE_STORAGE_BASE_URL + "/"
                + currentSpace.getIdChu() + "/"
                + currentSpace.getId() + "/"
                + currentSpace.getFirstImagePath();

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


    @Override
    public int getItemCount() {
        return spacesList.size();
    }

    public class SpaceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mTextViewSpaceTitle;
        public TextView mTextViewAddress;
        public ImageView mImageView;
        public TextView mTextViewPrice;

        public SpaceViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextViewSpaceTitle = itemView.findViewById(R.id.item_title);
            mTextViewAddress = itemView.findViewById(R.id.item_address);
            mImageView = itemView.findViewById(R.id.item_image);
            mTextViewPrice = itemView.findViewById(R.id.item_textview_price);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Space mCurrentSpace = spacesList.get(getAdapterPosition());
            Intent intent = new Intent(context, SpaceDetailActivity.class);
            intent.putExtra("space title", mCurrentSpace.getTieuDe());
            context.startActivity(intent);
        }
    }
}
