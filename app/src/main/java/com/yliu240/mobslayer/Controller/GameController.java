package com.yliu240.mobslayer.Controller;

import com.google.gson.annotations.Expose;

import com.google.gson.annotations.SerializedName;
import com.yliu240.mobslayer.Model.Level;
import com.yliu240.mobslayer.Model.Map;
import com.yliu240.mobslayer.Model.Mob;
import com.yliu240.mobslayer.Model.Player;
import com.yliu240.mobslayer.Model.Skill;

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
    @SerializedName("player_info")
    private Player myPlayer = Player.getInstance(); //Singleton
    @SerializedName("mob_info")
    private List<Mob> mobs = new ArrayList<>();
    @SerializedName("level_info")
    private List<Level> levels = new ArrayList<>();
    @SerializedName("skill_info")
    private List<Skill> skills = new ArrayList<>();

    // Other properties of GameController
    @Expose(serialize = false, deserialize = false)
    private Mob current_mob = new Mob();
    @Expose(serialize = false, deserialize = false)
    private Level current_level = new Level();
    private int current_levelId;
    private int current_mobId;
    private int current_mobHp;

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


    // Getter methods
    public List<Skill> getSkills(){
        return this.skills;
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
        setCurrent_mob();
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
        setCurrent_mob();
        setCurrent_level();
    }

    public void updateLevel(int nextId){
        int currId = nextId-1;
        Level lvl = levels.get(currId);
        lvl.setNext(nextId);
        levels.set(currId, lvl); // is this necessary?
        this.current_levelId = nextId;
        setCurrent_level();
    }

    public void switch_level(int id){
        setCurrent_levelId(id);
        setCurrent_level();
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
    
    
    public Pair<Integer,Boolean> attackMob(){
        int damage = (int) ThreadLocalRandom.current().nextDouble(myPlayer.getAttack()*0.5,  (myPlayer.getAttack() + 1)*1.5);
        Boolean critical = isCritical();
        if(critical){
            damage *= myPlayer.getAttack_multiplier();
        }
        if(damage > 999999){
            damage = 999999;
        }
        this.current_mob.takeDamage(damage);
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

    public Skill getSkill(int id){
        return skills.get(id);
    }

    public Mob getMob(int id){
        return mobs.get(id);
    }

    public Boolean hasNextLevel(){
        return getLevels().size() >= myPlayer.getLevel();
    }
}
