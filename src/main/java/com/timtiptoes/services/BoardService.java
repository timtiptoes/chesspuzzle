package com.timtiptoes.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.json.JSONArray;

import com.timtiptoes.helpers.Env;

public class BoardService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	Map<String, List<String>> letterMap = new HashMap<String, List<String>>();

	public Map<String, List<String>> getLetterMap() {
		return letterMap;
	}

	public BoardService() {

		// Initialize map
		for (int letterCode = 97; letterCode < 123; letterCode++) {
			char letter = (char) letterCode;
			letterMap.put(String.valueOf(letter), new ArrayList<String>());
		}
		// Populate map
		for (int letterCode = 97; letterCode < 105; letterCode++) {
			char letter = (char) letterCode;
			for (int row = 0; row < 8; row++) {
				String coordinate = String.valueOf(letter) + (row + 1);
				String lookedUpLetter = BoardService.lookup_letter(coordinate);
				letterMap.get(lookedUpLetter).add(coordinate);

			}
		}

	}

	public String getCoordinate(String letter) {
		List letterList = letterMap.get(letter);
		Random rand = new Random();
		String coordinate = (String) letterList.get(rand.nextInt(letterList.size()));
		return coordinate;
	}

	public static String lookup_letter(String coordinate) {
		int x = (int) coordinate.charAt(0) - 97;
		int y = 7 - (Integer.parseInt(String.valueOf(coordinate.charAt(1))) - 1);
		int letter_index = (x + 8 * y) % 26;
		char letter = (char) (letter_index + 97);
		return String.valueOf(letter);

	}

	public String drawBoard(JSONArray jsonList) {
        StringBuilder str = new StringBuilder();

        //set CHESSPUZZLE_RENDER_PATH to point somewhere else, it defaults to board_render under the working directory
        String render_path=Env.renderPath();
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(render_path + "/doit.sh"));
            //sudo is for tx2, set CHESSPUZZLE_DOCKER_CMD to "sudo docker" there
        writer.write(String.format(Env.dockerCmd()+" run --rm -v "+render_path+":/tmp chess:latest '%s'",jsonList.toString()));
          writer.close();
          new File(render_path + "/doit.sh").setExecutable(true);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        String command=render_path+"/doit.sh";

        log.info("in drawBoard and about to execute "+command);
        try {
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                str.append(line);
            }

            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    return str.toString();
    }

}