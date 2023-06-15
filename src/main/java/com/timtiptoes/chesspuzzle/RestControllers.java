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

/*
Tim! There are three things you have to change when switching environments
path in storage service
path in board service
remove sudo in docker run



 */
@RestController
@Validated
public class RestControllers {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/hello/{name}", method = RequestMethod.GET)
    public String helloWorld(@PathVariable(value = "name") String name) {
        return "Hello World," + name + " from Spring Boot";

    }


    @RequestMapping(value = "/key", method = RequestMethod.GET,
            produces = MediaType.IMAGE_PNG_VALUE)
    public void getImage(HttpServletResponse response) throws IOException {

        var imgFile = new ClassPathResource("static/board_key.png");

        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        StreamUtils.copy(imgFile.getInputStream(), response.getOutputStream());
    }

    @RequestMapping(value = "/lastmove/{mateNum}/{lastMove}", method = RequestMethod.GET)
    public String getLastMove(@PathVariable @Min(1) @Max(5) int mateNum, @PathVariable @Pattern(regexp = "^[a-h][1-8]$") String lastMove) {
        StorageService storageService = new StorageService();
        log.info(String.format("This is lastmove and I got mateNum:%s and lastMove:%s",mateNum,lastMove));

        return storageService.query(mateNum,lastMove);

    }

    @RequestMapping(value = "/drawit/{mateNum}/{lastMove}", method = RequestMethod.GET)
    public String drawIt(@PathVariable @Min(1) @Max(5) int mateNum, @PathVariable @Pattern(regexp = "^[a-h][1-8]$") String lastMove) {

        log.info(String.format("This is drawit and I got mateNum:%s and lastMove:%s",mateNum,lastMove));

        StorageService storageService = new StorageService();

        String FEN = storageService.query(mateNum,lastMove);
        BoardService boardService = new BoardService();
        return boardService.drawBoard(FEN,0);

    }

    @RequestMapping(value = "/make/{mateNum}/{rawClue}", method = RequestMethod.GET)
    public String make(@PathVariable @Min(1) @Max(5)  int mateNum, @Pattern(regexp = "^[a-z]{1,21}$") @PathVariable String rawClue) {

        log.info(String.format("This is make and I got mateNum:%s and lastMove:%s",mateNum,rawClue));

        String FEN = "";
        String letter;
        String lastMove;
        ArrayList<String> outputDoc = new ArrayList<String>();

    //TODO is this bad not use autowired?
        String clue = rawClue.replace(" ", "").toLowerCase();
        StorageService storageService = new StorageService();
        BoardService boardService = new BoardService();
        StringBuilder outputStr = SheetHelper.buildHeader(new StringBuilder(),clue.length());

        for (int i = 0; i < clue.length(); i++) {
            letter = String.valueOf(clue.charAt(i));
            lastMove = boardService.getCoordinate(letter);
            FEN = storageService.query(mateNum,lastMove);
            if (i % 4 == 0) {
                outputStr.append("<tr>");
            }
            outputStr.append("<th>");

            outputStr.append(boardService.drawBoard(FEN,i+1));
            outputStr.append("</th>");
            if (i % 4 == 3) {
                outputStr.append("</tr>");
            }
        }
        outputStr.append("</TABLE></BODY></HTML>");
        return outputStr.toString();

    }
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException e) {
        return new ResponseEntity<>("not valid due to validation error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}