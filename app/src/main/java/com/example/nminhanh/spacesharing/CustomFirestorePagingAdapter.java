package com.example.nminhanh.spacesharing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.example.nminhanh.spacesharing.Model.Space;
import com.example.nminhanh.spacesharing.Utils.AddressUtils;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CustomFirestorePagingAdapter extends FirestorePagingAdapter<Space, CustomFirestorePagingAdapter.SpaceViewHolder> {

    public static final int REQUEST_DETAIL_SPACE = 100;
    private static final String TAG = "MA:FirestorePaging";
    AddressUtils mAddressUtils;
    Context context;
    FirebaseStorage mFirebaseStorage;
    FirebaseFirestore mFirestore;


    /**
     * Construct a new FirestorePagingAdapter from the given {@link FirestorePagingOptions, @link }.
     *
     * @param options
     * @Context context
     */

    public CustomFirestorePagingAdapter(@NonNull FirestorePagingOptions<Space> options, Context context) {
        super(options);
        mAddressUtils = new AddressUtils(context);
        this.context = context;
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onBindViewHolder(@NonNull final SpaceViewHolder holder, int position, @NonNull Space model) {
        Log.d(TAG, "go to onBindViewHolder");
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
    }

    @NonNull
    @Override
    public SpaceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new SpaceViewHolder(inflater.inflate(R.layout.search_recycleview_item_layout, viewGroup, false));
    }


    private String formatMoney(int gia) {
        return String.format(Locale.getDefault(), "%,d", gia);
    }

    public class SpaceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mTextViewType;
        public TextView mTextViewSpaceTitle;
        public TextView mTextViewDate;
        public TextView mTextViewAddress;
        public ImageView mImageView;
        public TextView mTextViewPrice;


        public SpaceViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextViewType = itemView.findViewById(R.id.item_type);
            mTextViewSpaceTitle = itemView.findViewById(R.id.item_title);
            mTextViewDate = itemView.findViewById(R.id.item_date);
            mTextViewAddress = itemView.findViewById(R.id.item_address);
            mImageView = itemView.findViewById(R.id.item_image);
            mTextViewPrice = itemView.findViewById(R.id.item_textview_price);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            DocumentSnapshot documentSnapshot = getCurrentList().get(getAdapterPosition());
            Space mCurrentSpace = documentSnapshot.toObject(Space.class);
            Intent intent = new Intent(context, SpaceDetailActivity.class);
            intent.putExtra("current space", mCurrentSpace);
            intent.putExtra("from", context.getClass().getSimpleName());
            ((Activity) context).startActivityForResult(intent, REQUEST_DETAIL_SPACE);
        }
    }
}
