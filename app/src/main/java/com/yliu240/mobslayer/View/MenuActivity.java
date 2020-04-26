package com.yliu240.mobslayer.View;

import android.content.Context;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.javatuples.Pair;
import java.io.File;
import java.io.IOException;

import com.yliu240.mobslayer.Controller.GameController;

public class MenuActivity extends AppCompatActivity {

    private Boolean sound_muted, music_muted;
    private FrameLayout menuFrame;
    private Context mContext;
    private RelativeLayout menuBoard;
    private ImageButton new_game, load_game, sound, music;
    private MediaPlayer bgm;
    private static final String RAW = "raw";
    private Pair<SoundPool, Integer> button_pressed;
    private GameController gcInstance;
    //Log.d("[DEBUG] @@@@@@@@@@@:", "debug message");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Intent intent = getIntent();
        sound_muted = Boolean.FALSE;
        music_muted = Boolean.FALSE;
        if(intent.hasExtra("SOUND_MUTED")){
            sound_muted = getIntent().getExtras().getBoolean("SOUND_MUTED");
        }
        if (intent.hasExtra("MUSIC_MUTED")) {
            music_muted = getIntent().getExtras().getBoolean("MUSIC_MUTED");
        }
        gcInstance = GameController.getInstance();

        Typeface comic_sans = Typeface.createFromAsset(getAssets(), "comic-sans-ms-bold.ttf");
        mContext = getApplicationContext();
        menuFrame = findViewById(R.id.menuFrameLayout);
        menuBoard = findViewById(R.id.menuBoard);
        TextView menuTitle = findViewById(R.id.menuTitle);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        int topMenuColor = ContextCompat.getColor(mContext, R.color.TopMenuColor);
        int bottomMenuColor = ContextCompat.getColor(mContext, R.color.BottomMenuColor);
        Shader shader = new LinearGradient(0, 0, 0, menuTitle.getTextSize(),
                topMenuColor, bottomMenuColor, Shader.TileMode.CLAMP);
        menuTitle.getPaint().setShader(shader);
        menuTitle.setTypeface(comic_sans);
        menuTitle.setTextColor(menuTitle.getTextColors().withAlpha(255));
        menuBoard.startAnimation(fadeIn);

        // Set sounds
        sound = findViewById(R.id.sound);
        music = findViewById(R.id.music);
        createBgm();
        button_pressed = loadSound("button_pressed");

        // Set menu buttons
        new_game = findViewById(R.id.new_game);
        load_game = findViewById(R.id.load_game);
    }

    private void startTransition(){
        Animation fade_out = AnimationUtils.loadAnimation(mContext, R.anim.fade_out);
        fade_out.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {
                menuFrame.removeView(menuBoard);
            }
        });
        menuBoard.startAnimation(fade_out);
        Thread transition = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(2000);
                    Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(mContext,
                            android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("SOUND_MUTED", sound_muted);
                    intent.putExtra("MUSIC_MUTED", music_muted);
                    clearMediaPlayerAndSoundPool();
                    startActivity(intent, bundle);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        transition.start();
    }
    @Override
    protected void onStart() {
        super.onStart();
        setListeners();
        if(sound_muted){
            sound.setImageResource(R.drawable.baseline_volume_off_24);
        }
        if(music_muted){
            music.setImageResource(R.drawable.round_music_off_24);
            bgm.pause();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(bgm != null){
            bgm.pause();
        }
        clearListeners();
        System.gc();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearListeners();
        System.gc();
    }
    @Override
    protected void onResume() {
        super.onResume();
        gcInstance = GameController.getInstance();
        setListeners();
        if(!sound_muted){
            sound.setImageResource(R.drawable.baseline_volume_up_24);
        }
        if(!music_muted){
            music.setImageResource(R.drawable.round_music_note_24);
            bgm.start();
        }
    }

    private void loadJSON(Boolean loadGame) {
        gcInstance = GameController.getInstance();
        try {
            gcInstance.loadJSON(loadGame, mContext);
            startTransition();
        } catch (IOException e) {
            setListeners();
            e.printStackTrace();
        }
    }

    /**
     * Starts listener for sound button
     */
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

    /**
     * Plays the sound effect
     * @param sfx Pair<SoundPool, Integer> of a sound effect
     */
    private void playSoundEffect(Pair<SoundPool, Integer> sfx) {
        if(!sound_muted){
            sfx.getValue0().play(sfx.getValue1(), 1.0F, 1.0F, 0, 0, 1.0F);
        }
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

    /**
     * Sets and starts the background music
     */
    private void createBgm(){
        if (bgm != null){
            bgm.stop();
        }
        bgm = MediaPlayer.create(mContext, getResourceId("recollecting_memories", "raw"));
        bgm.setLooping(true);
        bgm.start();
        if(music_muted){
            bgm.pause();
        }
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

    private boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        if(file == null || !file.exists()) {
            return false;
        }
        return true;
    }

    private void startClickListener(ImageButton ib, final Boolean loadGame){
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearListeners();
                playSoundEffect(button_pressed);
                loadJSON(loadGame);
            }
        });
    }

    private void setListeners(){
        startClickListener(new_game, false);
        startClickListener(load_game, true);
        startBgmListener();
    }

    private void clearListeners(){
        new_game.setOnClickListener(null);
        load_game.setOnClickListener(null);
        sound.setOnClickListener(null);
    }

    private void clearMediaPlayerAndSoundPool(){
        bgm.stop();
        bgm.release();
        bgm = null;
        button_pressed.getValue0().release();
    }
}
