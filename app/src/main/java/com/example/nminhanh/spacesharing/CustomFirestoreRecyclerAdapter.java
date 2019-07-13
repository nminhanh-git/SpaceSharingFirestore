package com.example.nminhanh.spacesharing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.example.nminhanh.spacesharing.Model.Space;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomFirestoreRecyclerAdapter
        extends FirestoreRecyclerAdapter<Space, RecyclerView.ViewHolder> {

    Context context;
    FirebaseStorage mFirebaseStorage;
    FirebaseFirestore mFirestore;

    interface RecyclerViewDataChangedListener {
        void onRecyclerViewDataChanged(int itemCount);
    }

    RecyclerViewDataChangedListener listener;

    public CustomFirestoreRecyclerAdapter(@NonNull FirestoreRecyclerOptions<Space> options, Context context) {
        super(options);
        this.context = context;
        listener = (RecyclerViewDataChangedListener) context;
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
    }


    @Override
    public void onDataChanged() {
        super.onDataChanged();
        listener.onRecyclerViewDataChanged(getItemCount());
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull final Space model) {
        if (holder instanceof SpaceManagementViewHolder) {
            bindDataForSpaceManagement((SpaceManagementViewHolder) holder, model);
        } else {
            bindDataForAdmin((AdminViewHolder) holder, model);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View view;
        if (context instanceof SpaceManagementActivity) {
            view = inflater.inflate(R.layout.favorite_recycler_view_item_layout,
                    viewGroup, false);
            return new SpaceManagementViewHolder(view);
        } else if (context instanceof AdminActivity) {
            view = inflater.inflate(R.layout.search_recycleview_item_layout,
                    viewGroup, false);
            return new AdminViewHolder(view);
        }
        return null;
    }

    public void bindDataForSpaceManagement(SpaceManagementViewHolder holder, final Space model) {
        holder.mTextViewType.setText(model.getLoai());
        holder.mTextViewSpaceTitle.setText(model.getTieuDe());
        holder.mTextViewAddress.setText(model.getDiaChiDayDu());

        holder.mImageBageStatus.setVisibility(View.VISIBLE);
        switch (model.getTrangThai()) {
            case "allow":
                holder.mImageBageStatus.setColorFilter(context.getColor(android.R.color.holo_green_light));
                break;
            case "pending":
                holder.mImageBageStatus.setColorFilter(context.getColor(android.R.color.holo_orange_dark));
                break;
            case "not_allow":
                holder.mImageBageStatus.setColorFilter(context.getColor(android.R.color.holo_red_light));
                break;
            case "not_publish":
                holder.mImageBageStatus.setColorFilter(context.getColor(R.color.dark_gray));
                break;
        }

        Glide.with(context)
                .load(R.raw.loading)
                .useAnimationPool(true)
                .into(holder.mImageView);

        StorageReference mFirtImageRef = mFirebaseStorage
                .getReference(model.getIdChu())
                .child(model.getId())
                .child(1 + "");

        Glide.with(context)
                .load(mFirtImageRef)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.mImageView);

        holder.setOnItemClickLister(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SpaceDetailActivity.class);
                intent.putExtra("current space", model);
                intent.putExtra("from", context.getClass().getSimpleName());
                context.startActivity(intent);
            }
        });
    }

    public void bindDataForAdmin(final AdminViewHolder holder, final Space model) {
        holder.mTextViewType.setText(model.getLoai());
        holder.mTextViewSpaceTitle.setText(model.getTieuDe());
        holder.mTextViewAddress.setText(model.getDiaChiDayDu());
        holder.mTextViewPrice.setText((int) model.getDienTich() + "");
        holder.mTextViewPrice.append(Html.fromHtml("m<sup><small>2</small></sup>"));
        holder.mTextViewPrice.append(" - ");
        double price = model.getGia();
        String priceStr = String.valueOf((int) price);
        if (priceStr.length() >= 10) {
            price /= 1000000000.0;
            holder.mTextViewPrice.append(String.format("%.1f tỉ đồng", price));
        } else if (priceStr.length() >= 7) {
            price /= 1000000.0;
            holder.mTextViewPrice.append(String.format("%.1f triệu đồng", price));
        } else if (priceStr.length() >= 4) {
            price /= 1000.0;
            holder.mTextViewPrice.append(price + " nghìn đồng");
        } else {
            holder.mTextViewPrice.append(price + " đồng");
        }

        DateFormat dateFormat = new SimpleDateFormat("dd - MM - yyyy");
        Date currentDate = model.getTimeAdded();
        holder.mTextViewDate.setText("Ngày đăng: " + dateFormat.format(currentDate));

        GlideApp.with(context)
                .load(R.raw.loading)
                .useAnimationPool(true)
                .into(holder.mImageView);

        final StorageReference mFirtImageRef = mFirebaseStorage
                .getReference(model.getIdChu())
                .child(model.getId())
                .child(1 + "");

        mFirtImageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                long updatedTime = storageMetadata.getUpdatedTimeMillis();
                Glide.with(holder.mImageView.getContext())
                        .load(mFirtImageRef)
                        .signature(new ObjectKey(updatedTime))
                        .into(holder.mImageView);
            }
        });

        holder.setOnItemClickLister(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SpaceDetailActivity.class);
                intent.putExtra("current space", model);
                intent.putExtra("from", context.getClass().getSimpleName());
                context.startActivity(intent);
            }
        });
    }


    public class SpaceManagementViewHolder extends RecyclerView.ViewHolder {
        TextView mTextViewType;
        TextView mTextViewSpaceTitle;
        TextView mTextViewAddress;
        ImageView mImageView;
        ImageView mImageBageStatus;


        public SpaceManagementViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextViewType = itemView.findViewById(R.id.item_type);
            mTextViewSpaceTitle = itemView.findViewById(R.id.item_title);
            mTextViewAddress = itemView.findViewById(R.id.item_address);
            mImageView = itemView.findViewById(R.id.item_image);
            mImageBageStatus = itemView.findViewById(R.id.item_image_status_badge);
        }

        public void setOnItemClickLister(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);
        }
    }

    public class AdminViewHolder extends RecyclerView.ViewHolder {
        TextView mTextViewType;
        TextView mTextViewSpaceTitle;
        TextView mTextViewDate;
        TextView mTextViewAddress;
        TextView mTextViewPrice;
        ImageView mImageView;


        public AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextViewType = itemView.findViewById(R.id.item_type);
            mTextViewSpaceTitle = itemView.findViewById(R.id.item_title);
            mTextViewDate = itemView.findViewById(R.id.item_date);
            mTextViewAddress = itemView.findViewById(R.id.item_address);
            mTextViewPrice = itemView.findViewById(R.id.item_textview_price);
            mImageView = itemView.findViewById(R.id.item_image);
        }

        public void setOnItemClickLister(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);
        }
    }


}
