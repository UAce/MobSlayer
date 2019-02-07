package com.yliu240.painbutton;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.javatuples.Pair;

import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import static com.yliu240.painbutton.R.drawable.no_mob;
import static com.yliu240.painbutton.R.drawable.slime_death;
import static com.yliu240.painbutton.R.drawable.slime_hurt;
import static com.yliu240.painbutton.R.drawable.slime_move;
import static com.yliu240.painbutton.R.drawable.slime_spawn;

public class MainActivity extends AppCompatActivity {

    int dmgTop, dmgBottom, critTop, critBottom;
    int bossHP;
    int totalHP = 100000000;
    int totalEXP = 1000;
    int currentEXP = 0;
    int amt_exp = 100;
    int currentLevel = 1;
    ProgressBar HpBar, ExpBar;
    Boolean isAlive;
    ImageButton sound = null;
    MediaPlayer bgm;
    Pair<SoundPool, Integer> dmgFx, spawnFx, deathFx;
    ImageView screenInFrontOfMob, bg_img;
    TextView bossHP_text, level_text, exp_val_text, exp_percent_text;
    GifImageView mob;
    GifDrawable mob_drawable;
    AnimationListener mob_death, mob_move;
    FrameLayout FL;
    FrameLayout.LayoutParams FL_lp;
    RelativeLayout RL, RL_hp;
    RelativeLayout.LayoutParams RL_lp, RL_lp_exp;
    Typeface comic_sans;
    int dmgSize = 50;
    private static final String TAG = "DEBUG: ";
    private static final String LV = "LV. ";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Load and Start Bgm/Sound Fx
        dmgFx = loadSfx(R.raw.slime_damage_sound);
        deathFx = loadSfx(R.raw.slime_death_sound);
        spawnFx = loadSfx(R.raw.slime_spawn_sound);
        bgm = MediaPlayer.create(MainActivity.this,R.raw.ellinia_bgm1);
        bgm.start();
        bgm.setLooping(true);

        // Sound button for 'Mute' & 'Sound On'
        sound = (ImageButton) findViewById(R.id.sound);

        // Get Layouts
        FL = (FrameLayout) findViewById(R.id.framelayout);
        FL_lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, // Width of TextView
                FrameLayout.LayoutParams.WRAP_CONTENT); // Height of TextView
        FL_lp.gravity = Gravity.CENTER;
        RL = (RelativeLayout) findViewById(R.id.relayout);
        RL_lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, // Width of TextView
                RelativeLayout.LayoutParams.WRAP_CONTENT); // Height of TextView
        RL_lp_exp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, // Width of TextView
                RelativeLayout.LayoutParams.WRAP_CONTENT); // Height of TextView
        RL_lp_exp.addRule(RelativeLayout.ABOVE,R.id.bottom_bar);
        RL_hp = (RelativeLayout) findViewById(R.id.relayout_hp);


        loadJson();


        //Colors For damageText
        dmgTop=ContextCompat.getColor(MainActivity.this, R.color.dmgTop);
        dmgBottom=ContextCompat.getColor(MainActivity.this, R.color.dmgBottom);
        critTop=ContextCompat.getColor(MainActivity.this, R.color.critTop);
        critBottom=ContextCompat.getColor(MainActivity.this, R.color.critBottom);
        comic_sans = Typeface.createFromAsset(getAssets(),"comic-sans-ms-bold.ttf");

        // Set Other Variables
        HpBar = (ProgressBar)findViewById(R.id.HpBar);
        HpBar.setProgress(100);
        ExpBar = (ProgressBar)findViewById(R.id.ExpBar);
        ExpBar.setMax(totalEXP);
        ExpBar.setProgress(currentEXP);
        Drawable exp_drawable = getResources().getDrawable(R.drawable.expbar_drawable);
        ExpBar.setProgressDrawable(exp_drawable);
        bossHP=totalHP;

        // Get TextViews
        bossHP_text = (TextView) findViewById(R.id.hp);
        level_text = (TextView) findViewById(R.id.level);
        exp_val_text = (TextView) findViewById(R.id.Exp_val);
        exp_percent_text = (TextView) findViewById(R.id.Exp_percent);

        // Set TextViews
        bossHP_text.setTypeface(comic_sans);
        bossHP_text.setText(String.valueOf(bossHP)+"/"+String.valueOf(totalHP));
        level_text.setText(String.format(Locale.CANADA,"%s %d",LV, currentLevel));
        exp_val_text.setText(String.valueOf(currentEXP));
        exp_percent_text.setText(String.format(Locale.CANADA," [%.2f%%]",toPercentage(currentEXP,totalEXP)));

    }

    private void loadJson() {
        // Creating Mob
        screenInFrontOfMob = (ImageView) findViewById(R.id.transparent);
        FL.setBackgroundResource(R.drawable.treedungeon);
        mob = new GifImageView(getApplicationContext());
        mob.setLayoutParams(FL_lp);
        mob.setImageResource(R.drawable.no_mob);
        FL.addView(mob,0);
        spawn_mob();
    }


    @Override
    protected void onStart() {
        super.onStart();

        //Listens to sound Button if pressed
        startBgmListener();
    }

    @Override
    protected void onPause(){
        super.onPause();

        bgm.pause();
        sound.setOnClickListener(null);
        System.gc();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        bgm.stop();
        sound.setOnClickListener(null);
        System.gc();
    }

    @Override
    protected void onResume(){
        super.onResume();

        startBgmListener();
        bgm.start();
    }


    // Useful method for another time
