package com.yliu240.mobslayer.Model;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yu-yu on 2019-10-20.
 *
 * This class is a superClass.
 * Buff and Attack classes both inherit from Skill
 */
public class Skill {
    private String name;
    private String sound_effect;
    private int duration;
    private int width;
    private int height;

    // Maybe Pass a data structure with all the infos
    public Skill(){}

    // Getter Methods
    public String getName(){ return this.name; }
    public String getSound_effect(){ return this.sound_effect; }
    public int getDuration() { return this.duration; }
    public int getWidth(){ return this.width; }
    public int getHeight(){ return this.height; }

    // Setter Methods
    public void setDuration(int duration){ this.duration = duration; }

    public void setName(String name){
            this.name = name;
        }


    // Other methods
}