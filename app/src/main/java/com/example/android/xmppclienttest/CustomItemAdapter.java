package com.example.android.xmppclienttest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CustomItemAdapter extends RecyclerView.Adapter<CustomItemAdapter.NumberViewHolder> {

    private String[] mDataset;

    public CustomItemAdapter(String[] dataset) {
        mDataset = dataset;
    }

    @NonNull
    @Override
    public CustomItemAdapter.NumberViewHolder onCreateViewHolder(
            @NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.list_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(
                layoutIdForListItem, viewGroup, false);
        return new NumberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CustomItemAdapter.NumberViewHolder numberViewHolder, int position) {
        numberViewHolder.mItemTitle.setText("Title");
        numberViewHolder.mItemDescription.setText(mDataset[position]);
    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }

    public class NumberViewHolder extends RecyclerView.ViewHolder {
        TextView mItemTitle, mItemDescription;

        public NumberViewHolder(@NonNull View itemView) {
            super(itemView);

            mItemTitle = itemView.findViewById(R.id.tv_item_title);
            mItemDescription = itemView.findViewById(R.id.tv_item_description);
        }
    }
}
