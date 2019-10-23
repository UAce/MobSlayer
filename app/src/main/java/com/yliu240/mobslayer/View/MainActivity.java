package com.yliu240.mobslayer.View;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.javatuples.Pair;
import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.yliu240.mobslayer.Controller.GameController;
import com.yliu240.mobslayer.Model.Attack;
import com.yliu240.mobslayer.Model.Buff;
import com.yliu240.mobslayer.Model.Mob;
import com.yliu240.mobslayer.Model.Player;
import com.yliu240.mobslayer.Model.Skill;

public class MainActivity extends AppCompatActivity {

    public MediaPlayer bgm;
    private int dmgTop, dmgBottom, critTop, critBottom, skillTop, skillBottom;
    private ProgressBar HpBar, ExpBar;
    private Boolean isAlive = false, hit, sound_muted, music_muted, isBoss = false;
    private ImageButton sound, music, save, menu;
    private ImageButton right_arrow;
    private ImageButton left_arrow;
    private Pair<SoundPool, Integer> hit_sound, death_sound, levelUp, miss;
    private ImageView screenInFrontOfMob, mapView;
    private TextView mobHP_text, level_text, exp_val_text, exp_percent_text;
    private GifImageView mobView;
    private GifDrawable mob_drawable;
    private AnimationListener mobDeath;
    private FrameLayout FL;
    private FrameLayout.LayoutParams mob_layout, level_up_layout;
    private RelativeLayout RL, mob_hp;
    private RelativeLayout.LayoutParams buff_layout, attack_layout, text_layout, exp_layout;
    private LinearLayout BL, AL;
    private Typeface comic_sans;
    private Context mContext;
    private Handler mobHandler;
    private Runnable mobRecoil;
    private TimerTask attack_task;
    private Timer attack_timer;
    private int[] atk_fx = new int[]{R.drawable.b_atk, R.drawable.c_atk};
    private int screenWidth, screenHeight;
    private static final int HIT_SIZE = 250;
    private static final int DAMAGE_SIZE = 60;
    private static final String ENABLE = "_enable";
    private static final String DISABLE = "_disable";
    private static final String DEBUG = "[DEBUG] @@@@@@@@:";
    private static final String MISS = "  MISS  ";
    private static final String LV = "LV. ";
    private static final String RAW = "raw";
    private static final String DRAW = "drawable";

    private GameController gc;
    private Mob current_mob;
    private int streamID;
    private boolean saved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        if (savedInstanceState != null) {
            isAlive = savedInstanceState.getBoolean("isAlive");
        }
        sound_muted = Boolean.FALSE;
        music_muted = Boolean.FALSE;
        if (intent.hasExtra("SOUND_MUTED")) {
            sound_muted = getIntent().getExtras().getBoolean("SOUND_MUTED");
        }
        if (intent.hasExtra("MUSIC_MUTED")) {
            music_muted = getIntent().getExtras().getBoolean("MUSIC_MUTED");
        }
        mContext = getApplicationContext();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
//        Log.d(DEBUG, String.format("Width: %d  - Height: %d", screenWidth, screenHeight));

        // Variables for Layouts, Views
        screenInFrontOfMob = findViewById(R.id.transparent);
        mobHP_text = findViewById(R.id.hp);
        mapView = findViewById(R.id.mapView);
        FL = findViewById(R.id.framelayout);
        RL = findViewById(R.id.relayout);
        BL = findViewById(R.id.buffs_layout);
        AL = findViewById(R.id.attacks_layout);
        mob_hp = findViewById(R.id.mob_hp);

