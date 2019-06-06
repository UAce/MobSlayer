package com.yliu240.mobslayer.View;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.google.gson.Gson;
import com.yliu240.mobslayer.Controller.GameController;
import com.yliu240.mobslayer.R;

public class SplashScreenActivity extends AppCompatActivity {

    private Context mContext;
    private static final String TAG = "@@@@@@@@@@@@@@@DEBUG ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mContext = getApplicationContext();
        ImageView appIcon = findViewById(R.id.appIcon);
        TextView appName = findViewById(R.id.appName);
        Animation fadeIn = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
        int topTextColor = ContextCompat.getColor(mContext, R.color.TopTextColor);
        int bottomTextColor = ContextCompat.getColor(mContext, R.color.BottomTextColor);

        Shader textShader = new LinearGradient(0, 0, 0, appName.getTextSize(),
                topTextColor, bottomTextColor, Shader.TileMode.CLAMP);
        appName.getPaint().setShader(textShader);
        appIcon.startAnimation(fadeIn);
        appName.startAnimation(fadeIn);

        Thread splash = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(3000);
                    Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(mContext,
                            android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                    Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                    startActivity(intent, bundle);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        splash.start();
    }
}
