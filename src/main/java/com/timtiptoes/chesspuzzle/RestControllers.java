package com.timtiptoes.chesspuzzle;

import com.timtiptoes.helpers.SheetHelper;
import com.timtiptoes.services.BoardService;
import com.timtiptoes.services.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;


/*
Tim! 

Here's the general idea

The full flow for /make/2/lm/random:

Word selection — StorageService.randomWord() queries the words table in lichessDB.db for a random word longer than 6 characters. If you pass a category name instead of "random" (e.g. /make/2/lm/animals), it checks categoryExists() first and pulls a word from that category instead.

Letter → coordinate mapping — For each letter in the word, BoardService.getCoordinate(letter) picks a random chess square (e.g. a1–h8) that maps to that letter. The mapping is mathematical: a board has 64 squares, each square maps to one of 26 letters via (x + 8*y) % 26. Multiple squares can map to the same letter, so it picks one randomly from the matching squares.

Puzzle lookup — For each coordinate, StorageService.query(mateNum, moveType, coordinate) queries the mateIn table for a random puzzle landing on that square AND tagged mateIn2 AND exactly 2*mateNum plies long. moveType picks which end of the puzzle the coordinate refers to: lm is the square the mating move lands on (the lastMove column), fm is the square the solver's first move lands on (the firstMove column). It returns FEN, move sequence, and puzzle ID as JSON.

doit.sh generation — BoardService.drawBoard() writes a shell script (board_render/doit.sh) that runs the Docker container, passing the full JSON array of all puzzles as a single argument.

Docker execution — The Java process executes doit.sh directly via Runtime.getRuntime().exec(), reads stdout line by line, and returns it as the HTTP response body.

Output — The Docker container renders the board positions and writes SVG (or similar) to stdout, which gets returned as the response.

board_key.png — bundled in the jar AND read from it via ClassPathResource. No scp needed, works automatically.

lichessDB.db — also bundled in the jar (it's in src/main/resources/static/), but the SQLite connection opens a file path off disk (CHESSPUZZLE_DB_URL, defaulting to src/main/resources/static/lichessDB.db under the working directory), so the bundled copy is ignored. You already wrote this note to yourself in RestControllers.java: "NOTE! Even though lichessDB.db is included in the jar, it has nothing to do with what's used by the code." You must scp it separately.

Full checklist to deploy to the Jetson tx2:

No source edits needed for the paths any more: run the jar from the checkout root and the defaults in Env.java resolve, or set CHESSPUZZLE_DB_URL / CHESSPUZZLE_RENDER_PATH to point elsewhere, e.g.
	export CHESSPUZZLE_DB_URL=/media/tim/JetsonSSD-500/git_repos/chesspuzzle/src/main/resources/static/lichessDB.db
	export CHESSPUZZLE_RENDER_PATH=/media/tim/JetsonSSD-500/git_repos/chesspuzzle/board_render
Build: ./gradlew bootJar
scp the jar to the Jetson
scp src/main/resources/lichessDB.db to /media/tim/JetsonSSD-500/git_repos/chesspuzzle/src/main/resources/static/lichessDB.db on the Jetson
On the Jetson, make sure the Docker image is built (docker build -t chess:latest . in board_render/)
Run: java -jar chesspuzzle-0.0.1-SNAPSHOT.jar


First you have to make sure there's a chesspuzzle:latest docker image available. So cd into board_render and go
	cd board_render
	docker build -t chess:latest .

test that it works on mac with
	docker run --rm -v /Users/206660287/Documents/personal/chess_puzzle/chesspuzzle/board_render:/tmp chess:latest '1k1r1b1r/1pR5/p1R5/3Np1p1/7p/3PB3/PPP5/7K b - - 0 25' 'b7c6 e3a7 b8a8 d5b6' 'tJ48p' 2 5

test that it works on tx2 with
	docker run --rm -v /media/tim/JetsonSSD-500/git_repos/chesspuzzle/board_render:/tmp chess:latest '[{"fen": "1k1r1b1r/1pR5/p1R5/3Np1p1/7p/3PB3/PPP5/7K b - - 0 25", "moves": "b7c6 e3a7 b8a8 d5b6", "puzzle_id": "tJ48p", "mateNum": 2, "puzzleNum": 5},{"fen": "1k1r1b1r/1pR5/p1R5/3Np1p1/7p/3PB3/PPP5/7K b - - 0 25", "moves": "b7c6 e3a7 b8a8 d5b6", "puzzle_id": "tJ48p", "mateNum": 2, "puzzleNum": 5},{"fen": "1k1r1b1r/1pR5/p1R5/3Np1p1/7p/3PB3/PPP5/7K b - - 0 25", "moves": "b7c6 e3a7 b8a8 d5b6", "puzzle_id": "tJ48p", "mateNum": 2, "puzzleNum": 5},{"fen": "1k1r1b1r/1pR5/p1R5/3Np1p1/7p/3PB3/PPP5/7K b - - 0 25", "moves": "b7c6 e3a7 b8a8 d5b6", "puzzle_id": "tJ48p", "mateNum": 2, "puzzleNum": 5}]'

Nothing has to be changed in the source when switching environments any more:
the paths come from CHESSPUZZLE_DB_URL and CHESSPUZZLE_RENDER_PATH (see Env.java)
and default to the checkout root, and docker runs without sudo on both machines
since tim is in the docker group on the tx2.

To build
	./gradlew bootJar

Visit
	http://localhost:8081/make/2/random


To deploy to tx2
	ssh to tx2 (one time)
		tmux att -t 0
		cd /media/tim/JetsonSSD-5001/git_repos
		git clone https://github.com/timtiptoes/chesspuzzle.git
		mkdir /media/tim/JetsonSSD-500/git_repos/chesspuzzle/src/main/resources/static
	Locally on mac
		git commit -am 'whatever changed'
		git push
		./gradlew bootJar -x test
		scp -i ~/.ssh/id_jetson build/libs/chesspuzzle-0.0.1-SNAPSHOT.jar tim@192.168.68.134:/media/tim/JetsonSSD-500/git_repos/chesspuzzle/.
		scp /Users/206660287/Documents/personal/chesspuzzle/src/main/resources/static/board_key.png  tim@192.168.68.134:/media/tim/JetsonSSD-500/git_repos/chesspuzzle/src/main/resources/static/.
		scp -i ~/.ssh/id_jetson /Users/206660287/Documents/personal/github/chesspuzzle/src/main/resources/static/lichessDB.db tim@192.168.68.134:/media/tim/JetsonSSD-500/git_repos/chesspuzzle/src/main/resources/static/.
		tmux att -t 0
		cd /media/tim/JetsonSSD-5001/git_repos/chesspuzzle/board_render
		docker build -t chess:latest .
		touch board_render/doit.sh
		chmod 777 board_render/doit.sh
		java -jar chesspuzzle-0.0.1-SNAPSHOT.jar
		cd ..
	
	1/17/2026
		build locally
			cd /Users/timlevine/Documents/github/chesspuzzle
			./gradlew bootJar
			scp build/libs/starter_web-0.0.1-SNAPSHOT.jar tim@192.168.68.60:/mnt/Jetson-500/git_repos/chesspuzzle/chesspuzzle-0.0.1-SNAPSHOT.jar

	NOTE! Even though lichessDB.db is included in the jar, it has nothing to do with what's used by the code. Instead modify the local copy of lichessDB.db and move it up to 
		  the tx2 like this
		  		scp src/main/resources/lichessDB.db tim@192.168.68.60:/media/tim/JetsonSSD-500/git_repos/chesspuzzle/src/main/resources/static/lichessDB.db

 */
