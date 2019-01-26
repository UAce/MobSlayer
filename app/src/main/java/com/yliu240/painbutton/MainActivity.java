package com.yliu240.painbutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

import pl.droidsonroids.gif.GifImageView;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageButton sound = (ImageButton) findViewById(R.id.sound);
        Boolean clicked = new Boolean(false);
        sound.setTag(clicked); // wasn't clicked
        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( ((Boolean)sound.getTag())==false ){
                    sound.setImageResource(R.drawable.baseline_volume_off_24);
                    Toast.makeText(MainActivity.this, "Mute", Toast.LENGTH_SHORT).show();
                    sound.setTag(new Boolean(true));
                }else{
                    sound.setImageResource(R.drawable.baseline_volume_up_24);
                    Toast.makeText(MainActivity.this, "Sound On", Toast.LENGTH_SHORT).show();
                    sound.setTag(new Boolean(false));
                }
            }
        });

        final ImageView imageView = (ImageView) findViewById(R.id.transparent);
        final GifImageView gifImageView = (GifImageView) findViewById(R.id.slimeGif);

        //Set Animation
        final Animation in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(500);

        final Animation out = new AlphaAnimation(1.0f, 0.0f);
        out.setDuration(500);

        AnimationSet as = new AnimationSet(true);
        as.addAnimation(out);
        in.setStartOffset(1000);
        as.addAnimation(in);

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch (View v, MotionEvent event){
                TextView damageText = (TextView) findViewById(R.id.textView1);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {

                        // Damage
                        int randomNum = ThreadLocalRandom.current().nextInt(0, 999999 + 1); // nextInt is normally exclusive of the top value, so add 1 to make it inclusive
                        String damage = Integer.toString(randomNum);
                        //Start animation
                        damageText.setText(damage);
                        damageText.setTextColor(getResources().getColor(R.color.damage));
                        damageText.startAnimation(in);
                        damageText.startAnimation(out);
                        gifImageView.setImageResource(R.drawable.kingslimehurt);
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        gifImageView.setImageResource(R.drawable.kingslime_animation);
                        break;
                    }
                }
                return true;
            }
        });
    }
}

