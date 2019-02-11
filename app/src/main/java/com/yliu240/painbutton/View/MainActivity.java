package com.yliu240.painbutton.View;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
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

import com.google.gson.Gson;
import com.yliu240.painbutton.Controller.GameController;
import com.yliu240.painbutton.R;

import org.javatuples.Pair;

import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity {

    private int dmgTop, dmgBottom, critTop, critBottom;
    private int mobHP;
    private int totalHP = 50000000;
    private int totalEXP = 1000;
    private int currentEXP = 0;
    private int amt_exp = 850;
    private int currentLevel = 1;
    private int attack; //How much can the character hit
    private ProgressBar HpBar, ExpBar;
    private Boolean isAlive;
    private Boolean hit = false;
    private ImageButton sound = null;
    private MediaPlayer bgm;
    private Pair<SoundPool, Integer> hit_sound, spawn_sound, death_sound;
    private ImageView screenInFrontOfMob, bg_img;
    private TextView mobHP_text, level_text, exp_val_text, exp_percent_text;
    private GifImageView mobView;
    private GifDrawable mob_drawable;
    private AnimationListener mob_death, mob_move;
    private FrameLayout FL;
    private FrameLayout.LayoutParams FL_lp;
    private RelativeLayout RL, RL_hp;
    private RelativeLayout.LayoutParams RL_lp, RL_lp_exp;
    private Typeface comic_sans;
    final private int dmgSize = 50;
    private static final String TAG = "@@@@@@@@@@@@@@@DEBUG ";
    private static final String MISS = "  MISS";
    private static final String LV = "LV. ";
    private static final String RAW = "raw";
    private static final String DRAW = "drawable";
    private GameController gcInstance;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();

        // Variables for Layouts, Views
        screenInFrontOfMob = (ImageView) findViewById(R.id.transparent);
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
        RL_lp_exp.addRule(RelativeLayout.ABOVE, R.id.bottom_bar);
        RL_hp = (RelativeLayout) findViewById(R.id.relayout_hp);

        // Variable for Sound button for 'Mute' & 'Sound On'
        sound = (ImageButton) findViewById(R.id.sound);

        // Variables for damageText Colors
        dmgTop = ContextCompat.getColor(mContext, R.color.dmgTop);
        dmgBottom = ContextCompat.getColor(mContext, R.color.dmgBottom);
        critTop = ContextCompat.getColor(mContext, R.color.critTop);
        critBottom = ContextCompat.getColor(mContext, R.color.critBottom);
        comic_sans = Typeface.createFromAsset(getAssets(), "comic-sans-ms-bold.ttf");

        // Variables for HP bar, EXP bar and set values
        HpBar = (ProgressBar) findViewById(R.id.HpBar);
//        HpBar.setMax(100);
//        HpBar.setProgress(100);
        ExpBar = (ProgressBar) findViewById(R.id.ExpBar);
//        ExpBar.setMax(totalEXP);
//        ExpBar.setProgress(currentEXP);
        Drawable exp_drawable = ContextCompat.getDrawable(mContext, R.drawable.expbar_drawable);;
        ExpBar.setProgressDrawable(exp_drawable);
//        mobHP = totalHP;

        // Get TextViews
        mobHP_text = (TextView) findViewById(R.id.hp);
        level_text = (TextView) findViewById(R.id.level);
        exp_val_text = (TextView) findViewById(R.id.Exp_val);
        exp_percent_text = (TextView) findViewById(R.id.Exp_percent);

        // Set TextViews
        mobHP_text.setTypeface(comic_sans);
//        mobHP_text.setText(String.valueOf(mobHP) + "/" + String.valueOf(totalHP));
//        level_text.setText(String.format(Locale.CANADA, "%s %d", LV, currentLevel));
//        exp_val_text.setText(String.valueOf(currentEXP));
//        exp_percent_text.setText(String.format(Locale.CANADA, " [%.2f%%]", toPercentage(currentEXP, totalEXP)));
        setGameProperties();
    }

    // Parse gameInfo.json file and set gameController instance
    private void loadJson() {
        Gson gson = new Gson();
        AssetManager assetManager = getAssets();
        try {
            InputStream ims = assetManager.open("gameInfo.json");
            Reader reader = new InputStreamReader(ims);
            gcInstance = gson.fromJson(reader, GameController.class);
            GameController.setInstance(gcInstance);
            gcInstance.setCurrent_map(gcInstance.getCurrent_mapId());
            gcInstance.setCurrent_mob(gcInstance.getCurrent_mobId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setGameProperties() {
        loadJson();

        // Set Background picture
        FL.setBackgroundResource(get_drawable_id(gcInstance.getCurrent_map().getBg_image(), DRAW));

        // Set Background music
        set_mob_sound();
        bgm = MediaPlayer.create(mContext, get_drawable_id(gcInstance.getCurrent_map().getBgm_name(), RAW));
        bgm.start();
        bgm.setLooping(true);

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
        mobView= new GifImageView(mContext);
        mobView.setLayoutParams(FL_lp);
        mobView.setImageResource(R.drawable.no_mob);
        FL.addView(mobView, 0);
        wait_to_spawn();
    }


    @Override
    protected void onStart() {
        super.onStart();
//        setGameProperties();
        //Listens to sound Button if pressed
        startBgmListener();
    }

    @Override
    protected void onPause() {
        super.onPause();

        bgm.pause();
        sound.setOnClickListener(null);
        System.gc();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        bgm.stop();
        sound.setOnClickListener(null);
        System.gc();
    }

    @Override
    protected void onResume() {
        super.onResume();

        gcInstance = GameController.getInstance();
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
     * Sound Functions
     */
    private void play_Sound_Effect(Pair<SoundPool, Integer> sfx) {
        sfx.getValue0().play(sfx.getValue1(), 1.0F, 1.0F, 0, 0, 1.0F);
    }

    private Pair<SoundPool, Integer> load_Sound(String name) {
        int soundId = get_drawable_id(name, RAW);

        SoundPool soundPool;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            soundPool = (new SoundPool.Builder()).setMaxStreams(2).build();
        } else {
            soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 5);
        }
        //Load the sound
        int sound_val = soundPool.load(mContext, soundId, 1);
//        soundPool.play(sound_val, 1.0F, 1.0F, 0, 0, 1.0F);
        Pair<SoundPool, Integer> sfx = new Pair<SoundPool, Integer>(soundPool, sound_val);

        return sfx;
    }

    private void set_mob_sound(){
        hit_sound = load_Sound(gcInstance.getCurrent_mob().getHit_sound());
        death_sound = load_Sound(gcInstance.getCurrent_mob().getDeath_sound());
        spawn_sound = load_Sound(gcInstance.getCurrent_mob().getSpawn_sound());
    }

    private void startBgmListener() {
        Boolean clicked = new Boolean(false);
        sound.setTag(clicked); // Button isn't clicked on default
        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg;
                if (((Boolean) sound.getTag()) == false) {
                    sound.setImageResource(R.drawable.baseline_volume_off_24);
                    bgm.pause();
                    msg = "Mute";
                    sound.setTag(new Boolean(true));
                } else {
                    sound.setImageResource(R.drawable.baseline_volume_up_24);
                    bgm.start();
                    msg = "Sound On";
                    sound.setTag(new Boolean(false));
                }
//                final Toast toast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
//                toast.show();
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        toast.cancel();
//                    }
//                }, 1000);
            }
        });
    }


    /*
     * Mob Functions
     */
    private void startAttackListener() {

        screenInFrontOfMob.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {

                    // Pressed
                    case MotionEvent.ACTION_DOWN: {
                        if (mobHP == 0) {
                            break;
                        }
                        play_Sound_Effect(hit_sound);

                        // Generate random Damage and create damage Text
                        int damage = 0;
                        int x = (int) event.getRawX() + 140; //Hardcoded adjustment for mob position
                        int y = (int) event.getRawY() - 180;

                        if (inRange(x, y)) {
                            damage = ThreadLocalRandom.current().nextInt(500000, 999999 + 1);
                        }

                        if (!createDamageText(pad_damage(damage)).equals(MISS)) {
                            FL.removeView(mobView);
                            mobView.setImageResource(get_drawable_id(gcInstance.getCurrent_mob().getHit(), DRAW));
                            FL.addView(mobView, 0);
                            hit = true;
                        }
                        gcInstance.decreaseHP(damage);
                        updateHP();
                        break;
                    }
                    // Released
                    case MotionEvent.ACTION_UP: {
                        if (mobHP == 0) {
                            break;
                        } else if (hit) {
                            FL.removeView(mobView);
                            mobView.setImageResource(get_drawable_id(gcInstance.getCurrent_mob().getMove(), DRAW));
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

    private void updateHP() {
        if (gcInstance.getCurrent_mob().getCurrent_hp() == 0){
            screenInFrontOfMob.setOnTouchListener(null);
            isAlive = false;
            Activity lC = MainActivity.this;
            lC.runOnUiThread(new Runnable() {
//            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mob_death();
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
//        HpBar.setVisibility(View.VISIBLE);
//        mobHP_text.setVisibility(View.VISIBLE);
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
//                mobHP_text.setVisibility(View.INVISIBLE);
//                HpBar.setVisibility(View.INVISIBLE);
            }
        });
        RL_hp.startAnimation(fadeOut);
//        mobHP_text.startAnimation(fadeOut);
//        HpBar.startAnimation(fadeOut);
    }

    // Create Damage Text and sets the font, size, text, position
    private String createDamageText(String damage) {
        final WeakReference<TextView> damageText = new WeakReference<>(new TextView(mContext));
        damageText.get().setLayoutParams(RL_lp);
        damageText.get().setSingleLine();
        RL.addView(damageText.get());

        damageText.get().setTypeface(comic_sans);
        damageText.get().setTextSize(dmgSize);
        Shader textShader = new LinearGradient(0, 0, 0, damageText.get().getPaint().getTextSize(),
                new int[]{dmgTop, dmgBottom},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        damageText.get().setShadowLayer(1.5f, 5.0f, 5.0f, Color.BLACK);

        damageText.get().getPaint().setShader(textShader);
        if (gcInstance.isCritical()) {
            damageText.get().setTextSize(dmgSize);
            Shader critShader = new LinearGradient(0, 0, 0, damageText.get().getPaint().getTextSize(),
                    new int[]{critTop, critBottom},
                    new float[]{0, 1}, Shader.TileMode.CLAMP);
            damageText.get().getPaint().setShader(critShader);
        }

        damageText.get().setText(damage);

        // Position damageText to Click position
        damageText.get().setX(FL.getWidth() / 2 - 300);
        damageText.get().setY(FL.getHeight() / 2 - 50);
        damageText.get().animate().translationYBy(-300).alpha(0.15f).setDuration(1000).withEndAction(new Runnable() {
            public void run() {
                // rRemove the view from the parent layout
                RL.removeView(damageText.get());
            }
        });
        return damage;
    }

    private String pad_damage(int dmg) {
        if (dmg == 0) {
            return MISS;
        }
        int length = (int) (Math.log10(dmg) + 1);
        StringBuffer sb = new StringBuffer();
        while (length < 6) {
            sb.append(" ");
        }
        sb.append(dmg);
        return sb.toString();
    }

    private void mob_death() {
        play_Sound_Effect(death_sound);
        mobView.setImageResource(get_drawable_id(gcInstance.getCurrent_mob().getDeath(), DRAW));
        mob_drawable = (GifDrawable) mobView.getDrawable();
        mob_drawable.setLoopCount(1);
        mob_death = new AnimationListener() {
            @Override
            public void onAnimationCompleted(int loopNumber) {
                mobView.setImageResource(get_drawable_id("no_mob", DRAW));
            }

        };
        mob_drawable.removeAnimationListener(mob_move);
        mob_drawable.addAnimationListener(mob_death);
        updateExp();
        wait_to_spawn();
    }

    private void updateExp() {
        final TextView mob_exp = new TextView(mContext);
        mob_exp.setLayoutParams(RL_lp_exp);
        mob_exp.setTextColor(ContextCompat.getColor(mContext, R.color.levelColor));
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
            ExpBar.setMax(gcInstance.getPlayer().getTotal_exp());
            level_text.setText(String.format(Locale.CANADA, "%s %d", LV, gcInstance.getPlayer().getLevel()));
        }
        ExpBar.setProgress(gcInstance.getPlayer().getExp());
        exp_val_text.setText(gcInstance.getPlayer().getEXP_toString());
        exp_percent_text.setText(String.format(Locale.CANADA, " [%.2f%%]", gcInstance.getPlayer().getEXP_percent()));
    }


    private void wait_to_spawn() {
        int spawnTime = ThreadLocalRandom.current().nextInt(5000, 9000 + 1);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Activity lB = MainActivity.this;
                lB.runOnUiThread(new Runnable() {
//                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(mContext, String.format(Locale.CANADA, "A wild %s has appeared!",
                                gcInstance.getCurrent_mob().getName()), Toast.LENGTH_SHORT).show();
                        spawn_mob();
                    }
                });
            }
        }, spawnTime);
    }

    private void spawn_mob() {
        gcInstance.getCurrent_mob().resetHP();
        play_Sound_Effect(spawn_sound);
        mobView.setImageResource(get_drawable_id(gcInstance.getCurrent_mob().getSpawn(), DRAW));
        mob_drawable = (GifDrawable) mobView.getDrawable();
        mob_drawable.setLoopCount(1);
        mob_move = new AnimationListener() {
            @Override
            public void onAnimationCompleted(int loopNumber) {
                mobView.setImageResource(get_drawable_id(gcInstance.getCurrent_mob().getMove(), DRAW));
            }
        };
        mob_drawable.removeAnimationListener(mob_death);
        mob_drawable.addAnimationListener(mob_move);

        mobHP = totalHP;
        isAlive = true;//Listens to screen being pressed
        startAttackListener();
    }

    /*
     * Helper functions
     */
    // Check whether player click is on the monster
    private Boolean inRange(int x, int y) {
        return (x > (FL.getWidth() / 2) - (mobView.getWidth() / 3) && x < (FL.getWidth() / 2) + (mobView.getWidth() / 3))
                && (y > (FL.getHeight() / 2) - (mobView.getHeight() / 3) && y < (FL.getHeight() / 2) + (mobView.getHeight() / 3));
    }

    private int get_drawable_id(String name, String type){
        return getResources().getIdentifier(name, type, getPackageName());
    }
}

