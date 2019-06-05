package com.yliu240.mobslayer.Controller;

import android.util.Log;

import com.google.gson.annotations.Expose;

import com.google.gson.annotations.SerializedName;
import com.yliu240.mobslayer.Model.Buff;
import com.yliu240.mobslayer.Model.MapLevel;
import com.yliu240.mobslayer.Model.Mob;
import com.yliu240.mobslayer.Model.Player;

import org.javatuples.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by yu-yu on 2019-02-07.
 */

public class GameController {

    private static final String TAG = "DEBUG";
    // Fields in JSON file
    @SerializedName("map_info")
    private List<MapLevel> maps = new ArrayList<>();
    private int current_mapId;
    @SerializedName("mob_info")
    private List<Mob> mobs = new ArrayList<>();
    private int current_mobId;
    @SerializedName("player_info")
    private Player myPlayer = Player.getInstance(); //Singleton
    @SerializedName("buff_info")
    private List<Buff> buffs = new ArrayList<>();
//    @SerializedName("skill_info")
//    private List<Skill> skills = new ArrayList<>();

    // Other properties of GameController
    @Expose(serialize = false, deserialize = false)
    private Mob current_mob = new Mob();
    @Expose(serialize = false, deserialize = false)
    private MapLevel current_map = new MapLevel();
    @Expose(serialize = false, deserialize = false)
    private Mob currentMob;

    private static GameController gameControllerInstance;

    private GameController() {}

    public static synchronized GameController getInstance(){
        if(gameControllerInstance == null){
            gameControllerInstance = new GameController();
        }
        return gameControllerInstance;
    }

    static public synchronized void setInstance(GameController newInstance) {
        gameControllerInstance = newInstance;
    }


    // Getter methods
    public List<MapLevel> getMaps(){
        return this.maps;
    }
    public List<Mob> getMobs(){
        return this.mobs;
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
    public MapLevel getCurrent_map(){
        return this.current_map;
    }


    //Setter methods
    public void setCurrent_mobId(int idx){
        this.current_mobId = idx;
    }
    public void setCurrent_mapId(int idx){
        this.current_mapId = idx;
    }
    public void setCurrent_mob(int idx){
        Mob new_mob = mobs.get(idx);
        this.current_mob.setName(new_mob.getName());
        this.current_mob.setTotal_hp(new_mob.getTotal_hp());
        this.current_mob.setCurrent_hp(new_mob.getCurrent_hp());
        this.current_mob.setExp(new_mob.getExp());
        this.current_mob.setOffsetX(new_mob.getOffsetX());
        this.current_mob.setOffsetY(new_mob.getOffsetY());
        this.current_mob.setMove(new_mob.getMove());
        this.current_mob.setSpawn(new_mob.getSpawn());
        this.current_mob.setDeath(new_mob.getDeath());
        this.current_mob.setHit(new_mob.getHit());
        this.current_mob.setSpawn_sound(new_mob.getSpawn_sound());
        this.current_mob.setDeath_sound(new_mob.getDeath_sound());
        this.current_mob.setHit_sound(new_mob.getHit_sound());
    }

    public void setCurrent_map(){
        MapLevel new_map = maps.get(this.current_mapId);
        this.current_map.setName(new_map.getName());
        this.current_map.setBg_image(new_map.getBg_image());
        this.current_map.setBgm_name(new_map.getBgm_name());
    }


    // Game Methods
    public void switch_map(boolean right){
        int mapId = this.current_mapId;
        int len = this.maps.size();
        if(right){
            mapId+=1;
        }else{
            mapId-=1;
        }
        int newMapId = (((mapId % len) + len) % len);

        setCurrent_mapId(newMapId);
        setCurrent_map();
    }
    
    public void getNewMob(){
        int randomMobId = ThreadLocalRandom.current().nextInt(0, this.mobs.size() + 1);
        Mob new_mob = mobs.get(randomMobId);
        this.current_mob.setName(new_mob.getName());
        this.current_mob.setTotal_hp(new_mob.getTotal_hp());
        this.current_mob.setCurrent_hp(new_mob.getCurrent_hp());
        this.current_mob.setExp(new_mob.getExp());
        this.current_mob.setOffsetX(new_mob.getOffsetX());
        this.current_mob.setOffsetY(new_mob.getOffsetY());
        this.current_mob.setMove(new_mob.getMove());
        this.current_mob.setSpawn(new_mob.getSpawn());
        this.current_mob.setDeath(new_mob.getDeath());
        this.current_mob.setHit(new_mob.getHit());
        this.current_mob.setSpawn_sound(new_mob.getSpawn_sound());
        this.current_mob.setDeath_sound(new_mob.getDeath_sound());
        this.current_mob.setHit_sound(new_mob.getHit_sound());
    }
    
    
    public Pair<Integer,Boolean> attackMob(){
        int damage = (int) ThreadLocalRandom.current().nextDouble(myPlayer.getAttack()*0.5,  (myPlayer.getAttack() + 1)*1.5);
        Boolean critical = isCritical();
        if(critical){
            damage *= myPlayer.getAttack_multiplier();
        }
        if(damage > 999999){
            damage = 999999;
        }
        current_mob.takeDamage(damage);
        return new Pair<>(damage, critical);
    }

    public Boolean isCritical(){
        double critical_rate = ThreadLocalRandom.current().nextInt(0, 100 + 1);
        return critical_rate < myPlayer.getCritical_rate();
    }

    public Boolean gainEXP(){
        int new_exp = myPlayer.getExp()+current_mob.getExp();
        if(new_exp >= myPlayer.getTotal_exp()){
            myPlayer.level_up();
            return true;
        }else{
            myPlayer.setExp(new_exp);
            return false;
        }
    }
}
