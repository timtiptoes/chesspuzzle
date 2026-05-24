Tim! 

Here's the general idea

The full flow for /make/2/random:

Word selection — StorageService.randomWord() queries the words table in lichessDB.db for a random word longer than 6 characters. If you pass a category name instead of "random" (e.g. /make/2/animals), it checks categoryExists() first and pulls a word from that category instead.

Letter → coordinate mapping — For each letter in the word, BoardService.getCoordinate(letter) picks a random chess square (e.g. a1–h8) that maps to that letter. The mapping is mathematical: a board has 64 squares, each square maps to one of 26 letters via (x + 8*y) % 26. Multiple squares can map to the same letter, so it picks one randomly from the matching squares.

Puzzle lookup — For each coordinate (the "lastMove"), StorageService.query(mateNum, lastMove) queries the mateIn table for a random puzzle where the last move lands on that square AND the theme is mateIn2. It returns FEN, move sequence, and puzzle ID as JSON.

doit.sh generation — BoardService.drawBoard() writes a shell script (board_render/doit.sh) that runs the Docker container, passing the full JSON array of all puzzles as a single argument.

Docker execution — The Java process executes doit.sh directly via Runtime.getRuntime().exec(), reads stdout line by line, and returns it as the HTTP response body.

Output — The Docker container renders the board positions and writes SVG (or similar) to stdout, which gets returned as the response.

board_key.png — bundled in the jar AND read from it via ClassPathResource. No scp needed, works automatically.

lichessDB.db — also bundled in the jar (it's in src/main/resources/static/), but the SQLite connection uses a hardcoded absolute file path (jdbc:sqlite:/Users/timlevine/...), so the bundled copy is ignored. You already wrote this note to yourself in RestControllers.java: "NOTE! Even though lichessDB.db is included in the jar, it has nothing to do with what's used by the code." You must scp it separately.

Full checklist to deploy to the Jetson tx2:

StorageService.java — swap the url to the Jetson path (comment/uncomment lines 21–22)
BoardService.java — swap render_path to the Jetson path, and switch back to sudo docker run (lines 64–72)
Build: ./gradlew bootJar
scp the jar to the Jetson
scp src/main/resources/lichessDB.db to /media/tim/JetsonSSD-500/git_repos/chesspuzzle/src/main/resources/static/lichessDB.db on the Jetson
On the Jetson, make sure the Docker image is built (docker build -t chess:latest . in board_render/)
Run: java -jar chesspuzzle-0.0.1-SNAPSHOT.jar


First you have to make sure there's a chesspuzzle:latest docker image available. So cd into board_render and go
	cd board_render
	docker build -t chess:latest .

test that it works on mac with
	docker run --rm -v /Users/206660287/Documents/personal/chess_puzzle/chesspuzzle/board_render:/tmp chess:latest '1k1r1b1r/1pR5/p1R5/3Np1p1/7p/3PB3/PPP5/7K b - - 0 25' 'b7c6 e3a7 b8a8 d5b6' 'tJ48p' 2 5

test that it works on tx2 with
	docker run --rm -v /media/tim/JetsonSSD-500/git_repos/chesspuzzle/board_render:/tmp chess:latest '[{"fen": "1k1r1b1r/1pR5/p1R5/3Np1p1/7p/3PB3/PPP5/7K b - - 0 25", "moves": "b7c6 e3a7 b8a8 d5b6", "puzzle_id": "tJ48p", "mateNum": 2, "puzzleNum": 5},{"fen": "1k1r1b1r/1pR5/p1R5/3Np1p1/7p/3PB3/PPP5/7K b - - 0 25", "moves": "b7c6 e3a7 b8a8 d5b6", "puzzle_id": "tJ48p", "mateNum": 2, "puzzleNum": 5},{"fen": "1k1r1b1r/1pR5/p1R5/3Np1p1/7p/3PB3/PPP5/7K b - - 0 25", "moves": "b7c6 e3a7 b8a8 d5b6", "puzzle_id": "tJ48p", "mateNum": 2, "puzzleNum": 5},{"fen": "1k1r1b1r/1pR5/p1R5/3Np1p1/7p/3PB3/PPP5/7K b - - 0 25", "moves": "b7c6 e3a7 b8a8 d5b6", "puzzle_id": "tJ48p", "mateNum": 2, "puzzleNum": 5}]'

There are three things you have to change when switching environments
path in storage service
path in board service
add sudo in docker run also in board service for tx2

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
		scp -i ~/.ssh/id_jetson build/libs/chesspuzzle-0.0.1-SNAPSHOT.jar tim@192.168.68.134:/media/tim/JetsonSSD-500/git_repos/chesspuzzle/.
		scp /Users/206660287/Documents/personal/chesspuzzle/src/main/resources/static/board_key.png  tim@192.168.68.134:/media/tim/JetsonSSD-500/git_repos/chesspuzzle/src/main/resources/static/.
		scp -i ~/.ssh/id_jetson /Users/206660287/Documents/personal/github/chesspuzzle/src/main/resources/static/lichessDB.db tim@192.168.68.134:/media/tim/JetsonSSD-500/git_repos/chesspuzzle/src/main/resources/static/.
		tmux att -t 0
		cd /media/tim/JetsonSSD-5001/git_repos/chesspuzzle/board_render
		docker build -t chess:latest .
		touch board_render/doit.sh
		chmod 777 board_render/doit.sh
		java -jar chesspuzzle-0.0.1-SNAPSHOT.jar
		cd ..
	
	1/17/2026
		build locally
			cd /Users/timlevine/Documents/github/chesspuzzle
			./gradlew bootJar
			scp build/libs/starter_web-0.0.1-SNAPSHOT.jar tim@192.168.68.60:/mnt/Jetson-500/git_repos/chesspuzzle/chesspuzzle-0.0.1-SNAPSHOT.jar

	NOTE! Even though lichessDB.db is included in the jar, it has nothing to do with what's used by the code. Instead modify the local copy of lichessDB.db and move it up to 
		  the tx2 like this
		  		scp src/main/resources/lichessDB.db tim@192.168.68.60:/media/tim/JetsonSSD-500/git_repos/chesspuzzle/src/main/resources/static/lichessDB.db