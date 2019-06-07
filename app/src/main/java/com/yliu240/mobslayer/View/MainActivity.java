package com.yliu240.mobslayer.View;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

import org.javatuples.Pair;
import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import com.google.gson.Gson;

import com.google.gson.GsonBuilder;
import com.yliu240.mobslayer.Controller.GameController;
import com.yliu240.mobslayer.R;

public class MainActivity extends AppCompatActivity {

    private int dmgTop, dmgBottom, critTop, critBottom, offsetX, offsetY;
    private ProgressBar HpBar, ExpBar;
    private Boolean isAlive = false, hit, sound_muted;
    private ImageButton sound, right_arrow, left_arrow, skill_0 = null;
    private Pair<SoundPool, Integer> hit_sound, spawn_sound, death_sound, sharp_eyes_effect, levelUp;
    private MediaPlayer bgm;
    private ImageView screenInFrontOfMob, mapView;

    // Get TextViews
    private TextView mobHP_text, level_text, exp_val_text, exp_percent_text;
    private GifImageView mobView, buffView, levelUpView;
    private GifDrawable mob_drawable, buff_drawable, levelUp_drawable;
    private AnimationListener mobDeath, mob_move, buff_effect, levelUp_effect;
    private FrameLayout FL;
    private FrameLayout.LayoutParams FL_lp, FL_lp_lvlup;
    private RelativeLayout RL, RL_hp;
    private RelativeLayout.LayoutParams RL_lp, RL_lp_exp, RL_lp_buff;
    private Typeface comic_sans;
    private GameController gcInstance;
    private Context mContext;
    private final int DAMAGE_SIZE = 60;
    private static final String TAG = "@@@@@@@@@@@@@@@DEBUG ";
    private static final String MISS = "  MISS  ";
    private static final String LV = "LV. ";
    private static final String RAW = "raw";
    private static final String DRAW = "drawable";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            isAlive = savedInstanceState.getBoolean("isAlive");
            int pos = savedInstanceState.getInt("position");
            bgm.seekTo(pos);
        }
        mContext = getApplicationContext();

        // Variables for Layouts, Views
        screenInFrontOfMob = findViewById(R.id.transparent);
        mobHP_text = findViewById(R.id.hp);
        mapView = findViewById(R.id.mapView);
        FL = findViewById(R.id.framelayout);
        FL_lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, // Width of TextView
                FrameLayout.LayoutParams.WRAP_CONTENT); // Height of TextView
        FL_lp.gravity = Gravity.CENTER;
        FL_lp_lvlup = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, // Width of TextView
                FrameLayout.LayoutParams.WRAP_CONTENT); // Height of TextView
        FL_lp_lvlup.gravity = Gravity.BOTTOM;
        RL = findViewById(R.id.relayout);
        RL_hp = findViewById(R.id.relayout_hp);
        RL_lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, // Width of TextView
                RelativeLayout.LayoutParams.WRAP_CONTENT); // Height of TextView
        RL_lp_buff = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, // Width of TextView
                RelativeLayout.LayoutParams.WRAP_CONTENT); // Height of TextView
        RL_lp_buff.addRule(RelativeLayout.CENTER_HORIZONTAL);
