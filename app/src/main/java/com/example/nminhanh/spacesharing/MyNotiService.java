package com.example.nminhanh.spacesharing;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.nminhanh.spacesharing.Model.Conversation;
import com.example.nminhanh.spacesharing.Model.Message;
import com.example.nminhanh.spacesharing.Model.Space;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class MyNotiService extends Service {
    public static final String TAG = "MA:MyNotiService";
    public static final int NOTIFICATION_SPACE_ID = 2312;
    public static final int NOTIFICATION_CHAT_ID = 1904;
    private static final int REQUEST_SPACE_DETAIL_FROM_NOTI = 0;

    FirebaseAuth mFirebaseAuth;
    FirebaseUser mCurrentUser;
    FirebaseFirestore mFirestore;
    FirebaseStorage mStorage;


    public MyNotiService() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        mFirestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mCurrentUser = mFirebaseAuth.getCurrentUser();
        if (mCurrentUser != null) {
            listenForSpacePublishApproved();
            listenForNewMessage();
        }
        return START_NOT_STICKY;
    }

    private void listenForNewMessage() {
        final CollectionReference mUserConversationRef =
                mFirestore.collection("user_data")
                        .document(mCurrentUser.getUid())
                        .collection("conversation");

        mUserConversationRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d(TAG, e.getMessage());
                }
                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    switch (dc.getType()) {
                        case MODIFIED:
                            final DocumentReference mMessageRef =
                                    dc.getDocument()
                                            .getReference()
                                            .collection("message")
                                            .document(dc.getDocument().getString("newMessageId"));
                            final Conversation mCurrentConversation = dc.getDocument().toObject(Conversation.class);
                            mMessageRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        Message newMessage = task.getResult().toObject(Message.class);
                                        if (!newMessage.getSenderId().equalsIgnoreCase(mCurrentUser.getUid())) {
                                            if (!newMessage.isRead()) {
                                                sendMessageNoti(mCurrentConversation, newMessage);
                                            }
                                        }
                                    }
                                }
                            });
                            break;
                    }
                }
            }
        });
    }

    private void listenForSpacePublishApproved() {
        mFirestore.collection("space")
                .whereEqualTo("idChu", mCurrentUser.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, e.getMessage());
                        } else {
                            boolean isNotified = false;
                            for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                                switch (dc.getType()) {
                                    case MODIFIED:
                                        Space modifiedSpace = dc.getDocument().toObject(Space.class);
                                        if (modifiedSpace.getTrangThai().equalsIgnoreCase("enabled")) {
                                            sendSpaceNotification(modifiedSpace);
                                        }
                                        isNotified = true;
                                        break;
                                }
                                if (isNotified) {
                                    break;
                                }
                            }
                        }
                    }
                });
    }

    private void sendMessageNoti(final Conversation currentConversation, final Message newMessage) {

        mFirestore.collection("user_data")
                .document(currentConversation.getId())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                final String conversationName = documentSnapshot.getString("name");
                final String phone = documentSnapshot.getString("phone_number");

                StorageReference mImageAvaRef = mStorage
                        .getReference()
                        .child(newMessage.getSenderId())
                        .child("avatar");
                GlideApp.with(MyNotiService.this)
                        .asBitmap()
                        .circleCrop()
                        .load(mImageAvaRef)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @android.support.annotation.Nullable Transition<? super Bitmap> transition) {
                                Intent mConversationIntent = new Intent(MyNotiService.this, ChatActivity.class);
                                mConversationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                mConversationIntent.putExtra("conversation id",currentConversation.getId());
                                mConversationIntent.putExtra("conversation name", conversationName);
                                mConversationIntent.putExtra("conversation phone", phone);
                                PendingIntent mPendingIntent = PendingIntent.getActivity(MyNotiService.this,
                                        REQUEST_SPACE_DETAIL_FROM_NOTI, mConversationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                                Notification mSpaceNoti = new NotificationCompat.Builder(MyNotiService.this, MainActivity.NOTI_CHANNEL_ID)
                                        .setSmallIcon(R.drawable.ic_message)
                                        .setColor(getColor(R.color.colorPrimary))
                                        .setShowWhen(true)
                                        .setContentTitle(conversationName)
                                        .setContentText(newMessage.getMessage())
                                        .setLargeIcon(resource)
                                .setContentIntent(mPendingIntent)
                                        .setAutoCancel(true)
                                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                        .build();
                                NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(MyNotiService.this);
                                mNotificationManager.notify(NOTIFICATION_CHAT_ID, mSpaceNoti);
                            }

                            @Override
                            public void onLoadCleared(@android.support.annotation.Nullable Drawable placeholder) {

                            }
                        });

            }
        });


    }

    private void sendSpaceNotification(final Space modifiedSpace) {
        StorageReference mImageSpaceRef = mStorage
                .getReference()
                .child(mCurrentUser.getUid())
                .child(modifiedSpace.getId())
                .child(1 + "");
        final Bitmap[] mBigPicture = new Bitmap[1];
        GlideApp.with(this)
                .asBitmap()
                .load(mImageSpaceRef)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @android.support.annotation.Nullable Transition<? super Bitmap> transition) {
                        mBigPicture[0] = resource;
                        // Notification Action
                        Intent mDetailSpaceIntent = new Intent(MyNotiService.this, SpaceDetailActivity.class);
                        mDetailSpaceIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        mDetailSpaceIntent.putExtra("current space", modifiedSpace);
                        mDetailSpaceIntent.putExtra("from", MyNotiService.class.getSimpleName());
                        PendingIntent mPendingIntent = PendingIntent.getActivity(MyNotiService.this,
                                REQUEST_SPACE_DETAIL_FROM_NOTI, mDetailSpaceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        Notification mSpaceNoti = new NotificationCompat.Builder(MyNotiService.this, MainActivity.NOTI_CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_house2)
                                .setColor(getColor(android.R.color.holo_orange_dark))
                                .setShowWhen(true)
                                .setContentText("Tin đăng của bạn đã được xét duyệt!")
                                .setLargeIcon(mBigPicture[0])
                                .setStyle(new NotificationCompat.BigPictureStyle()
                                        .bigPicture(mBigPicture[0])
                                        .bigLargeIcon(null))
                                .setContentIntent(mPendingIntent)
                                .setAutoCancel(true)
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .setDefaults(NotificationCompat.DEFAULT_ALL)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .build();
                        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(MyNotiService.this);
                        mNotificationManager.notify(NOTIFICATION_SPACE_ID, mSpaceNoti);
                    }

                    @Override
                    public void onLoadCleared(@android.support.annotation.Nullable Drawable placeholder) {

                    }
                });


    }
}
