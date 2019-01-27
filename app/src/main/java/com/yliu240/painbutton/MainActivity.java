package com.yliu240.painbutton;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
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

import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity {

    int dmgTop,dmgBottom, critTop, critBottom;
    int bossHP;
    final int totalHP = 10000000;
    String maxHP;
    Boolean isAlive;
    int[] screenCenter = new int[2];
    ImageButton sound = null;
    MediaPlayer damageFx, spawnFx, deathFx, bgm;
    ImageView screenInFrontOfMob;
    TextView bossHP_text;
    GifImageView mob;
    AnimationListener mob_move, mob_death, mob_appear;
    GifDrawable mob_drawable;
    RelativeLayout RL;
    RelativeLayout.LayoutParams lp;
    Typeface comic_sans;
    final int critialDmgSize = 50;
    final int normalDmgSize = 50;
    static int spawn_rate = 5;
    static Timer timer;
    int delay = 1000;
    int period = 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Start Bgm and Sound fx
        damageFx = MediaPlayer.create(MainActivity.this,R.raw.slime_damage_sound);
        deathFx = MediaPlayer.create(MainActivity.this,R.raw.slime_death_sound);
        spawnFx = MediaPlayer.create(MainActivity.this,R.raw.slime_spawn_sound);
        bgm = MediaPlayer.create(MainActivity.this,R.raw.ellinia_bgm1);
        bgm.start();
        bgm.setLooping(true);

        // Sound button for 'Mute' & 'Sound On'
        sound = (ImageButton) findViewById(R.id.sound);

        // Creating Mob
        screenInFrontOfMob = (ImageView) findViewById(R.id.transparent);
        mob = (GifImageView) findViewById(R.id.slime);
        spawn_mob(5);

        // RelativeLayout that will contain the damage Text
        RL = (RelativeLayout) findViewById(R.id.relayout);
        lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, // Width of TextView
                RelativeLayout.LayoutParams.WRAP_CONTENT); // Height of TextView

        //Colors For damageText
        dmgTop=ContextCompat.getColor(MainActivity.this, R.color.dmgTop);
        dmgBottom=ContextCompat.getColor(MainActivity.this, R.color.dmgBottom);
        critTop=ContextCompat.getColor(MainActivity.this, R.color.critTop);
        critBottom=ContextCompat.getColor(MainActivity.this, R.color.critBottom);
        comic_sans = Typeface.createFromAsset(getAssets(),"comic-sans-ms-bold.ttf");
        screenCenter[0]=this.getResources().getDisplayMetrics().widthPixels;
        screenCenter[0]-=this.getResources().getDisplayMetrics().widthPixels/2;
        screenCenter[1]=this.getResources().getDisplayMetrics().heightPixels;
        screenCenter[1]-=this.getResources().getDisplayMetrics().heightPixels/2;

        // Set Boss HP
        bossHP=totalHP;
        maxHP = String.valueOf(bossHP);
        bossHP_text = (TextView) findViewById(R.id.hp);
        bossHP_text.setTypeface(comic_sans);
        bossHP_text.setText(String.valueOf(bossHP)+"/"+maxHP);
    }


    @Override
    protected void onStart() {
        super.onStart();

        startBgmListener(); //Listens to sound Button if pressed
        screenInFrontOfMob.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch (View v, MotionEvent event){
                switch (event.getAction()) {

                    // Pressed
                    case MotionEvent.ACTION_DOWN: {
                        if (bossHP == 0){
                            break;
                        }
                        if(damageFx.isPlaying()){
                            damageFx.seekTo(0);
                        }
                        damageFx.start();

                        // Generate random Damage and create damage Text
                        int damageTaken=0;
                        int x = (int) event.getRawX();
                        int y = (int) event.getRawY();

                        //TODO: Make this relative to mob position
                        if (screenCenter[0]+100>x && screenCenter[0]-300<x && screenCenter[1]-50<y && screenCenter[1]+350>y){
                            damageTaken = ThreadLocalRandom.current().nextInt(500000, 999999 + 1);
                        }
                        createDamageText(damageTaken, x, y);
                        decreaseHP(damageTaken);
                        mob.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                        mob.setImageResource(R.drawable.slime_hurt);
                        break;
                    }
                    // Released
                    case MotionEvent.ACTION_UP: {
                        if (bossHP == 0){
                            mob_death();
                            isAlive = false;
                        }else{
                            mob.setImageResource(R.drawable.slime_move);
                        }
                        break;
                    }
                }
                return isAlive;
            }
        });
    }

    private void mob_death() {
        deathFx.start();
        mob.setImageResource(R.drawable.slime_death);
        mob_drawable = (GifDrawable) mob.getDrawable();
        mob_drawable.setLoopCount(1);
        mob_death = new AnimationListener() {
            @Override
            public void onAnimationCompleted(int loopNumber) {
                mob.setImageResource(R.drawable.no_mob);
                mob_drawable.setLoopCount(0);
            }
        };
        mob_drawable.addAnimationListener(mob_death);
        Toast.makeText(MainActivity.this, "DEFEATED", Toast.LENGTH_SHORT).show();
        spawn_mob(5);
    }

    private void spawn_mob(int rate) {
        spawn_rate=rate;
        timer = new Timer();
        Toast.makeText(MainActivity.this, "Spawning Mob...", Toast.LENGTH_LONG).show();
        timer.scheduleAtFixedRate(new TimerTask() {

            public void run() {
                if (spawn_rate == 1) {
                    mob_start();
                    timer.cancel();
                }else {
                    --spawn_rate;
                }
            }
        }, delay, period);
    }

    private void mob_start(){
            spawnFx.start();
            mob.setImageResource(R.drawable.slime_spawn);
            mob_drawable = (GifDrawable) mob.getDrawable();
            mob_drawable.setLoopCount(1);
            mob_move = new AnimationListener() {
                @Override
                public void onAnimationCompleted(int loopNumber) {
                    mob.setImageResource(R.drawable.slime_move);
                    mob_drawable.setLoopCount(0);
                }
            };
            mob_drawable.addAnimationListener(mob_move);

            bossHP=totalHP;
            isAlive=true;
    }

    public void startBgmListener(){
        Boolean clicked = new Boolean(false);
        sound.setTag(clicked); // Button wasn't clicked
        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg;
                if( ((Boolean)sound.getTag())==false ){
                    sound.setImageResource(R.drawable.baseline_volume_off_24);
                    bgm.pause();
                    msg="Mute";
                    sound.setTag(new Boolean(true));
                }else{
                    sound.setImageResource(R.drawable.baseline_volume_up_24);
                    bgm.start();
                    msg="Sound On";
                    sound.setTag(new Boolean(false));
                }
                final Toast toast = Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT);
                toast.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toast.cancel();
                    }
                }, 1000);
            }
        });
    }


    // Create Damage Text and sets the font, size, text, position
    public void createDamageText(int damageTaken, float x, float y){
        int slideHeight = ThreadLocalRandom.current().nextInt(200, 350 + 1);
        int slideWidth = ThreadLocalRandom.current().nextInt(-20, 20 + 1);
        String damage = Integer.toString(damageTaken);

        final TextView damageText = new TextView(getApplicationContext());
        damageText.setLayoutParams(lp);
        damageText.setSingleLine();
        RL.addView(damageText);

        // fadeIn/Out Animations
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(10);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setStartOffset(175);
        fadeOut.setDuration(350);

        TranslateAnimation moveUp = new TranslateAnimation(0, slideWidth, 0, -slideHeight);
        moveUp.setDuration(150);


        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(fadeIn);
        animationSet.addAnimation(moveUp);

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

        damageText.setTypeface(comic_sans);
        damageText.setTextSize(normalDmgSize);
        Shader textShader=new LinearGradient(0, 0, 0, damageText.getPaint().getTextSize(),
                new int[]{dmgTop,dmgBottom},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        damageText.setShadowLayer(1.5f, 5.0f, 5.0f, Color.BLACK);

        damageText.getPaint().setShader(textShader);
        if(damageTaken>=900000){
            damageText.setTextSize(critialDmgSize);
            Shader critShader=new LinearGradient(0, 0, 0, damageText.getPaint().getTextSize(),
                    new int[]{critTop, critBottom},
                    new float[]{0, 1}, Shader.TileMode.CLAMP);
            damageText.getPaint().setShader(critShader);
        }else if (damageTaken<500000){
            damage = " MISS ";
        }
        damageText.setText(damage);
        // Position damageText to Click position
        damageText.setX(screenCenter[0]-300);
        damageText.setY(screenCenter[1]);
        damageText.setVisibility(View.VISIBLE);
        damageText.startAnimation(animationSet);
    }


    // Create AnimationSet for Damage text, returns animationSet
    public AnimationSet createDamageTextAnimation(){
        // fade Out Animations
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setStartOffset(150);
        fadeOut.setDuration(400);

        int slideHeight = ThreadLocalRandom.current().nextInt(100, 200 + 1);
        int slideWidth = ThreadLocalRandom.current().nextInt(-5, 5 + 1);
        TranslateAnimation moveUp = new TranslateAnimation(0, slideWidth, 0, -slideHeight);
        moveUp.setDuration(150);
        moveUp.setStartOffset(50);

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(moveUp);

        return animationSet;
    }

    public void decreaseHP(int damageTaken){
        if (bossHP-damageTaken>0) {
            bossHP -= damageTaken;
        }else{
            bossHP = 0;
        }
        bossHP_text.setText(String.valueOf(bossHP)+"/"+maxHP);
        bossHP_text.setVisibility(View.VISIBLE);
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setDuration(4000);
        fadeOut.setAnimationListener(new TranslateAnimation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationRepeat(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                bossHP_text.setVisibility(View.INVISIBLE);
            }
        });
        bossHP_text.startAnimation(fadeOut);
    }
}

