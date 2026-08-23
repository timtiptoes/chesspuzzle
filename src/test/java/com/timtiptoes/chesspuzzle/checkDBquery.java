package com.timtiptoes.chesspuzzle;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
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
			String url = "jdbc:sqlite:/Users/timlevine/Documents/github/chesspuzzle/src/main/resources/static/lichessDB.db";

			Connection conn = null;
			try {
				conn = DriverManager.getConnection(url);
			} catch (SQLException e) {

			}
			// Keep this in step with StorageService.query: the theme alone is not enough,
			// since Lichess tags every mate of five moves or more as "mateIn5". The move
			// list is the opponent's first move plus the solution, so a true mate in
			// mateNum is exactly 2 * mateNum plies, counted here as spaces + 1.
			String sql = "SELECT quote('fen')||':'||quote(FEN) || ',' || quote('moves')||':'||quote(moves) || ',' ||quote('puzzle_id')||':'||quote(PuzzleId) as FEN FROM mateIn"
					+ " WHERE lastMove = ?"
					+ " AND INSTR(Themes, 'mateIn' || ?)"
					+ " AND length(moves) - length(replace(moves, ' ', '')) + 1 = 2 * ?"
					+ " ORDER BY RANDOM() LIMIT 1;";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lastMove);
			pstmt.setInt(2, mateNum);
			pstmt.setInt(3, mateNum);

			//System.out.println(sql);

			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				System.out.println(rs.getObject("FEN"));
			}
		}

		catch (Exception e) {

		}

	}

}
