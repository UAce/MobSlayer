package com.yliu240.mobslayer.Model;

/**
 * Created by yu-yu on 2019-10-20.
 */
public class Attack extends Skill {
    private int attack_multiplier;
    private int attack_count;
    private long delay;

    // Maybe Pass a data structure with all the infos
    public Attack(){}

    // Getter Methods
    public int getAttack_multiplier(){ return this.attack_multiplier; }
    public int getAttack_count(){ return this.attack_count; }
    public long getDelay(){ return this.delay; }

    // Setter Methods
}
