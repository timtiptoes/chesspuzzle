package com.timtiptoes.chesspuzzle;

import com.timtiptoes.helpers.SheetHelper;
import com.timtiptoes.services.BoardService;
import com.timtiptoes.services.StorageService;
import lombok.var;
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

First you have to make sure there's a chesspuzzle:latest docker image available. So cd into board_render and go
	cd board_render
	docker build -t chess:latest .

test that it works with
	docker run --rm -v /Users/206660287/Documents/personal/chess_puzzle/chesspuzzle/board_render:/tmp chess:latest '1k1r1b1r/1pR5/p1R5/3Np1p1/7p/3PB3/PPP5/7K b - - 0 25' 'b7c6 e3a7 b8a8 d5b6' 'tJ48p' 2 5

There are three things you have to change when switching environments
path in storage service
path in board service
remove sudo in docker run also in board service

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
		Change the path in BoardService and StorageSerivce
		Put in sudo in BoardService for the docker run command
		git commit -am 'modified paths'
		git push
		./gradlew bootJar -x test
		scp build/libs/chesspuzzle-0.0.1-SNAPSHOT.jar tim@192.168.68.134:/media/tim/JetsonSSD-5001/git_repos/chesspuzzle/.
		scp /Users/206660287/Documents/personal/chesspuzzle/src/main/resources/static/lichessDB.db  tim@192.168.68.134:/media/tim/JetsonSSD-500/git_repos/chesspuzzle/src/main/resources/static/.
		scp /Users/206660287/Documents/personal/chesspuzzle/src/main/resources/static/board_key.png  tim@192.168.68.134:/media/tim/JetsonSSD-500/git_repos/chesspuzzle/src/main/resources/static/.
	ssh to tx2
		tmux att -t 0
		cd /media/tim/JetsonSSD-5001/git_repos/chesspuzzle/board_render
		docker build -t chess:latest .
		java -jar chesspuzzle-0.0.1-SNAPSHOT.jar
		cd ..
	
	

 */
@RestController
@Validated
public class RestControllers {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@RequestMapping(value = "/hello/{name}", method = RequestMethod.GET)
	public String helloWorld(@PathVariable(value = "name") String name) {
		return "Hello World," + name + " from Spring Boot";

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

	@RequestMapping(value = "/make/{mateNum}/{rawClue}", method = RequestMethod.GET)
	public String make(@PathVariable @Min(1) @Max(5) int mateNum,
			@Pattern(regexp = "^[a-z]{1,21}$") @PathVariable String rawClue) {

		log.info(String.format("This is make and I got mateNum:%s and lastMove:%s", mateNum, rawClue));

		String FEN = "";
		String letter;
		String lastMove;
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonList = new JSONArray();

		
		// TODO is this bad not use autowired?
		String clue = rawClue.replace(" ", "").toLowerCase();
		StorageService storageService = new StorageService();
		BoardService boardService = new BoardService();

		if (clue.equals("random")) {
			clue = storageService.randomWord();
		}

		
		
		for (int i = 1; i <= clue.length(); i++) {
			letter = String.valueOf(clue.charAt(i - 1));
			lastMove = boardService.getCoordinate(letter);
			jsonObject = storageService.query(mateNum, lastMove);
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