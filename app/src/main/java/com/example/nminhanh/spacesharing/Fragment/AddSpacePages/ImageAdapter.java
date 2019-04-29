package com.example.nminhanh.spacesharing.Fragment.AddSpacePages;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.nminhanh.spacesharing.R;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    Context context;
    ArrayList<String> imagePath;

    public ImageAdapter(Context context, ArrayList<String> imagePath) {
        this.context = context;
        this.imagePath = imagePath;
    }

    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(
                LayoutInflater.from(context)
                        .inflate(R.layout.list_image_item_layout, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        String path = imagePath.get(i);
        Glide.with(context.getApplicationContext()).load(Uri.parse(path)).into(viewHolder.mImageView);
        Toast.makeText(context.getApplicationContext(), imagePath.size() + "", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return imagePath.size();
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
