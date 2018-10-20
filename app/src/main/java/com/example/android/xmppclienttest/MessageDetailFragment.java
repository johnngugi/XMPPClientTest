package com.example.android.xmppclienttest;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.xmppclienttest.database.AppDatabase;
import com.example.android.xmppclienttest.database.MessageEntry;

public class MessageDetailFragment extends Fragment {

    public static final String TAG = MessageDetailFragment.class.getSimpleName();
    public static final String INSTANCE_MESSAGE_ID = "instanceMessageId";
    private static final int DEFAULT_MESSAGE_ID = -1;

    private TextView mMessageBody;
    private int mMessageId;
    private View mRootView;
    private CollapsingToolbarLayout appBarLayout;

    public static MessageDetailFragment newInstance() {
        return new MessageDetailFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(MessageDetailActivity.EXTRA_MESSAGE_ID)) {
            mMessageId = getArguments().getInt(MessageDetailActivity.EXTRA_MESSAGE_ID);
        }

        Activity activity = this.getActivity();
        appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.collapsingToolbar);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.message_detail, container, false);
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        initViews();
        AppDatabase db = AppDatabase.getInstance(ApplicationContextProvider.getContext());
        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_MESSAGE_ID)) {
            mMessageId = savedInstanceState.getInt(INSTANCE_MESSAGE_ID, DEFAULT_MESSAGE_ID);
        }

        MessageDetailViewModelFactory factory = new MessageDetailViewModelFactory(db, mMessageId);
        final MessageDetailViewModel viewModel
                = ViewModelProviders.of(this, factory).get(MessageDetailViewModel.class);

        viewModel.getMessageEntry().observe(this, new Observer<MessageEntry>() {
            @Override
            public void onChanged(@Nullable MessageEntry messageEntry) {
                viewModel.getMessageEntry().removeObserver(this);
                assert messageEntry != null;
                if (appBarLayout != null) {
                    appBarLayout.setTitle(messageEntry.getSubject());
                }
                populateUI(messageEntry);
            }
        });

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(INSTANCE_MESSAGE_ID, mMessageId);
        super.onSaveInstanceState(outState);
    }

    private void populateUI(MessageEntry messageEntry) {
        mMessageBody.setText(messageEntry.getBody());
    }

    private void initViews() {
        mMessageBody = mRootView.findViewById(R.id.messageBody_tv);
    }
}