//        RL_lp_buff.addRule(RelativeLayout.BELOW, R.id.relayout_hp);
//        RL_lp_buff.addRule(RelativeLayout.ABOVE, R.id.bottom_bar);
//        RL_lp_buff.addRule(RelativeLayout.ALIGN_PARENT_START);
        RL_lp_exp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, // Width of TextView
                RelativeLayout.LayoutParams.WRAP_CONTENT); // Height of TextView
        RL_lp_exp.addRule(RelativeLayout.ABOVE, R.id.bottom_bar);


        // Buttons
        sound = findViewById(R.id.sound);
        left_arrow = findViewById(R.id.left_arrow);
        right_arrow = findViewById(R.id.right_arrow);
        sound_muted = Boolean.FALSE;

        // Variable for Buff effect and Skill buttons
        buffView = new GifImageView(mContext); // Could be refactored in SetGameProperties()
        buffView.setLayoutParams(RL_lp_buff);
        buffView.setImageResource(R.drawable.no_buff);
        RL.addView(buffView, 1);
        skill_0 = findViewById(R.id.skill_0);
        setSkillListener("sharp_eyes");
        levelUpView = new GifImageView(mContext); // Needs refactoring
        levelUpView.setLayoutParams(FL_lp_lvlup);
        levelUpView.setImageResource(R.drawable.no_buff);
        FL.addView(levelUpView,1);

        // Variables for damageText Colors
        dmgTop = ContextCompat.getColor(mContext, R.color.dmgTop);
        dmgBottom = ContextCompat.getColor(mContext, R.color.dmgBottom);
        critTop = ContextCompat.getColor(mContext, R.color.critTop);
        critBottom = ContextCompat.getColor(mContext, R.color.critBottom);
        comic_sans = Typeface.createFromAsset(getAssets(), "comic-sans-ms-bold.ttf");

        // Variables for HP bar, EXP bar and set values
        HpBar = findViewById(R.id.HpBar);
        ExpBar = findViewById(R.id.ExpBar);
        Drawable exp_drawable = ContextCompat.getDrawable(mContext, R.drawable.expbar_drawable);;
        ExpBar.setProgressDrawable(exp_drawable);

        level_text = findViewById(R.id.level);
        exp_val_text = findViewById(R.id.Exp_val);
        exp_percent_text = findViewById(R.id.Exp_percent);

        // Set TextViews
        mobHP_text.setTypeface(comic_sans);
        setGameProperties();
    }

    // Parse currentGameInfo.json file and set gameController instance
