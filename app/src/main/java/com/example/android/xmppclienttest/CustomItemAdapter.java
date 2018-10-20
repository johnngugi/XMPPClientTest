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

    final private ItemClickListener mItemClickListener;

    private List<MessageEntry> mMessages;

    private Context mContext;

    public CustomItemAdapter(Context context, ItemClickListener listener) {
        mContext = context;
        mItemClickListener = listener;
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
        String subject = messageEntry.getSubject();
        String body = messageEntry.getBody();

        numberViewHolder.mItemTitle.setText(subject);
        numberViewHolder.mItemDescription.setText(body);
    }

    @Override
    public int getItemCount() {
        if (mMessages == null) {
            return 0;
        }
        return mMessages.size();
    }

    public void setMessages(List<MessageEntry> messages) {
        mMessages = messages;
        notifyDataSetChanged();
    }

    public interface ItemClickListener {
        void onItemClickListener(int itemId);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mItemTitle, mItemDescription;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            mItemTitle = itemView.findViewById(R.id.tv_item_title);
            mItemDescription = itemView.findViewById(R.id.tv_item_description);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int elementId = mMessages.get(getAdapterPosition()).getId();
            mItemClickListener.onItemClickListener(elementId);
        }
    }
}
