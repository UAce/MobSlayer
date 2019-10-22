package com.yliu240.mobslayer.Model;

import com.google.gson.annotations.Expose;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yu-yu on 2019-10-20.
 */
public class Buff extends Skill{
    private String message;
    private String buff;
    private int value;
    private int cooldown;
    @Expose(serialize = false, deserialize = false)
    private Boolean isCooldown = false;

    // Maybe Pass a data structure with all the infos
    public Buff(){}

    // Getter Methods
    // Getter Methods
    public String getMessage(){ return this.message; }
    public String getBuff(){ return this.buff; }
    public int getValue() { return this.value; }
    public int getCooldown() { return this.cooldown; }
    public Boolean getIsCooldown() { return this.isCooldown; }

    // Setter Methods
    public void startCooldown() {
        if(!this.isCooldown) {
            this.isCooldown = true;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    isCooldown = false;
                }
            }, this.cooldown);
        }
    }
    public void resetCooldown(){ this.isCooldown = false; }

}
