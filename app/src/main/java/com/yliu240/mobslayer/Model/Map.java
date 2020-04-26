package com.yliu240.mobslayer.Model;

/**
 * Created by yu-yu on 2019-02-07.
 */

public class Map {
    private String name;
    private String bgm_name;
    private String bg_image;
    private int[] mobs;
    private int boss;
    private int[] buffs;
    private int[] attacks;

    // Maybe Pass a data structure with all the infos
    public Map(){}
    public Map(String name, String bgm_name, String bg_image){
        this.name = name;
        this.bgm_name = bgm_name;
        this.bg_image = bg_image;
    }

    // Getter Methods
    public String getName(){ return this.name; }
    public String getBgm_name(){ return this.bgm_name; }
    public String getBg_image(){ return this.bg_image; }
    public int[] getMobs(){ return this.mobs; }
    public int getBoss(){ return this.boss; }
    public int[] getBuffs(){ return this.buffs; }
    public int[] getAttacks() { return this.attacks; }

    // Setter Methods
    public void setName(String name){
        this.name = name;
    }
    public void setBgm_name(String bgm_name){
        this.bgm_name = bgm_name;
    }
    public void setBg_image(String bg_image){
        this.bg_image = bg_image;
    }
}
