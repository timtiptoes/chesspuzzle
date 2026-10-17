import io
import json
import os
import sys

import chess
import chess.svg
from reportlab.graphics import renderPDF
from reportlab.lib.pagesizes import landscape, letter
from reportlab.lib.units import inch
from reportlab.pdfgen import canvas
from svglib.svglib import svg2rlg

# Use for testing
# python main.py '[{"fen": "1k1r1b1r/1pR5/p1R5/3Np1p1/7p/3PB3/PPP5/7K b - - 0 25", "moves": "b7c6 e3a7 b8a8 d5b6", "puzzle_id": "tJ48p", "mateNum": 2, "moveType": "lm"}]' puzzle.pdf

# ---------------------------------------------------------------- layout knobs
COLS = 4                          # boards across a page
ROWS = 3                          # boards down a page
PAGE_SIZE = landscape(letter)     # drop landscape() for portrait, or use A4
MARGIN = 0.35 * inch
GUTTER = 3                        # points between boards, they sit almost edge to edge
CAPTION_FONT = "Helvetica"
CAPTION_SIZE = 8
KEY_IMAGE = "board_key.png"       # dropped into the mounted dir by BoardService
TITLE_BLOCK = 30                  # title line above the answer blanks
BLANK_MIN_WIDTH = 26              # narrowest an answer blank may get before wrapping
BLANK_ROW_HEIGHT = 22             # one wrapped row of answer blanks
# -----------------------------------------------------------------------------

CAPTION_BLOCK = CAPTION_SIZE + 2
PAGE_W, PAGE_H = PAGE_SIZE
# a one row header is the baseline every page reserves, so boards stay the same
# size whether or not the clue needed its blanks wrapped over several rows
HEADER_BASE = TITLE_BLOCK + BLANK_ROW_HEIGHT