//    private void loadJson() {
//        Gson gson = new Gson();
//        AssetManager assetManager = getAssets();
//        try {
//            InputStream ims = assetManager.open("currentGameInfo.json");
//            Reader reader = new InputStreamReader(ims);
//            GameController instance = gson.fromJson(reader, GameController.class);
//            GameController.setInstance(instance);
//            gcInstance = GameController.getInstance();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void saveJson() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting().serializeNulls();
        Gson gson = builder.create();
        String newGameInfo = gson.toJson(gcInstance);
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput("currentGameInfo.json", Context.MODE_PRIVATE);
            outputStream.write(newGameInfo.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setGameProperties() {
        gcInstance = GameController.getInstance();

        // Set Map properties
        gcInstance.setCurrent_map();
        mapView.setImageResource(getResourceId(gcInstance.getCurrent_map().getBg_image(), DRAW));
        createBgm();

        // Set Mob Properties
        gcInstance.setCurrent_mob(gcInstance.getCurrent_mobId());
        setMobSoundAndOffsets();

        // Set HP and EXP bar
        HpBar.setMax(gcInstance.getCurrent_mob().getTotal_hp());
        HpBar.setProgress(gcInstance.getCurrent_mob().getCurrent_hp());
        ExpBar.setMax(gcInstance.getPlayer().getTotal_exp());
        ExpBar.setProgress(gcInstance.getPlayer().getExp());

        // Set Player HP & EXP
        level_text.setText(String.format(Locale.CANADA, "%s %d", LV, gcInstance.getPlayer().getLevel()));
        exp_val_text.setText(String.valueOf(gcInstance.getPlayer().getExp()));
        exp_percent_text.setText(String.format(Locale.CANADA, " [%.2f%%]", gcInstance.getPlayer().getEXP_percent()));

        // Create Mob
        mobHP_text.setText(gcInstance.getCurrent_mob().getHP_percent_string());
        mobView = new GifImageView(mContext);
        mobView.setLayoutParams(FL_lp);
        if(isAlive){
            mobView.setImageResource(getResourceId(gcInstance.getCurrent_mob().getMove(), DRAW));
            FL.addView(mobView, 0);
            hit = false;
            startAttackListener();
        }else{
            mobView.setImageResource(R.drawable.no_mob);
            FL.addView(mobView, 0);
            waitToSpawn();
        }
        mobView.setElevation(1);
    }


    @Override
    protected void onStart() {
        super.onStart();
        //Listens to sound Button if pressed
        startBgmListener();
        startArrowListener();
        if(sound_muted){
            bgm.pause();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        bgm.pause();
        sound.setOnClickListener(null);
        saveJson();
        System.gc();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        bgm.pause();
        sound.setOnClickListener(null);
        System.gc();
    }

    @Override
    protected void onResume() {
        super.onResume();

        gcInstance = GameController.getInstance();
        startBgmListener();
        if(!sound_muted){
           bgm.start();
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isAlive", true);
        outState.putInt("position", bgm.getCurrentPosition());
        bgm.pause();
        super.onSaveInstanceState(outState);
    }

    /*
     * Sound Functions
     */
    /**
     * Plays the sound effect
     * @param sfx Pair<SoundPool, Integer> of a sound effect
     */
    private void playSoundEffect(Pair<SoundPool, Integer> sfx) {
        sfx.getValue0().play(sfx.getValue1(), 1.0F, 1.0F, 0, 0, 1.0F);
    }

    /**
     *  Creates a SoundPool object for sound effect
     * @param name Name of sound effect
     * @return Returns a Pair<SoundPool, Integer> of the sound effect
     */
    private Pair<SoundPool, Integer> loadSound(String name) {
        int soundId = getResourceId(name, RAW);
        SoundPool soundPool;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = (new SoundPool.Builder()).setMaxStreams(2).build();
        } else {
            soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 5);
        }
        //Load the sound
        int sound_val = soundPool.load(mContext, soundId, 1);
        return new Pair<>(soundPool, sound_val);
    }

    public void createBgm(){
        if (bgm != null){
            bgm.pause();
        }
        bgm = MediaPlayer.create(mContext, getResourceId(gcInstance.getCurrent_map().getBgm_name(), "raw"));
        bgm.setLooping(true);
        if(sound_muted){
            return;
        }
        bgm.start();
    }

    private void setMobSoundAndOffsets(){
        hit_sound = loadSound(gcInstance.getCurrent_mob().getHit_sound());
        death_sound = loadSound(gcInstance.getCurrent_mob().getDeath_sound());
        spawn_sound = loadSound(gcInstance.getCurrent_mob().getSpawn_sound());
        offsetX = gcInstance.getCurrent_mob().getOffsetX();
        offsetY = gcInstance.getCurrent_mob().getOffsetY();
        sharp_eyes_effect = loadSound("sharp_eyes_effect"); //This is temporary
        levelUp = loadSound("level_up_effect"); //This is temporary
    }

    private void setBuffSound(){
        sharp_eyes_effect = loadSound("sharp_eyes_effect");
    }


    //Listeners
    private void setSkillListener(final String skill_name){
        skill_0.setOnTouchListener(new View.OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        if(!gcInstance.getPlayer().getBuffed()){
                            playSoundEffect(sharp_eyes_effect);
                            skill_0.setBackgroundResource(getResourceId(skill_name+"_disable", DRAW));
                            skill_0.invalidate();
                            RL.removeView(buffView);
                            buffView.setImageResource(getResourceId(skill_name, DRAW));
                            RL.addView(buffView, 1);
                            buff_drawable = (GifDrawable) buffView.getDrawable();
                            buff_drawable.setLoopCount(1);
                            buff_effect = new AnimationListener() {
                                @Override
                                public void onAnimationCompleted(int loopNumber) {
                                    buffView.setImageResource(getResourceId("no_buff", DRAW));
                                }
                            };
                            buff_drawable.removeAnimationListener(buff_effect);
                            buff_drawable.addAnimationListener(buff_effect);

                            createDamageText("+50% Crit", true);
                            gcInstance.getPlayer().setBuffed();
                            coolDown(skill_name);
                        }
                        //TODO: Inspect bug when player is already buffed
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        break;
                    }
                }
                return false;
            }
        });
    }

    public void coolDown(final String skill_name){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        skill_0.setBackgroundResource(getResourceId(skill_name+"_enable", DRAW));
                        skill_0.invalidate();
                    }
                });
            }
        }, 30000);
    }

    private void startBgmListener() {
        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sound_muted) {
                    sound.setImageResource(R.drawable.baseline_volume_off_24);
                    bgm.pause();
                    sound_muted=true;
                } else {
                    sound.setImageResource(R.drawable.baseline_volume_up_24);
                    bgm.start();
                    sound_muted=false;
                }
            }
        });
    }

    private void startArrowListener() {
        left_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gcInstance.switch_map(false);
//                Log.d(TAG, "Current Map: "+gcInstance.getCurrent_map().getBg_image());
//                Log.d(TAG, "Current BGM: "+gcInstance.getCurrent_map().getBgm_name());
                mapView.setImageResource(getResourceId(gcInstance.getCurrent_map().getBg_image(), DRAW));
                createBgm();
            }
        });
        right_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gcInstance.switch_map(true);
