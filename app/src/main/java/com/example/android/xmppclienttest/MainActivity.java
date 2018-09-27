package com.example.android.xmppclienttest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.xmppclienttest.database.AppDatabase;
import com.example.android.xmppclienttest.database.MessageEntry;

public class MainActivity extends AppCompatActivity {

    private AppDatabase mDb;

    private CustomItemAdapter mAdapter;

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.setmMessages(mDb.messageDao().loadAllMessages());
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
                deleteAllMessages();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertMessage() {
        String title = "Test";
        String description = "Lorem ipsum dolor sit amet";

        MessageEntry message = new MessageEntry(title, description);
        mDb.messageDao().insertSingleMessage(message);
    }

    private void deleteAllMessages() {

    }
}
