package com.example.nminhanh.spacesharing;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.signature.ObjectKey;
import com.example.nminhanh.spacesharing.Model.Message;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private static final int VIEW_TYPE_FRIEND = 0;
    private static final int VIEW_TYPE_USER = 1;
    private static final String TAG = "MA:ChatActivity";
    Toolbar mToolbar;
    ImageButton mBtnBack;
    CircleImageView mImageAvaReceiver;
    TextView mTextViewNameReceiver;
    ImageButton mBtnCall;
    EditText mEditMessage;
    ImageButton mBtnSend;

    RecyclerView mRecyclerViewBubbleChat;
    FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder> mMessageAdapter;

    FirebaseStorage mFirebaseStorage;
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mCurrentUser;
    FirebaseFirestore mFirestore;
    String mConversationId;
    String mConversationName;
    String mConversationPhone;
    String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        mFirestore = FirebaseFirestore.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        Intent parentIntent = getIntent();
        mConversationId = parentIntent.getStringExtra("conversation id");
        mConversationName = parentIntent.getStringExtra("conversation name");
        mConversationPhone = parentIntent.getStringExtra("conversation phone");
        initializeView();
        setupInfo();
        setupChatRecyclerView();

        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mBtnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toCall = "tel:" + mConversationPhone;
                Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(toCall));
                startActivity(callIntent);
            }
        });

        mEditMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    message = s.toString();
                } else {
                    message = "";
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mEditMessage.getText().toString().isEmpty()) {
                    final CollectionReference mMessageFriendRef = mFirestore
                            .collection("user_data")
                            .document(mConversationId)
                            .collection("conversation")
                            .document(mCurrentUser.getUid())
                            .collection("message");

                    final CollectionReference mMessageUserRef = mFirestore
                            .collection("user_data")
                            .document(mCurrentUser.getUid())
                            .collection("conversation")
                            .document(mConversationId)
                            .collection("message");

                    final Message currentMessage = new Message(mCurrentUser.getUid(), "", message, System.currentTimeMillis());
                    mMessageUserRef.add(currentMessage).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "update user's conversation successfully");
                            } else {
                                Log.d(TAG, "update user's conversation error: " + task.getException().getMessage());
                            }
                        }
                    });

                    mMessageFriendRef.add(currentMessage).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "update friend's conversation successfully");
                            } else {
                                Log.d(TAG, "update friend's conversation error: " + task.getException().getMessage());
                            }
                        }
                    });

                    mRecyclerViewBubbleChat.scrollToPosition(mMessageAdapter.getItemCount() - 1);
                    mEditMessage.setText("");
                }else{
                    Toast.makeText(ChatActivity.this, "Bạn không thể gửi một tin nhắn rỗng!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mMessageAdapter.startListening();
    }

    private void initializeView() {
        mToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mBtnBack = mToolbar.findViewById(R.id.chat_btn_back);
        mImageAvaReceiver = mToolbar.findViewById(R.id.chat_header_image_receiver);
        mTextViewNameReceiver = mToolbar.findViewById(R.id.chat_text_view_receiver_name);
        mBtnCall = mToolbar.findViewById(R.id.chat_btn_call);

        mEditMessage = findViewById(R.id.chat_edit_text_message);
        mBtnSend = findViewById(R.id.chat_btn_send);
        mBtnSend.setVisibility(View.GONE);

        mRecyclerViewBubbleChat = findViewById(R.id.chat_message_recycle_view);
    }


    private void setupInfo() {
        StorageReference mFriendImageRef = mFirebaseStorage
                .getReference()
                .child(mConversationId)
                .child("avatar");
        GlideApp.with(ChatActivity.this)
                .load(mFriendImageRef)
                .into(mImageAvaReceiver);

        mTextViewNameReceiver.setText(mConversationName);
    }

    private void setupChatRecyclerView() {
        final RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerViewBubbleChat.setLayoutManager(mLayoutManager);

        CollectionReference mMessagesColRef = mFirestore
                .collection("user_data")
                .document(mCurrentUser.getUid())
                .collection("conversation")
                .document(mConversationId)
                .collection("message");

        Query baseQuery = mMessagesColRef.orderBy("timeAdded", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Message> options =
                new FirestoreRecyclerOptions.Builder<Message>()
                        .setQuery(baseQuery, Message.class)
                        .build();

        mMessageAdapter = new FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull Message model) {
                if (holder instanceof MessageFriendViewHolder) {
                    ((MessageFriendViewHolder) holder).mTextViewMessage.setText(model.getMessage());

                    LocalDate today = LocalDate.now();
                    LocalDateTime currentDate = Instant.ofEpochMilli(model.getTimeAdded())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    DateTimeFormatter formatter;
                    if (today.isEqual(currentDate.toLocalDate())) {
                        formatter = DateTimeFormatter.ofPattern("HH:mm");
                        ((MessageFriendViewHolder) holder).mTextViewDate.setText("Hôm nay lúc " + currentDate.format(formatter));
                    } else if (today.isEqual(currentDate.toLocalDate().minusDays(1))) {
                        formatter = DateTimeFormatter.ofPattern("HH:mm");
                        ((MessageFriendViewHolder) holder).mTextViewDate.setText("Hôm qua lúc " + currentDate.format(formatter));
                    } else {
                        formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
                        ((MessageFriendViewHolder) holder).mTextViewDate.setText("Hôm qua lúc " + currentDate.format(formatter));
                    }


                    StorageReference mImageAvaRef = mFirebaseStorage.getReference()
                            .child(mConversationId)
                            .child("avatar");

                    GlideApp.with(ChatActivity.this)
                            .load(mImageAvaRef)
                            .into(((MessageFriendViewHolder) holder).mImageViewAva);
                } else {
                    ((MessageUserViewHolder) holder).mTextViewMessage.setText(model.getMessage());

                    LocalDateTime date = Instant.ofEpochMilli(model.getTimeAdded())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                    ((MessageUserViewHolder) holder).mTextViewDate.setText(date.format(formatter));

                    StorageReference mImageAvaRef = mFirebaseStorage.getReference()
                            .child(mCurrentUser.getUid())
                            .child("avatar");

                    GlideApp.with(ChatActivity.this)
                            .load(mImageAvaRef)
                            .signature(new ObjectKey(mCurrentUser.getUid()))
                            .into(((MessageUserViewHolder) holder).mImageViewAva);
                }
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view;
                if (i == VIEW_TYPE_FRIEND) {
                    view = getLayoutInflater().inflate(R.layout.friend_message_item_layout, viewGroup, false);
                    return new MessageFriendViewHolder(view);
                } else if (i == VIEW_TYPE_USER) {
                    view = getLayoutInflater().inflate(R.layout.user_message_item_layout, viewGroup, false);
                    return new MessageUserViewHolder(view);
                }
                return null;
            }

            @Override
            public int getItemViewType(int position) {
                Message currentMessage = getSnapshots().get(position);
                if (currentMessage.getSenderId().equalsIgnoreCase(mCurrentUser.getUid())) {
                    return VIEW_TYPE_USER;
                } else {
                    return VIEW_TYPE_FRIEND;
                }
            }
        };
        mRecyclerViewBubbleChat.setAdapter(mMessageAdapter);
        mMessageAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int messageCount = mMessageAdapter.getItemCount();
                int lastVisiblePosition = ((LinearLayoutManager) mLayoutManager).findLastCompletelyVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= messageCount - 1
                                && lastVisiblePosition == positionStart - 1)) {
                    mRecyclerViewBubbleChat.scrollToPosition(positionStart);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMessageAdapter.stopListening();
    }

    private class MessageFriendViewHolder extends RecyclerView.ViewHolder {
        CircleImageView mImageViewAva;
        TextView mTextViewMessage;
        TextView mTextViewDate;

        public MessageFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageViewAva = itemView.findViewById(R.id.bubble_chat_image_ava);
            mTextViewMessage = itemView.findViewById(R.id.bubble_chat_text_message);
            mTextViewDate = itemView.findViewById(R.id.bubble_chat_text_date);
        }
    }

    private class MessageUserViewHolder extends RecyclerView.ViewHolder {
        CircleImageView mImageViewAva;
        TextView mTextViewMessage;
        TextView mTextViewDate;

        public MessageUserViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageViewAva = itemView.findViewById(R.id.bubble_chat_image_ava);
            mTextViewMessage = itemView.findViewById(R.id.bubble_chat_text_message);
            mTextViewDate = itemView.findViewById(R.id.bubble_chat_text_date);
        }
    }
}
