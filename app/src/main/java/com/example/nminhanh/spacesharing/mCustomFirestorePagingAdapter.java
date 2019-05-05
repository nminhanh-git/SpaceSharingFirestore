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
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Locale;

public class mCustomFirestorePagingAdapter extends FirestorePagingAdapter<Space, mCustomFirestorePagingAdapter.SpaceViewHolder> {

    static final String IMAGE_STORAGE_BASE_URL = "gs://spacesharing-298d6.appspot.com";
    static final String LOADING_PLACEHOLDER_IMAGE = "https://media.giphy.com/media/6036p0cTnjUrNFpAlr/giphy.gif";

    AddressUtils mAddressUtils;
    Context context;

    /**
     * Construct a new FirestorePagingAdapter from the given {@link FirestorePagingOptions}.
     *
     * @param options
     */
    public mCustomFirestorePagingAdapter(@NonNull FirestorePagingOptions<Space> options, Context context) {
        super(options);
        mAddressUtils = new AddressUtils(context);
        this.context = context;
    }

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

        holder.mTextViewPrice.setText(
                (int) model.getDienTich() + Html.fromHtml(context.getString(R.string.square_metrer_metric_string)).toString()
                        + " - " + formatMoney((int) model.getGia()) + " đồng"
        );
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


    private String formatMoney(int gia) {
        return String.format(Locale.getDefault(), "%,d", gia);
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
            DocumentSnapshot documentSnapshot = getCurrentList().get(getAdapterPosition());
            Space mCurrentSpace = documentSnapshot.toObject(Space.class);
            Intent intent = new Intent(context, SpaceDetailActivity.class);
            intent.putExtra("space title", mCurrentSpace.getTieuDe());
            context.startActivity(intent);
        }
    }


}
