package com.yliu240.mobslayer.View;

import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.yliu240.mobslayer.R;

        public class SplashScreenActivity extends AppCompatActivity {

            private int TopTextColor, BottomTextColor;
            private Context mContext;
            private TextView appName;
            private ImageView appIcon;
            private Animation fadeIn;

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_splash_screen);

                mContext = getApplicationContext();
                appIcon = findViewById(R.id.appIcon);
                appName = findViewById(R.id.appName);
                fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                TopTextColor = ContextCompat.getColor(mContext, R.color.TopTextColor);
                BottomTextColor = ContextCompat.getColor(mContext, R.color.BottomTextColor);

                Shader textShader = new LinearGradient(0, 0, 0, appName.getTextSize(),
                        TopTextColor, BottomTextColor, Shader.TileMode.CLAMP);
                appName.getPaint().setShader(textShader);
                appIcon.startAnimation(fadeIn);
                appName.startAnimation(fadeIn);

        Thread splash = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(5000);
                    Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        splash.start();
    }
}
