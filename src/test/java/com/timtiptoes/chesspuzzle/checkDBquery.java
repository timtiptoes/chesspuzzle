package com.timtiptoes.chesspuzzle;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class checkDBquery {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {

	}

	@Test
	void test() {
		StringBuilder str = new StringBuilder();
		int mateNum=2;
		String lastMove="d7";
		
		try {
			String url = "jdbc:sqlite:/Users/206660287/Documents/personal/chess_puzzle/chesspuzzle/src/main/resources/static/lichessDB.db";

			Connection conn = null;
			try {
				conn = DriverManager.getConnection(url);
			} catch (SQLException e) {

			}
			Statement pstmt = conn.createStatement();
			String sql = String.format(
					"SELECT quote('fen')||':'||quote(FEN) || ',' || quote('moves')||':'||quote(moves) || ',' ||quote('puzzle_id')||':'||quote(PuzzleId) as FEN FROM mateIn WHERE lastMove = '%s'",
					lastMove) + "AND INSTR(Themes,'mateIn" + Integer.valueOf(mateNum)
					+ "') and rowid > (ABS(RANDOM()) % (SELECT max(rowid) FROM mateIn)) LIMIT 1;";

			//System.out.println(sql);
			
			ResultSet rs = pstmt.executeQuery(sql);
			while (rs.next()) {
				System.out.println(rs.getObject("FEN"));
			}
		}

		catch (Exception e) {

		}

	}

}
