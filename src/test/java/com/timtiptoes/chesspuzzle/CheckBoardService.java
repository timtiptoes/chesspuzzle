package com.timtiptoes.chesspuzzle;


import com.timtiptoes.services.BoardService;
import org.apache.tomcat.util.http.parser.Ranges;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CheckBoardService {

    //@Test
    public void lookupLetterTests() {
        assertEquals("a", BoardService.lookup_letter("a8"));
        assertEquals("h", BoardService.lookup_letter("b4"));
        assertEquals("j", BoardService.lookup_letter("f1"));
        assertEquals("l", BoardService.lookup_letter("h1"));
    }
    //@Test
    public void iterate(){
        //List<String> letters = new ArrayList<String>();
        for (int letterCode = 97; letterCode < 105; letterCode++) {
            char letter= (char) letterCode;
            for (int row = 0; row < 8; row++) {
                System.out.println(String.valueOf(letter) + (row+1));
            }
        }
    }

    @Test
    public void populate_map(){

        BoardService bs = new BoardService();
        //System.out.println(bs.getLetterMap().toString());
        System.out.println("for f I get "+bs.getCoordinate("f"));
        System.out.println("for f I get "+bs.getCoordinate("f"));
        System.out.println("for f I get "+bs.getCoordinate("f"));

    }
}
