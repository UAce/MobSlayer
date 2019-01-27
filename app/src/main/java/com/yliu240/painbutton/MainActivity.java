package com.yliu240.painbutton;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.BoringLayout;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import pl.droidsonroids.gif.GifImageView;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Start Bgm and Sound fx
        final MediaPlayer damageFx = MediaPlayer.create(MainActivity.this,R.raw.slime_damage_sound);
        final MediaPlayer bgm = MediaPlayer.create(MainActivity.this,R.raw.ellinia_bgm1);
        bgm.start();
        bgm.setLooping(true);

        // Sound button
        final ImageButton sound = (ImageButton) findViewById(R.id.sound);
        Boolean clicked = new Boolean(false);
        sound.setTag(clicked); // Button wasn't clicked
        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( ((Boolean)sound.getTag())==false ){
                    sound.setImageResource(R.drawable.baseline_volume_off_24);
                    bgm.pause();
//                    Toast.makeText(MainActivity.this, "Mute", Toast.LENGTH_SHORT).show();
                    sound.setTag(new Boolean(true));
                }else{
                    sound.setImageResource(R.drawable.baseline_volume_up_24);
                    bgm.start();
//                    Toast.makeText(MainActivity.this, "Sound On", Toast.LENGTH_SHORT).show();
                    sound.setTag(new Boolean(false));
                }
            }
        });

        final ImageView imageView = (ImageView) findViewById(R.id.transparent);
        final GifImageView gifImageView = (GifImageView) findViewById(R.id.slimeGif);

        final RelativeLayout RL = (RelativeLayout) findViewById(R.id.relayout);
        final RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, // Width of TextView
                RelativeLayout.LayoutParams.WRAP_CONTENT); // Height of TextView

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch (View v, MotionEvent event){
//                TextView damageText = (TextView) findViewById(R.id.textView1);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        if(damageFx.isPlaying()){
                            damageFx.seekTo(0);
                        }
                        damageFx.start();

                        //Animations
                        Animation fadeIn = new AlphaAnimation(0, 1);
                        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
                        fadeIn.setDuration(300);

                        Animation fadeOut = new AlphaAnimation(1, 0);
                        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
                        fadeOut.setStartOffset(300);
                        fadeOut.setDuration(300);
                        final AnimationSet animationSet = new AnimationSet(true);
                        animationSet.addAnimation(fadeIn);

                        final TextView damageText = new TextView(getApplicationContext());
                        damageText.setLayoutParams(lp);
                        RL.addView(damageText);
                        damageText.setTextAppearance(MainActivity.this, R.style.AudioFileInfoOverlayText);
                        int x = (int) event.getX()-120;
                        int y = (int) event.getY()-50;
                        int slideHeight = ThreadLocalRandom.current().nextInt(80, 200 + 1);
                        TranslateAnimation transAnimation= new TranslateAnimation(0, 0, 0, -slideHeight);
                        transAnimation.setDuration(200);
                        transAnimation.setStartOffset(50);
                        animationSet.addAnimation(transAnimation);
                        fadeOut.setAnimationListener(new TranslateAnimation.AnimationListener() {

                            @Override
                            public void onAnimationStart(Animation animation) { }

                            @Override
                            public void onAnimationRepeat(Animation animation) { }

                            @Override
                            public void onAnimationEnd(Animation animation)
                            {
                                damageText.setVisibility(View.GONE);
                            }
                        });
                        animationSet.addAnimation(fadeOut);

                        // Damage
                        int randomNum = ThreadLocalRandom.current().nextInt(0, 999999 + 1); // nextInt is normally exclusive of the top value, so add 1 to make it inclusive
                        String damage = Integer.toString(randomNum);
                        damageText.setTextSize(25);

                        if(randomNum>=900000){
                            damageText.setTextSize(65);
                        }else if (randomNum<900000 && randomNum>=75000){
                            damageText.setTextSize(40);
                        }else if (randomNum<500000){
                            damage = "MISS";
                        }
                        int top=ContextCompat.getColor(MainActivity.this, R.color.TopText);
                        int bottom=ContextCompat.getColor(MainActivity.this, R.color.BottomText);
                        Shader textShader=new LinearGradient(0, 0, 0, damageText.getPaint().getTextSize(),
                                new int[]{top ,bottom},
                                new float[]{0, 1}, Shader.TileMode.CLAMP);//Assumes bottom and top are colors defined above
                        damageText.getPaint().setShader(textShader);
                        damageText.setShadowLayer(1, 0, 0, Color.BLACK);
                        Typeface comic_sans = Typeface.createFromAsset(getAssets(),"comic-sans-ms-bold.ttf");
                        damageText.setTypeface(comic_sans);

                        //Start animation
                        damageText.setX(x);
                        damageText.setY(y);
                        damageText.setText(damage);
                        damageText.startAnimation(animationSet);
//                        damageText.animate()
//                                .translationY(damageText.getHeight())
//                                .alpha(0.0f)
//                                .setDuration(3000)
//                                .setListener(new AnimatorListenerAdapter() {
//                                    @Override
//                                    public void onAnimationEnd(android.animation.Animator animation) {
//                                        super.onAnimationEnd(animation);
//                                        damageText.setVisibility(View.GONE);
//                                    }
//                                });
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

