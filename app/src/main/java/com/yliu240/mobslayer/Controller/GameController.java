package com.yliu240.mobslayer.Controller;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.yliu240.mobslayer.Model.Attack;
import com.yliu240.mobslayer.Model.Buff;
import com.yliu240.mobslayer.Model.Level;
import com.yliu240.mobslayer.Model.Mob;
import com.yliu240.mobslayer.Model.Player;

import org.javatuples.Pair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by yu-yu on 2019-02-07.
 */

public class GameController {

    private static final String DEBUG = "DEBUG";
    private static final String[] JSONFiles = {"levels.json", "mobs.json", "buffs.json", "attacks.json"};
    private static Gson gson;

    // Fields in JSON file
    private Player myPlayer = Player.getInstance(); //Singleton
    private List<Mob> mobs = new ArrayList<>();
    private List<Level> levels = new ArrayList<>();
    private List<Buff> buffs = new ArrayList<>();
    private List<Attack> attacks = new ArrayList<>();

    // Other properties of GameController
    private Mob current_mob = new Mob();
    private Level current_level = new Level();
    private int current_levelId = 0;
    private int current_mobId = 0;
    private int current_mobHp = 100;

    private static GameController gameControllerInstance;

    private GameController() { }

    public static synchronized GameController getInstance(){
        if(gameControllerInstance == null){
            gameControllerInstance = new GameController();
        }
        return gameControllerInstance;
    }

    static public synchronized void setInstance(GameController newInstance) {
        gameControllerInstance = newInstance;

    }

    public void loadJSON(Boolean load, Context mContext) throws IOException{
        gson = new Gson();
        AssetManager assetManager = mContext.getAssets();
        InputStream player_ims;
        Reader player_reader;

        Log.d(DEBUG, "Loading Game data");
        for (int i = 0; i < JSONFiles.length - 1; i++) {
            InputStream ims = assetManager.open(JSONFiles[i]);
            Reader reader = new InputStreamReader(ims);
            deserializeJSON(JSONFiles[i], reader);
        }

        if (load && fileExists(mContext, "player.json")){
            Log.d(DEBUG, "Loading player data");
            player_ims = mContext.openFileInput("player.json");
        } else if(!load){
            Log.d(DEBUG, "New player data");
            player_ims = assetManager.open("newPlayer.json");
        } else {
            throw new IOException("Cannot load player data: player.json not found.");
        }

        player_reader = new InputStreamReader(player_ims);
        myPlayer = gson.fromJson(player_reader, Player.class);
        Log.d(DEBUG, "Player loaded");
    }

    private void deserializeJSON(String filename, Reader reader) {
        switch (filename) {
            case "levels.json":
                levels = gson.fromJson(reader, new TypeToken<List<Level>>(){}.getType());
                Log.d(DEBUG, "Levels loaded");
                break;
            case "mobs.json":
                mobs = gson.fromJson(reader, new TypeToken<List<Mob>>(){}.getType());
                Log.d(DEBUG, "Mobs loaded");
                break;
            case "buffs.json":
                buffs = gson.fromJson(reader, new TypeToken<List<Buff>>(){}.getType());
                Log.d(DEBUG, "Buffs loaded");
                break;
            case "attacks.json":
                attacks = gson.fromJson(reader, new TypeToken<List<Attack>>(){}.getType());
                Log.d(DEBUG, "Attacks loaded");
                break;
            default:
                break;
        }

    }
    private boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        if(file == null || !file.exists()) {
            return false;
        }
        return true;
    }

    // Getter methods
    public List<Buff> getBuffs(){
        return this.buffs;
    }
    public List<Mob> getMobs(){
        return this.mobs;
    }
    public List<Level> getLevels(){
        return this.levels;
    }
    public Player getPlayer() {
        return this.myPlayer;
    }
    public int getCurrent_mobId(){
        return this.current_mobId;
    }
    public int getCurrent_levelId(){
        return this.current_levelId;
    }
    public Mob getCurrent_mob(){
        return this.current_mob;
    }
    public Level getCurrent_level(){
        return this.current_level;
    }


    //Setter methods
    public void setCurrent_mobId(int idx){
        this.current_mobId = idx;
    }
    public void setCurrent_levelId(int idx){
        this.current_levelId = idx;
        setCurrent_level();
    }
    public void setCurrent_mob(){
        this.current_mob = mobs.get(this.current_mobId);
    }
    public void setCurrent_level(){
        this.current_level = levels.get(this.current_levelId);
    }


    // Game Methods
    public void setProperties(){
        setCurrent_level();
        if(getCurrent_mobId() != -1){
            setCurrent_mob();
        }
    }

    public void updateLevel(int nextId){
        int currId = nextId-1;
        Level lvl = levels.get(currId);
        lvl.setNext(nextId);
        levels.set(currId, lvl);
        setCurrent_levelId(nextId);
    }

    public void switch_level(int id){
        setCurrent_levelId(id);
    }
    
    public void getNewMob(){
        int randomMobId = ThreadLocalRandom.current().nextInt(0, this.mobs.size() + 1);
        Mob new_mob = mobs.get(randomMobId);
        this.current_mob.setName(new_mob.getName());
        this.current_mob.setTotal_hp(new_mob.getTotal_hp());
        this.current_mob.setCurrent_hp(new_mob.getCurrent_hp());
        this.current_mob.setExp(new_mob.getExp());
        this.current_mob.setWidth(new_mob.getWidth());
        this.current_mob.setHeight(new_mob.getHeight());
        this.current_mob.setMove(new_mob.getMove());
        this.current_mob.setDeath(new_mob.getDeath());
        this.current_mob.setHit(new_mob.getHit());
        this.current_mob.setDeath_sound(new_mob.getDeath_sound());
        this.current_mob.setHit_sound(new_mob.getHit_sound());
    }
    
    
    public Pair<Integer,Boolean> attackMob(int skill_multiplier){
        int damage = (int) ThreadLocalRandom.current().nextDouble(myPlayer.getAttack()*0.5,  (myPlayer.getAttack() + 1)*1.5);
        Boolean critical = isCritical();
        if(critical){
            damage *= myPlayer.getAttack_multiplier();
        }
        if(damage > 999999){
            damage = 999999;
        }
        if(skill_multiplier != 0){
            damage *= skill_multiplier;
        }
        this.current_mob.takeDamage(damage);
        return new Pair<>(damage, critical);
    }

    private Boolean isCritical(){
        double critical_rate = ThreadLocalRandom.current().nextInt(0, 100 + 1);
        return critical_rate < myPlayer.getCritical_rate();
    }

    public Boolean gainEXP(){
        int new_exp = myPlayer.getExp()+current_mob.getExp();
        int diff = new_exp - myPlayer.getTotal_exp();
        if(diff >= 0){
            myPlayer.levelUp(diff);
            return true;
        }else{
            myPlayer.setExp(new_exp);
            return false;
        }
    }

    public Buff getSkill(int id){
        return buffs.get(id);
    }

    public Mob getMob(int id){
        return mobs.get(id);
    }

    public Boolean hasNextLevel(){
        return getLevels().size() >= myPlayer.getLevel();
    }

    public Boolean isBoss(){
        double boss_rate = ThreadLocalRandom.current().nextInt(0, 100 + 1);
        return boss_rate < 10;
    }
}
