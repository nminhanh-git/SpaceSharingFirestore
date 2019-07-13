package com.example.nminhanh.spacesharing.Fragment.MainPages;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.signature.ObjectKey;
import com.example.nminhanh.spacesharing.ChatActivity;
import com.example.nminhanh.spacesharing.GlideApp;
import com.example.nminhanh.spacesharing.Interface.SignOutListener;
import com.example.nminhanh.spacesharing.Model.Conversation;
import com.example.nminhanh.spacesharing.R;
import com.example.nminhanh.spacesharing.WelcomeActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment implements SignOutListener {

    private static final String TAG = "MA:ChatFragment";
    private static final String USER_CHILD = "user_data";
    View view;

    FirebaseStorage mFirebaseStorage;
    FirebaseFirestore mFirestore;
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mCurrentUser;

    RecyclerView mChatRecyclerView;
    FirestoreRecyclerAdapter<Conversation, ConversationViewHolder> mConversationAdapter;

    RelativeLayout mLayoutSignIn;
    Button mBtnRecommendSignIn;

    RelativeLayout mLayoutEmpty;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        view = inflater.inflate(R.layout.fragment_chat, container, false);

        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();

        initializeView();
        if (mCurrentUser != null) {
            mChatRecyclerView.setVisibility(View.VISIBLE);
            setUpRecyclerViewData();
        }

        mBtnRecommendSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = new Intent(getActivity(), WelcomeActivity.class);
                startActivity(signInIntent);
            }
        });
        return view;
    }

    @Override
    public void onStart() {

        super.onStart();

    }

    private void setUpRecyclerViewData() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mChatRecyclerView.setLayoutManager(mLayoutManager);

        final CollectionReference mConversationColRef = mFirestore
                .collection(USER_CHILD)
                .document(mCurrentUser.getUid())
                .collection("conversation");

        Query baseQuery = mConversationColRef.orderBy("timeAdded", Query.Direction.DESCENDING);


        FirestoreRecyclerOptions<Conversation> options = new FirestoreRecyclerOptions.Builder<Conversation>()
                .setQuery(baseQuery, Conversation.class)
                .build();

        mConversationAdapter = new FirestoreRecyclerAdapter<Conversation, ConversationViewHolder>(options) {
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                if(getItemCount() == 0){
                    mLayoutEmpty.setVisibility(View.VISIBLE);
                }else{
                    mLayoutEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onBindViewHolder(@NonNull final ConversationViewHolder holder, int position, @NonNull final Conversation model) {
                String id = model.getId();
                final String[] title = new String[1];
                final String[] phone = new String[1];
                StorageReference mImageRef = mFirebaseStorage.getReference(id).child("avatar");
                GlideApp.with(getContext())
                        .load(mImageRef)
                        .signature(new ObjectKey(model.getId()))
                        .into(holder.mImageView);

                DocumentReference mUserNameDocRef = mFirestore
                        .collection(USER_CHILD)
                        .document(id);
                mUserNameDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        title[0] = documentSnapshot.getString("name");
                        phone[0] = documentSnapshot.getString("phone_number");
                        holder.mTextViewTitle.setText(title[0]);
                    }
                });

                Date date = model.getTimeAdded();
                DateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
                holder.mTextViewDate.setText(dateFormat.format(date));

                holder.setOnItemClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra("conversation id", model.getId());
                        intent.putExtra("conversation name", title[0]);
                        intent.putExtra("conversation phone", phone[0]);
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = getActivity().getLayoutInflater()
                        .inflate(R.layout.conversation_item_layout, viewGroup, false);
                return new ConversationViewHolder(view);
            }
        };
        mChatRecyclerView.setAdapter(mConversationAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        if (mCurrentUser != null) {
            mConversationAdapter.startListening();
            mLayoutSignIn.setVisibility(View.GONE);
        } else {
            mLayoutSignIn.setVisibility(View.VISIBLE);
        }
    }

    private void initializeView() {
        mChatRecyclerView = view.findViewById(R.id.conversation_recycle_view);
        mLayoutSignIn = view.findViewById(R.id.conversation_layout_recommend_sign_in);
        mBtnRecommendSignIn = view.findViewById(R.id.conversation_button_sign_in);

        mLayoutEmpty = view.findViewById(R.id.empty_layout);
    }

    @Override
    public void onStop() {
        if (mCurrentUser != null) {
            mConversationAdapter.stopListening();
        }
        super.onStop();
    }

    @Override
    public void onSignOut() {
        mLayoutSignIn.setVisibility(View.VISIBLE);
        mConversationAdapter.stopListening();
    }

    private class ConversationViewHolder extends RecyclerView.ViewHolder {
        CircleImageView mImageView;
        TextView mTextViewTitle;
        TextView mTextViewDate;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.conversation_image);
            mTextViewTitle = itemView.findViewById(R.id.conversation_title);
            mTextViewDate = itemView.findViewById(R.id.conversation_date);
        }

        public void setOnItemClickListener(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);
        }
    }
}