@RestController
@Validated
public class RestControllers {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@RequestMapping(value = "/hello/{name}", method = RequestMethod.GET)
	public String helloWorld(@PathVariable(value = "name") String name) {
		return "Hello World," + name + " from Spring Boot";

	}

	@RequestMapping(value = "/listcategories", method = RequestMethod.GET)
	public String ListCategories(HttpServletResponse response) {
		StorageService storageService = new StorageService();

		return storageService.listCategories();

	}

	@RequestMapping(value = "/key", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
	public void getImage(HttpServletResponse response) throws IOException {

		var imgFile = new ClassPathResource("static/board_key.png");

		response.setContentType(MediaType.IMAGE_PNG_VALUE);
		StreamUtils.copy(imgFile.getInputStream(), response.getOutputStream());
	}

	@RequestMapping(value = "/lastmove/{mateNum}/{lastMove}", method = RequestMethod.GET)
	public String getLastMove(@PathVariable @Min(1) @Max(5) int mateNum,
			@PathVariable @Pattern(regexp = "^[a-h][1-8]$") String lastMove) {
		StorageService storageService = new StorageService();
		log.info(String.format("This is lastmove and I got mateNum:%s and lastMove:%s", mateNum, lastMove));

		return storageService.query(mateNum, lastMove).toString();

	}

	@RequestMapping(value = "/drawit/{mateNum}/{lastMove}", method = RequestMethod.GET)
	public String drawIt(@PathVariable @Min(1) @Max(5) int mateNum,
			@PathVariable @Pattern(regexp = "^[a-h][1-8]$") String lastMove) {

		log.info(String.format("This is drawit and I got mateNum:%s and lastMove:%s", mateNum, lastMove));

		StorageService storageService = new StorageService();

		JSONObject jsonObject = storageService.query(mateNum, lastMove);
		JSONArray jsonList = new JSONArray();
		jsonList.put(jsonObject);
		BoardService boardService = new BoardService();
		return boardService.drawBoard(jsonList);

	}

	@RequestMapping(value = "/make/{mateNum}/{moveType}/{rawClue}", method = RequestMethod.GET)
	public String make(@PathVariable @Min(1) @Max(5) int mateNum,
			@PathVariable @Pattern(regexp = "^(fm|lm)$") String moveType,
			@Pattern(regexp = "^[a-z]{1,21}$") @PathVariable String rawClue) {

		log.info(String.format("This is make and I got mateNum:%s moveType:%s and clue:%s", mateNum, moveType, rawClue));

		String FEN = "";
		String letter;
		String coordinate;
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonList = new JSONArray();

		
		// TODO is this bad not use autowired?
		String clue = rawClue.replace(" ", "").toLowerCase();
		StorageService storageService = new StorageService();
		BoardService boardService = new BoardService();

		if (clue.equalsIgnoreCase("random")){
			log.info("Choosing random word");
			clue= storageService.randomWord();
		}
		else if (storageService.categoryExists((clue))) {
			log.info("Back from categoryExists and it must be true");
			log.info(String.format("Found %s using category %s",clue,rawClue));
			clue = storageService.getCategoryWord(clue);
			log.info(String.format("Found %s using category %s",clue,rawClue));
		}
		else {
			log.info(String.format("Could not find category %s",rawClue));
		}

		log.info(String.format("About to make puzzle with %s",clue));
		
		for (int i = 1; i <= clue.length(); i++) {
			letter = String.valueOf(clue.charAt(i - 1));
			coordinate = boardService.getCoordinate(letter);
			jsonObject = storageService.query(mateNum, moveType, coordinate);
			jsonList.put(jsonObject);
		}

		return boardService.drawBoard(jsonList);

	}

	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException e) {
		return new ResponseEntity<>("not valid due to validation error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
	}
}