package com.timtiptoes.services;

import com.timtiptoes.chesspuzzle.RestControllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private Connection connect() {
		// // SQLite connection string
		String url = "jdbc:sqlite:/Users/206660287/Documents/personal/chess_puzzle/chesspuzzle/src/main/resources/static/lichessDB.db";
		// String url =
		// "jdbc:sqlite:/media/tim/JetsonSSD-500/git_repos/chess_puzzle/src/main/resources/static/lichessDB.db";

		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			log.debug(e.getMessage());
		}
		return conn;
	}

	public JSONObject query(int mateNum, String lastMove) {

		StringBuilder str = new StringBuilder();
		/*
		 * CREATE TABLE mateIn ( PuzzleId TEXT, FEN TEXT, moves TEXT, Rating INT,
		 * RatingDeviation INT, Popularity INT, NbPlays INT, Themes TEXT, GameUrl TEXT,
		 * OpeningTags TEXT, lastMove );
		 */
		JSONObject obj = new JSONObject();

		try {
			Connection conn = this.connect();
			Statement pstmt = conn.createStatement();
			String sql = String.format("SELECT FEN, moves, PuzzleId FROM mateIn WHERE lastMove = '%s'", lastMove)
					+ "AND INSTR(Themes,'mateIn" + Integer.valueOf(mateNum)
					+ "') and rowid > (ABS(RANDOM()) % (SELECT max(rowid) FROM mateIn)) LIMIT 1;";
			log.info(String.format("About to execute:%s", sql));

			ResultSet rs = pstmt.executeQuery(sql);


			while (rs.next()) {
				obj.put("fen", rs.getObject("FEN"));
				obj.put("moves", rs.getObject("moves"));
				obj.put("puzzle_id", rs.getObject("PuzzleId"));
				obj.put("mateNum", mateNum);
			}
		}

		catch (Exception e) {
			log.debug(e.toString());
		}
		return obj;

	}

	public String randomWord() {

		StringBuilder str = new StringBuilder();

		try {
			Connection conn = this.connect();
			Statement pstmt = conn.createStatement();
			String sql = "SELECT word as word FROM words  WHERE   length(word) > 6 and rowid > (ABS(RANDOM()) % (SELECT max(rowid) FROM words)) LIMIT 1;";
			log.info(String.format("About to find word with:%s", sql));
			System.out.println(String.format("About to find word with:%s", sql));

			ResultSet rs = pstmt.executeQuery(sql);
			while (rs.next()) {
				str.append(rs.getObject("word"));

			}
		} catch (Exception e) {
			log.debug(e.toString());
			return e.toString();
		}
		return str.toString();

	}

}
