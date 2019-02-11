package com.yliu240.painbutton.Model;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yu-yu on 2019-02-08.
 */

public class gameInfo {


    List<MapLevel> maps = new ArrayList<MapLevel>();

    List<Mob> mobs = new ArrayList<Mob>();

    Player player = Player.getInstance(); //Singleton

    public gameInfo() {}


    @JsonProperty(value="map_info")
    public List<MapLevel> getMaps(){
        return this.maps;
    }
    @JsonProperty(value="mob_info")
    public List<Mob> getMobs(){
        return this.mobs;
    }
    @JsonProperty(value="player_info")
    public Player getPlayer() {
        return this.player;
    }
}

