package com.yliu240.mobslayer.View;

import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yliu240.mobslayer.R;

public class MenuActivity extends AppCompatActivity {

    private int TopMenuColor, BottomMenuColor;
    private Context mContext;
    private RelativeLayout menuBoard;
    private TextView menuTitle;
    private ImageButton new_game, load_game;
    private Animation fadeIn;
    private Typeface comic_sans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        comic_sans = Typeface.createFromAsset(getAssets(), "comic-sans-ms-bold.ttf");

        mContext = getApplicationContext();
        menuBoard = findViewById(R.id.menuBoard);
        menuTitle = findViewById(R.id.menuTitle);
        new_game = findViewById(R.id.new_game);
        load_game = findViewById(R.id.load_game);
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        TopMenuColor = ContextCompat.getColor(mContext, R.color.TopMenuColor);
        BottomMenuColor = ContextCompat.getColor(mContext, R.color.BottomMenuColor);

        Shader shader = new LinearGradient(0, 0, 0, menuTitle.getTextSize(),
                TopMenuColor, BottomMenuColor, Shader.TileMode.CLAMP);
        menuTitle.getPaint().setShader(shader);
        menuTitle.setTypeface(comic_sans);
        menuTitle.setTextColor(menuTitle.getTextColors().withAlpha(255));
        menuBoard.startAnimation(fadeIn);
    }
}
