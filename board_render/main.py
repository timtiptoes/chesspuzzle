import chess
import chess.svg
import sys
import json

# Use for testing
#python main.py '[{"fen": "1k1r1b1r/1pR5/p1R5/3Np1p1/7p/3PB3/PPP5/7K b - - 0 25", "moves": "b7c6 e3a7 b8a8 d5b6", "puzzle_id": "tJ48p", "mateNum": 2, "puzzleNum": 5},{"fen": "1k1r1b1r/1pR5/p1R5/3Np1p1/7p/3PB3/PPP5/7K b - - 0 25", "moves": "b7c6 e3a7 b8a8 d5b6", "puzzle_id": "tJ48p", "mateNum": 2, "puzzleNum": 5},{"fen": "1k1r1b1r/1pR5/p1R5/3Np1p1/7p/3PB3/PPP5/7K b - - 0 25", "moves": "b7c6 e3a7 b8a8 d5b6", "puzzle_id": "tJ48p", "mateNum": 2, "puzzleNum": 5},{"fen": "1k1r1b1r/1pR5/p1R5/3Np1p1/7p/3PB3/PPP5/7K b - - 0 25", "moves": "b7c6 e3a7 b8a8 d5b6", "puzzle_id": "tJ48p", "mateNum": 2, "puzzleNum": 5}]'


data=json.loads(sys.argv[1])

for puzzle in data:
	fen=puzzle['fen']
	moves=puzzle['moves']
	puzzle_id=puzzle['puzzle_id']
	mateNum=puzzle['mateNum']
	puzzleNum=puzzle['puzzleNum']
	first_move=moves.split(' ')[0]
	whose_move='white' if fen.split(" ")[1]=='b' else 'black'

	move_string=f'<br><br><br><br><p style="margin-left: 0px;"># {puzzleNum}  mate in {mateNum}, {whose_move} to move&nbsp&nbsp&nbsp&nbsp&nbsp id:{puzzle_id}</p>'
	board = chess.Board(fen)
	Nf3 = chess.Move.from_uci(first_move)
	board.push(Nf3)  # Make the move
	print(move_string+chess.svg.board(board,size=350,colors={'square light':'#FFFFFF','square dark':'#D3D3D3'}))
