package com.example.nminhanh.spacesharing;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {
    private static final int REQUEST_SIGN_IN = 1;
    Toolbar mToolbar;
    ImageView mImageViewSignIn;
    EditText mEditTextAccount;
    Button mButtonSignIn;
    RelativeLayout mLayoutLoading;
    RelativeLayout mLayoutSignIn;
    ImageButton mBtnBack;

    String userName = "";
    boolean isSignInBtnClicked = false;

    FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        initialize();
        mFirebaseAuth = FirebaseAuth.getInstance();

        mEditTextAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.equals("")) {
                    userName = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String res = mEditTextAccount.getText().toString();
                if (!res.equals("")) {
                    userName = res;
                    if (userName.length() != 10) {
                        mEditTextAccount.setError("Bạn nhập số điện thoại chưa đúng định dạng");
                    } else {
                        mEditTextAccount.setError(null);
                    }
                }
            }
        });


        mButtonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSignInBtnClicked = true;
                if (!userName.isEmpty() && mEditTextAccount.getError() == null) {
                    signIn();
                } else {
                    mEditTextAccount.setError("Bạn chưa nhập số điện thoại");
                }
            }
        });

        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void initialize() {
        mToolbar = findViewById(R.id.sign_in_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        mLayoutSignIn = findViewById(R.id.sign_in_layout);
        mImageViewSignIn = findViewById(R.id.image_view_sign_in);
        Glide.with(this).load(R.drawable.sign_in_image).into(mImageViewSignIn);
        mEditTextAccount = findViewById(R.id.edit_text_sign_in_account);
        mButtonSignIn = findViewById(R.id.sign_in_btn_sign_in);
        mLayoutLoading = findViewById(R.id.sign_in_loading_layout);
        mBtnBack = findViewById(R.id.sign_in_btn_back);
    }

    private void signIn() {
        Intent OtpIntent = new Intent(SignInActivity.this, OTPActivity.class);
        OtpIntent.putExtra("command", "sign in");
        OtpIntent.putExtra("phone number", userName);
        startActivityForResult(OtpIntent, REQUEST_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                mLayoutLoading.setVisibility(View.GONE);
                Intent goToMainActivityIntent = new Intent(SignInActivity.this, MainActivity.class);
                startActivity(goToMainActivityIntent);
                finish();
            }
        }
    }
}
