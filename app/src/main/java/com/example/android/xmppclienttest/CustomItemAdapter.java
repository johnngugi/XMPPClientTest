package com.example.android.xmppclienttest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.xmppclienttest.database.MessageEntry;

import java.util.List;

public class CustomItemAdapter extends RecyclerView.Adapter<CustomItemAdapter.ItemViewHolder> {

    private List<MessageEntry> mMessages;

    private Context mContext;

    public CustomItemAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(
            @NonNull ViewGroup viewGroup, int i) {
        int layoutIdForListItem = R.layout.list_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View view = inflater.inflate(
                layoutIdForListItem, viewGroup, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ItemViewHolder numberViewHolder, int position) {
        MessageEntry messageEntry = mMessages.get(position);
        String title = messageEntry.getTitle();
        String description = messageEntry.getDescription();

        numberViewHolder.mItemTitle.setText(title);
        numberViewHolder.mItemDescription.setText(description);
    }

    @Override
    public int getItemCount() {
        if (mMessages == null) {
            return 0;
        }
        return mMessages.size();
    }

    public void setmMessages(List<MessageEntry> messages) {
        mMessages = messages;
        notifyDataSetChanged();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView mItemTitle, mItemDescription;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            mItemTitle = itemView.findViewById(R.id.tv_item_title);
            mItemDescription = itemView.findViewById(R.id.tv_item_description);
        }
    }
}
