package com.yliu240.painbutton.Model;

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
        return this.critical_rate;
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

    // Other methods
    public String getEXP_toString(){
        return String.valueOf(this.exp);
    }

    public float getEXP_percent() {
        return ((float) this.exp / (float) this.total_exp)*100.0f;
    }

}
