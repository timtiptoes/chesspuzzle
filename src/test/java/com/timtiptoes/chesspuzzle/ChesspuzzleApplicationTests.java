package com.timtiptoes.chesspuzzle;

import com.timtiptoes.helpers.Env;
import com.timtiptoes.services.BoardService;
import com.timtiptoes.services.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.script.*;
import java.io.*;
import java.util.List;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@SpringBootTest
class ChesspuzzleApplicationTests {

	//@Test
	public void givenPythonScriptEngineIsAvailable_whenScriptInvoked_thenOutputDisplayed() throws Exception {
		StringWriter writer = new StringWriter();
		ScriptContext context = new SimpleScriptContext();
		context.setWriter(writer);

		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("python");
		engine.eval(new FileReader(Env.testPythonPath() + "/print_version.py"), context);
		assertEquals("Should contain script output: ", "Hello Baeldung Readers!!", writer.toString().trim());
	}



//@Test
	public void runfromOS(){
	String command = Env.testPythonPath() + "/doit.sh";

	try {
		Process process = Runtime.getRuntime().exec(command);
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		int exitValue = process.exitValue();
		if (exitValue == 0) {
			System.out.println("Script executed successfully");
		} else {
			System.out.println("Script failed with exit value: " + exitValue);
		}
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}
		assertEquals("Should contain script output: ", "snorx", line);

		reader.close();

	} catch (IOException e) {
		e.printStackTrace();
	}
}

@Test
	public void getWordTest(){
	StorageService storageService = new StorageService();
	String word = storageService.randomWord();
	System.out.println("Random word:"+word);
}

}