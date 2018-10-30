package com.example.home.mytalk.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.example.home.mytalk.Activity.FullScreenActivity;
import com.example.home.mytalk.Activity.VideoViewActivity;
import com.example.home.mytalk.Model.Chat;
import com.example.home.mytalk.R;
import com.example.home.mytalk.Utils.CircleTransform;

import java.util.List;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<Chat> mChat;
    private String stEmail;
    private String searchText;
    public Context context;
    private String roomType;
    private final static int RIGHT_MESSAGE = 0;
    private final static int LEFT_MESSAGE = 1;
    private final static int RIGHT_MESSAGE_IMG = 2;
    private final static int LEFT_MESSAGE_IMG = 3;
    private final static int RIGHT_MESSAGE_VOD = 5;
    private final static int LEFT_MESSAGE_VOD = 6;
    private final static int MID_MESSAGE = 4;


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextView;
        public ImageView ivUser;
        public TextView tvName;
        public TextView tvTime;
        public ImageView mImageVIew;
        public VideoView mVideoView;
        public ImageView iconImage;
        public TextView tvUnRead;

        public ViewHolder(View item) {
            super(item);
            mTextView = (TextView)item.findViewById(R.id.my_chat_view);
            ivUser = (ImageView)item.findViewById(R.id.ivUser);
            tvName = (TextView)item.findViewById(R.id.tvEmail);
            tvTime = (TextView)item.findViewById(R.id.tvTime);
            mImageVIew = (ImageView)item.findViewById(R.id.my_chat_view_img);
            mVideoView = (VideoView)item.findViewById(R.id.video);
            iconImage = (ImageView)item.findViewById(R.id.ic_video);
            tvUnRead = (TextView)item.findViewById(R.id.tvUnRead);
        }
    }


    public ChatAdapter(List<Chat> mChat, String email, Context context, String searchText,String roomType ) {
        this.mChat = mChat;
        this.stEmail = email;
        this.context = context;
        this.searchText = searchText;
        this.roomType = roomType;
    }


    @Override
    public int getItemViewType(int position) {
        if(mChat.get(position).getEmail().equals(stEmail)) { //내 이메일이면 오른쪽 메시지뷰
            if(mChat.get(position).getType().equals("image")){ //이미지 메시지
                return RIGHT_MESSAGE_IMG;
            }else if(mChat.get(position).getType().equals("video")){ //비디오 메시지
                return RIGHT_MESSAGE_VOD;
            } else{
                return RIGHT_MESSAGE;
            }
        }else if(mChat.get(position).getType().equals("System")){  //초대 메시지 같은 시스템메시지는 가운데 메시지뷰
            return MID_MESSAGE;
        }else{
            if(mChat.get(position).getType().equals("image")){
                return LEFT_MESSAGE_IMG;
            }else if(mChat.get(position).getType().equals("video")){
                return LEFT_MESSAGE_VOD;
            }else{
                return LEFT_MESSAGE;
            }
        }
    }

    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v;
        if(viewType == RIGHT_MESSAGE) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.right_text_view, parent, false);
        }else if(viewType == MID_MESSAGE){
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.mid_text_view, parent, false);
        }else if(viewType == LEFT_MESSAGE){
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.left_text_view, parent, false);
        }else if(viewType == RIGHT_MESSAGE_IMG) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.right_text_view_img, parent, false);
        }else if(viewType == RIGHT_MESSAGE_VOD){
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.right_text_view_vod, parent, false);
        }else if(viewType == LEFT_MESSAGE_VOD){
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.left_text_view_vod, parent, false);
        }else{
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.left_text_view_img, parent, false);
        }

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if(holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
            holder.iconImage.setColorFilter(Color.parseColor("#88000000"), PorterDuff.Mode.MULTIPLY); //이미지뷰 어둡게 효과주기
            String message = mChat.get(holder.getAdapterPosition()).getText();
            String ID = mChat.get(holder.getAdapterPosition()).getName(); //채팅창 좌,우 구분값은 Email로 구분하고 뷰에 보여지는것은 Name으로 함.
            holder.tvName.setText(ID);
            String stPhoto = mChat.get(holder.getAdapterPosition()).getPhoto();

            holder.tvUnRead.setTypeface(holder.tvUnRead.getTypeface(), Typeface.BOLD);
            // 그룹채팅 메시지읽음 표시
            if(!mChat.get(holder.getAdapterPosition()).getText().equals("System")) {
                int unReadCount = mChat.get(holder.getAdapterPosition()).getUnReadCount();
               if(unReadCount == 0){
                   holder.tvUnRead.setText("");
               }else{
                   holder.tvUnRead.setText(String.valueOf(unReadCount));
               }
            }
            // 1:1 채팅 메시지 읽음 표시
            if(!mChat.get(holder.getAdapterPosition()).getText().equals("System") && !TextUtils.isEmpty(roomType) && !roomType.contains("Group@")) {
                boolean isSeen = mChat.get(holder.getAdapterPosition()).isSeen();
                if(!isSeen){
                    holder.tvUnRead.setText("1");
                }else{
                    holder.tvUnRead.setText("");
                }
            }

            holder.tvTime.setText(mChat.get(holder.getAdapterPosition()).getTime());

            if (TextUtils.isEmpty(stPhoto)) {
                //Picasso.with(context).load(R.drawable.ic_unknown_user).transform(new RoundedTransformation(0, 0)).fit().centerCrop().into(holder.ivUser);
                Glide.with(context).load(R.drawable.ic_unknown_user).transform(new CircleTransform(context), new FitCenter(context)).into(holder.ivUser);
            } else {
                //Picasso.with(context).load(stPhoto).error(R.drawable.ic_unknown_user).transform(new RoundedTransformation(200, 0)).fit().centerCrop().into(holder.ivUser);
                if (!mChat.get(holder.getAdapterPosition()).getEmail().equals(stEmail) && !mChat.get(holder.getAdapterPosition()).getType().equals("System")) {
                    if (holder.getAdapterPosition() == 0) { // position 값이 0일경우는 -1 position 값이 없기때문에 첫 메시지 위치에는 무조건 뷰 보이도록함.
                        Glide.with(context).load(stPhoto).error(R.drawable.ic_unknown_user).transform(new CircleTransform(context), new FitCenter(context)).into(holder.ivUser);
                        holder.ivUser.setVisibility(View.VISIBLE);
                        holder.tvName.setVisibility(View.VISIBLE);
                    } else {
                        String targetUser = mChat.get(holder.getAdapterPosition() - 1).getEmail();
                        String currentUser = mChat.get(holder.getAdapterPosition()).getEmail();
                        if (targetUser.equals(currentUser)) {
                            String targetTime = mChat.get(holder.getAdapterPosition() - 1).getTime();
                            String currentTime = mChat.get(holder.getAdapterPosition()).getTime();
                            int num_pos_current = Integer.parseInt(targetTime.substring(17));
                            int num_pos_last = Integer.parseInt(currentTime.substring(17)); //타임스탬프 초단위 부분
                            int num_min_target = Integer.parseInt(targetTime.substring(14, 16));
                            int num_min_current = Integer.parseInt(currentTime.substring(14, 16)); //타임스탬프 분단위 부분
                            if (num_min_target == num_min_current && num_pos_last - num_pos_current > 10) { //메시지 분단위가 같고 초단위 입력시간 차이가 10초를 넘으면
                                Glide.with(context).load(stPhoto).error(R.drawable.ic_unknown_user).transform(new CircleTransform(context), new FitCenter(context)).into(holder.ivUser);
                                holder.tvName.setVisibility(View.VISIBLE);
                                holder.ivUser.setVisibility(View.VISIBLE); //뷰 보임
                            } else if (num_min_target != num_min_current) { //분단위가 다를경우 뷰 보임
                                Glide.with(context).load(stPhoto).error(R.drawable.ic_unknown_user).transform(new CircleTransform(context), new FitCenter(context)).into(holder.ivUser);
                                holder.tvName.setVisibility(View.VISIBLE);
                                holder.ivUser.setVisibility(View.VISIBLE);
                            } else { // 메시지 초단위 입력시간 차이가 10초 이하이면 이미지뷰, 이름 텍스트뷰 없앰
                                holder.ivUser.setVisibility(View.INVISIBLE);
                                holder.tvName.setVisibility(View.GONE);
                            }
                        } else { //새로운 유저의 메시지일 경우 다시 뷰 보임
                            Glide.with(context).load(stPhoto).error(R.drawable.ic_unknown_user).transform(new CircleTransform(context), new FitCenter(context)).into(holder.ivUser);
                            holder.ivUser.setVisibility(View.VISIBLE);
                            holder.tvName.setVisibility(View.VISIBLE);
                        }//타임스탬프값 비교해서 10초 이내에 입력된 메시지면 프로필사진과 이름, 타임스탬프값은 안보이도록함.
                    }
                }
            }

            holder.ivUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = mChat.get(holder.getAdapterPosition()).getPhoto();
                    Intent intent = new Intent(context, FullScreenActivity.class);
                    intent.putExtra("chatProfileImage", url);
                    context.startActivity(intent);
                }
            });

            if (holder.mImageVIew != null) { //채팅창 사진메시지 클릭시 풀스크린 액티비티로 이동되어 전체화면 이미지 보여줌.
                Glide.with(context).load(message).override(400, 400).into(holder.mImageVIew);
                holder.mImageVIew.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, FullScreenActivity.class);
                        intent.putExtra("urlPhotoClick", mChat.get(holder.getAdapterPosition()).getText());
                        context.startActivity(intent);
                    }
                });
            }
            if (holder.mTextView != null) {
                //Toast.makeText(context,searchText,Toast.LENGTH_SHORT).show();
                if(searchText != null) {
                    if (mChat.get(holder.getAdapterPosition()).getText().contains(searchText)) {
                        holder.setIsRecyclable(false);
                        // 메시지 검색으로 어댑터 생성 시 리사이클러뷰 재사용 중지 (뷰 재사용으로인해 다른 position에도 적용됨)
                        holder.mTextView.setTextColor(Color.RED);
                        holder.mTextView.setTextSize(25f);
                    }
                }
                holder.mTextView.setText(message);
                holder.mTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context, "텍스트 메시지 클릭", Toast.LENGTH_SHORT).show();
                    }
                });

            }
            if (holder.mVideoView != null) {
                holder.mVideoView.setVideoURI(Uri.parse(message));
                holder.mVideoView.seekTo(100);
                holder.mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {

                    }
                });
                holder.mVideoView.setOnTouchListener(new View.OnTouchListener() { //비디오뷰는 onClickListener 사용불가,
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("경고");
                        builder.setMessage("WIFI 연결이 아닐 경우 데이터 과금이 발생할 수 있습니다. 영상을 실행하시겠습니까?");
                        builder.setPositiveButton("네",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(context, VideoViewActivity.class);
                                        intent.putExtra("urlVideoClick", mChat.get(holder.getAdapterPosition()).getText());
                                        context.startActivity(intent);
                                    }
                                });
                        builder.setNegativeButton("아니오",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                        builder.show();

                        return false;
                    }
                });
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mChat.size();
    }

    private int getItemPosition(String messageId){
        int position = 0;
        for(Chat chat : mChat){
            if(chat.getMessageID().equals(messageId)){
                return position;
            }
            position++;
        }
        return -1;
    }

    public void updateItem(Chat chat){
        int position = getItemPosition(chat.getMessageID());
        if(position<0){
            return;
        }
        mChat.set(position,chat);
        notifyItemChanged(position);
    }
}

