package com.yliu240.mobslayer.Model;

/**
 * Created by yu-yu on 2019-02-12.
 */

public class Level {
    private String name;
    private String bgm_name;
    private String bg_image;
    private int prev;
    private int next;
    private int map;
    private int[] mobs;
    private int boss;
    private int[] buffs;

    // Maybe Pass a data structure with all the infos
    public Level(){}

    // Getter Methods
    public String getName(){
        return this.name;
    }
    public String getBgm_name(){
        return this.bgm_name;
    }
    public String getBg_image(){
        return this.bg_image;
    }
    public int getPrev(){
        return this.prev;
    }
    public int getNext(){
        return this.next;
    }
    public int getMap(){
        return this.map;
    }
    public int[] getMobs(){
        return this.mobs;
    }
    public int getBoss(){ return this.boss; }
    public int[] getBuffs(){
        return this.buffs;
    }

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
    public void setPrev(int id){
        this.prev = id;
    }
    public void setNext(int id){
        this.next = id;
    }

}
