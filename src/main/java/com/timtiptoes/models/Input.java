package com.timtiptoes.models;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

public class Input {

    @Min(1)
    @Max(5)
    private int mateNum;

    @Pattern(regexp = "^[a-h][1-8]$")
    private String lastmove;

    public int getMateNum() {
        return mateNum;
    }

    public String getLastmove() {
        return lastmove;
    }
}