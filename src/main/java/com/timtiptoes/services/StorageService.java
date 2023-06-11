package com.timtiptoes.services;

import com.timtiptoes.chesspuzzle.RestControllers;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(RestControllers.class);

    private Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:/Users/206660287/Documents/personal/chess_puzzle/chesspuzzle/src/main/resources/static/lichessDB.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            log.debug(e.getMessage());
        }
        return conn;
    }

    public String query(String lastMove) {

        StringBuilder str = new StringBuilder();
/*        CREATE TABLE mateIn (
                PuzzleId        TEXT,
                FEN             TEXT,
                moves           TEXT,
                Rating          INT,
                RatingDeviation INT,
                Popularity      INT,
                NbPlays         INT,
                Themes          TEXT,
                GameUrl         TEXT,
                OpeningTags     TEXT,
                lastMove
        );*/
        try {
            Connection conn = this.connect();
            Statement pstmt = conn.createStatement();
            String sql = String.format("SELECT quote(FEN) || ' ' || quote(moves) as FEN FROM mateIn WHERE lastMove = '%s'",lastMove) + "AND INSTR(Themes,'mateIn1') and rowid > (ABS(RANDOM()) % (SELECT max(rowid) FROM mateIn)) LIMIT 1;";

            log.debug(String.format("About to execute:%s", sql));

            ResultSet rs = pstmt.executeQuery(sql);
            while (rs.next()) {
                    str.append(rs.getObject("FEN"));
                }
        }

        catch (Exception e) {
            log.debug(e.toString());
            return e.toString();
        }

        return str.toString();

    }


}
