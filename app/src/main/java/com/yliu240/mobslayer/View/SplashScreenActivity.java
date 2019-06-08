package com.yliu240.mobslayer.View;

import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yliu240.mobslayer.View.R;

public class SplashScreenActivity extends AppCompatActivity {

    private Context mContext;
    LinearLayout splashScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mContext = getApplicationContext();

        splashScreen = findViewById(R.id.splashScreen);;
        TextView appName = findViewById(R.id.appName);
        Animation fadeIn = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
        int topTextColor = ContextCompat.getColor(mContext, R.color.TopTextColor);
        int bottomTextColor = ContextCompat.getColor(mContext, R.color.BottomTextColor);
        Shader textShader = new LinearGradient(0, 0, 0, appName.getTextSize(),
                topTextColor, bottomTextColor, Shader.TileMode.CLAMP);
        appName.getPaint().setShader(textShader);
        splashScreen.startAnimation(fadeIn);

        Thread splash = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(3000);
                    Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(mContext,
                            R.anim.fade_in, R.anim.fade_out).toBundle();
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
