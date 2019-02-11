package com.yliu240.painbutton.Controller;

import com.google.gson.annotations.Expose;
import android.content.Context;
import android.media.MediaPlayer;

import com.google.gson.annotations.SerializedName;
import com.yliu240.painbutton.Model.MapLevel;
import com.yliu240.painbutton.Model.Mob;
import com.yliu240.painbutton.Model.Player;

import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by yu-yu on 2019-02-07.
 */

public class GameController {

    // Fields in JSON file
    @SerializedName("map_info")
    private List<MapLevel> maps = new ArrayList<>();
    @SerializedName("mob_info")
    private List<Mob> mobs = new ArrayList<>();
    @SerializedName("player_info")
    private Player myPlayer = Player.getInstance(); //Singleton
    private int current_mobId;
    private int current_mapId;

    // Other properties of GameController
    @Expose(serialize = false, deserialize = false)
    private Mob current_mob;
    @Expose(serialize = false, deserialize = false)
    private MapLevel current_map;
    @Expose(serialize = false, deserialize = false)
    private Mob currentMob;
    @Expose(serialize = false, deserialize = false)
    private Context context;
    @Expose(serialize = false, deserialize = false)
    private MediaPlayer bgm;


    private static GameController gameControllerInstance;

    private GameController() {}

    public static synchronized GameController getInstance(){
        if(gameControllerInstance == null){
            gameControllerInstance = new GameController();
        }
        return gameControllerInstance;
    }

    static public synchronized void setInstance(GameController newInstance, Context mContext) {
        gameControllerInstance = newInstance;
        gameControllerInstance.context = mContext;
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
        Mob tmp_mob = mobs.get(idx);
        this.current_mob = new Mob();
        this.current_mob.setName(tmp_mob.getName());
        this.current_mob.setTotal_hp(tmp_mob.getTotal_hp());
        this.current_mob.setCurrent_hp(tmp_mob.getCurrent_hp());
        this.current_mob.setExp(tmp_mob.getExp());
        this.current_mob.setOffsetX(tmp_mob.getOffsetX());
        this.current_mob.setOffsetY(tmp_mob.getOffsetY());
        this.current_mob.setMove(tmp_mob.getMove());
        this.current_mob.setSpawn(tmp_mob.getSpawn());
        this.current_mob.setDeath(tmp_mob.getDeath());
        this.current_mob.setHit(tmp_mob.getHit());
        this.current_mob.setSpawn_sound(tmp_mob.getSpawn_sound());
        this.current_mob.setDeath_sound(tmp_mob.getDeath_sound());
        this.current_mob.setHit_sound(tmp_mob.getHit_sound());
    }

    public void setCurrent_map(int idx){
        MapLevel tmp_map = maps.get(idx);
        this.current_map = new MapLevel();
        this.current_map.setName(tmp_map.getName());
        this.current_map.setBg_image(tmp_map.getBg_image());
        this.current_map.setBgm_name(tmp_map.getBgm_name());
    }


    // Game Methods
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

    public void createBgm(){
        if (bgm != null){
            bgmCtrl("stop");
        }
        bgm = MediaPlayer.create(this.context, getDrawableId(current_map.getBgm_name(), "raw"));
        bgm.start();
        bgm.setLooping(true);
    }

    public void bgmCtrl(String action){
        switch (action){
            case "stop":
                bgm.stop();
                break;
            case "pause":
                bgm.pause();
                break;
            case "start":
                bgm.start();
                break;
            default:
                bgm.stop();
                break;
        }
    }

    private int getDrawableId(String name, String type){
        return this.context.getResources().getIdentifier(name, type, this.context.getPackageName());
    }
}
