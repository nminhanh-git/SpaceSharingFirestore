package com.example.nminhanh.spacesharing;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;

public class SpaceDetailActivity extends AppCompatActivity {

    Toolbar mToolbar;
    ImageButton mBtnBack;
    ImageButton mBtnFavorite;
    Button mBtnEdit;

    RecyclerView mRecyclerViewImage;

    TextView mTextViewType;
    TextView mTextViewTitle;
    TextView mTextViewDate;

    TextView mTextViewPrice;
    TextView mTextViewSize;
    TextView mTextViewPrePaid;

    CircleImageView mImageOwnerAva;
    TextView mTextViewOwnerName;
    TextView mTextViewOwnerEmail;
    TextView mTextViewOwnerPhone;

    TextView mTextViewAddress;
    TextView mTextViewDescription;

    TextView mTextViewDoor;
    TextView mTextViewElectric;
    TextView mTextViewWater;
    TextView mTextViewBed;
    TextView mTextViewBath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_detail);

        initializeView();
    }

    private void initializeView() {
        mToolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        mBtnBack = mToolbar.findViewById(R.id.detail_btn_cancel);
        mBtnFavorite = mToolbar.findViewById(R.id.detail_btn_favorite);
        mBtnEdit = mToolbar.findViewById(R.id.detail_btn_edit);

        mTextViewType = findViewById(R.id.detail_text_view_type);
        mTextViewTitle = findViewById(R.id.detail_text_view_title);
        mTextViewDate = findViewById(R.id.detail_text_view_date);

        mTextViewPrice = findViewById(R.id.detail_text_view_price);
        mTextViewSize = findViewById(R.id.detail_text_view_size);
        mTextViewPrePaid = findViewById(R.id.detail_text_view_prepaid);

        mImageOwnerAva = findViewById(R.id.detail_account_image);
        mTextViewOwnerName = findViewById(R.id.detail_account_text_view_name);
        mTextViewOwnerEmail = findViewById(R.id.detail_account_text_view_email);
        mTextViewOwnerPhone = findViewById(R.id.detail_account_text_view_phone);

        mTextViewAddress = findViewById(R.id.detail_text_view_address_value);
        mTextViewDescription = findViewById(R.id.detail_text_view_description_value);

        mTextViewDoor = findViewById(R.id.detail_text_view_other_door);
        mTextViewElectric = findViewById(R.id.detail_text_view_other_electric);
        mTextViewWater = findViewById(R.id.detail_text_view_other_water);
        mTextViewBed = findViewById(R.id.detail_text_view_other_bed);
        mTextViewBath = findViewById(R.id.detail_text_view_other_bath);
    }
}
