package com.timtiptoes.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
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

	/**
	 * Renders the puzzles as a print-ready PDF and hands back the bytes. main.py
	 * writes the file into the mounted render directory and prints its name, so
	 * nothing binary has to travel over the container's stdout.
	 */
	public byte[] drawBoard(JSONArray jsonList) {

        //set CHESSPUZZLE_RENDER_PATH to point somewhere else, it defaults to board_render under the working directory
        String render_path=Env.renderPath();
        String pdfName = "puzzle-" + UUID.randomUUID() + ".pdf";
        File pdf = new File(render_path, pdfName);
        copyKeyImage(render_path);

        File script = new File(render_path, "doit.sh");
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(script));
            //tim is in the docker group on the tx2, so no sudo needed there either
        writer.write(String.format("docker run --rm -v "+render_path+":/tmp chess:latest '%s' %s",jsonList.toString(),pdfName));
          writer.close();
          script.setExecutable(true);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        log.info("in drawBoard and about to execute "+script.getPath()+" writing "+pdfName);
        try {
            Process process = Runtime.getRuntime().exec(script.getPath());
            String out = drain(process.getInputStream());
            String err = drain(process.getErrorStream());
            int exitValue = process.waitFor();

            if (exitValue != 0 || !pdf.exists()) {
                log.error(String.format("render failed, exit:%s stdout:%s stderr:%s", exitValue, out, err));
                throw new IllegalStateException("could not render the puzzles: " + err);
            }

            byte[] bytes = Files.readAllBytes(pdf.toPath());
            log.info(String.format("rendered %s into %s bytes of pdf", out.trim(), bytes.length));
            return bytes;

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            pdf.delete();
        }
    }

	private String drain(InputStream stream) throws IOException {
		StringBuilder str = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String line;
		while ((line = reader.readLine()) != null) {
			str.append(line).append("\n");
		}
		reader.close();
		return str.toString();
	}

	/**
	 * The decoding key goes on the PDF's first page, and the container can only
	 * see the render directory, so keep a copy of it there.
	 */
	private void copyKeyImage(String render_path) {
		File key = new File(render_path, "board_key.png");
		if (key.exists()) {
			return;
		}
		try (InputStream source = getClass().getResourceAsStream("/static/board_key.png")) {
			if (source == null) {
				log.warn("static/board_key.png is not on the classpath, the pdf will have no key page image");
				return;
			}
			Files.copy(source, key.toPath());
			log.info("copied the board key to " + key.getPath());
		} catch (IOException e) {
			log.warn("could not copy the board key: " + e);
		}
	}

}