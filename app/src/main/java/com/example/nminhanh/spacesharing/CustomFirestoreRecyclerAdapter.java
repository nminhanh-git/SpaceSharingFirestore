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
import com.bumptech.glide.signature.ObjectKey;
import com.example.nminhanh.spacesharing.Model.Space;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View view;
        if (context instanceof SpaceManagementActivity) {
            view = inflater.inflate(R.layout.search_recycleview_item_layout
                    , viewGroup, false);
            return new SpaceManagementViewHolder(view);
        }
        return null;
    }

    public void bindDataForSpaceManagement(SpaceManagementViewHolder holder, final Space model) {
        holder.mTextViewType.setText(model.getLoai());
        holder.mTextViewSpaceTitle.setText(model.getTieuDe());
        holder.mTextViewAddress.setText(model.getDiaChiDayDu());
        holder.mTextViewPrice.setText((int) model.getDienTich() + "");
        holder.mTextViewPrice.append(Html.fromHtml("m<sup><small>2</small></sup>"));
        holder.mTextViewPrice.append(" - ");
        double price = model.getGia();
        String priceStr = String.valueOf(price);
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

        holder.mImageView.setImageResource(R.drawable.loading2);
        StorageReference mFirtImageRef = mFirebaseStorage
                .getReference(model.getIdChu())
                .child(model.getId())
                .child(1 + "");

        Glide.with(context)
                .load(mFirtImageRef)
                .into(holder.mImageView);

        holder.setViewOnClickLister(new View.OnClickListener() {
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
        TextView mTextViewPrice;


        public SpaceManagementViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextViewType = itemView.findViewById(R.id.item_type);
            mTextViewSpaceTitle = itemView.findViewById(R.id.item_title);
            mTextViewAddress = itemView.findViewById(R.id.item_address);
            mImageView = itemView.findViewById(R.id.item_image);
            mTextViewPrice = itemView.findViewById(R.id.item_textview_price);
        }

        public void setViewOnClickLister(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);
        }
    }


}
