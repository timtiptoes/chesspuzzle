package com.timtiptoes.services;

import com.timtiptoes.chesspuzzle.RestControllers;
import com.timtiptoes.helpers.Env;

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
		// SQLite connection string, set CHESSPUZZLE_DB_URL to point somewhere else
		String url = Env.dbUrl();

		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			log.debug(e.getMessage());
		}
		return conn;
	}

	/** The two ends of a puzzle a clue coordinate can point at. */
	public static final String FIRST_MOVE = "fm";
	public static final String LAST_MOVE = "lm";

	/** Kept for callers that only ever ask about the mating move. */
	public JSONObject query(int mateNum, String lastMove) {
		return query(mateNum, LAST_MOVE, lastMove);
	}

	/**
	 * moveType is fm or lm: fm matches the square the solver's first move lands on,
	 * lm the square the mating move lands on.
	 */
	public JSONObject query(int mateNum, String moveType, String coordinate) {

		StringBuilder str = new StringBuilder();
		/*
		 * CREATE TABLE mateIn ( PuzzleId TEXT, FEN TEXT, moves TEXT, Rating INT,
		 * RatingDeviation INT, Popularity INT, NbPlays INT, Themes TEXT, GameUrl TEXT,
		 * OpeningTags TEXT, lastMove, firstMove TEXT );
		 * CREATE INDEX idx_mateIn_lastMove ON mateIn(lastMove);
		 * CREATE INDEX idx_mateIn_firstMove ON mateIn(firstMove);
		 */
		JSONObject obj = new JSONObject();

		try {
			Connection conn = this.connect();
			/*
			 * Lichess tags every mate of five moves or more as "mateIn5", so the theme on
			 * its own also serves up 6, 7 ... 10 move mates. The move list holds the
			 * opponent's first move plus the solution, so a true mate in mateNum is
			 * exactly 2 * mateNum plies; count them as spaces + 1 and filter on that.
			 */
			String sql = "SELECT FEN, moves, PuzzleId FROM mateIn WHERE " + columnFor(moveType) + " = ?"
					+ " AND INSTR(Themes, 'mateIn' || ?)"
					+ " AND length(moves) - length(replace(moves, ' ', '')) + 1 = 2 * ?"
					+ " ORDER BY RANDOM() LIMIT 1;";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, coordinate);
			pstmt.setInt(2, mateNum);
			pstmt.setInt(3, mateNum);
			log.info(String.format("About to execute:%s with %s:%s mateNum:%s", sql, moveType, coordinate, mateNum));

			ResultSet rs = pstmt.executeQuery();


			while (rs.next()) {
				obj.put("fen", rs.getObject("FEN"));
				obj.put("moves", rs.getObject("moves"));
				obj.put("puzzle_id", rs.getObject("PuzzleId"));
				obj.put("mateNum", mateNum);
				obj.put("moveType", moveType);
			}
		}

		catch (Exception e) {
			log.debug(e.toString());
		}
		return obj;

	}

	/**
	 * Column the clue coordinate is matched against. Whitelisted rather than passed
	 * through, since an identifier cannot be bound as a parameter.
	 */
	private static String columnFor(String moveType) {
		if (FIRST_MOVE.equalsIgnoreCase(moveType)) {
			return "firstMove";
		}
		if (LAST_MOVE.equalsIgnoreCase(moveType)) {
			return "lastMove";
		}
		throw new IllegalArgumentException("moveType must be " + FIRST_MOVE + " or " + LAST_MOVE + ", got:" + moveType);
	}

	public String randomWord() {
		Connection conn = null;
		ResultSet rs = null;
		Statement pstmt = null;

		StringBuilder str = new StringBuilder();

		try {
			conn = this.connect();
			pstmt = conn.createStatement();
			String sql = "SELECT word as word FROM words  WHERE   length(word) > 6 and rowid > (ABS(RANDOM()) % (SELECT max(rowid) FROM words)) LIMIT 1;";
			log.info(String.format("About to find word with:%s", sql));
			System.out.println(String.format("About to find word with:%s", sql));

			rs = pstmt.executeQuery(sql);
			while (rs.next()) {
				str.append(rs.getObject("word"));

			}
		} catch (Exception e) {
			log.debug(e.toString());
			return e.toString();
		}
		finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) { /* ignored */}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) { /* ignored */}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) { /* ignored */}
			}
		}
		return str.toString();

	}
	public String listCategories() {
		Connection conn = null;
		ResultSet rs = null;
		Statement pstmt = null;

		StringBuilder str = new StringBuilder();

		try {
			conn = this.connect();
			pstmt = conn.createStatement();
			String sql = "SELECT distinct(category) as category from words order by category;";
			log.info(String.format("About to find distinct categories with:%s", sql));
			System.out.println(String.format("About to find distinct categories with:%s", sql));

			rs = pstmt.executeQuery(sql);
			while (rs.next()) {
				str.append(rs.getObject("category")+"\n");

			}
		} catch (Exception e) {
			log.debug(e.toString());
			return e.toString();
		}
		finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) { /* ignored */}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) { /* ignored */}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) { /* ignored */}
			}
		}
		return str.toString();

	}

	public String getCategoryWord(String category) {

		Connection conn = null;
		ResultSet rs = null;
		Statement pstmt = null;

		StringBuilder str = new StringBuilder();

		try {
			conn = this.connect();
			pstmt = conn.createStatement();
			String sql = String.format("SELECT word as word FROM words  WHERE category='%s' and length(word) > 6 and rowid > (ABS(RANDOM()) %% (select (max(rowid)-min(rowid)) from words where category='%s')+ (select MIN(rowid) from words where category='%s')) LIMIT 1;",category,category,category);
			log.info(String.format("About to find word with:%s", sql));
			System.out.println(String.format("About to find word with:%s", sql));

			rs = pstmt.executeQuery(sql);
			while (rs.next()) {
				str.append(rs.getObject("word"));

			}
		} catch (Exception e) {
			log.info(e.toString());
			return e.toString();
		}
		finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) { /* ignored */}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) { /* ignored */}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) { /* ignored */}
			}
		}
		return str.toString();

	}
	public Boolean categoryExists(String category) {
		Connection conn = null;
		ResultSet rs = null;
		Statement pstmt = null;
		StringBuilder str = new StringBuilder();
		Boolean returnResult=false;
		log.info("Trying...");
		try {
			conn = this.connect();
			pstmt = conn.createStatement();
			String sql = "SELECT 1 FROM words  WHERE  category = '"+ category + "' LIMIT 10;";
			//String sql ="SELECT 1;";
			log.info(String.format("L:About to find category with:%s", sql));
			System.out.println(String.format("S:About to find category with:%s", sql));

			rs = pstmt.executeQuery(sql);
			int fetchSize = rs.getFetchSize();
			log.info(String.format("ResultSet fetchsize:%s",fetchSize));
			while (rs.next()) {
				returnResult=true;
				log.info("About to return true");
			}
		} catch (Exception e) {
			log.info("Catching...");
			log.info(e.toString());
			}
		finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) { log.info("rs:"+e.toString());}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {log.info("ps:"+e.toString());}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) { log.info("conn:"+e.toString());}
			}
		}
		log.info(String.format("Actually returning %s",returnResult.toString()));
		return returnResult;

		}

}
