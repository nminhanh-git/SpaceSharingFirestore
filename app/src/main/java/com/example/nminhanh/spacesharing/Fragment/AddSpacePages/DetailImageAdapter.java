package com.example.nminhanh.spacesharing.Fragment.AddSpacePages;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.bumptech.glide.signature.ObjectKey;
import com.example.nminhanh.spacesharing.GlideApp;
import com.example.nminhanh.spacesharing.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class DetailImageAdapter extends RecyclerView.Adapter<DetailImageAdapter.ViewHolder> {
    Context context;
    ArrayList<String> imagePath;
    FirebaseStorage mFirebaseStorage;
    String mCurrentSpaceId;
    String mCurrentOwnerId;

    public DetailImageAdapter(Context context, ArrayList<String> imagePath, String currentSpaceId, String currentOwnerId) {
        this.context = context;
        this.imagePath = imagePath;
        this.mFirebaseStorage = FirebaseStorage.getInstance();
        this.mCurrentSpaceId = currentSpaceId;
        this.mCurrentOwnerId = currentOwnerId;
    }

    @NonNull
    @Override
    public DetailImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(
                LayoutInflater.from(context)
                        .inflate(R.layout.list_image_item_layout, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        DrawableImageViewTarget imageViewTarget = new DrawableImageViewTarget(viewHolder.mImageView);
        GlideApp.with(context).load(R.raw.loading2).into(imageViewTarget);
        String name = imagePath.get(i);
        final StorageReference mImageRef = mFirebaseStorage.getReference(mCurrentOwnerId).child(mCurrentSpaceId).child(name);

        mImageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
               long updatedTimeMillis = storageMetadata.getUpdatedTimeMillis();
                GlideApp.with(context).load(mImageRef)
                        .signature(new ObjectKey(updatedTimeMillis))
                        .into(viewHolder.mImageView);
            }
        });
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
