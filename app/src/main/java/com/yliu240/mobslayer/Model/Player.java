package com.yliu240.mobslayer.Model;

import com.google.gson.annotations.Expose;
import org.javatuples.Pair;

import java.util.Timer;
import java.util.TimerTask;
import java.util.List;

/**
 * Created by yu-yu on 2019-02-07.
 */

public class Player {
    private int level;
    private int exp;
    private int total_exp;
    private double attack;
    private double attack_multiplier;
    private double critical_rate;

    // non-player related states
    private int current_mapId;
    private int current_mobId;
    private int current_mobHp;
    private boolean sound_effect_on;
    private boolean bg_music_on;
    private List<Integer> unlocked_maps;
    private List<Integer> unlocked_buffs;
    private List<Integer> unlocked_attacks;
    
    @Expose(serialize = false, deserialize = false)
    private boolean attack_buffed = false;
    @Expose(serialize = false, deserialize = false)
    private boolean critical_buffed = false;

    private static Player playerInstance;

    private Player() {}

    public static synchronized Player getInstance(){
        if(playerInstance == null){
            playerInstance = new Player();
        }
        return playerInstance;
    }


    // Getter Methods
    public int getLevel(){
        return this.level;
    }
    public int getExp(){
        return this.exp;
    }
    public int getTotal_exp(){
        return this.total_exp;
    }
    public double getAttack(){
        return this.attack;
    }
    public double getAttack_multiplier(){
        return this.attack_multiplier;
    }
    public double getCritical_rate(){
        if(critical_buffed){
            return this.critical_rate+50;
        }else{
            return this.critical_rate;
        }
    }
    public boolean getSound_effect_on() { return this.sound_effect_on; }
    public boolean getBg_music_on() { return this.bg_music_on; }
    public int getCurrent_mapId(){ return this.current_mapId; }
    public int getCurrent_mobId(){ return this.current_mobId; }
    public int getCurrent_mobHp(){ return this.current_mobHp; }
    public List<Integer> getUnlockedMaps(){ return this.unlocked_maps; }
    public List<Integer> getUnlockedBuffs(){ return this.unlocked_buffs; }
    public List<Integer> getUnlockedAttacks(){ return this.unlocked_attacks; }

    // Setter Methods
    public void setLevel(int level){
        this.level = level;
    }
    public void setExp(int exp){
        this.exp = exp;
    }
    public void setTotal_exp(int total_exp){
        this.total_exp = total_exp;
    }
    public void setAttack(double attack){
        this.attack = attack;
    }
    public void setAttack_multiplier(double attack_multiplier){
        this.attack_multiplier = attack_multiplier;
    }
    public void setCritical_rate(double critical_rate){
        this.critical_rate = critical_rate;
    }
    public void setCurrent_mapId(int current_mapId) { 
        this.current_mapId = current_mapId;
    }
    public void setCurrent_mobId(int current_mobId) { 
        this.current_mobId = current_mobId;
    }
    public void setCurrent_mobHp(int current_mobHp) { 
        this.current_mobHp = current_mobHp;
    }
    public void setSound_effect_on(boolean sound_effect_on) { 
        this.sound_effect_on = sound_effect_on;
    }
    public void setBg_music_on(boolean bg_music_on) { 
        this.bg_music_on = bg_music_on;
    }


    // Other methods
    public void levelUp(int newExp){
        this.level+=1;
        this.attack+=10;
        this.attack_multiplier+=0.5;
        this.exp=newExp;
        this.total_exp*=3;
    }
    public String getEXP_toString(){
        return String.valueOf(this.exp);
    }

    public float getEXP_percent() {
        return ((float) this.exp / (float) this.total_exp)*100.0f;
    }

    // Find a way to generalize this
    public void sharp_eyes(){
        if(!this.critical_buffed) {
            this.critical_buffed = true;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    critical_buffed = false;
                }
            }, 30000);
        }
    }
    public void reset_buff(){
        this.critical_buffed = false;
        this.attack_buffed = false;
    }

    // Map, Attack, Buff
    public void unlockMap(int mapId) {
        this.unlocked_maps.add(mapId);
    }
    public void unlockAttackSkill(int attackId) {
        this.unlocked_attacks.add(attackId);
    }
    public void unlockBuffSkill(int buffId) {
        this.unlocked_buffs.add(buffId);
    }

    public boolean isMapUnlock(int mapId) {
        return this.unlocked_maps.contains(mapId);
    }

    public boolean isBuffUnlocked(int buffId) {
        return this.unlocked_buffs.contains(buffId);
    }

    public boolean isAttackUnlocked(int attackId) {
        return this.unlocked_attacks.contains(attackId);
    }

}
