package com.example.nminhanh.spacesharing.Fragment.MainPages;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.example.nminhanh.spacesharing.FavoriteSpaceActivity;
import com.example.nminhanh.spacesharing.GlideApp;
import com.example.nminhanh.spacesharing.R;
import com.example.nminhanh.spacesharing.SpaceManagementActivity;
import com.example.nminhanh.spacesharing.UserInfoActivity;
import com.example.nminhanh.spacesharing.WelcomeActivity;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {

    private static final String TAG = "MinhAnh:AccountFragment";
    private static final int REQUEST_EDIT_PROFILE = 1;
    private static final int REQUEST_CHANGE_PROFILE_PICTURE = 2;

    public AccountFragment() {
        // Required empty public constructor
    }

    View view;
    CircleImageView mImageViewProfile;
    TextView mTextViewName;
    TextView mTextViewEmail;
    TextView mTextViewPhone;
    Button mBtnEditProfile;
    TextView mTextViewFacebookIntro;
    TextView mTextViewFacebookName;
    Button mBtnConnectFacebook;
    RelativeLayout mBtnSpaceManagement;
    RelativeLayout mBtnFavoriteSpace;
    Button mBtnSignOut;
    Button mBtnChangePicture;
    RelativeLayout mLayoutRecommendSignIn;
    Button mBtnRecommenddSignIn;
    AlertDialog facebookLoadingDialog;


    SignOutListener mSignOutListener;
    ShowFacebookLoadingListener mShowLoadingListener;

    FirebaseAuth mFirebaseAuth;
    FirebaseUser mCurrentUser;
    FirebaseFirestore mFirestore;

    FirebaseStorage mFirebaseStorage;

    String mFacebookNameData;
    String mEmailData;

    CallbackManager mFacebookCallbackManager;


    @Override
    public void onAttach(Context context) {
        mSignOutListener = (SignOutListener) context;
        mShowLoadingListener = (ShowFacebookLoadingListener) context;
        super.onAttach(context);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_account, container, false);
        setHasOptionsMenu(true);
        intitializeView();

        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mFacebookCallbackManager = CallbackManager.Factory.create();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        mFirestore = FirebaseFirestore.getInstance();

        mFirebaseStorage = FirebaseStorage.getInstance();

        mBtnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentUser != null) {
                    mFirebaseAuth.signOut();
                    if (isLinkedWithFacebook()) {
                        LoginManager.getInstance().logOut();
                    }
                    mLayoutRecommendSignIn.setVisibility(View.VISIBLE);
                    mSignOutListener.onSignOut();
                }
            }
        });

        mBtnRecommenddSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = new Intent(getActivity(), WelcomeActivity.class);
                startActivity(signInIntent);
            }
        });

        mBtnConnectFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLinkedWithFacebook()) {
                    showUnlinkFacebookDialog();
                } else {
                    facebookLoadingDialog.show();
                    if (AccessToken.getCurrentAccessToken() != null) {
                        LoginManager.getInstance().logOut();
                    }
                    LoginManager.getInstance().logInWithReadPermissions(AccountFragment.this, Arrays.asList("email", "public_profile"));
                }
            }
        });

        mBtnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editInfoIntent = new Intent(getActivity(), UserInfoActivity.class);
                editInfoIntent.putExtra("user name", mCurrentUser.getDisplayName());
                editInfoIntent.putExtra("user mail", mEmailData);
                editInfoIntent.putExtra("user phone", mCurrentUser.getPhoneNumber());
                editInfoIntent.putExtra("provider", "account management");
                startActivityForResult(editInfoIntent, REQUEST_EDIT_PROFILE);
            }
        });

        mBtnChangePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_CHANGE_PROFILE_PICTURE);
            }
        });

        mBtnSpaceManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SpaceManagementActivity.class);
                startActivity(intent);
            }
        });

        mBtnFavoriteSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FavoriteSpaceActivity.class);
                startActivity(intent);
            }
        });


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        LoginManager.getInstance().registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "registerCallback:onSuccess");
                LinkWithFacebookAccount(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {
                Log.d(TAG, "registerCallback:onCancel");
                mShowLoadingListener.onHidingFacebookLoading();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "registerCallback:onError", error);
            }
        });
    }


    private void LinkWithFacebookAccount(AccessToken accessToken) {
        AuthCredential mFbCredential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mCurrentUser.linkWithCredential(mFbCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    facebookLoadingDialog.dismiss();
                    GraphRequest FbInfoRequest = GraphRequest.newMeRequest(
                            AccessToken.getCurrentAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {
                                    String currentFacebookName = "null";
                                    try {
                                        currentFacebookName = object.getString("name");
                                        DocumentReference mUserRef = mFirestore.collection("user_data").document(mCurrentUser.getUid());
                                        mUserRef.update("facebookName", currentFacebookName);

                                        String facebookImageUrl = response.getJSONObject()
                                                .getJSONObject("picture")
                                                .getJSONObject("data")
                                                .getString("url");
                                        updateProfilePicture(facebookImageUrl);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                    Bundle requestFbInfoParameter = new Bundle();
                    requestFbInfoParameter.putString("fields", "name,picture");
                    FbInfoRequest.setParameters(requestFbInfoParameter);
                    FbInfoRequest.executeAsync();

                    Toast.makeText(getContext(), "Kết nối thành công!", Toast.LENGTH_SHORT).show();
                    updateUIWithUserInfo();
                } else {
                    Log.d(TAG, task.getException().getMessage());
                    mShowLoadingListener.onHidingFacebookLoading();
                    AlertDialog mFacebookErrorDialog = new AlertDialog.Builder(getContext())
                            .setIcon(R.drawable.facebook_logo_colored)
                            .setTitle("Lỗi khi liên kết tài khoản Facebook")
                            .setMessage("Tài khoản Facebook hiện tại đã liên kết với một ứng dụng khác, vui lòng đăng xuất bằng ứng dụng Facebook và thử lại bằng một tài khoản khác")
                            .setNegativeButton("Đóng", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create();
                    mFacebookErrorDialog.show();
                }
            }
        });
    }

    private void showUnlinkFacebookDialog() {
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(this.getContext());

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_unlink_fb_layout, null);
        TextView mTextViewCancel = view.findViewById(R.id.dialog_facebook_no);
        TextView mTextViewOk = view.findViewById(R.id.dialog_facebook_yes);

        mDialogBuilder.setView(view);
        final AlertDialog mFacebookDialog = mDialogBuilder.create();
        InsetDrawable insetDrawable = new InsetDrawable(new ColorDrawable(Color.TRANSPARENT), 50, 200, 50, 200);
        mFacebookDialog.getWindow().setBackgroundDrawable(insetDrawable);

        mTextViewCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFacebookDialog.cancel();
            }
        });
        mTextViewOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentUser.unlink("facebook.com").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Hủy liên kết thành công", Toast.LENGTH_SHORT).show();
                            updateUIWithUserInfo();
                            LoginManager.getInstance().logOut();
                            mFacebookDialog.dismiss();
                        }
                    }
                });
            }
        });

        mFacebookDialog.show();


    }

    private void updateUIWithUserInfo() {
        DocumentReference mCurrentUserDocRef = mFirestore.collection("user_data").document(mCurrentUser.getUid());
        mCurrentUserDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "document exists");
                        mEmailData = documentSnapshot.get("email").toString();
                        mTextViewEmail.setText(mEmailData);
                        if (isLinkedWithFacebook()) {
                            mFacebookNameData = documentSnapshot.get("facebookName").toString();
                            mTextViewFacebookName.setText(mFacebookNameData);
                        }
                    } else {
                        Log.d(TAG, "document doesn't exist");
                    }
                } else {
                    Log.d(TAG, task.getException().getMessage());
                }

            }
        });
        mTextViewName.setText(mCurrentUser.getDisplayName());
        mTextViewPhone.setText(mCurrentUser.getPhoneNumber());
        StorageReference mAvatarRef = mFirebaseStorage.getReference(mCurrentUser.getUid() + "/avatar");
        if (!mAvatarRef.getDownloadUrl().isSuccessful()) {
            GlideApp.with(this)
                    .load(mAvatarRef)
                    .signature(new ObjectKey(System.currentTimeMillis()))
                    .into(mImageViewProfile);
        }
        if (isLinkedWithFacebook()) {
            mTextViewFacebookIntro.setText("Tài khoản Facebook:");
            mTextViewFacebookName.setVisibility(View.VISIBLE);
            mBtnConnectFacebook.setText("Hủy kết nối");
            mBtnConnectFacebook.setTextColor(getResources().getColor(R.color.colorCancel, null));
        } else {
            mTextViewFacebookIntro.setText(R.string.fragment_account_facebook_into_string);
            mTextViewFacebookName.setText("");
            mTextViewFacebookName.setVisibility(View.GONE);
            mBtnConnectFacebook.setText("Kết nối ngay");
            mBtnConnectFacebook.setTextColor(getResources().getColor(R.color.facebook_color, null));
        }
    }

    private boolean isLinkedWithFacebook() {
        if (mCurrentUser != null) {
            for (UserInfo user : mCurrentUser.getProviderData()) {
                if (user.getProviderId().equals("facebook.com")) {
                    return true;
                }
            }
        }
        return false;
    }

    private void intitializeView() {
        mImageViewProfile = view.findViewById(R.id.account_image);
        mTextViewName = view.findViewById(R.id.account_text_view_name);
        mTextViewEmail = view.findViewById(R.id.account_text_view_email);
        mTextViewPhone = view.findViewById(R.id.account_text_view_phone);
        mBtnEditProfile = view.findViewById(R.id.account_button_edit_profile);

        mTextViewFacebookIntro = view.findViewById(R.id.account_facebook_text_view_intro);
        mTextViewFacebookName = view.findViewById(R.id.account_facebook_text_view_name);
        mBtnConnectFacebook = view.findViewById(R.id.account_button_connect_facebook);

        mBtnSpaceManagement = view.findViewById(R.id.account_button_space_management);
        mBtnFavoriteSpace = view.findViewById(R.id.account_button_favorite);

        mBtnChangePicture = view.findViewById(R.id.account_button_edit_profile_avatar);

        mBtnSignOut = view.findViewById(R.id.account_button_sign_out);
        mLayoutRecommendSignIn = view.findViewById(R.id.account_layout_recommend_sign_in);
        mBtnRecommenddSignIn = view.findViewById(R.id.account_button_sign_in);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.facebook_loading_layout, null);
        ImageView mImageFbLoading = view.findViewById(R.id.fb_loading_image);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(view);
        GlideApp.with(this)
                .load(R.raw.fb_emo)
                .into(mImageFbLoading);
        facebookLoadingDialog = builder.create();
        InsetDrawable insetDrawable = new InsetDrawable(new ColorDrawable(Color.TRANSPARENT), 50, 200, 50, 200);
        facebookLoadingDialog.getWindow().setBackgroundDrawable(insetDrawable);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_PROFILE) {
            if (resultCode == RESULT_OK) {
                updateUIWithUserInfo();
            }
        }
        if (requestCode == REQUEST_CHANGE_PROFILE_PICTURE) {
            if (resultCode == RESULT_OK) {
                Uri imagePath = data.getData();
                updateProfilePicture(imagePath.toString());
            }
        }

    }

    private void updateProfilePicture(String imagePath) {
        StorageReference mUserImageRef = mFirebaseStorage.getReference(mCurrentUser.getUid()).child("avatar");
        if (!imagePath.contains("http")) {
            mUserImageRef.putFile(Uri.parse(imagePath)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        StorageReference mAvatarRef = mFirebaseStorage.getReference(mCurrentUser.getUid() + "/avatar");
                        GlideApp.with(AccountFragment.this)
                                .load(mAvatarRef)
                                .signature(new ObjectKey(System.currentTimeMillis()))
                                .into(mImageViewProfile);
                        Toast.makeText(getContext(), "Cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, task.getException().getMessage());
                        Toast.makeText(getContext(), "Có lỗi đã xảy ra, xin vui lòng thử lại", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            try {
                URL imageURL = new URL(imagePath);
                InputStream mInputStream = (InputStream) imageURL.getContent();
                mUserImageRef.putStream(mInputStream).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            StorageReference mAvatarRef = mFirebaseStorage.getReference(mCurrentUser.getUid() + "/avatar");
                            GlideApp.with(AccountFragment.this).load(mAvatarRef).into(mImageViewProfile);
                            Toast.makeText(getContext(), "Cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, task.getException().getMessage());
                            Toast.makeText(getContext(), "Có lỗi đã xảy ra, xin vui lòng thử lại", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d(TAG, e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop called");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy called");
        super.onDestroy();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume called");
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        if (mCurrentUser != null) {
            mLayoutRecommendSignIn.setVisibility(View.GONE);
            updateUIWithUserInfo();
        } else {
            mLayoutRecommendSignIn.setVisibility(View.VISIBLE);
        }
        super.onResume();
    }
}