        int frame_layout_wrap = FrameLayout.LayoutParams.WRAP_CONTENT;
        int rel_layout_wrap = RelativeLayout.LayoutParams.WRAP_CONTENT;
        mob_layout = new FrameLayout.LayoutParams(frame_layout_wrap, frame_layout_wrap, Gravity.CENTER);
        level_up_layout = new FrameLayout.LayoutParams(frame_layout_wrap, frame_layout_wrap, Gravity.BOTTOM);
        text_layout = new RelativeLayout.LayoutParams(rel_layout_wrap, rel_layout_wrap);
        buff_layout = new RelativeLayout.LayoutParams(rel_layout_wrap, rel_layout_wrap);
        attack_layout = new RelativeLayout.LayoutParams(rel_layout_wrap, rel_layout_wrap);
        buff_layout.addRule(RelativeLayout.CENTER_HORIZONTAL);
        attack_layout.addRule(RelativeLayout.CENTER_HORIZONTAL);
        attack_layout.addRule(RelativeLayout.CENTER_VERTICAL);
        exp_layout = new RelativeLayout.LayoutParams(rel_layout_wrap, rel_layout_wrap);
        exp_layout.addRule(RelativeLayout.ABOVE, R.id.bottom_bar);
        BL.setGravity(Gravity.END);
        AL.setGravity(Gravity.END);

        // Buttons
        sound = findViewById(R.id.sound);
        music = findViewById(R.id.music);
        save = findViewById(R.id.save);
        menu = findViewById(R.id.menu);
        left_arrow = findViewById(R.id.left_arrow);
        right_arrow = findViewById(R.id.right_arrow);

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
        Drawable exp_drawable = ContextCompat.getDrawable(mContext, R.drawable.expbar_drawable);
        ExpBar.setProgressDrawable(exp_drawable);

        level_text = findViewById(R.id.level);
        exp_val_text = findViewById(R.id.Exp_val);
        exp_percent_text = findViewById(R.id.Exp_percent);

        // Set TextViews
        mobHP_text.setTypeface(comic_sans);