//                Log.d(TAG, "Current Map: "+gcInstance.getCurrent_map().getBg_image());
//                Log.d(TAG, "Current BGM: "+gcInstance.getCurrent_map().getBgm_name());
                mapView.setImageResource(getResourceId(gcInstance.getCurrent_map().getBg_image(), DRAW));
                createBgm();
            }
        });
    }

    /*
     * Mob Functions
     */
    @SuppressLint("ClickableViewAccessibility")
    private void startAttackListener() {

        screenInFrontOfMob.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {

                    // Pressed
                    case MotionEvent.ACTION_DOWN: {
                        if (gcInstance.getCurrent_mob().isDead()) {
                            break;
                        }
                        playSoundEffect(hit_sound);

                        // Generate random Damage and create damage Text
                        Pair<Integer, Boolean> damage = new Pair<>(0, false);
                        int x = (int) event.getRawX() + offsetX; //Hardcoded adjustment for mob position
                        int y = (int) event.getRawY() + offsetY;

                        if (inRange(x, y)) {
                            damage = gcInstance.attackMob(); //ThreadLocalRandom.current().nextInt(500000, 999999 + 1);
                        }

                        if (!createDamageText(padDamageText(damage.getValue0()),damage.getValue1()).equals(MISS)) {
                            FL.removeView(mobView);
                            mobView.setImageResource(getResourceId(gcInstance.getCurrent_mob().getHit(), DRAW));
                            FL.addView(mobView, 0);
                            hit = true;
                        }
//                        gcInstance.decreaseHP(damage);
                        updateHP();
                        break;
                    }
                    // Released
                    case MotionEvent.ACTION_UP: {
                        if (gcInstance.getCurrent_mob().isDead()) {
                            break;
                        } else if (hit) {
                            FL.removeView(mobView);
                            mobView.setImageResource(getResourceId(gcInstance.getCurrent_mob().getMove(), DRAW));
                            FL.addView(mobView, 0);
                            hit = false;
                        }
                        break;
                    }
                }
                return isAlive;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void updateHP() {
        if (gcInstance.getCurrent_mob().getCurrent_hp() == 0){
            screenInFrontOfMob.setOnTouchListener(null);
            isAlive = false;
            Activity lC = MainActivity.this;
            lC.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mobDeath();
                }
            });
        }
        mobHP_text.setText(gcInstance.getCurrent_mob().getHP_percent_string());
        float hp = gcInstance.getCurrent_mob().getHP_percent();
        if (hp >= 66) {
            HpBar.getProgressDrawable().setColorFilter(0xff00ff00, android.graphics.PorterDuff.Mode.MULTIPLY);
        } else if (hp < 66 && hp >= 33) {
            HpBar.getProgressDrawable().setColorFilter(0xffffff00, android.graphics.PorterDuff.Mode.MULTIPLY);
        } else {
            HpBar.getProgressDrawable().setColorFilter(0xFFFF0000, android.graphics.PorterDuff.Mode.MULTIPLY);
        }

        HpBar.setProgress(gcInstance.getCurrent_mob().getCurrent_hp());
        RL_hp.setVisibility(View.VISIBLE);
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setDuration(4000);
        fadeOut.setAnimationListener(new TranslateAnimation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                RL_hp.setVisibility(View.INVISIBLE);
            }
        });
        RL_hp.startAnimation(fadeOut);
    }

    // Create Damage Text and sets the font, size, text, position
    private String createDamageText(String damage, Boolean critical) {
        final TextView damageText = new TextView(getApplicationContext());
        damageText.setLayoutParams(RL_lp);
        damageText.setSingleLine();
        damageText.setTypeface(comic_sans);
        damageText.setTextSize(DAMAGE_SIZE);
        damageText.setText(damage);

        Shader shader;
        if (critical) {
            shader = new LinearGradient(0, 0, 0, damageText.getTextSize(),
                    critTop, critBottom, Shader.TileMode.CLAMP);
            damageText.setShadowLayer(0.01f, -2, 2, critTop);
        } else {
            shader = new LinearGradient(0, 0, 0, damageText.getTextSize(),
                    dmgTop, dmgBottom, Shader.TileMode.CLAMP);
            damageText.setShadowLayer(0.01f, -2, 2, dmgTop);
        }
        damageText.getPaint().setShader(shader);
        damageText.setTextColor(damageText.getTextColors().withAlpha(255));

        // Position damageText to Click position
        damageText.measure(0,0);
        damageText.setX((int)(FL.getWidth()/2) - (int)(damageText.getMeasuredWidth()/2));
        damageText.setY((int)(FL.getHeight()/2) - (damageText.getMeasuredHeight()));

        RL.addView(damageText);
        damageText.animate().translationYBy(-400).alpha(0.2f).setDuration(1000).withEndAction(new Runnable() {
            public void run() {
                // Remove the view from the parent layout
                RL.removeView(damageText);
            }
        });
        return damage;
    }

    private String padDamageText(int dmg) {
        if (dmg == 0) {
            return MISS;
        }
        StringBuffer sb = new StringBuffer();
        int length = (int) (Math.log10(dmg) + 1);
        if (length == 6){
            sb.append(" ");
            sb.append(dmg);
            sb.append(" ");
        }else if(length == 5){
            sb.append("  ");
            sb.append(dmg);
            sb.append("  ");
        }else if(length == 4) {
            sb.append("   ");
            sb.append(dmg);
            sb.append("   ");
        }else if(length == 3) {
            sb.append("    ");
            sb.append(dmg);
            sb.append("    ");
        }else if(length == 2) {
            sb.append("    ");
            sb.append(dmg);
            sb.append("    ");
        }else if(length == 1) {
            sb.append("    ");
            sb.append(dmg);
            sb.append("     ");
        }
        return sb.toString();
    }

    private void mobDeath() {
        playSoundEffect(death_sound);
        mobView.setImageResource(getResourceId(gcInstance.getCurrent_mob().getDeath(), DRAW));
        mob_drawable = (GifDrawable) mobView.getDrawable();
        mob_drawable.setLoopCount(1);
        mobDeath = new AnimationListener() {
            @Override
            public void onAnimationCompleted(int loopNumber) {
                mobView.setImageResource(getResourceId("no_mob", DRAW));
            }

        };
        mob_drawable.removeAnimationListener(mob_move);
        mob_drawable.addAnimationListener(mobDeath);
        updateExp();
        waitToSpawn();
    }

    private void updateExp() {
        final TextView mob_exp = new TextView(mContext);
        mob_exp.setLayoutParams(RL_lp_exp);
        mob_exp.setTextColor(ContextCompat.getColor(mContext, R.color.GainExpColor));
        mob_exp.setText(String.format(Locale.CANADA, " +%d EXP", gcInstance.getCurrent_mob().getExp()));
        RL.addView(mob_exp);
        mob_exp.animate().translationYBy(-50).alpha(1f).alpha(0.15f).setDuration(3500).withEndAction(new Runnable() {
            public void run() {
                // Remove the view from the parent layout
                RL.removeView(mob_exp);
            }
        });
        Boolean level_Up = gcInstance.gainEXP();
        if(level_Up){
            levelUp();
            ExpBar.setMax(gcInstance.getPlayer().getTotal_exp());
            level_text.setText(String.format(Locale.CANADA, "%s %d", LV, gcInstance.getPlayer().getLevel()));
        }
        ExpBar.setProgress(gcInstance.getPlayer().getExp());
        exp_val_text.setText(gcInstance.getPlayer().getEXP_toString());
        exp_percent_text.setText(String.format(Locale.CANADA, " [%.2f%%]", gcInstance.getPlayer().getEXP_percent()));
    }

    private void levelUp(){
        playSoundEffect(levelUp);
        RL.removeView(buffView);
        buffView.setImageResource(getResourceId("level_up", DRAW));
        RL.addView(buffView, 1);
        buff_drawable = (GifDrawable) buffView.getDrawable();
        buff_drawable.setLoopCount(1);
        buff_effect = new AnimationListener() {
            @Override
            public void onAnimationCompleted(int loopNumber) {
                buffView.setImageResource(getResourceId("no_buff", DRAW));
            }
        };
        buff_drawable.removeAnimationListener(buff_effect);
        buff_drawable.addAnimationListener(buff_effect);
//        FL.removeView(levelUpView);
//        levelUpView.setImageResource(getResourceId("level_up", DRAW));
//        FL.addView(levelUpView, 1);
//        levelUpView.bringToFront();
//        levelUp_drawable = (GifDrawable) levelUpView.getDrawable();
//        levelUp_drawable.setLoopCount(1);
//        levelUp_effect = new AnimationListener() {
//            @Override
//            public void onAnimationCompleted(int loopNumber) {
//                levelUpView.setImageResource(getResourceId("no_buff", DRAW));
//            }
//        };
//        levelUp_drawable.removeAnimationListener(levelUp_effect);
//        levelUp_drawable.addAnimationListener(levelUp_effect);
    }
    private void waitToSpawn() {
//        int spawnTime = ThreadLocalRandom.current().nextInt(1500, 2000 + 1);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Activity lB = MainActivity.this;
                lB.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        Toast.makeText(mContext, String.format(Locale.CANADA, "A wild %s has appeared!",
//                                gcInstance.getCurrent_mob().getName()), Toast.LENGTH_SHORT).show();
                        spawnMob();
                    }
                });
            }
        }, 2500);
    }

    private void spawnMob() {
        gcInstance.getCurrent_mob().resetHP();
        playSoundEffect(spawn_sound);
        mobView.setImageResource(getResourceId(gcInstance.getCurrent_mob().getSpawn(), DRAW));
        mob_drawable = (GifDrawable) mobView.getDrawable();
        mob_drawable.setLoopCount(1);
        mob_move = new AnimationListener() {
            @Override
            public void onAnimationCompleted(int loopNumber) {
                mobView.setImageResource(getResourceId(gcInstance.getCurrent_mob().getMove(), DRAW));
            }
        };
        mob_drawable.removeAnimationListener(mobDeath);
        mob_drawable.addAnimationListener(mob_move);

        isAlive = true;
        //Listens to screen being pressed
        hit = false;
        startAttackListener();
    }

    /*
     * Helper functions
     */
    //

    /**
     * Check whether the clicked position is on the monster or not
     * @param x x-coordinate
     * @param y y-coordinate
     * @return Returns a boolean of whether the clicked point is on the monster or not
     */
    private Boolean inRange(int x, int y) {
        return (x > (FL.getWidth() / 2) - (mobView.getWidth() / 3) && x < (FL.getWidth() / 2) + (mobView.getWidth() / 3))
                && (y > (FL.getHeight() / 2) - (mobView.getHeight() / 3) && y < (FL.getHeight() / 2) + (mobView.getHeight() / 3));
    }

    /**
     * Gets the resource Id
     * @param name Name of resource
     * @param type Type of resource (e.g. drawable, raw)
     * @return Returns id of resource
     */
    private int getResourceId(String name, String type){
        return getResources().getIdentifier(name, type, getPackageName());
    }
}

