package com.timtiptoes.chesspuzzle;

import static org.junit.jupiter.api.Assertions.*;

import com.timtiptoes.services.StorageService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.json.JSONObject;

class CheckStorageService {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@Test
	void test() {

		int mateNum = 2;
		String lastMove = "d7";
		StorageService storageService = new StorageService();
		JSONObject json = storageService.query(mateNum, lastMove);
		System.out.println("I got " + json.toString());

	}
}
