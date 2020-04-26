package com.yliu240.mobslayer.Controller;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.javatuples.Pair;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.yliu240.mobslayer.Model.Attack;
import com.yliu240.mobslayer.Model.Buff;
import com.yliu240.mobslayer.Model.Map;
import com.yliu240.mobslayer.Model.Mob;
import com.yliu240.mobslayer.Model.Level;
import com.yliu240.mobslayer.Model.Player;

/**
 * Created by yu-yu on 2019-02-07.
 */

public class GameController {

    private static final String DEBUG = "DEBUG";
    private static final String ERROR = "ERROR";
    private static final String[] JSONFiles = {"maps.json", "mobs.json", "buffs.json", "attacks.json", "levels.json"};
    private static Gson gson;

    // Fields in JSON file
    private Player myPlayer = Player.getInstance(); // Singleton
    private List<Mob> mobs;
    private List<Map> maps;
    private List<Buff> buffs;
    private List<Attack> attacks;
    private List<Level> levels;

    // Other properties of GameController
    private Mob current_mob = new Mob();
    private Map current_map = new Map();
    private int current_mapId;
    private int current_mobId;

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
        for (int i = 0; i < JSONFiles.length; i++) {
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
        this.current_mapId = myPlayer.getCurrent_mapId();
        this.current_mobId = myPlayer.getCurrent_mobId();
        Log.d(DEBUG, "Player loaded");
    }

    private void deserializeJSON(String filename, Reader reader) {
        switch (filename) {
            case "levels.json":
                levels = gson.fromJson(reader, new TypeToken<List<Level>>(){}.getType());
                Log.d(DEBUG, "Levels loaded");
                break;
            case "maps.json":
                maps = gson.fromJson(reader, new TypeToken<List<Map>>(){}.getType());
                Log.d(DEBUG, "Maps loaded");
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
                Log.e(ERROR, "File " + filename + " does not exist.");
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

    public void setCurrent_data() {
        myPlayer.setCurrent_mapId(this.current_mapId);
        myPlayer.setCurrent_mobId(this.current_mobId);
    }

    // Getter methods
    public List<Attack> getAttacks(){
        return this.attacks;
    }
    public List<Buff> getBuffs(){
        return this.buffs;
    }
    public List<Mob> getMobs(){
        return this.mobs;
    }
    public List<Map> getMaps(){
        return this.maps;
    }
    public Player getPlayer() {
        return this.myPlayer;
    }
    public int getCurrent_mobId(){
        return this.current_mobId;
    }
    public int getCurrent_mapId(){
        return this.current_mapId;
    }
    public Mob getCurrent_mob(){
        return this.current_mob;
    }
    public Map getCurrent_map(){
        return this.current_map;
    }


    //Setter methods
    public void setCurrent_mobId(int mobId){
        this.current_mobId = mobId;
    }
    public void setCurrent_mapId(int mapId){
        this.current_mapId = mapId;
    }

    public void setCurrent_mob(){
        this.current_mob = mobs.get(this.current_mobId);
        // TODO: set HP
    }
    public void setCurrent_map(){
        this.current_map = maps.get(this.current_mapId);
    }

    public void setCurrent_mobById(int idx){
        this.current_mobId = idx;
        if (idx >= 0) {
            setCurrent_mob();
        }
    }
    public void setCurrent_mapById(int idx){
        this.current_mapId = idx;
        if (idx >= 0) {
            setCurrent_map();
        }
    }

    // Game Methods
    public void setProperties(){
        setCurrent_map();
        if(getCurrent_mobId() != -1){
            setCurrent_mob();
        }
    }

    public boolean unlockMap(){
        List<Integer> unlocked_maps = myPlayer.getUnlockedMaps();
        int nextMapId = unlocked_maps.get(unlocked_maps.size() - 1).intValue() + 1;
        if (nextMapId < maps.size()) {
            myPlayer.unlockMap(nextMapId);
            return true;
        }
        return false;
    }

    public boolean isMapUnlocked(int mapId) {
        return mapId < maps.size() && myPlayer.isMapUnlock(mapId);
    }

    public void unlockSkillsByLevel() {
        int levelId = myPlayer.getLevel()-1;
        if (levelId < levels.size()) {
            Level currentLevel = levels.get(levelId);
            for (int buff_id : currentLevel.getBuffs()) {
                myPlayer.unlockBuffSkill(buff_id);
            }
            for (int attack_id : currentLevel.getAttacks()) {
                myPlayer.unlockAttackSkill(attack_id);
            }
        }
    }

    public void switchMap(int id){
        setCurrent_mapById(id);
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
        int next_level_exp = new_exp - myPlayer.getTotal_exp();
        if(next_level_exp >= 0){
            myPlayer.levelUp(next_level_exp);
            return true;
        }else{
            myPlayer.setExp(new_exp);
            return false;
        }
    }

    public Buff getBuff(int id){
        return buffs.get(id);
    }
    public Attack getAttack(int id){
        return attacks.get(id);
    }

    public Mob getMob(int id){
        return mobs.get(id);
    }

    public Boolean isBoss(){
        double boss_rate = ThreadLocalRandom.current().nextInt(0, 100 + 1);
        return boss_rate < 5;
    }

    public void resetSkill() {
        for(Buff buff : buffs) {
            buff.setIn_use(false);
        }
        for(Attack attack : attacks) {
            attack.setIn_use(false);
        }
    }
}
