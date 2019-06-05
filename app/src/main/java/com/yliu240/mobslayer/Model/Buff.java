package com.yliu240.mobslayer.Model;

/**
 * Created by yu-yu on 2019-02-12.
 */

public class Buff {
    private String name;
    private String icon_enable;
    private String icon_disable;
    private String sound_effect;

    // Maybe Pass a data structure with all the infos
    public Buff(){}

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

    // Setter Methods
    public void setName(String name){
        this.name = name;
    }

}
