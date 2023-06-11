package com.timtiptoes.services;

public class ClueService {

    public String prepareClue(String rawClue){
        String clue = rawClue.replace(" ","").toLowerCase();
        return clue;
    }

}
