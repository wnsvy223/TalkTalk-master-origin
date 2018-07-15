package com.example.home.mytalk.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.home.mytalk.Activity.ChatActivity;
import com.example.home.mytalk.R;

import java.util.List;



public class OpenChatAdapter extends RecyclerView.Adapter<OpenChatAdapter.ViewHolder> {

    private List<String > mChatRoom;
    public Context context;



    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView roomname;
        public RelativeLayout room;


        private ViewHolder(View item) {
            super(item);
            roomname = (TextView)item.findViewById(R.id.roomName);
            room = (RelativeLayout)item.findViewById(R.id.room);
        }
    }


    public OpenChatAdapter(List<String > mChatRoom, Context context) {
        this.mChatRoom = mChatRoom;
        this.context = context;

    }


    @Override
    public OpenChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_chat_room, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder,  int position) {
        if(holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
            holder.roomname.setText(mChatRoom.get(position));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String roomnum = mChatRoom.get(holder.getAdapterPosition());
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("OpenChat", roomnum);
                    context.startActivity(intent);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mChatRoom.size();
    }


}


