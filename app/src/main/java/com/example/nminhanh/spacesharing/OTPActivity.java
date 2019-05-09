package com.example.nminhanh.spacesharing;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mukesh.OnOtpCompletionListener;
import com.mukesh.OtpView;

import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {

    private static final String TAG = "MinhAnhOTPActivity";
    private static final int REQUEST_NEW_USER_INFO = 1;
    Toolbar mToolbar;
    ImageView mImageViewToolbar;
    ImageButton mButtonBack;
    ImageView mImageBackground;
    OtpView mOtpView;
    TextView mTextViewCountDownTitle;
    TextView mTextViewCountDownReset;
    Button mBtnVerify;
    RelativeLayout mLayoutLoading;
    TextView mTextViewLoading;

    String phoneNumber = "";
    CountDownTimer mCountDown;
    long minutes = 0;
    long seconds = 0;

    FirebaseAuth mFirebaseAuth;

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack;
    PhoneAuthCredential mPhoneAuthCredential;
    String VerificationId;
    String verificationCode;
    private String command;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        mFirebaseAuth = FirebaseAuth.getInstance();

        initializeView();
        // init count down timer
        mCountDown = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String minString = "";
                String secString = "";
                seconds = millisUntilFinished / 1000;
                if (seconds >= 60) {
                    minutes = 1;
                    seconds -= 60;
                } else {
                    minutes = 0;
                }
                if (minutes < 10) {
                    minString = "0" + minutes;
                }
                if (seconds < 10) {
                    secString = "0" + seconds;
                } else {
                    secString = "" + seconds;
                }
                String string = "Mã xác nhận sẽ bị hủy trong <font color=#e83841>" + minString + ":" + secString + "</font>";
                mTextViewCountDownTitle.setText(Html.fromHtml(string));
            }


            @Override
            public void onFinish() {
                mTextViewCountDownTitle.setText("Mã xác nhận đã bị hủy. ");
                mTextViewCountDownReset.setVisibility(View.VISIBLE);
            }
        };
        mTextViewCountDownReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextViewCountDownReset.setVisibility(View.GONE);
                sendVerificationCode(phoneNumber);
            }
        });

        // init OtpView
        mOtpView.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                verificationCode = otp;
                Toast.makeText(OTPActivity.this, verificationCode, Toast.LENGTH_SHORT).show();
                mBtnVerify.setVisibility(View.VISIBLE);
            }
        });

        mOtpView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() < 6) {
                    mBtnVerify.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // get phone number from previous intent and get the command to know where the
        // intent comes from
        final Intent mIntent = getIntent();
        phoneNumber = mIntent.getStringExtra("phone number");
        command = mIntent.getStringExtra("command");

        mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.d("MaVerification", e.getMessage());
                if (e.getMessage().equalsIgnoreCase(getString(R.string.op_network_error_string))) {
                    mBtnVerify.setVisibility(View.GONE);
                    showNetworkErrorDialog();


                }
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                Log.d("OTPMinhAnh", "go to onCodeSent");
                VerificationId = s;
                mCountDown.start();
                mOtpView.setText("");
            }
        };

        sendVerificationCode(phoneNumber);

        mBtnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLayoutLoading.setVisibility(View.VISIBLE);
                mTextViewLoading.setText("Đang xác nhận...");
                mPhoneAuthCredential = PhoneAuthProvider.getCredential(VerificationId, verificationCode);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        switch (command.toLowerCase()) {
                            case "sign in":
                                signInWithPhoneAuthCredential(mPhoneAuthCredential);
                                break;
                            case "verify":
                                linkWithPhoneAuthCredential(mPhoneAuthCredential);
                                break;
                            case "edit":
                                updateWithPhoneAuthCredential(mPhoneAuthCredential);
                                break;
                        }
                    }
                }, 1200);
            }
        });

        mButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntentBack = new Intent();
                mIntentBack.putExtra("result from event", "cancel");
                setResult(RESULT_CANCELED, mIntentBack);
                finish();
            }
        });
    }

    private void showNetworkErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Đã có lỗi xảy ra")
                .setMessage("Xác thực thất bại do thiết bị chưa kết nối mạng, vui lòng thử lại.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    private void initializeView() {
        mToolbar = findViewById(R.id.otp_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mImageViewToolbar = mToolbar.findViewById(R.id.otp_logo);
        Glide.with(this).load(R.drawable.logo_2).into(mImageViewToolbar);

        mButtonBack = findViewById(R.id.otp_btn_back);

        mImageBackground = findViewById(R.id.otp_image_view);
        Glide.with(this).load(R.drawable.otp_image).into(mImageBackground);

        mOtpView = findViewById(R.id.otp_view);
        mTextViewCountDownTitle = findViewById(R.id.otp_text_view_countdown_text);
        mTextViewCountDownReset = findViewById(R.id.otp_text_view_countdown_reset);

        mBtnVerify = findViewById(R.id.otp_btn_verify);
        mBtnVerify.setVisibility(View.INVISIBLE);

        mLayoutLoading = findViewById(R.id.otp_loading_layout);
        mTextViewLoading = findViewById(R.id.progress_text);
    }

    private void sendVerificationCode(String number) {
        StringBuilder builder = new StringBuilder(number);
        builder.replace(0, 0, "+84");
//        number = PhoneNumberUtils.formatNumberToE164(number, "VN");
        number = builder.toString();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                this,
                mCallBack);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        mTextViewLoading.setText("Đang đăng nhập, đợi xíu nha...");
        mFirebaseAuth.signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mLayoutLoading.setVisibility(View.GONE);
                            Toast.makeText(OTPActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                                FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
                                setUpInfoForNewUser(mCurrentUser);
                            }
                            setResult(RESULT_OK);
                            finish();
                        } else if (task.getException().getMessage().equalsIgnoreCase(getString(R.string.otp_phone_auth_error_wrong_code_string))) {
                            mOtpView.setText("");
                            Toast.makeText(OTPActivity.this, "Mã xác nhận không đúng. Vui lòng nhập lại", Toast.LENGTH_SHORT).show();
                        } else if (task.getException().getMessage().equalsIgnoreCase(getString(R.string.otp_phone_auth_error_linked_credential_string))) {
                            Log.d(TAG, task.getException().getMessage());
                            Intent intent = new Intent();
                            intent.putExtra("result from event", "linking error");
                            setResult(RESULT_CANCELED, intent);
                            finish();
                        }
                    }
                });
    }

    private void linkWithPhoneAuthCredential(PhoneAuthCredential mPhoneAuthCredential) {
        mFirebaseAuth.getCurrentUser().linkWithCredential(mPhoneAuthCredential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            if (task.getException().getMessage().equalsIgnoreCase(getString(R.string.otp_phone_auth_error_wrong_code_string))) {
                                Toast.makeText(OTPActivity.this, "Mã xác nhận không đúng. Vui lòng nhập lại", Toast.LENGTH_SHORT).show();
                            } else if (task.getException().getMessage().equalsIgnoreCase(getString(R.string.otp_phone_auth_error_linked_credential_string))) {
                                Log.d(TAG, task.getException().getMessage());
                                Intent intent = new Intent();
                                intent.putExtra("result from event", "linking error");
                                setResult(RESULT_CANCELED, intent);
                                finish();
                            }
                        }
                    }
                });
    }

    private void updateWithPhoneAuthCredential(PhoneAuthCredential mPhoneAuthCredential) {
        mFirebaseAuth.getCurrentUser().updatePhoneNumber(mPhoneAuthCredential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            if (task.getException().getMessage().equalsIgnoreCase(getString(R.string.otp_phone_auth_error_wrong_code_string))) {
                                Toast.makeText(OTPActivity.this, "Mã xác nhận không đúng. Vui lòng nhập lại", Toast.LENGTH_SHORT).show();
                            } else if (task.getException().getMessage().equalsIgnoreCase(getString(R.string.otp_phone_auth_error_linked_credential_string))) {
                                Log.d(TAG, task.getException().getMessage());
                                Intent intent = new Intent();
                                intent.putExtra("result from event", "linking error");
                                setResult(RESULT_CANCELED, intent);
                                finish();
                            }
                        }
                    }
                });
    }

    public void setUpInfoForNewUser(FirebaseUser mCurrentUser) {
        Intent infoIntent = new Intent(OTPActivity.this, UserInfoActivity.class);
        String name = "";
        String phoneNumber = "";
        String mail = "";
        if (mCurrentUser.getDisplayName() != null) {
            name = mCurrentUser.getDisplayName();
        }
        if (mCurrentUser.getEmail() != null) {
            mail = mCurrentUser.getEmail();
        }
        if (mCurrentUser.getPhoneNumber() != null) {
            phoneNumber = mCurrentUser.getPhoneNumber();
        }
        infoIntent.putExtra("user name", name);
        infoIntent.putExtra("user mail", mail);
        infoIntent.putExtra("user phone", phoneNumber);
        infoIntent.putExtra("provider", "sign in");
        startActivity(infoIntent);
    }
}
