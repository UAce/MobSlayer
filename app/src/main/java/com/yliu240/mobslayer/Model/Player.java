package com.yliu240.mobslayer.Model;

import java.util.Timer;
import java.util.TimerTask;

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
    private Boolean buffed=false;

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
        if(buffed){
            return this.critical_rate+50;
        }else{
            return this.critical_rate;
        }
    }
    public Boolean getBuffed(){
        return this.buffed;
    }


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
    public void setBuffedCoolDown(){
        if(!this.buffed) {
            this.buffed = true;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    buffed = false;
                }
            }, 30000);
        }
    }

    // Other methods
    public void level_up(){
        this.level+=1;
        this.attack*=2.5;
        this.attack_multiplier+=2;
        this.exp=0;
        this.total_exp*=3;
    }
    public String getEXP_toString(){
        return String.valueOf(this.exp);
    }

    public float getEXP_percent() {
        return ((float) this.exp / (float) this.total_exp)*100.0f;
    }
}
