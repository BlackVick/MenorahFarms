package com.blackviking.menorahfarms.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.Models.DueSponsorshipModel;
import com.blackviking.menorahfarms.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DueSponsorshipAdapter extends RecyclerView.Adapter<DueSponsorshipAdapter.TheViewHolder> {

    List<DueSponsorshipModel> dueSponsorshipList;
    Context context;
    Activity activity;

    public DueSponsorshipAdapter(List<DueSponsorshipModel> dueSponsorshipList, Context context, Activity activity) {
        this.dueSponsorshipList = dueSponsorshipList;
        this.context = context;
        this.activity = activity;
    }

    public void addAll(List<DueSponsorshipModel> newDueSponsorships){

        int initSize = dueSponsorshipList.size();
        dueSponsorshipList.addAll(newDueSponsorships);
        notifyItemRangeChanged(initSize, newDueSponsorships.size());

    }

    public String getLatItemKey(){

        return dueSponsorshipList.get(dueSponsorshipList.size() - 1).getSponsorshipId();

    }

    @NonNull
    @Override
    public TheViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(context).inflate(R.layout.admin_item, parent, false);
        return new TheViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TheViewHolder holder, int position) {

        holder.itemIdentification.setText(dueSponsorshipList.get(position).getSponsorshipId());

    }

    @Override
    public int getItemCount() {
        return dueSponsorshipList.size();
    }

    public class TheViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ItemClickListener itemClickListener;
        TextView itemIdentification;

        public TheViewHolder(@NonNull View itemView) {
            super(itemView);

            itemIdentification = itemView.findViewById(R.id.itemIdentification);

            itemView.setOnClickListener(this);
        }

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onClick(view, getAdapterPosition(), false);
        }
    }
}
