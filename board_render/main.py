import chess
import chess.svg
import sys

fen=sys.argv[1]
moves=sys.argv[2]
first_move=moves.split(' ')[0]
whose_move='white' if fen.split(" ")[1]=='b' else 'black'

move_string=f'<p style="margin-left: 70px;">mate in 1, {whose_move} to move</p>'

#docker run --rm -v /Users/206660287/Documents/personal/chess_puzzle/chesspuzzle/latex_stuff:/tmp chess:latest
board = chess.Board(fen)
Nf3 = chess.Move.from_uci(first_move)
board.push(Nf3)  # Make the move
print(move_string+chess.svg.board(board,size=350,colors={'square light':'#FFFFFF','square dark':'#D3D3D3'}))
