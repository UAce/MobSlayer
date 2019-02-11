package com.yliu240.painbutton.Model;

/**
 * Created by yu-yu on 2019-02-07.
 */

public class Mob {
    private String name;
    private int total_hp;
    private int current_hp;
    private int exp;
    private int offsetX;
    private int offsetY;

    private String move;
    private String spawn;
    private String death;
    private String hit;

    private String spawn_sound;
    private String death_sound;
    private String hit_sound;

    // Maybe pass a data Structure with all the infos
    public Mob(){}

    // Getter Methods
    public String getName(){
        return this.name;
    }
    public int getTotal_hp(){
        return this.total_hp;
    }
    public int getCurrent_hp(){
        return this.current_hp;
    }
    public int getExp(){
        return this.exp;
    }
    public int getOffsetX(){
        return this.offsetX;
    }
    public int getOffsetY(){
        return this.offsetY;
    }
    public String getMove(){
        return this.move;
    }
    public String getSpawn(){
        return this.spawn;
    }
    public String getDeath(){
        return this.death;
    }
    public String getHit(){
        return this.hit;
    }
    public String getSpawn_sound(){
        return this.spawn_sound;
    }
    public String getDeath_sound(){
        return this.death_sound;
    }
    public String getHit_sound(){
        return this.hit_sound;
    }


    // Setter Methods
    public void setCurrent_hp(int hp){
         this.current_hp=hp;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setTotal_hp(int hp){
        this.total_hp = hp;
    }
    public void setExp(int exp){
        this.exp = exp;
    }
    public void setOffsetX(int x){
        this.offsetX = x;
    }
    public void setOffsetY(int y){
        this.offsetY = y;
    }
    public void setMove(String move){
        this.move = move;
    }
    public void setSpawn(String spawn){
        this.spawn = spawn;
    }
    public void setDeath(String death){
        this.death = death;
    }
    public void setHit(String hit){
        this.hit = hit;
    }
    public void setSpawn_sound(String spawn_sound){
        this.spawn_sound = spawn_sound;
    }
    public void setDeath_sound(String death_sound){
        this.death_sound = death_sound;
    }
    public void setHit_sound(String hit_sound){
        this.hit_sound = hit_sound;
    }

    // Other methods
    public void resetHP(){
        this.current_hp = this.total_hp;
    }
    public String getHP_percent_string(){
        return String.valueOf(this.current_hp) + "/" + String.valueOf(this.total_hp);
    }

    public float getHP_percent() {
        return ((float) this.current_hp / (float) this.total_hp)*100.0f;
    }

    public Boolean isDead(){
        return this.current_hp==0;
    }

    public void takeDamage(int damage){
        if (this.current_hp - damage > 0) {
            this.current_hp -= damage;
        } else {
            this.current_hp = 0;
        }
    }

}
