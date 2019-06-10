package com.yliu240.mobslayer.View;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
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
import android.widget.TableRow;
import android.widget.TextView;

import org.javatuples.Pair;
import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import java.io.FileOutputStream;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.yliu240.mobslayer.Controller.GameController;
import com.yliu240.mobslayer.Model.Player;
import com.yliu240.mobslayer.Model.Skill;
import com.yliu240.mobslayer.View.R;

public class MainActivity extends AppCompatActivity {

    private int dmgTop, dmgBottom, critTop, critBottom, skillTop, skillBottom;
    private ProgressBar HpBar, ExpBar;
    private Boolean isAlive = false, hit, sound_muted;
    private ImageButton sound;
    private ImageButton right_arrow;
    private ImageButton left_arrow;
    private Pair<SoundPool, Integer> hit_sound, spawn_sound, death_sound, levelUp;
    public MediaPlayer bgm;
    private ImageView screenInFrontOfMob, mapView;
    private TextView mobHP_text, level_text, exp_val_text, exp_percent_text;
    private GifImageView mobView;
    private GifDrawable mob_drawable;
    private AnimationListener mobDeath;
    private AnimationListener mob_move;
    private FrameLayout FL;
    private FrameLayout.LayoutParams mob_layout;
    private FrameLayout.LayoutParams level_up_layout;
    private RelativeLayout RL, mob_hp;
    private RelativeLayout.LayoutParams skill_layout;
    private RelativeLayout.LayoutParams text_layout;
    private RelativeLayout.LayoutParams exp_layout;
    private Typeface comic_sans;
    private GameController gc;
    private Context mContext;
    private int DAMAGE_SIZE = 60;
    private int HIT_SIZE = 350;
    private int[] atk_fx = new int[]{R.drawable.b_atk, R.drawable.c_atk};
    private static final String DEBUG = "[DEBUG] @@@@@@@@@@@:";
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
        }
        mContext = getApplicationContext();

        // Variables for Layouts, Views
        screenInFrontOfMob = findViewById(R.id.transparent);
        mobHP_text = findViewById(R.id.hp);
        mapView = findViewById(R.id.mapView);
        FL = findViewById(R.id.framelayout);
        RL = findViewById(R.id.relayout);
        mob_hp = findViewById(R.id.mob_hp);

        int frame_layout_wrap = FrameLayout.LayoutParams.WRAP_CONTENT;
        int rel_layout_wrap = RelativeLayout.LayoutParams.WRAP_CONTENT;
        mob_layout = new FrameLayout.LayoutParams(frame_layout_wrap, frame_layout_wrap, Gravity.CENTER);
        level_up_layout = new FrameLayout.LayoutParams(frame_layout_wrap, frame_layout_wrap, Gravity.BOTTOM);
        text_layout = new RelativeLayout.LayoutParams(rel_layout_wrap, rel_layout_wrap);
        skill_layout = new RelativeLayout.LayoutParams(rel_layout_wrap, rel_layout_wrap);
        skill_layout.addRule(RelativeLayout.CENTER_HORIZONTAL);
        exp_layout = new RelativeLayout.LayoutParams(rel_layout_wrap, rel_layout_wrap);
        exp_layout.addRule(RelativeLayout.ABOVE, R.id.bottom_bar);

        // Buttons
        sound = findViewById(R.id.sound);
        left_arrow = findViewById(R.id.left_arrow);
        right_arrow = findViewById(R.id.right_arrow);
        sound_muted = Boolean.FALSE;

        // Variables for newText Colors
        dmgTop = ContextCompat.getColor(mContext, R.color.dmgTop);
        dmgBottom = ContextCompat.getColor(mContext, R.color.dmgBottom);
        critTop = ContextCompat.getColor(mContext, R.color.critTop);
        critBottom = ContextCompat.getColor(mContext, R.color.critBottom);
        skillTop = ContextCompat.getColor(mContext, R.color.skillTop);
        skillBottom = ContextCompat.getColor(mContext, R.color.skillBottom);
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

    private void saveJson() {
        ExclusionStrategy strategy = new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes field) {
                return field.getDeclaringClass() == Player.class && field.getName().equals("buffed");
            }
            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        };
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting().serializeNulls();
        Gson gson = builder.addSerializationExclusionStrategy(strategy).create();
        String newGameInfo = gson.toJson(gc);
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
        gc = GameController.getInstance();

        // Set Map properties
        gc.setCurrent_map();
        mapView.setImageResource(getResourceId(gc.getCurrent_map().getBg_image(), DRAW));
        createBgm();

        // Set Mob Properties
        gc.setCurrent_mob(gc.getCurrent_mobId());
        setSoundEffects();

        // Set HP and EXP bar
        HpBar.setMax(gc.getCurrent_mob().getTotal_hp());
        HpBar.setProgress(gc.getCurrent_mob().getCurrent_hp());
        ExpBar.setMax(gc.getPlayer().getTotal_exp());
        ExpBar.setProgress(gc.getPlayer().getExp());

        // Set Player HP & EXP
        level_text.setText(String.format(Locale.CANADA, "%s %d", LV, gc.getPlayer().getLevel()));
        exp_val_text.setText(String.valueOf(gc.getPlayer().getExp()));
        exp_percent_text.setText(String.format(Locale.CANADA, " [%.2f%%]", gc.getPlayer().getEXP_percent()));

        // Set Skills
        ImageButton skill_0 = findViewById(R.id.skill_0);
        setSkillListener(skill_0,0);

        // Create Mob
        mobHP_text.setText(gc.getCurrent_mob().getHP_percent_string());
        mobView = new GifImageView(mContext);
        mobView.setLayoutParams(mob_layout);
        mobView.requestLayout();
        mobView.getLayoutParams().height = gc.getCurrent_mob().getHeight();
        mobView.getLayoutParams().width = gc.getCurrent_mob().getWidth();
        if(isAlive){
            mobView.setImageResource(getResourceId(gc.getCurrent_mob().getMove(), DRAW));
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
        saveJson();
        System.gc();
    }
    @Override
    protected void onResume() {
        super.onResume();

        gc = GameController.getInstance();
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
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        int pos = savedInstanceState.getInt("position");
        bgm.seekTo(pos);
        super.onRestoreInstanceState(savedInstanceState);
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
        bgm = MediaPlayer.create(mContext, getResourceId(gc.getCurrent_map().getBgm_name(), "raw"));
        bgm.setLooping(true);
        if(sound_muted){
            return;
        }
        bgm.start();
    }

    private void setSoundEffects(){
        hit_sound = loadSound(gc.getCurrent_mob().getHit_sound());
        death_sound = loadSound(gc.getCurrent_mob().getDeath_sound());
        levelUp = loadSound("level_up_effect"); //This is temporary
    }

    //Listeners
    @SuppressLint("ClickableViewAccessibility")
    private void setSkillListener(final ImageView iv, int i){
        Skill skill = gc.getSkill(i);
        final String skill_name = skill.getName();
        final String message = skill.getMessage();
        final Pair<SoundPool, Integer> s_sfx = loadSound(skill.getSound_effect());
        final int s_width = skill.getWidth();
        final int s_height = skill.getHeight();
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!gc.getPlayer().getBuffed()){
                    playSoundEffect(s_sfx);
                    iv.setBackgroundResource(getResourceId(skill_name+"_disable", DRAW));
                    iv.invalidate();

                    ImageView skill_img = new ImageView(getApplicationContext());
                    skill_img.setBackgroundResource(R.drawable.sharp_eyes);
                    skill_img.setLayoutParams(skill_layout);
                    skill_img.requestLayout();
                    skill_img.getLayoutParams().height = s_height;
                    skill_img.getLayoutParams().width = s_width;
                    skill_img.setElevation(3);
                    RL.addView(skill_img);
                    AnimationDrawable skill_anim = (AnimationDrawable) skill_img.getBackground();
                    skill_anim.start();
                    checkIfAnimationDone(skill_anim, skill_img, RL);

                    createText(message, "skill", 0, 0);
                    gc.getPlayer().setSkillCoolDown();
                    coolDown(iv, skill_name);
                }
            }
        });
    }

    public void coolDown(final ImageView iv, final String skill_name){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        iv.setBackgroundResource(getResourceId(skill_name+"_enable", DRAW));
                        iv.invalidate();
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
                gc.switch_map(false);
//                Log.d(DEBUG, "Current Map: "+gc.getCurrent_map().getBg_image());
//                Log.d(DEBUG, "Current BGM: "+gc.getCurrent_map().getBgm_name());
                mapView.setImageResource(getResourceId(gc.getCurrent_map().getBg_image(), DRAW));
                createBgm();
            }
        });
        right_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gc.switch_map(true);