//    public static void sendViewToBack(final View child) {
//        final ViewGroup parent = (ViewGroup)child.getParent();
//        if (null != parent) {
//            parent.removeView(child);
//            parent.addView(child, 0);
//        }
//    }


    /*
     * Sound functions
     */
    private void playSfx(Pair<SoundPool, Integer> sfx){
        sfx.getValue0().play(sfx.getValue1(), 1.0F, 1.0F, 0, 0, 1.0F);
    }

    private Pair<SoundPool, Integer> loadSfx(int sound){
        SoundPool soundPool;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            soundPool = (new SoundPool.Builder()).setMaxStreams(2).build();
        }else{
            soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 5);
        }
        //Load the sound
        int sound_val = soundPool.load(getApplicationContext(), sound, 1);
//        soundPool.play(sound_val, 1.0F, 1.0F, 0, 0, 1.0F);
        Pair<SoundPool, Integer> sfx = new Pair<SoundPool, Integer>(soundPool, sound_val);

        return sfx;
    }

    private void startBgmListener(){
        Boolean clicked = new Boolean(false);
        sound.setTag(clicked); // Button isn't clicked on default
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



    /*
     * Mob Functions
     */
    private void startAttackListener() {

        screenInFrontOfMob.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch (View v, MotionEvent event){
                switch (event.getAction()) {

                    // Pressed
                    case MotionEvent.ACTION_DOWN: {
                        if (bossHP == 0){
                            break;
                        }
                        playSfx(dmgFx);

                        // Generate random Damage and create damage Text
                        int damageTaken=0;
                        int x = (int) event.getRawX()+140; //Hardcoded adjustment for mob position
                        int y = (int) event.getRawY()-180;

                        if (inRange(x,y)){
                            damageTaken = ThreadLocalRandom.current().nextInt(500000, 999999 + 1);
                        }
                        createDamageText(damageTaken, x, y);

                        FL.removeView(mob);
                        mob.setImageResource(slime_hurt);
                        FL.addView(mob,0);
                        decreaseHP(damageTaken);
                        break;
                    }
                    // Released
                    case MotionEvent.ACTION_UP: {
                        if (bossHP == 0){
                            break;
                        }
                        FL.removeView(mob);
                        mob.setImageResource(slime_move);
                        FL.addView(mob,0);
                        break;
                    }
                }
                return isAlive;
            }
        });
    }

    private void decreaseHP(int damageTaken){
        if (bossHP-damageTaken>0) {
            bossHP -= damageTaken;
        }else{
            bossHP = 0;
            screenInFrontOfMob.setOnTouchListener(null);
            isAlive = false;
            Activity lC = MainActivity.this;
            lC.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mob_death();
                }
            });
        }
        bossHP_text.setText(String.valueOf(bossHP)+"/"+String.valueOf(totalHP));
        int hp = (int)toPercentage(bossHP,totalHP);
        if(hp >= 66){
            HpBar.getProgressDrawable().setColorFilter(0xff00ff00,android.graphics.PorterDuff.Mode.MULTIPLY);
        }else if(hp < 66 && hp >= 33){
            HpBar.getProgressDrawable().setColorFilter(0xffffff00,android.graphics.PorterDuff.Mode.MULTIPLY);
        }else{
            HpBar.getProgressDrawable().setColorFilter(0xFFFF0000,android.graphics.PorterDuff.Mode.MULTIPLY);
        }

        HpBar.setProgress(hp);
        RL_hp.setVisibility(View.VISIBLE);
//        HpBar.setVisibility(View.VISIBLE);
//        bossHP_text.setVisibility(View.VISIBLE);
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
                RL_hp.setVisibility(View.INVISIBLE);
//                bossHP_text.setVisibility(View.INVISIBLE);
//                HpBar.setVisibility(View.INVISIBLE);
            }
        });
        RL_hp.startAnimation(fadeOut);
