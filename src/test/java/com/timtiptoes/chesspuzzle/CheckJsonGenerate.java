package com.timtiptoes.chesspuzzle;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.json.JSONObject;
import org.json.JSONArray;

class CheckJsonGenerate {
//{"fen": "1k1r1b1r/1pR5/p1R5/3Np1p1/7p/3PB3/PPP5/7K b - - 0 25", "moves": "b7c6 e3a7 b8a8 d5b6", "puzzle_id": "tJ48p", "mateNum": 2, "puzzleNum": 5},{"fen": "1k1r1b1r/1pR5/p1R5/3Np1p1/7p/3PB3/PPP5/7K b - - 0 25", "moves": "b7c6 e3a7 b8a8 d5b6", "puzzle_id": "tJ48p", "mateNum": 2, "puzzleNum": 5}
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@Test
	void test() {
		
		JSONObject obj = new JSONObject();
		JSONArray list = new JSONArray();
		
		obj.put("fen", "1k1r1b1r/1pR5/p1R5/3Np1p1/7p/3PB3/PPP5/7K b - - 0 25");
		obj.put("moves", "b7c6 e3a7 b8a8 d5b6");
		obj.put("puzzle_id",  "tJ48p");
		obj.put("mateNum","2");
		
		
		list.put(obj);
		
		String newString=list.toString();
		
		System.out.println(newString);
	}

}
