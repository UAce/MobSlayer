package com.yliu240.mobslayer.Model;

/**
 * Created by yu-yu on 2019-02-12.
 */

public class Skill {
    private String name;
    private String icon_enable;
    private String icon_disable;
    private String sound_effect;
    private double attack_multiplier;

    // Maybe Pass a data structure with all the infos
    public Skill(){}

    // Getter Methods
    public String getName(){
        return this.name;
    }
    public String getIcon_enable(){
        return this.icon_enable;
    }
    public String getIcon_disable(){
        return this.icon_disable;
    }
    public String getSound_effect(){
        return this.sound_effect;
    }
    public double getAttack_multiplier(){
        return this.attack_multiplier;
    }

    // Setter Methods
    public void setName(String name){
        this.name = name;
    }
}
