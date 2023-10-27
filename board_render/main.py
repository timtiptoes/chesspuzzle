import chess
import chess.svg
import sys
import json

# Use for testing
#python main.py '[{"fen": "1k1r1b1r/1pR5/p1R5/3Np1p1/7p/3PB3/PPP5/7K b - - 0 25", "moves": "b7c6 e3a7 b8a8 d5b6", "puzzle_id": "tJ48p", "mateNum": 2, "puzzleNum": 5},{"fen": "1k1r1b1r/1pR5/p1R5/3Np1p1/7p/3PB3/PPP5/7K b - - 0 25", "moves": "b7c6 e3a7 b8a8 d5b6", "puzzle_id": "tJ48p", "mateNum": 2, "puzzleNum": 5},{"fen": "1k1r1b1r/1pR5/p1R5/3Np1p1/7p/3PB3/PPP5/7K b - - 0 25", "moves": "b7c6 e3a7 b8a8 d5b6", "puzzle_id": "tJ48p", "mateNum": 2, "puzzleNum": 5},{"fen": "1k1r1b1r/1pR5/p1R5/3Np1p1/7p/3PB3/PPP5/7K b - - 0 25", "moves": "b7c6 e3a7 b8a8 d5b6", "puzzle_id": "tJ48p", "mateNum": 2, "puzzleNum": 5}]'

header='''
<!DOCTYPE html>
<html>
<head>
  <style>
    .number {
      position: relative;
      display: inline-block;
      font-size: 24px;
      font-weight: bold;
    }
    .number:before {
      content: "";
      position: absolute;
      top: 50%;
      left: 40;
      width: 100%;
      height: 2px;
      background-color: black;
      transform: translateY(-700%);
    }
  </style>
</head>
<body>
  <br><br><br>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</th>
'''
'''
            for (int i = 1; i <= clue.length(); i++) {
            letter = String.valueOf(clue.charAt(i-1));
            lastMove = boardService.getCoordinate(letter);
            FEN = storageService.query(mateNum,lastMove);
            if (i % 4 == 0) {
                outputStr.append("<tr>");
            }
            outputStr.append("<th>");

            outputStr.append(boardService.drawBoard(FEN,i));
            outputStr.append("</th>");
            if (i % 4 == 3) {
                outputStr.append("</tr>");
            }
        }
        outputStr.append("</TABLE></BODY></HTML>");
        return outputStr.toString();

      for (int i = 0; i < clueLength; i++) {
            stringBuilder.append(String.format("&nbsp&nbsp&nbsp<div class=\"number\">&nbsp&nbsp%s&nbsp&nbsp</div>",i+1));
        }
        stringBuilder.append("<TABLE>");
        stringBuilder.append("<TR><TH>");
        stringBuilder.append("<br><br><br><br><br><br><br><img src=\"/board_key.png\" alt=\"chess key\" width=\"350\" height=\"350\">");
        return stringBuilder;
    }


'''


data=json.loads(sys.argv[1])

line_cnt=1
print(header)
outputStr=""
for i in range(len(data)):
	outputStr+="&nbsp&nbsp&nbsp<div class=\"number\">&nbsp&nbsp{}&nbsp&nbsp</div>".format(i+1)
print(outputStr)
print("<TABLE>")
print('<tr><br><br><br><br><br><br><br><img src="/board_key.png" alt="chess key" width="350" height="350"></th>')

for puzzle in data:
	fen=puzzle['fen']
	moves=puzzle['moves']
	puzzle_id=puzzle['puzzle_id']
	mateNum=puzzle['mateNum']
	puzzleNum=line_cnt+1
	first_move=moves.split(' ')[0]
	whose_move='white' if fen.split(" ")[1]=='b' else 'black'
	move_string=f'<br><br><br><br><p style="margin-left: 0px;"># {puzzleNum}  mate in {mateNum}, {whose_move} to move&nbsp&nbsp&nbsp&nbsp&nbsp id:{puzzle_id}</p>'
	board = chess.Board(fen)
	Nf3 = chess.Move.from_uci(first_move)
	board.push(Nf3)  # Make the move

	if (line_cnt % 4 ==0):
		print("<tr>")
	print("<th>")
	print(move_string+chess.svg.board(board,size=350,colors={'square light':'#FFFFFF','square dark':'#D3D3D3'}))
	print("</th>")
	if (line_cnt %4 == 3):
		print("</tr>")
	line_cnt+=1
print("</TABLE></BODY></HTML>")