        setGameProperties();
    }

    private void saveJSON() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting().serializeNulls();
        Gson gson = builder.create();
        String playerInfo = gson.toJson(gc.getPlayer());
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput("player.json", Context.MODE_PRIVATE);
            outputStream.write(playerInfo.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setGameProperties() {
        gc = GameController.getInstance();
        gc.setProperties();

        setLevelArrows();
        mapView.setImageResource(getResourceId(gc.getCurrent_level().getBg_image(), DRAW));
        levelUp = loadSound("level_up_effect");
        miss = loadSound("miss");
        createBgm();
        populateSkills();
        mobView = new GifImageView(mContext);
        mobView.setLayoutParams(mob_layout);
        mobView.setImageResource(R.drawable.no_mob);
        if (isAlive) {
            FL.addView(mobView);
            setMob();
            startAttackListener();
        } else {
            spawnMob();
        }
        mobView.setElevation(1);

        ExpBar.setMax(gc.getPlayer().getTotal_exp());
        ExpBar.setProgress(gc.getPlayer().getExp());

        level_text.setText(String.format(Locale.CANADA, "%s %d", LV, gc.getPlayer().getLevel()));
        exp_val_text.setText(String.valueOf(gc.getPlayer().getExp()));
        exp_percent_text.setText(String.format(Locale.CANADA, " [%.2f%%]", gc.getPlayer().getEXP_percent()));
    }

    // Could refactor this to GameController
    private void setMob() {
        int[] available_mobs = gc.getCurrent_level().getMobs();
        int bossId = gc.getCurrent_level().getBoss();
        if (gc.getCurrent_mobId() == -1) {
            int new_mobId;
            if (gc.isBoss() && bossId != -1) {
                isBoss = true;
                new_mobId = bossId;
                bossAlert(5);
            } else {
                new_mobId = available_mobs[ThreadLocalRandom.current().nextInt(0, available_mobs.length)];
            }
            gc.setCurrent_mobId(new_mobId);
            gc.setCurrent_mob();
        }
        current_mob = gc.getCurrent_mob();
        setSoundEffects();
        mobView.setImageResource(getResourceId(current_mob.getMove(), DRAW));
        if (current_mob.getWidth() != -1 && current_mob.getHeight() != -1) {
            float width_mult = (float)screenWidth/720;
            float height_mult = (float)screenHeight/1193;
            mobView.requestLayout();
            mobView.getLayoutParams().height = (int)(current_mob.getHeight()*height_mult);
            mobView.getLayoutParams().width = (int)(current_mob.getWidth()*width_mult);
        }
        isAlive = true;
        hit = false;
    }

    private void setLevelArrows() {
        final int prev = gc.getCurrent_level().getPrev();
        final int next = gc.getCurrent_level().getNext();
        if (prev != -1) {
            left_arrow.setVisibility(View.VISIBLE);
            setLevelListener(left_arrow, prev);
        } else {
            left_arrow.setVisibility(View.INVISIBLE);
        }
        if (next != -1) {
            right_arrow.setVisibility(View.VISIBLE);
            setLevelListener(right_arrow, next);
        } else {
            right_arrow.setVisibility(View.INVISIBLE);
        }
    }

    public void addSkillIcon(ImageButton ib, LinearLayout buff_layout) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.END;
        ib.setLayoutParams(params);
        buff_layout.addView(ib);
    }

    @Override
    protected void onStart() {
        super.onStart();

        startMenuListener();
        startSaveListener();
        startBgmListener();
        if (sound_muted) {
            sound.setImageResource(R.drawable.baseline_volume_off_24);
        }
        if (music_muted) {
            music.setImageResource(R.drawable.round_music_off_24);
            bgm.pause();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();

        if (bgm != null) {
            bgm.pause();
        }
        sound.setOnClickListener(null);
        music.setOnClickListener(null);
        saveJSON();
        System.gc();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        sound.setOnClickListener(null);
        music.setOnClickListener(null);
        saveJSON();
        System.gc();
    }
    @Override
    protected void onResume() {
        super.onResume();

        gc = GameController.getInstance();
        startBgmListener();
        if (!sound_muted) {
            sound.setImageResource(R.drawable.baseline_volume_up_24);
        }
        if (!music_muted) {
            music.setImageResource(R.drawable.round_music_note_24);
            bgm.start();
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isAlive", true);
        outState.putInt("position", bgm.getCurrentPosition());
        int i = getIndexOf(gc.getMobs(), current_mob.getName());
        outState.putInt("mob_index", i);
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
        SoundPool sound = sfx.getValue0();
        if (streamID != 0) {
            sound.stop(streamID);
//            Log.d(DEBUG, "stopped sound");
        }
        if (!sound_muted) {
//            Log.d(DEBUG, "Play sound");
            streamID = sound.play(sfx.getValue1(), 1.0F, 1.0F, 0, 0, 1.0F);
        }
    }

    /**
     *  Creates a SoundPool object for sound effect
     * @param name Name of sound effect
     * @return Returns a Pair<SoundPool, Integer> of the sound effect
     */
    @SuppressWarnings("deprecation")
    private Pair<SoundPool, Integer> loadSound(String name) {
        int soundId = getResourceId(name, RAW);
        SoundPool soundPool;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = (new SoundPool.Builder()).setMaxStreams(2).build();
        } else {
            soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 5);
        }

        int sound_val = soundPool.load(mContext, soundId, 1);
        return new Pair<>(soundPool, sound_val);
    }

    public void createBgm() {
        if (bgm != null) {
            bgm.stop();
        }
        bgm = MediaPlayer.create(mContext, getResourceId(gc.getCurrent_level().getBgm_name(), "raw"));
        bgm.setLooping(true);
        bgm.start();
        if (music_muted) {
            bgm.pause();
        }
    }

    private void setSoundEffects() {
        if (hit_sound != null) {
            hit_sound.getValue0().release();
        }
        if (death_sound != null) {
            death_sound.getValue0().release();
        }
        hit_sound = loadSound(current_mob.getHit_sound());
        death_sound = loadSound(current_mob.getDeath_sound());
    }

    //Listeners
    @SuppressLint("ClickableViewAccessibility")
    private void setBuffListener(final ImageView iv, int i) {
        final Buff buff = gc.getBuff(i);
        buff.resetCooldown(); //find out what this does
        final String skill_name = buff.getName();
        final String message = buff.getMessage();
        final int cooldown_time = buff.getCooldown();
        final Pair<SoundPool, Integer> s_sfx = loadSound(buff.getSound_effect());
        final int s_width = buff.getWidth();
        final int s_height = buff.getHeight();
        iv.setBackgroundResource(getResourceId(skill_name+ENABLE, DRAW));
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!buff.getIn_use()) {
                    playSoundEffect(s_sfx);
                    iv.setBackgroundResource(getResourceId(skill_name+DISABLE, DRAW));
                    iv.invalidate();
                    startCooldown(iv, skill_name, cooldown_time, buff);
                    if (skill_name.equals("sharp_eyes")) {
                        gc.getPlayer().sharp_eyes();
                    }
                    ImageView skill_img = new ImageView(getApplicationContext());
                    skill_img.setBackgroundResource(getResourceId(skill_name, DRAW));
                    skill_img.setLayoutParams(buff_layout);
                    skill_img.requestLayout();
                    skill_img.getLayoutParams().height = s_height;
                    skill_img.getLayoutParams().width = s_width;
                    skill_img.setElevation(3);
                    RL.addView(skill_img);
                    AnimationDrawable skill_anim = (AnimationDrawable) skill_img.getBackground();
                    skill_anim.start();
                    checkIfAnimationDone(skill_anim, skill_img, RL);

                    if (!message.equals("")) {
                        createText(message, "buff", 0, 0);
                    }
                }
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setAttackListener(final ImageView iv, int i) {
        final Attack attack = gc.getAttack(i);
        final String skill_name = attack.getName();
        final int multiplier = attack.getAttack_multiplier();
        final int duration = attack.getDuration();
        final int period = duration/attack.getAttack_count();
        final long delay = attack.getDelay();
        final Pair<SoundPool, Integer> s_sfx = loadSound(attack.getSound_effect());
        final int s_width = attack.getWidth();
        final int s_height = attack.getHeight();
        iv.setBackgroundResource(getResourceId(skill_name+ENABLE, DRAW));
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!attack.getIn_use()) {
                    playSoundEffect(s_sfx);
                    iv.setBackgroundResource(getResourceId(skill_name + DISABLE, DRAW));
                    iv.invalidate();
                    attack.setIn_use(true);
                    startCooldown(iv, skill_name, duration, attack);
                    ImageView skill_img = new ImageView(getApplicationContext());
                    skill_img.setBackgroundResource(getResourceId(skill_name, DRAW));
                    skill_img.setLayoutParams(attack_layout);
                    skill_img.requestLayout();
                    skill_img.getLayoutParams().height = s_height;
                    skill_img.getLayoutParams().width = s_width;
                    skill_img.setElevation(3);
                    RL.addView(skill_img);
                    AnimationDrawable skill_anim = (AnimationDrawable) skill_img.getBackground();
                    skill_anim.start();
                    checkIfAnimationDone(skill_anim, skill_img, RL);
                    attack_timer = new Timer();
                    attack_task = new TimerTask() {
                        final long t0 = System.currentTimeMillis();
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (System.currentTimeMillis() - t0 >= duration || current_mob.isDead()) {
                                        attack_timer.cancel();
                                        attack_task.cancel();
                                    } else {
                                        attackMob(true, screenWidth/2, screenHeight/2, multiplier);
                                    }
                                }
                            });
                        }
                    };
                    attack_timer.scheduleAtFixedRate(attack_task, delay, period);
                }
            }
        });
    }

    public void startCooldown(ImageView iv, String name, int cooldown, Skill skill_object) {
        final ImageView image = iv;
        final String skill_name = name;
        final Skill skill = skill_object;
        skill.setIn_use(true);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        image.setBackgroundResource(getResourceId(skill_name+ENABLE, DRAW));
                        image.invalidate();
                        skill.setIn_use(false);
                    }
                });
            }
        }, cooldown);
    }

    public void skillAttack(int time_before_attack) {
        removeAttackListener();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //TODO: attack mob
                        startAttackListener();
                    }
                });
            }
        }, time_before_attack);
    }

    private void startBgmListener() {
        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sound_muted) {
                    sound.setImageResource(R.drawable.baseline_volume_off_24);
                    sound_muted=true;
                } else {
                    sound.setImageResource(R.drawable.baseline_volume_up_24);
                    sound_muted=false;
                }
            }
        });
        music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!music_muted) {
                    music.setImageResource(R.drawable.round_music_off_24);
                    bgm.pause();
                    music_muted=true;
                } else {
                    music.setImageResource(R.drawable.round_music_note_24);
                    bgm.start();
                    music_muted=false;
                }
            }
        });
    }

    private void startSaveListener() {
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
    }

    private void startMenuListener() {
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
                Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(mContext,
                        android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                intent.putExtra("SOUND_MUTED", sound_muted);
                intent.putExtra("MUSIC_MUTED", music_muted);
                clearMediaPlayerAndSoundPool();
                startActivity(intent, bundle);
                finish();
            }
        });
    }

    private void save() {
        if (!saved) {
            saveJSON();
            makeToast("saved");
            saved = true;
        }
    }

    private void setLevelListener(final ImageButton ib, final int id) {
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gc.switch_level(id);
                mapView.setImageResource(getResourceId(gc.getCurrent_level().getBg_image(), DRAW));
                setLevelArrows();
                createBgm();
            }
        });
    }

    /*
     * Mob Functions
     */
    @SuppressLint("ClickableViewAccessibility")
    private void startAttackListener() {

        mobHandler = new Handler();
        mobRecoil = new Runnable() {
            public void run() {
                FL.removeView(mobView);
                mobView.setImageResource(getResourceId(current_mob.getMove(), DRAW));
                FL.addView(mobView, 0);
            }
        };
        screenInFrontOfMob.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {

                    // Pressed
                    case MotionEvent.ACTION_DOWN: {
                        if (saved) {
                            saved = false;
                        }
                        if (current_mob.isDead()) {
                            break;
                        }
                        int x = (int) event.getRawX();
                        int y = (int) event.getRawY();
                        Rect mob_position = getLocationOnScreen(mobView);
                        Boolean isHit = mob_position.contains(x,y);
                        attackMob(isHit, (int) event.getX(), (int) event.getY(), 0);
                        break;
                    }
                    // Released
                    case MotionEvent.ACTION_UP: {
                        if (current_mob.isDead()) {
                            break;
                        }
                    }
                }
                return isAlive;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void removeAttackListener() {
        screenInFrontOfMob.setOnTouchListener(null);
    }

    private void attackMob(Boolean isHit, int x, int y, int multiplier) {
        mobHandler.removeCallbacks(mobRecoil);
        // Generate random Damage and create damage Text
        Pair<Integer, Boolean> damage = new Pair<>(0, false);
        if (isHit) {
            playSoundEffect(hit_sound);
            damage = gc.attackMob(multiplier); //ThreadLocalRandom.current().nextInt(500000, 999999 + 1);
            drawDmg(x, y, damage.getValue1());
            FL.removeView(mobView);
            mobView.setImageResource(getResourceId(current_mob.getHit(), DRAW));
            FL.addView(mobView, 0);
        } else {
            playSoundEffect(miss);
        }
        String type = "";
        if (damage.getValue1() && damage.getValue0() > 0) type = "critical";

        createText(padText(damage.getValue0()), type, x, y);
        updateHP();
        if(!current_mob.isDead()) {
            mobHandler.postDelayed(mobRecoil, 150);
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private void updateHP() {
        if (current_mob.getCurrent_hp() == 0) {
            removeAttackListener();
            isAlive = false;
            isBoss = false;
            Activity lC = MainActivity.this;
            lC.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mobDeath();
                }
            });
        }
        setMobHP();
        mob_hp.setVisibility(View.VISIBLE);
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
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
    @SuppressWarnings({"UnclearExpression", "IntegerDivisionInFloatingPointContext", "RedundantCast"})
    private void createText(String message, String type, int x, int y) {
        final TextView newText = new TextView(getApplicationContext());
        newText.setLayoutParams(text_layout);
        newText.setSingleLine();
        newText.setTypeface(comic_sans);
        newText.setTextSize(DAMAGE_SIZE);
        newText.setText(message);

        Shader shader;
        switch(type) {
            case "critical":
                shader = new LinearGradient(0, 0, 0, newText.getTextSize(),
                        critTop, critBottom, Shader.TileMode.CLAMP);
                newText.setShadowLayer(0.01f, -2, 2, critTop);
                break;
            case "buff":
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
        newText.measure(0,0);
        int w = newText.getMeasuredWidth();
        int h = newText.getMeasuredHeight();
        if (x==0 && y==0) {
            // Above center of screen
            newText.setX((int)(FL.getWidth()/2) - (int)(w/2));
            newText.setY((int)(FL.getHeight()/2) - (h));
        } else {
            // Above click area
            newText.setX(x-w/2);
            newText.setY(y-h-150);
        }

        RL.addView(newText);
        newText.animate().translationYBy(-300).alpha(0.2f).setDuration(900).withEndAction(new Runnable() {
            public void run() {
                // Remove the view from the parent layout
                RL.removeView(newText);
            }
        });
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    private void drawDmg(int x, int y, boolean isCrit) {
        ImageView dmg = new ImageView(getApplicationContext());
        int i = ThreadLocalRandom.current().nextInt(0, atk_fx.length);
        dmg.setBackgroundResource(atk_fx[i]);
        if (isCrit) {
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

    private void checkIfAnimationDone(AnimationDrawable a, ImageView iv, ViewGroup vg) {
        final AnimationDrawable anim = a;
        final ImageView image = iv;
        final ViewGroup parent_layout = vg;
        // Make the other variables final here
        int timeBetweenChecks = 100;
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            public void run() {
                if (anim.getCurrent() != anim.getFrame(anim.getNumberOfFrames() - 1)) {
                    checkIfAnimationDone(anim, image, parent_layout);
                } else{
                    parent_layout.removeView(image);
                }
            }
        }, timeBetweenChecks);
    }

    private String padText(int dmg) {
        if (dmg == 0) {
            return MISS;
        }
        StringBuilder sb = new StringBuilder();
        int length = (int) (Math.log10(dmg) + 1);
        if (length == 6) {
            sb.append(" ");
            sb.append(dmg);
            sb.append(" ");
        }else if (length == 5) {
            sb.append("  ");
            sb.append(dmg);
            sb.append("  ");
        }else if (length == 4) {
            sb.append("   ");
            sb.append(dmg);
            sb.append("   ");
        }else if (length == 3) {
            sb.append("    ");
            sb.append(dmg);
            sb.append("    ");
        }else if (length == 2) {
            sb.append("    ");
            sb.append(dmg);
            sb.append("    ");
        }else if (length == 1) {
            sb.append("    ");
            sb.append(dmg);
            sb.append("     ");
        }
        return sb.toString();
    }

    private void mobDeath() {
        playSoundEffect(death_sound);
        mobView.setImageResource(getResourceId(current_mob.getDeath(), DRAW));
        mob_drawable = (GifDrawable) mobView.getDrawable();
        mob_drawable.setLoopCount(1);
        mobDeath = new AnimationListener() {
            @Override
            public void onAnimationCompleted(int loopNumber) {
                FL.removeView(mobView);
                gc.setCurrent_mobId(-1);
                current_mob.resetHP();
                spawnMob();
            }
        };
        mob_drawable.addAnimationListener(mobDeath);
        updateExp();
    }

    private void updateExp() {
        final TextView mob_exp = new TextView(mContext);
        mob_exp.setLayoutParams(exp_layout);
        mob_exp.setTextColor(ContextCompat.getColor(mContext, R.color.GainExpColor));
        mob_exp.setText(String.format(Locale.CANADA, " +%d EXP", current_mob.getExp()));
        RL.addView(mob_exp);
        mob_exp.animate().translationYBy(-50).alpha(1f).alpha(0.15f).setDuration(3500).withEndAction(new Runnable() {
            public void run() {
                RL.removeView(mob_exp);
            }
        });
        Boolean level_Up = gc.gainEXP();
        if (level_Up) {
            levelUp();
            ExpBar.setMax(gc.getPlayer().getTotal_exp());
            level_text.setText(String.format(Locale.CANADA, "%s %d", LV, gc.getPlayer().getLevel()));
        }
        ExpBar.setProgress(gc.getPlayer().getExp());
        exp_val_text.setText(gc.getPlayer().getEXP_toString());
        exp_percent_text.setText(String.format(Locale.CANADA, " [%.2f%%]", gc.getPlayer().getEXP_percent()));
    }

    private void levelUp() {
        gc.getPlayer().reset_buff(); //Can this be removed?
        if (gc.hasNextLevel()) {
            gc.updateLevel(gc.getPlayer().getLevel()-1);
            setLevelArrows();
            mapView.setImageResource(getResourceId(gc.getCurrent_level().getBg_image(), DRAW));
            createBgm();
        }
        ImageView lvlup_img = new ImageView(mContext);
        lvlup_img.setBackgroundResource(R.drawable.level_up);
        lvlup_img.setLayoutParams(level_up_layout);
        FL.addView(lvlup_img,0);
        lvlup_img.setElevation(1);
        lvlup_img.setX(-100);
        AnimationDrawable lvlup_anim = (AnimationDrawable) lvlup_img.getBackground();
        lvlup_anim.start();
        checkIfAnimationDone(lvlup_anim, lvlup_img, FL);
        playSoundEffect(levelUp);
        populateSkills();
    }

    private void spawnMob() {
        setMob();
        long MOB_SPAWN_TIME;
        if (isBoss) {
            MOB_SPAWN_TIME = 3000;
        } else {
            MOB_SPAWN_TIME = 800;
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Activity lB = MainActivity.this;
                lB.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FL.removeView(mobView);
                        mobView.setImageResource(getResourceId(current_mob.getMove(), DRAW));
                        FL.addView(mobView);
                        startAttackListener();
                        mob_drawable = (GifDrawable) mobView.getDrawable();
                        mob_drawable.removeAnimationListener(mobDeath);
                    }
                });
            }
        }, MOB_SPAWN_TIME);
    }

    public void setMobHP() {
        mobHP_text.setText(current_mob.getHP_percent_string());
        HpBar.setMax(current_mob.getTotal_hp());
        HpBar.setProgress(current_mob.getCurrent_hp());

        float hp = current_mob.getHP_percent();
        if (hp >= 66) {
            HpBar.getProgressDrawable().setColorFilter(0xff00ff00, android.graphics.PorterDuff.Mode.MULTIPLY);
        } else if (hp < 66 && hp >= 33) {
            HpBar.getProgressDrawable().setColorFilter(0xffffff00, android.graphics.PorterDuff.Mode.MULTIPLY);
        } else {
            HpBar.getProgressDrawable().setColorFilter(0xFFFF0000, android.graphics.PorterDuff.Mode.MULTIPLY);
        }
    }


    private void populateSkills() {
        BL.removeAllViews();
        AL.removeAllViews();
        gc.resetSkill();
        int[] available_buffs = gc.getCurrent_level().getBuffs();
        for (int buff_id : available_buffs) {
            ImageButton skill_icon = new ImageButton(mContext);
            addSkillIcon(skill_icon, BL);
            setBuffListener(skill_icon, buff_id);
        }
        int[] available_attacks = gc.getCurrent_level().getAttacks();
        for (int attack_id : available_attacks) {
            ImageButton skill_icon = new ImageButton(mContext);
            addSkillIcon(skill_icon, AL);
            setAttackListener(skill_icon, attack_id);
        }
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

    private int getIndexOf(List<Mob> mob_list, String name) {
        int pos = 0;

        for(Mob mob : mob_list) {
            if (name.equalsIgnoreCase(mob.getName()))
                return pos;
            pos++;
        }
        return -1;
    }

    private void clearMediaPlayerAndSoundPool() {
        bgm.stop();
        bgm.release();
        bgm = null;
        hit_sound.getValue0().release();
        death_sound.getValue0().release();
    }

    private void makeToast(String msg) {
        Toast toast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
        TextView toastMessage = toast.getView().findViewById(android.R.id.message);
        toastMessage.setTextColor(Color.WHITE);
        toast.show();
    }

    private void bossAlert(final int count) {
        final Toast toast = Toast.makeText(mContext, "Boss Alert", Toast.LENGTH_SHORT);
        View toastView = toast.getView();
        toastView.setBackgroundResource(R.drawable.warning_gradient);
        TextView toastMessage = toast.getView().findViewById(android.R.id.message);
        toastMessage.setTextColor(Color.BLACK);

        CountDownTimer toastCountDown;
        toastCountDown = new CountDownTimer(500, 1000 ) {
            public void onTick(long millisUntilFinished) {
                toast.show();
            }
            public void onFinish() {
                toast.cancel();
                if (count > 0) {
                    int nextCount = count - 1;
                    bossAlert(nextCount);
                }
            }
        };
        toastCountDown.start();
    }
}

