package com.example.nminhanh.spacesharing;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class SpaceViewHolder extends RecyclerView.ViewHolder {
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
    }
}
