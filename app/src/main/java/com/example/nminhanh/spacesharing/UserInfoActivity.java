package com.example.nminhanh.spacesharing;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserInfoActivity extends AppCompatActivity {

    private static final int REQUEST_VERIFY_PHONE_NUMBER = 1;
    private static final int REQUEST_UPDATE_PHONE_NUMBER = 2;
    private static final String TAG = "MA:UserInfoActivity";
    Toolbar mToolbar;
    ImageView mImageViewLogo;
    TextView mTextViewSubtile;
    EditText mEditName;
    EditText mEditPhone;
    EditText mEditMail;
    Button mBtnContinue;
    Button mBtnVerify;
    ImageView mImageViewVerified;

    boolean isPhoneNumberVerified = false;
    String mName = "";
    String mFacebookName = "";
    String mMail = "";
    String mPhone = "";
    String mOldPhone = "";
    String mProvider = "";

    FirebaseAuth mFirebaseAuth;
    FirebaseUser mCurrentUser;
    FirebaseFirestore mFirestore;
    CollectionReference mUserCollRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        initializeView();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        mFirestore = FirebaseFirestore.getInstance();
        mUserCollRef = mFirestore.collection("user_data");

        setupUI();

        mBtnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mName.isEmpty() && !mPhone.isEmpty() && !mMail.isEmpty()
                        && mEditName.getError() == null && mEditPhone.getError() == null && mEditMail.getError() == null
                        && isPhoneNumberVerified) {
                    UpdateUserInfo();
                } else {
                    if (mEditName.getText().toString().isEmpty()) {
                        mEditName.setError("Bạn chưa nhập họ tên");
                    }

                    if (mEditMail.getText().toString().isEmpty()) {
                        mEditMail.setError("Bạn chưa nhập địa chỉ email");
                    }

                    if (mEditPhone.getText().toString().isEmpty()) {
                        mBtnVerify.setVisibility(View.GONE);
                        mImageViewVerified.setVisibility(View.GONE);
                        mEditPhone.setError("Bạn chưa nhập số điện thoại");
                    } else if (!isPhoneNumberVerified) {
                        mBtnVerify.setVisibility(View.VISIBLE);
                        mImageViewVerified.setVisibility(View.GONE);
                        mEditPhone.setError("Bạn chưa xác thực số điện thoại", null);
                    }
                }
            }
        });
        mBtnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent verifyIntent = new Intent(UserInfoActivity.this, OTPActivity.class);
                verifyIntent.putExtra("phone number", mPhone);
                if (mProvider.equalsIgnoreCase("facebook")) {
                    verifyIntent.putExtra("command", "verify");
                    startActivityForResult(verifyIntent, REQUEST_VERIFY_PHONE_NUMBER);
                }
                if (mProvider.equalsIgnoreCase("account management")) {
                    verifyIntent.putExtra("command", "edit");
                    startActivityForResult(verifyIntent, REQUEST_UPDATE_PHONE_NUMBER);
                }
            }
        });


    }

    private void setupUI() {
        Intent mIntent = getIntent();
        mName = mIntent.getStringExtra("user name");
        mMail = mIntent.getStringExtra("user mail");
        mPhone = mIntent.getStringExtra("user phone");
        mPhone = mPhone.replace("+84", "0");
        mOldPhone = mPhone;
        mFacebookName = "rỗng";
        mProvider = mIntent.getStringExtra("provider");

        mEditName.setText(mName);
        mEditPhone.setText(mPhone);
        mEditMail.setText(mMail);


        if (mProvider.equalsIgnoreCase("facebook")) {
            GraphRequest mFbInfoRequest = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken()
                    , new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            try {
                                mFacebookName = object.getString("name");
                                mMail = object.getString("email");
                                mEditName.setText(mFacebookName);
                                mEditMail.setText(mMail);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            Bundle infoParams = new Bundle();
            infoParams.putString("fields", "name,email");
            mFbInfoRequest.setParameters(infoParams);
            mFbInfoRequest.executeAsync();
        }

        if (mProvider.equalsIgnoreCase("sign in")) {
            isPhoneNumberVerified = true;
            mEditPhone.setEnabled(false);
            mBtnVerify.setVisibility(View.GONE);
            mImageViewVerified.setVisibility(View.VISIBLE);
        }
        if (mProvider.equalsIgnoreCase("account management")) {
            mBtnVerify.setVisibility(View.GONE);
            mImageViewVerified.setVisibility(View.VISIBLE);
            isPhoneNumberVerified = true;
            mTextViewSubtile.setText("Thay đổi thông tin cá nhân của bạn.");

            AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(this)
                    .setTitle("Thay đổi thông tin cá nhân")
                    .setMessage("Lưu ý rằng số điện thoại mới, sau khi được xác thực, sẽ ngay lập tức được cập nhật thành số điện thoại của tài khoản.")
                    .setPositiveButton("Tôi đã hiểu", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog userPhoneNumberDialog = mDialogBuilder.create();
            userPhoneNumberDialog.show();
        }
        mEditName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    mName = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mEditMail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    mMail = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!mEditMail.getText().toString().isEmpty()) {
                    mMail = mEditMail.getText().toString();
                    if (!mMail.contains("@")) {
                        mEditMail.setError("bạn nhập địa chỉ email chưa đúng định dạng");
                    } else {
                        mEditMail.setError(null);
                    }
                }
            }
        });

        mEditPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.equals("")) {
                    mPhone = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mEditPhone.getText().toString().isEmpty()) {
                    mImageViewVerified.setVisibility(View.GONE);
                    mEditPhone.setError("Bạn chưa nhập số điện thoại");
                    mBtnVerify.setVisibility(View.GONE);
                } else {
                    if (mEditPhone.getText().toString().length() != 10) {
                        mEditPhone.setError("Bạn chưa nhập số điện thoại đúng định dạng");
                        mBtnVerify.setVisibility(View.GONE);
                        mImageViewVerified.setVisibility(View.GONE);
                    } else {
                        mEditPhone.setError(null);
                        if (mProvider.equalsIgnoreCase("account management")
                                && mPhone.equalsIgnoreCase(mOldPhone)) {
                            mBtnVerify.setVisibility(View.GONE);
                            mImageViewVerified.setVisibility(View.VISIBLE);
                            isPhoneNumberVerified = true;
                        } else {
                            mBtnVerify.setVisibility(View.VISIBLE);
                            mImageViewVerified.setVisibility(View.GONE);
                            isPhoneNumberVerified = false;
                        }
                    }
                }
            }
        });
    }

    private void UpdateUserInfo() {
        if (mProvider.equalsIgnoreCase("sign in")
                || mProvider.equalsIgnoreCase("facebook")) {
            Map<String, String> userData = new HashMap<>();
            userData.put("email", mMail);
            userData.put("facebookName", mFacebookName);
            mUserCollRef.document(mCurrentUser.getUid()).set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "setup user_data successfully");
                    UserProfileChangeRequest profileUpdateRequest = new UserProfileChangeRequest.Builder()
                            .setDisplayName(mName)
                            .build();
                    mCurrentUser.updateProfile(profileUpdateRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "update profile successfully!");
                                finishUpdateProfile();
                            } else {
                                Toast.makeText(UserInfoActivity.this, "đã xảy ra lỗi, vui lòng thử lại\n"
                                        + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                Log.d(TAG, task.getException().getMessage());
                            }
                        }
                    });
                }
            });
        }

        if (mProvider.equalsIgnoreCase("account management")) {
            mUserCollRef.document(mCurrentUser.getUid()).update("email", mMail)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            UserProfileChangeRequest profileUpdateRequest = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(mName)
                                    .build();
                            mCurrentUser.updateProfile(profileUpdateRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "update profile successfully!");
                                        finishUpdateProfile();
                                    } else {
                                        Toast.makeText(UserInfoActivity.this, "đã xảy ra lỗi, vui lòng thử lại\n"
                                                + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, task.getException().getMessage());
                                    }
                                }
                            });
                        }
                    });
        }
    }

    private void finishUpdateProfile() {
        if (mProvider.equalsIgnoreCase("account management")) {
            setResult(RESULT_OK);
            finish();
        }
        if (mProvider.equalsIgnoreCase("facebook")) {
            Intent intent = new Intent(UserInfoActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        if (mProvider.equalsIgnoreCase("sign in")) {
            finish();
        }
    }

    private void initializeView() {
        mToolbar = findViewById(R.id.user_info_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mImageViewLogo = mToolbar.findViewById(R.id.user_info_logo);

        mTextViewSubtile = findViewById(R.id.user_info_subtitle);
        mEditName = findViewById(R.id.user_info_edit_text_name);
        mEditMail = findViewById(R.id.user_info_mail);
        mEditPhone = findViewById(R.id.user_info_edit_text_phone);
        mBtnContinue = findViewById(R.id.user_info_btn_continue);
        mBtnVerify = findViewById(R.id.user_info_btn_phone_verify);
        mBtnVerify.setVisibility(View.GONE);
        mImageViewVerified = findViewById(R.id.user_info_image_view_verified);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_VERIFY_PHONE_NUMBER ||
                requestCode == REQUEST_UPDATE_PHONE_NUMBER) {
            if (resultCode == RESULT_OK) {
                mBtnVerify.setVisibility(View.GONE);
                mImageViewVerified.setVisibility(View.VISIBLE);
                mEditPhone.setEnabled(false);
                isPhoneNumberVerified = true;
                mEditPhone.setError(null);

            } else if (resultCode == RESULT_CANCELED) {
                mBtnVerify.setVisibility(View.VISIBLE);
                mEditPhone.setError("Bạn chưa xác thực số điện thoại", null);
                if (data.getStringExtra("result from event").equalsIgnoreCase("linking error")) {
                    mEditPhone.setError("Số điện thoại này đã được liên kết với một tài khoản khác, vui lòng chọn số điện thoại khác", null);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Do nothing
    }
}
