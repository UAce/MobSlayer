package com.yliu240.mobslayer.Model;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yu-yu on 2019-02-12.
 */
// Could create sub-classes that extend Skill
// e.g. Buff, Attack or just split into two classes
public class Skill {
    private String name;
    private String message;
    private String sound_effect;
    private double attack_multiplier;
    private int attack_timing;
    private int width;
    private int height;
    private int cooldown;
    private Boolean in_use = false;

    // Maybe Pass a data structure with all the infos
    public Skill(){}

    // Getter Methods
    public String getName(){ return this.name; }
    public String getMessage(){ return this.message; }
    public String getSound_effect(){
        return this.sound_effect;
    }
    public double getAttack_multiplier(){
        return this.attack_multiplier;
    }
    public int getWidth(){ return this.width; }
    public int getHeight(){ return this.height; }
    public int getCooldown() {
        return this.cooldown;
    }

    // Setter Methods
    public void setName(String name){
        this.name = name;
    }
    public void setIn_use(){
        if(!this.in_use) {
            this.in_use = true;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    in_use = false;
                }
            }, this.cooldown);
        }
    }

    // Other methods
    public Boolean isCooldown(){
        return this.in_use;
    }
    public void resetCooldown(){ this.in_use = false; }
    public int getAttack_timing() {
        return this.attack_timing;
    }
}
