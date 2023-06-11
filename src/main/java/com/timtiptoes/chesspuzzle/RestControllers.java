package com.timtiptoes.chesspuzzle;

import com.timtiptoes.services.BoardService;
import com.timtiptoes.services.StorageService;
import lombok.var;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


@RestController
public class RestControllers {
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

    @RequestMapping(value = "/lastmove/{lastMove}", method = RequestMethod.GET)
    public String getLastMove(@PathVariable(value = "lastMove") String lastMove) {
        StorageService storageService = new StorageService();

        return storageService.query(lastMove);

    }

    @RequestMapping(value = "/drawit/{lastMove}", method = RequestMethod.GET)
    public String drawIt(@PathVariable(value = "lastMove") String lastMove) {
        StorageService storageService = new StorageService();

        String FEN = storageService.query(lastMove);
        BoardService boardService = new BoardService();
        return boardService.drawBoard(FEN);

    }

    @RequestMapping(value = "/make/{rawClue}", method = RequestMethod.GET)
    public String make(@PathVariable(value = "rawClue") String rawClue) {
        String FEN = "";
        String letter;
        String lastMove;
        ArrayList<String> outputDoc = new ArrayList<String>();


        String clue = rawClue.replace(" ", "").toLowerCase();
        StorageService storageService = new StorageService();
        BoardService boardService = new BoardService();
        StringBuilder outputStr = new StringBuilder("<HTML><BODY><TABLE>");

        for (int i = 0; i < clue.length(); i++) {
            letter = String.valueOf(clue.charAt(i));
            lastMove = boardService.getCoordinate(letter);
            FEN = storageService.query(lastMove);
            if (i % 4 == 0) {
                outputStr.append("<tr>");
            }
            outputStr.append("<th>");

            outputStr.append(boardService.drawBoard(FEN));
            outputStr.append("</th>");
            if (i % 4 == 3) {
                outputStr.append("</tr>");
            }
        }
        outputStr.append("</TABLE></BODY></HTML>");
        return outputStr.toString();

    }
}