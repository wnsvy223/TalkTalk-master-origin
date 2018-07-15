package com.example.home.mytalk.Utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.example.home.mytalk.Adapter.InfiniteScrollAdapter;
import com.example.home.mytalk.Model.CardViewItem;
import com.srx.widget.PullCallback;
import com.srx.widget.PullToLoadView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xnote on 2018-05-15.
 */

public class Paginator {

    Context context;
    private PullToLoadView pullToLoadView;
    private InfiniteScrollAdapter adapter;
    private boolean isLoading = false;
    private boolean hasLoadedAll = false;
    private int nextPage;
    private String pageUrl;
    private List<String > imgList;
    private List<String > typeList;
    private List<String > dirList;
    private List<String > titlList;
    private List<String> linkList;

    public Paginator(Context context, PullToLoadView pullToLoadView) {
        this.context = context;
        this.pullToLoadView = pullToLoadView;

        //RECYCLERVIEW
        RecyclerView rv=pullToLoadView.getRecyclerView();
        rv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false));
        adapter=new InfiniteScrollAdapter(context,new ArrayList<CardViewItem>());
        rv.setAdapter(adapter);

        pageUrl = "https://movie.naver.com/movie/running/current.nhn";
        new jsoup().execute(); // 백그라운드로 jsoup 라이브러리를 이용 웹페이지 파싱
    }

    /*
    PAGE DATA
     */
    public void initializePaginator()
    {
        pullToLoadView.isLoadMoreEnabled(true);
        pullToLoadView.setPullCallback(new PullCallback() {

            //LOAD MORE DATA
            @Override
            public void onLoadMore() {
                loadData(nextPage);
            }

            //REFRESH AND TAKE US TO FIRST PAGE
            @Override
            public void onRefresh() {
                adapter.clear();
                hasLoadedAll = false;
                loadData(1);
            }

            //IS LOADING
            @Override
            public boolean isLoading() {
                return isLoading;
            }

            //CURRENT PAGE LOADED
            @Override
            public boolean hasLoadedAllItems() {
                return hasLoadedAll;
            }
        });

        pullToLoadView.initLoad();
    }

    /*
     LOAD MORE DATA
     SIMULATE USING HANDLERS
     */
    private void loadData(final int page)
    {

        isLoading = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(imgList != null) {
                for (int i = 0; i < imgList.size() -1; i++) {
                    adapter.add(new CardViewItem(titlList.get(i), imgList.get(i), typeList.get(i), dirList.get(i), linkList.get(i)));
                }
                hasLoadedAll = true; //파싱된 리스트의 길이만큼 어댑터에 추가되면 로딩상태값을 true로 하여 리스트 추가되지 않도록 함.
                pullToLoadView.setComplete();
                isLoading = false;
                nextPage = page + 1;
                }
            }
        }, 3000);

    }


    private class jsoup extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Document document = Jsoup.connect(pageUrl).get();
                Elements elements = document.select("ul.lst_detail_t1").select("li");
                imgList = new ArrayList<>();
                typeList = new ArrayList<>();
                dirList = new ArrayList<>();
                titlList = new ArrayList<>();
                linkList = new ArrayList<>();
                imgList.clear();
                typeList.clear();
                dirList.clear();
                titlList.clear();
                linkList.clear();
                for(Element element : elements){
                    String imgUrl = element.select("li div[class=thumb] a img").attr("src");
                    imgList.add(imgUrl);

                    String title =  element.select("li dt[class=tit] a").text();
                    titlList.add(title);

                    Element elmDir = element.select("dt.tit_t2").first().nextElementSibling();
                    String director = "감독 : " + elmDir.select("a").text();
                    dirList.add(director);

                    Element elmType = element.select("dl[class=info_txt1] dt").first().nextElementSibling();
                    String type = elmType.select("dd").text();
                    typeList.add(type);

                    String link = element.select("li div[class=thumb] a").attr("href");
                    linkList.add(link);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        // AsyncTask doInBackground 메소드에서 백그라운드로 리스트에 파싱된 데이터들을 담는 작업만 하고 UI에 적용하는 작업은
        // 핸들러를 이용한 loadData 메소드를 이용해 적용.
    }
}
