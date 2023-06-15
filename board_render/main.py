import chess
import chess.svg
import sys

fen=sys.argv[1]
moves=sys.argv[2]
game_url=sys.argv[3]
mateNum=sys.argv[4]
puzzleNum=sys.argv[5]

first_move=moves.split(' ')[0]
whose_move='white' if fen.split(" ")[1]=='b' else 'black'

move_string=f'<br><br><br><br><p style="margin-left: 0px;"># {puzzleNum}&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp  mate in {mateNum}, {whose_move} to move</p>'
#move_string=f'<p style="margin-left: 70px;">mate in {mateNum}, {whose_move} to move {game_url}</p>'


#docker run --rm -v /Users/206660287/Documents/personal/chess_puzzle/chesspuzzle/latex_stuff:/tmp chess:latest
board = chess.Board(fen)
Nf3 = chess.Move.from_uci(first_move)
board.push(Nf3)  # Make the move
print(move_string+chess.svg.board(board,size=350,colors={'square light':'#FFFFFF','square dark':'#D3D3D3'}))
