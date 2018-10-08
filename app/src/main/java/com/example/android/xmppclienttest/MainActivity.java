package com.example.android.xmppclienttest;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.xmppclienttest.database.AppDatabase;
import com.example.android.xmppclienttest.database.MessageEntry;
import com.example.android.xmppclienttest.sync.ConnectionService;
import com.example.android.xmppclienttest.sync.EventSyncUtils;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppDatabase mDb;

    private CustomItemAdapter mAdapter;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.rv_numbers);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new CustomItemAdapter(this);
        recyclerView.setAdapter(mAdapter);

        mDb = AppDatabase.getInstance(getApplicationContext());
        setupViewModel();

//        final BroadcastClientTest clientTest = new BroadcastClientTest(mDb);
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                clientTest.broadCast();
//            }
//        });
//        thread.start();

        Intent backgroundService = new Intent(this, ConnectionService.class);
        startService(backgroundService);
//
//        Intent eventNotificationIntent = new Intent(this, NewEventIntentService.class);
//        eventNotificationIntent.setAction(Tasks.ACTION_NEW_EVENT);
//        startService(eventNotificationIntent);

//        EventSyncUtils.initialize(getApplicationContext(), ConnectionService.mConnection);
    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getMessages().observe(this, new Observer<List<MessageEntry>>() {
            @Override
            public void onChanged(@Nullable List<MessageEntry> messageEntries) {
                mAdapter.setMessages(messageEntries);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mBroadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                String action = intent.getAction();
//                switch (action) {
//                    case ConnectionService.NEW_EVENT:
//                        String from = intent.getStringExtra(ConnectionService.BUNDLE_FROM_JID);
//                        String body = intent.getStringExtra(ConnectionService.BUNDLE_MESSAGE_BODY);
//
//                        return;
//                }
//
//            }
//        };

//        IntentFilter filter = new IntentFilter(ConnectionService.NEW_EVENT);
//        registerReceiver(mBroadcastReceiver,filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        unregisterReceiver(mBroadcastReceiver);
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

        AppExecutors.getsInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.messageDao().insertSingleMessage(message);
            }
        });
    }

    private void deleteMessages() {
        AppExecutors.getsInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.messageDao().deleteAllMessages();
            }
        });
    }
}
