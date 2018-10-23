package com.example.android.xmppclienttest;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.xmppclienttest.database.AppDatabase;
import com.example.android.xmppclienttest.database.MessageEntry;
import com.example.android.xmppclienttest.sync.ConnectionService;

import java.util.List;

public class MainActivity extends AppCompatActivity implements CustomItemAdapter.ItemClickListener {

    private AppDatabase mDb;
    private CustomItemAdapter mAdapter;
    private TextView mEmptyStateTextView;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        mRecyclerView = findViewById(R.id.rv_numbers);
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new CustomItemAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);

        mEmptyStateTextView = findViewById(R.id.empty_view);

        mDb = AppDatabase.getInstance(getApplicationContext());
        setupViewModel();

        boolean isConnected = isConnectedToWifi();

        if (isConnected) {
            Intent backgroundService = new Intent(this, ConnectionService.class);
            startService(backgroundService);
        } else {
//            // Otherwise, display error
//            // First, hide loading indicator so error message will be visible
//            View loadingIndicator = findViewById(R.id.loading_indicator);
//            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            mRecyclerView.setVisibility(View.GONE);
            mEmptyStateTextView.setText(R.string.no_wifi_connection);
        }

//        MessageUtilities.scheduleRetrieveNewMessages(this);
    }

    private boolean isConnectedToWifi() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            NetworkCapabilities networkCapabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            isConnected =
                    isConnected &&
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        } else {
            isConnected = isConnected && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        }
        return isConnected;
    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getMessages().observe(this, new Observer<List<MessageEntry>>() {
            @Override
            public void onChanged(@Nullable List<MessageEntry> messageEntries) {
                mAdapter.setMessages(messageEntries);
                if (mAdapter.getItemCount() == 0) {
                    mRecyclerView.setVisibility(View.GONE);
                    mEmptyStateTextView.setVisibility(View.VISIBLE);
                } else {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mEmptyStateTextView.setText(R.string.No_messages);
                    mEmptyStateTextView.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_main.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertMessage();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteMessages();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertMessage() {
        final String title = "Test";
        final String description = "Lorem ipsum dolor sit amet";
        final MessageEntry message = new MessageEntry(title, description);

        AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.messageDao().insertSingleMessage(message);
            }
        });
    }

    private void deleteMessages() {
        AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.messageDao().deleteAllMessages();
            }
        });
    }

    @Override
    public void onItemClickListener(int itemId) {
        // Launch MessageDetailActivity adding the itemId as an extra in the intent
        Intent intent = new Intent(MainActivity.this, MessageDetailActivity.class);
        intent.putExtra(MessageDetailActivity.EXTRA_MESSAGE_ID, itemId);
        startActivity(intent);
    }
}
