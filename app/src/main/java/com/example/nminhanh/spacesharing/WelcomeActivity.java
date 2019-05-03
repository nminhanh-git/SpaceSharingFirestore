package com.example.nminhanh.spacesharing;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.nminhanh.spacesharing.Utils.FacebookConnectUtils;
import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = "MinhAnh.welcomeActiviy";
    Button mBtnSignIn;
    Button mBtnFacebook;
    TextView mTextViewNoSignIn;
    ImageView mImageViewWelcome;
    RelativeLayout mLoadingLayout;

    FacebookConnectUtils mFacebookUtils;
    FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // initial step for everything
        initialize();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFacebookUtils = new FacebookConnectUtils();

        // Add onClick listener for BUTTONS
        mBtnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });

        mTextViewNoSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mBtnFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFacebookUtils.loginWithReadPermission(WelcomeActivity.this, Arrays.asList("public_profile"));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (AccessToken.getCurrentAccessToken() != null) {
            mFacebookUtils.logOut();
        }
        mFacebookUtils.registerLoginCallback(new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
                mLoadingLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");

            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onCancel", error);

            }
        });
    }

    @Override
    protected void onStop() {
        mFacebookUtils.unregisterLoginCallback();
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mFacebookUtils.getmFacebookCallbackManager().onActivityResult(requestCode, resultCode, data);
    }

    private void initialize() {
        mBtnSignIn = findViewById(R.id.btn_welcome_sign_in);
        mBtnFacebook = findViewById(R.id.welcome_fb_button);
        mImageViewWelcome = findViewById(R.id.image_view_welcome);
        mTextViewNoSignIn = findViewById(R.id.welcome_text_view_no_sign_in);
        Glide.with(this).load(R.drawable.welcome_image).into(mImageViewWelcome);
        mLoadingLayout = findViewById(R.id.welcome_loading_layout);
    }

    private void handleFacebookAccessToken(AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());

        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
                            mLoadingLayout.setVisibility(View.INVISIBLE);
                            if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                                setUpInfoForNewUser(mCurrentUser);
                            } else {
                                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                                startActivity(intent);
                                Toast.makeText(WelcomeActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            Log.d(TAG, "onFirebaseSignInCompleteFail:" + task.getException().getMessage());
                        }
                    }
                });
    }

    public void setUpInfoForNewUser(FirebaseUser mCurrentUser) {
        Intent infoIntent = new Intent(WelcomeActivity.this, UserInfoActivity.class);
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
        infoIntent.putExtra("provider", "facebook");
        startActivity(infoIntent);
        finish();
    }


}
