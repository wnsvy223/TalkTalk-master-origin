package com.example.home.mytalk.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.example.home.mytalk.Model.Friend;
import com.example.home.mytalk.R;
import com.example.home.mytalk.Utils.CircleTransform;
import com.example.home.mytalk.Utils.RoundedTransformation;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;


public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.ViewHolder> {

    private List<Friend> mFriend;
    public Context context;
    private String stPhoto;
    private List<String> mInvite;
    public String name;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tvName;
        public ImageView ivUser;
        public CheckBox checkBox;

        private ViewHolder(View item) {
            super(item);

            tvName = (TextView)item.findViewById(R.id.tvEmail);
            ivUser = (ImageView)item.findViewById(R.id.ivUser);
            checkBox = (CheckBox)item.findViewById(R.id.checkBox);

        }
    }

    public GroupChatAdapter(List<Friend> mFriend, List<String> mInvite, Context context) {
        this.mFriend = mFriend;
        this.context = context;
        this.mInvite = mInvite;

    }


    @Override
    public GroupChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_groupchat_user, parent, false);

        ViewHolder vh = new ViewHolder(v);

        return vh;

    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if(holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
            if (mInvite != null) {
                for (int i = 0; i < mInvite.size(); i++) {
                    name = mInvite.get(i);
                    if (mFriend.get(position).getName().equals(name)) {
                        holder.checkBox.setEnabled(false);
                    }
                }
            }
            //GroupChatActivity에서 어댑터를 2가지 경우에 호출 1 - 신규 그룹방 생성 시,  2 - 추가 인원 초대 시
            //1-의 경우 어댑터 객체생성 시 mInvite 리스트 객체는 null값임(초대인원이 없으니)
            //2-의 경우 어댑터 객체생성 시 mInvite 리스트 객체는 현재 방에 초대된 인원들 값임.
            //현재 방에 초대된 인원들의 이름 값과 비교해서 같을(=이미 방에 초대된 인원)경우 체크박스 체크값 변경 disable로 세팅

            holder.tvName.setText(mFriend.get(position).getName());
            stPhoto = mFriend.get(position).getPhoto();

            if (TextUtils.isEmpty(stPhoto)) {
                Glide.with(context)
                        .load(R.drawable.ic_unknown_user)
                        .transform(new CircleTransform(context), new FitCenter(context)).into(holder.ivUser);
            } else {
                Glide.with(context)
                        .load(stPhoto)
                        .dontAnimate()
                        .transform(new CircleTransform(context), new FitCenter(context)).into(holder.ivUser);
                //이미지 로딩 속도 개선
            }

            holder.checkBox.setChecked(mFriend.get(holder.getAdapterPosition()).isCheck());
            holder.checkBox.setTag(mFriend.get(holder.getAdapterPosition()));
            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CheckBox checkBox = (CheckBox) view;
                    Friend friend = (Friend) checkBox.getTag();
                    friend.setCheck(checkBox.isChecked());
                    mFriend.get(holder.getAdapterPosition()).setCheck(checkBox.isChecked());
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return mFriend.size();
    }

    public List<Friend> getFriendList(){
        return mFriend;
    }

}