//                Log.d(DEBUG, "Current Map: "+gc.getCurrent_map().getBg_image());
//                Log.d(DEBUG, "Current BGM: "+gc.getCurrent_map().getBgm_name());
                mapView.setImageResource(getResourceId(gc.getCurrent_map().getBg_image(), DRAW));
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
                        if (gc.getCurrent_mob().isDead()) {
                            break;
                        }
                        playSoundEffect(hit_sound);

                        // Generate random Damage and create damage Text
                        Pair<Integer, Boolean> damage = new Pair<>(0, false);
                        int x = (int) event.getRawX();
                        int y = (int) event.getRawY();
                        Rect mob_position = getLocationOnScreen(mobView);

                        if (mob_position.contains(x,y)) {
                            damage = gc.attackMob(); //ThreadLocalRandom.current().nextInt(500000, 999999 + 1);
                            FL.removeView(mobView);
                            mobView.setImageResource(getResourceId(gc.getCurrent_mob().getHit(), DRAW));
                            FL.addView(mobView, 0);
                            hit = true;
                        }
                        String type = "";
                        if (damage.getValue1()){
                            type = "critical";
                        }
                        createText(padText(damage.getValue0()), type, x, y);
                        drawDmg((int) event.getX(), (int) event.getY(), damage.getValue1());
                        updateHP();
                        break;
                    }
                    // Released
                    case MotionEvent.ACTION_UP: {
                        if (gc.getCurrent_mob().isDead()) {
                            break;
                        } else if (hit) {
                            FL.removeView(mobView);

                            mobView.setImageResource(getResourceId(gc.getCurrent_mob().getMove(), DRAW));
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
        if (gc.getCurrent_mob().getCurrent_hp() == 0){
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
        mobHP_text.setText(gc.getCurrent_mob().getHP_percent_string());
        float hp = gc.getCurrent_mob().getHP_percent();
        if (hp >= 66) {
            HpBar.getProgressDrawable().setColorFilter(0xff00ff00, android.graphics.PorterDuff.Mode.MULTIPLY);
        } else if (hp < 66 && hp >= 33) {
            HpBar.getProgressDrawable().setColorFilter(0xffffff00, android.graphics.PorterDuff.Mode.MULTIPLY);
        } else {
            HpBar.getProgressDrawable().setColorFilter(0xFFFF0000, android.graphics.PorterDuff.Mode.MULTIPLY);
        }

        HpBar.setProgress(gc.getCurrent_mob().getCurrent_hp());
        mob_hp.setVisibility(View.VISIBLE);
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
                mob_hp.setVisibility(View.INVISIBLE);
            }
        });
        mob_hp.startAnimation(fadeOut);
    }

    // Create Damage Text and sets the font, size, text, position
    private void createText(String message, String type, int x, int y) {
        final TextView newText = new TextView(getApplicationContext());
        newText.setLayoutParams(text_layout);
        newText.setSingleLine();
        newText.setTypeface(comic_sans);
        newText.setTextSize(DAMAGE_SIZE);
        newText.setText(message);

        Shader shader;
        switch(type){
            case "critical":
                shader = new LinearGradient(0, 0, 0, newText.getTextSize(),
                        critTop, critBottom, Shader.TileMode.CLAMP);
                newText.setShadowLayer(0.01f, -2, 2, critTop);
                break;
            case "skill":
                shader = new LinearGradient(0, 0, 0, newText.getTextSize(),
                        skillTop, skillBottom, Shader.TileMode.CLAMP);
                newText.setShadowLayer(0.01f, -2, 2, dmgTop);
                break;
            default:
                shader = new LinearGradient(0, 0, 0, newText.getTextSize(),
                        dmgTop, dmgBottom, Shader.TileMode.CLAMP);
                newText.setShadowLayer(0.01f, -2, 2, dmgTop);
                break;
        }
        newText.getPaint().setShader(shader);
        newText.setTextColor(newText.getTextColors().withAlpha(255));

        // Position newText to Click position
        newText.measure(0,0);
//        Log.d(DEBUG, String.format("width: %d, height: %d ",newText.getMeasuredWidth(), newText.getMeasuredHeight()));
        int w = newText.getMeasuredWidth();
        int h = newText.getMeasuredHeight();
        if(x==0 && y==0){
            newText.setX((int)(FL.getWidth()/2) - (int)(w/2));
            newText.setY((int)(FL.getHeight()/2) - (h));
        }else{
            newText.setX(x-w/2);
            newText.setY(y-h-100);
        }

        RL.addView(newText);
        newText.animate().translationYBy(-400).alpha(0.2f).setDuration(1000).withEndAction(new Runnable() {
            public void run() {
                // Remove the view from the parent layout
                RL.removeView(newText);
            }
        });
    }

    private void drawDmg(int x, int y, boolean isCrit){
        ImageView dmg = new ImageView(getApplicationContext());
        int i = ThreadLocalRandom.current().nextInt(0, atk_fx.length);
        dmg.setBackgroundResource(atk_fx[i]);
        if(isCrit){
            dmg.setBackgroundResource(R.drawable.a_atk);
        }
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(HIT_SIZE, HIT_SIZE);
        dmg.setLayoutParams(layoutParams);
        FL.addView(dmg);
        dmg.setElevation(3);
        dmg.setX(x-HIT_SIZE/2);
        dmg.setY(y-HIT_SIZE/2);
        AnimationDrawable atkAnimation = (AnimationDrawable) dmg.getBackground();
        atkAnimation.start();
        checkIfAnimationDone(atkAnimation, dmg, FL);
    }

    private void checkIfAnimationDone(AnimationDrawable anim, final ImageView iv, final ViewGroup parent_layout){
        final AnimationDrawable a = anim;
        // Make the other variables final here
        int timeBetweenChecks = 100;
        Handler h = new Handler();
        h.postDelayed(new Runnable(){
            public void run(){
                if (a.getCurrent() != a.getFrame(a.getNumberOfFrames() - 1)){
                    checkIfAnimationDone(a, iv, parent_layout);
                } else{
                    parent_layout.removeView(iv);
                }
            }
        }, timeBetweenChecks);
    }

    private String padText(int dmg) {
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
        mobView.setImageResource(getResourceId(gc.getCurrent_mob().getDeath(), DRAW));
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
        mob_exp.setLayoutParams(exp_layout);
        mob_exp.setTextColor(ContextCompat.getColor(mContext, R.color.GainExpColor));
        mob_exp.setText(String.format(Locale.CANADA, " +%d EXP", gc.getCurrent_mob().getExp()));
        RL.addView(mob_exp);
        mob_exp.animate().translationYBy(-50).alpha(1f).alpha(0.15f).setDuration(3500).withEndAction(new Runnable() {
            public void run() {
                // Remove the view from the parent layout
                RL.removeView(mob_exp);
            }
        });
        Boolean level_Up = gc.gainEXP();
        if(level_Up){
            levelUp();
            ExpBar.setMax(gc.getPlayer().getTotal_exp());
            level_text.setText(String.format(Locale.CANADA, "%s %d", LV, gc.getPlayer().getLevel()));
        }
        ExpBar.setProgress(gc.getPlayer().getExp());
        exp_val_text.setText(gc.getPlayer().getEXP_toString());
        exp_percent_text.setText(String.format(Locale.CANADA, " [%.2f%%]", gc.getPlayer().getEXP_percent()));
    }

    private void levelUp(){
        playSoundEffect(levelUp);
        ImageView lvlup_img = new ImageView(getApplicationContext());
        lvlup_img.setBackgroundResource(R.drawable.level_up);
        lvlup_img.setLayoutParams(level_up_layout);
        FL.addView(lvlup_img,0);
        lvlup_img.setElevation(3000);
        lvlup_img.setX(-100);
        AnimationDrawable lvlup_anim = (AnimationDrawable) lvlup_img.getBackground();
        lvlup_anim.start();
        checkIfAnimationDone(lvlup_anim, lvlup_img, FL);
        gc.setCurrent_mob(gc.getCurrent_mobId());
    }

    private void waitToSpawn() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Activity lB = MainActivity.this;
                lB.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        spawnMob();
                    }
                });
            }
        }, 1000);
    }

    private void spawnMob() {
        gc.getCurrent_mob().resetHP();
        mobView.setImageResource(getResourceId(gc.getCurrent_mob().getMove(), DRAW));
        isAlive = true;
        hit = false;
        startAttackListener();
    }

    /*
     * Helper functions
     */
    private Rect getLocationOnScreen(View mView) {
        Rect mRect = new Rect();
        int[] location = new int[2];

        mView.getLocationOnScreen(location);

        mRect.left = location[0];
        mRect.top = location[1];
        mRect.right = location[0] + mView.getWidth();
        mRect.bottom = location[1] + mView.getHeight();

        return mRect;
    }

    /**
     * Gets the resource Id
     * @param name Name of resource
     * @param type Type of resource (e.g. drawable, raw)
     * @return Returns id of resource
     */
    private int getResourceId(String name, String type) {
        return getResources().getIdentifier(name, type, getPackageName());
    }
}

