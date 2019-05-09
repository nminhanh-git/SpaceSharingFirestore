package com.example.nminhanh.spacesharing.Fragment.AddSpacePages;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.nminhanh.spacesharing.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class AddImageAdapter extends RecyclerView.Adapter<AddImageAdapter.ViewHolder> {
    Context context;
    ArrayList<Bitmap> imagePath;

    public AddImageAdapter(Context context, ArrayList<Bitmap> imagePath) {
        this.context = context;
        this.imagePath = imagePath;

    }

    @NonNull
    @Override
    public AddImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(
                LayoutInflater.from(context)
                        .inflate(R.layout.list_image_item_layout, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Bitmap mImage = imagePath.get(i);
        Glide.with(context).load(mImage).into(viewHolder.mImageView);
    }


    @Override
    public int getItemCount() {
        return imagePath.size();
    }

    public void addImage(Bitmap bitmap) {
        imagePath.add(bitmap);
        notifyItemInserted(imagePath.size() - 1);
    }

    public void removeImage(int position) {
        imagePath.remove(position);
        notifyItemRemoved(position);
    }

    public ArrayList<Bitmap> getImagePathList() {
        return imagePath;
    }

    public void setImagePathList(ArrayList<Bitmap> list) {
        imagePath.clear();
        imagePath.addAll(list);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.add_address_recycleview_item_image_view);
            Log.e("Minh Anh", mImageView.toString());
        }
    }
}