def blank_layout(count):
    """How many answer blanks fit on a row, and how many rows that takes."""
    per_row = max(1, int((PAGE_W - 2 * MARGIN) // BLANK_MIN_WIDTH))
    per_row = min(per_row, count)
    rows = -(-count // per_row)        # ceiling division
    return per_row, rows


def header_height(count):
    return TITLE_BLOCK + blank_layout(count)[1] * BLANK_ROW_HEIGHT


def grid_metrics():
    """
    Boards are square, so size them off whichever runs out first, width or
    height, and give every cell exactly that much room. Sizing the cells to the
    board instead of to the page is what keeps them nearly touching, rather than
    floating in the middle of a cell that is wider than it is tall.
    """
    top = PAGE_H - MARGIN - HEADER_BASE
    avail_w = PAGE_W - 2 * MARGIN - (COLS - 1) * GUTTER
    avail_h = top - MARGIN - ROWS * CAPTION_BLOCK - (ROWS - 1) * GUTTER
    board = min(avail_w / COLS, avail_h / ROWS)
    grid_w = COLS * board + (COLS - 1) * GUTTER
    return board, (PAGE_W - grid_w) / 2.0, top


def board_after_setup_move(puzzle):
    """The position the solver sees: the stored position plus the opponent's move."""
    board = chess.Board(puzzle["fen"])
    board.push(chess.Move.from_uci(puzzle["moves"].split(" ")[0]))
    return board


def caption_for(puzzle, number):
    whose_move = "white" if puzzle["fen"].split(" ")[1] == "b" else "black"
    # lm means the letter comes from the square the mate lands on, fm from where the key move lands
    square_term = "mate" if puzzle.get("moveType", "lm") == "lm" else "key move"
    return "# {}  mate in {}, {} to move, {}   id:{}".format(
        number, puzzle["mateNum"], whose_move, square_term, puzzle["puzzle_id"]
    )


def draw_svg(c, svg_text, box_x, box_y, box_w, box_h):
    """Drop an SVG into the box, centred, scaled to fit, still vector in the PDF."""
    drawing = svg2rlg(io.StringIO(svg_text))
    scale = min(box_w / drawing.width, box_h / drawing.height)
    drawing.scale(scale, scale)
    drawing.width *= scale
    drawing.height *= scale
    renderPDF.draw(
        drawing,
        c,
        box_x + (box_w - drawing.width) / 2.0,
        box_y + (box_h - drawing.height) / 2.0,
    )


def draw_header(c, puzzles):
    """Title, then a numbered blank per puzzle, wrapped over as many rows as it takes."""
    mate_text = "/".join(str(n) for n in sorted({p["mateNum"] for p in puzzles}))
    y = PAGE_H - MARGIN - 13

    c.setFont("Helvetica-Bold", 14)
    c.drawString(MARGIN, y, "Chess Puzzles - mate in {}".format(mate_text))

    c.setFont(CAPTION_FONT, 9)
    c.drawString(MARGIN + 180, y, "Solve each board, read its letter off the key, "
                                  "write it in the blank with the same number.")

    per_row, _ = blank_layout(len(puzzles))
    step = (PAGE_W - 2 * MARGIN) / float(per_row)
    c.setFont(CAPTION_FONT, CAPTION_SIZE)
    for i in range(len(puzzles)):
        x = MARGIN + (i % per_row) * step
        line_y = PAGE_H - MARGIN - TITLE_BLOCK - (i // per_row) * BLANK_ROW_HEIGHT - 9
        c.line(x, line_y, x + step * 0.75, line_y)
        c.drawString(x + step * 0.28, line_y - CAPTION_SIZE - 1, str(i + 1))


def draw_caption(c, text, x, y, width):
    """Shrink the caption until it fits over its own board, never the next one."""
    size = CAPTION_SIZE
    while size > 4.5 and c.stringWidth(text, CAPTION_FONT, size) > width:
        size -= 0.25
    c.setFont(CAPTION_FONT, size)
    c.drawString(x, y, text)


def draw_cell(c, kind, item, number, x, board_top, side):
    caption = "key - every square is a letter" if kind == "key" else caption_for(item, number)
    draw_caption(c, caption, x, board_top + 2, side)

    if kind == "key":
        c.drawImage(KEY_IMAGE, x, board_top - side, width=side, height=side,
                    preserveAspectRatio=True)
        return

    draw_svg(
        c,
        chess.svg.board(
            board_after_setup_move(item),
            size=350,
            colors={"square light": "#FFFFFF", "square dark": "#D3D3D3"},
        ),
        x,
        board_top - side,
        side,
        side,
    )


def draw_pages(c, puzzles):
    # the key rides along as the first cell rather than taking a page of its own
    cells = [("key", None)] if os.path.exists(KEY_IMAGE) else []
    cells += [("puzzle", p) for p in puzzles]

    side, left, top = grid_metrics()
    row_height = side + CAPTION_BLOCK + GUTTER

    # a clue long enough to wrap its blanks eats into the first page's grid, so
    # that page carries fewer rows of puzzles and the rest spill onto page two
    first_top = PAGE_H - MARGIN - header_height(len(puzzles))
    first_rows = max(1, int((first_top - MARGIN) // row_height))
    first_cells = first_rows * COLS

    puzzle_number = 0
    page = 0
    start = 0

    while start < len(cells):
        page_top = first_top if page == 0 else top
        capacity = first_cells if page == 0 else ROWS * COLS
        if page == 0:
            draw_header(c, puzzles)

        for slot, (kind, item) in enumerate(cells[start:start + capacity]):
            if kind == "puzzle":
                puzzle_number += 1
            draw_cell(
                c,
                kind,
                item,
                puzzle_number,
                left + (slot % COLS) * (side + GUTTER),
                page_top - (slot // COLS) * row_height - CAPTION_BLOCK,
                side,
            )

        c.showPage()
        start += capacity
        page += 1


def main():
    data = json.loads(sys.argv[1])
    out_path = sys.argv[2] if len(sys.argv) > 2 else "puzzle.pdf"

    # a failed lookup comes back as {}, so skip anything without a position
    puzzles = [p for p in data if p.get("fen") and p.get("moves")]
    if not puzzles:
        sys.exit("no puzzles to draw")

    c = canvas.Canvas(out_path, pagesize=PAGE_SIZE)
    c.setTitle("Chess Puzzles")
    draw_pages(c, puzzles)

    # keep the sheet count whole so a stack duplexes without bleeding into the next copy
    if c.getPageNumber() % 2 == 0:
        c.showPage()

    c.save()
    print(out_path)


main()