//        bossHP_text.startAnimation(fadeOut);
//        HpBar.startAnimation(fadeOut);
    }

    // Create Damage Text and sets the font, size, text, position
    private void createDamageText(int damageTaken, float x, float y){
        String damage = Integer.toString(damageTaken);
        final WeakReference<TextView> damageText = new WeakReference<TextView>(new TextView(getApplicationContext()));
        damageText.get().setLayoutParams(RL_lp);
        damageText.get().setSingleLine();
        RL.addView(damageText.get());

        damageText.get().setTypeface(comic_sans);
        damageText.get().setTextSize(dmgSize);
        Shader textShader=new LinearGradient(0, 0, 0, damageText.get().getPaint().getTextSize(),
                new int[]{dmgTop,dmgBottom},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        damageText.get().setShadowLayer(1.5f, 5.0f, 5.0f, Color.BLACK);

        damageText.get().getPaint().setShader(textShader);
        if(damageTaken>=900000){
            damageText.get().setTextSize(dmgSize);
            Shader critShader=new LinearGradient(0, 0, 0, damageText.get().getPaint().getTextSize(),
                    new int[]{critTop, critBottom},
                    new float[]{0, 1}, Shader.TileMode.CLAMP);
            damageText.get().getPaint().setShader(critShader);
        }else if (damageTaken<500000){
            damage = " MISS ";
        }
        damageText.get().setText(damage);

        // Position damageText to Click position
        damageText.get().setX(FL.getWidth()/2-350);
        damageText.get().setY(FL.getHeight()/2-50);
        damageText.get().animate().translationYBy(-300).alpha(0.15f).setDuration(1000).withEndAction(new Runnable(){
            public void run(){
                // rRemove the view from the parent layout
                RL.removeView(damageText.get());
            }
        });
    }
    private void mob_death() {
        playSfx(deathFx);
        mob.setImageResource(slime_death);
        mob_drawable = (GifDrawable) mob.getDrawable();
        mob_drawable.setLoopCount(1);
        mob_death = new AnimationListener() {
            @Override
            public void onAnimationCompleted(int loopNumber) {
                mob.setImageResource(no_mob);
            }

        };
        mob_drawable.removeAnimationListener(mob_move);
        mob_drawable.addAnimationListener(mob_death);
        updateExp();
        spawn_mob();
    }

    private void updateExp() {
        final TextView gainExp = new TextView(getApplicationContext());
        gainExp.setLayoutParams(RL_lp_exp);
        gainExp.setTextColor(getResources().getColor(R.color.levelColor));
        gainExp.setText(String.format(Locale.CANADA, "+%d EXP", amt_exp));
        RL.addView(gainExp);
        gainExp.animate().translationYBy(-40).alpha(1f).setDuration(3000).withEndAction(new Runnable(){
            public void run(){
                // Remove the view from the parent layout
                RL.removeView(gainExp);
            }
        });
        currentEXP+=amt_exp;
        exp_val_text.setText(String.valueOf(currentEXP));
        exp_percent_text.setText(String.format(Locale.CANADA," [%.2f%%]",toPercentage(currentEXP,totalEXP)));
        ExpBar.setProgress(currentEXP);
        if(currentEXP>=totalEXP){
            currentLevel++;
            currentEXP=0;
            ExpBar.setProgress(currentEXP);
            level_text.setText(String.format(Locale.CANADA,"%s %d",LV, currentLevel));
            amt_exp+=amt_exp;
            totalEXP*=3;
            exp_val_text.setText(String.valueOf(currentEXP));
            exp_percent_text.setText(String.format(Locale.CANADA," [%.2f%%]",toPercentage(currentEXP,totalEXP)));
        }
    }


    private void spawn_mob() {
        int spawnTime = ThreadLocalRandom.current().nextInt(5000, 9000+1);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Activity lB = MainActivity.this;
                lB.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "A wild Slime has appeared!", Toast.LENGTH_SHORT).show();
                        mob_start();

                    }
                });
            }
        }, spawnTime);
    }

    private void mob_start(){
        playSfx(spawnFx);
        mob.setImageResource(slime_spawn);
        mob_drawable = (GifDrawable) mob.getDrawable();
        mob_drawable.setLoopCount(1);
        mob_move = new AnimationListener() {
            @Override
            public void onAnimationCompleted(int loopNumber) {
                mob.setImageResource(slime_move);

            }
        };
        mob_drawable.removeAnimationListener(mob_death);
        mob_drawable.addAnimationListener(mob_move);

        bossHP=totalHP;
        isAlive=true;//Listens to screen being pressed
        startAttackListener();

    }

    /*
     * Helper functions
     */

    private float toPercentage(int val, int total){
        return (((float)val/(float)total)*100);
    }

    // Check whether player click is on the monster
    private Boolean inRange(int x, int y){
        return (x>(FL.getWidth()/2)-(mob.getWidth()/3) && x<(FL.getWidth()/2)+(mob.getWidth()/3))
                && (y>(FL.getHeight()/2)-(mob.getHeight()/3) && y<(FL.getHeight()/2)+(mob.getHeight()/3));
    }

}

