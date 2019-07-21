package com.example.home.mytalk.Fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.home.mytalk.Activity.WebActivity;
import com.example.home.mytalk.R;
import com.example.home.mytalk.Utils.Paginator;
import com.squareup.picasso.Picasso;
import com.srx.widget.PullToLoadView;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageClickListener;
import com.synnapps.carouselview.ImageListener;


public class Fragment_Home extends android.support.v4.app.Fragment{

    private String[] url = {
            "http://www.naver.com",
            "http://www.google.com",
            "http://www.daum.net",
            "http://www.facebook.com",
            "http://www.twitter.com"
    };
    private String[] urlImages = {
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSTUuG9rC24BqVoXdgCchH5UBL5Jcyt0zbSfwxw0pvAhJi_IsAODA",
            "http://brandemia.org/sites/default/files/sites/default/files/logo-google-antes.jpg",
            "http://m1.daumcdn.net/svc/image/U03/common_icon/5587C4E4012FCD0001",
            "http://www.bloter.net/wp-content/uploads/2012/08/Facebook-logo1.jpg",
            "https://www.androidpolice.com/wp-content/themes/ap2/ap_resize/ap_resize.php?src=https%3A%2F%2Fwww.androidpolice.com%2Fwp-content%2Fuploads%2F2016%2F02%2Fnexus2cee_twitter.png&w=728"
    };
    public Intent intent;
    public String TAG = getClass().getSimpleName();
    public CarouselView carouselView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);


        carouselView = (CarouselView)v.findViewById(R.id.carouselView);
        carouselView.setPageCount(urlImages.length);
        carouselView.setImageListener(imageListener);
        carouselView.setImageClickListener(new ImageClickListener() {
            @Override
            public void onClick(int position) {
               setWebVeiwIntent(position);
            }
        });

        PullToLoadView pullToLoadView= (PullToLoadView)v.findViewById(R.id.pullToLoadView);
        new Paginator(getActivity(),pullToLoadView).initializePaginator(); //광고 스크롤 뷰 생성 및 초기화

        return v;
    }

    ImageListener imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {
            Picasso.with(getContext())
                    .load(urlImages[position])
                    .fit().centerCrop()
                    .into(imageView);
        }
    };

    private void setWebVeiwIntent(int position){
        Intent intent = new Intent(getActivity(), WebActivity.class);
        intent.putExtra("url", url[position]);
        startActivity(intent);
    }
}
