package com.example.home.mytalk.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.home.mytalk.Activity.WebActivity;
import com.example.home.mytalk.Model.CardViewItem;
import com.example.home.mytalk.R;

import java.util.ArrayList;

/**
 * Created by xnote on 2018-05-15.
 */

public class InfiniteScrollAdapter extends RecyclerView.Adapter<InfiniteScrollAdapter.InfiniteScrollHolder> {

    private Context context;
    private ArrayList<CardViewItem> cardViewItems;

    public InfiniteScrollAdapter(Context context, ArrayList<CardViewItem> cardViewItems){
        this.context = context;
        this.cardViewItems = cardViewItems;
    }


    @Override
    public InfiniteScrollHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview,parent,false);
        InfiniteScrollHolder holder = new InfiniteScrollHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final InfiniteScrollHolder holder, int position) {
        holder.nameText.setText(cardViewItems.get(position).toString());
        holder.typeText.setText(cardViewItems.get(position).getType());
        holder.dirText.setText(cardViewItems.get(position).getDirector());
        Glide.with(context).load(cardViewItems.get(position).getImage()).override(300,400).into(holder.imageView);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String link = "https://movie.naver.com" + cardViewItems.get(holder.getAdapterPosition()).getLink();
                Intent intent = new Intent(context, WebActivity.class);
                intent.putExtra("url",link);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return cardViewItems.size();
    }

    public void add(CardViewItem cardViewItem){
        cardViewItems.add(cardViewItem);
        notifyDataSetChanged();
    }

    public void clear(){
        cardViewItems.clear();
        notifyDataSetChanged();
    }

    class InfiniteScrollHolder extends RecyclerView.ViewHolder{
        private TextView nameText;
        private ImageView imageView;
        private TextView typeText;
        private TextView dirText;
        private View mView;

        public InfiniteScrollHolder(View itemView) {
            super(itemView);
            this.nameText = (TextView)itemView.findViewById(R.id.nameTxt);
            this.imageView = (ImageView)itemView.findViewById(R.id.image);
            this.typeText = (TextView)itemView.findViewById(R.id.typeTxt);
            this.dirText = (TextView)itemView.findViewById(R.id.dirTxt);
            mView = itemView;
        }
    }
}
