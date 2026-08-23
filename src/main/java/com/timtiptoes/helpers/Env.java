package com.timtiptoes.helpers;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deployment specific locations, read from the environment so the same build
 * runs on the Mac and on the Jetson without editing source.
 *
 * Every value falls back to a path relative to the working directory, which is
 * the checkout root in both places, so nothing has to be set for a normal run.
 *
 * CHESSPUZZLE_DB_URL         jdbc url (or plain file path) of lichessDB.db
 * CHESSPUZZLE_RENDER_PATH    directory holding doit.sh, mounted into chess:latest
 * CHESSPUZZLE_TEST_PYTHON_PATH  directory holding the test python scripts
 */
public final class Env {

	public static final String DB_URL = "CHESSPUZZLE_DB_URL";
	public static final String RENDER_PATH = "CHESSPUZZLE_RENDER_PATH";
	public static final String TEST_PYTHON_PATH = "CHESSPUZZLE_TEST_PYTHON_PATH";

	private static final Logger log = LoggerFactory.getLogger(Env.class);

	private Env() {
	}

	/**
	 * JDBC url for lichessDB.db. A plain file path in the environment is accepted
	 * too and gets the jdbc:sqlite: prefix added.
	 */
	public static String dbUrl() {
		String value = value(DB_URL, "src/main/resources/static/lichessDB.db");
		if (value.startsWith("jdbc:")) {
			return value;
		}
		return "jdbc:sqlite:" + absolute(value);
	}

	/**
	 * Directory the render script is written to and mounted from. Docker needs an
	 * absolute host path for -v, so this is always resolved.
	 */
	public static String renderPath() {
		return absolute(value(RENDER_PATH, "board_render"));
	}

	/** Directory holding print_version.py and doit.sh. */
	public static String testPythonPath() {
		return absolute(value(TEST_PYTHON_PATH, "src/test/python"));
	}

	private static String value(String name, String defaultValue) {
		String value = System.getenv(name);
		if (value == null || value.isBlank()) {
			log.info(String.format("%s is not set, falling back to:%s", name, defaultValue));
			return defaultValue;
		}
		log.info(String.format("%s is set to:%s", name, value));
		return value;
	}

	private static String absolute(String path) {
		return new File(path).getAbsolutePath();
	}
}
