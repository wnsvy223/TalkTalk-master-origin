package com.example.home.mytalk.Activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.home.mytalk.R;

public class WebActivity extends AppCompatActivity {
    public static WebView webView;
    public EditText etURL;
    public ImageButton button_go;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(0xFF22CEF1));

        if(webView != null){
            webView.destroy();
        }
        webView = (WebView)findViewById(R.id.wv_list);
        etURL = (EditText)findViewById(R.id.etURL);
        etURL.setText("http://");
        setWebView();
        //View v =  inflater.inflate(R.layout.fragment_home, container, false);

        button_go = (ImageButton)findViewById(R.id.button2);
        button_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = etURL.getText().toString();
                webView.loadUrl(url);
            }
        });

    }
    private void setWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.setWebViewClient(new WebViewClient());
        Intent intent = getIntent();
        String uri = intent.getStringExtra("url");
        //webView.addJavascriptInterface(new JavascriptInterface(getActivity()), "android");
        webView.loadUrl(uri);
    }

    @Override
    public void onBackPressed() {

        if(webView != null && webView.canGoBack()){
            try
            {
                webView.goBack();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }else{
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
