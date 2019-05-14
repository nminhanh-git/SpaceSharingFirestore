package com.example.nminhanh.spacesharing;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.signature.ObjectKey;
import com.example.nminhanh.spacesharing.Model.Conversation;
import com.example.nminhanh.spacesharing.Model.Message;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
                if (!mEditMessage.getText().toString().isEmpty()) {


                    final DocumentReference mConversationFriendDocRef = mFirestore
                            .collection("user_data")
                            .document(mConversationId)
                            .collection("conversation")
                            .document(mCurrentUser.getUid());

                    mConversationFriendDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (!task.getResult().exists()) {
                                Conversation currentFriendConversation = new Conversation(mCurrentUser.getUid(), new Date());
                                mConversationFriendDocRef.set(currentFriendConversation);
                            }
                        }
                    });

                    final CollectionReference mMessageFriendColRef = mConversationFriendDocRef
                            .collection("message");


                    final DocumentReference mConversationUserDocRef = mFirestore
                            .collection("user_data")
                            .document(mCurrentUser.getUid())
                            .collection("conversation")
                            .document(mConversationId);

                    mConversationUserDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (!task.getResult().exists()) {
                                Conversation currentUserConversation = new Conversation(mConversationId, new Date());
                                mConversationUserDocRef.set(currentUserConversation);
                            }
                        }
                    });

                    final CollectionReference mMessageUserColRef = mConversationUserDocRef
                            .collection("message");

                    final Message currentMessage = new Message(mCurrentUser.getUid(), "", message, System.currentTimeMillis());
                    mMessageUserColRef.add(currentMessage).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "update user's conversation successfully");
                                Map<String, Object> updates =new HashMap<>();
                                updates.put("timeAdded",FieldValue.serverTimestamp());
                                mConversationUserDocRef.update(updates);
                            } else {
                                Log.d(TAG, "update user's conversation error: " + task.getException().getMessage());
                            }
                        }
                    });

                    mMessageFriendColRef.add(currentMessage).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "update friend's conversation successfully");
                                Map<String, Object> updates =new HashMap<>();
                                updates.put("timeAdded",FieldValue.serverTimestamp());
                                mConversationFriendDocRef.update(updates);
                            } else {
                                Log.d(TAG, "update friend's conversation error: " + task.getException().getMessage());
                            }
                        }
                    });

                    mRecyclerViewBubbleChat.scrollToPosition(mMessageAdapter.getItemCount() - 1);
                    mEditMessage.setText("");
                } else {
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
                    } else if (today.isEqual(currentDate.toLocalDate().plusDays(1))) {
                        formatter = DateTimeFormatter.ofPattern("HH:mm");
                        ((MessageFriendViewHolder) holder).mTextViewDate.setText("Hôm qua lúc " + currentDate.format(formatter));
                    } else {
                        formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
                        ((MessageFriendViewHolder) holder).mTextViewDate.setText(currentDate.format(formatter));
                    }


                    StorageReference mImageAvaRef = mFirebaseStorage.getReference()
                            .child(mConversationId)
                            .child("avatar");

                    GlideApp.with(ChatActivity.this)
                            .load(mImageAvaRef)
                            .into(((MessageFriendViewHolder) holder).mImageViewAva);
                } else {
                    ((MessageUserViewHolder) holder).mTextViewMessage.setText(model.getMessage());

                    LocalDate today = LocalDate.now();
                    LocalDateTime currentDate = Instant.ofEpochMilli(model.getTimeAdded())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    DateTimeFormatter formatter;
                    if (today.isEqual(currentDate.toLocalDate())) {
                        formatter = DateTimeFormatter.ofPattern("HH:mm");
                        ((MessageUserViewHolder) holder).mTextViewDate.setText("Hôm nay lúc " + currentDate.format(formatter));
                    } else if (today.isEqual(currentDate.toLocalDate().plusDays(1))) {
                        formatter = DateTimeFormatter.ofPattern("HH:mm");
                        ((MessageUserViewHolder) holder).mTextViewDate.setText("Hôm qua lúc " + currentDate.format(formatter));
                    } else {
                        formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
                        ((MessageUserViewHolder) holder).mTextViewDate.setText(currentDate.format(formatter));
                    }

                    StorageReference mImageAvaRef = mFirebaseStorage.getReference()
                            .child(mCurrentUser.getUid())
                            .child("avatar");

                    GlideApp.with(ChatActivity.this)
                            .load(mImageAvaRef)
                            .signature(new ObjectKey(System.currentTimeMillis()))
                            .into(((MessageUserViewHolder) holder).mImageViewAva);
                }
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view;
                if (i == VIEW_TYPE_FRIEND) {
                    view = getLayoutInflater().inflate(R.layout.message_friend_item_layout, viewGroup, false);
                    return new MessageFriendViewHolder(view);
                } else if (i == VIEW_TYPE_USER) {
                    view = getLayoutInflater().inflate(R.layout.message_user_item_layout, viewGroup, false);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.chat_option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_delete:
                showDeleteConversationDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConversationDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        final View mDialogView = inflater.inflate(R.layout.detail_delete_dialog_layout, null);
        TextView mTextView = mDialogView.findViewById(R.id.dialog_delete_subtitle);
        TextView mBtnCancel = mDialogView.findViewById(R.id.dialog_delete_no);
        TextView mBtnOk = mDialogView.findViewById(R.id.dialog_delete_yes);

        mTextView.setText("Bạn có thực sự muốn xóa cuộc trò chuyện này không? Bạn sẽ không thể khôi phục lại sau khi xóa. Cuộc trò chuyện này sẽ chỉ bị xóa trên thiết bị của bạn.");

        final AlertDialog deleteDialog = builder.setView(mDialogView).create();
        ColorDrawable dialogBackground = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(dialogBackground, 40, 50, 40, 50);
        deleteDialog.getWindow().setBackgroundDrawable(inset);

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog.dismiss();
            }
        });
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference conversationDocRef = mFirestore
                        .collection("user_data")
                        .document(mCurrentUser.getUid())
                        .collection("conversation")
                        .document(mConversationId);
                conversationDocRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "delete conversation successfully");
                        } else {
                            Log.d(TAG, task.getException().getMessage());
                        }
                    }
                });
                final CollectionReference messageColRef = conversationDocRef.collection("message");
                messageColRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            final List<DocumentSnapshot> messageList = task.getResult().getDocuments();
                            int count = 0;
                            for (DocumentSnapshot d : messageList) {
                                messageColRef.document(d.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "Delete Message successfully");
                                        }
                                    }
                                });
                            }
                            deleteDialog.dismiss();
                            finish();
                        }
                    }
                });
            }
        });
        deleteDialog.show();
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
