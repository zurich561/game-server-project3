package ch.fhnw.richards.aigs_spring_server.gameEngines.TicTacToe;

public class MinimaxPlayer implements ttt_ai {
	private long myPiece = -1; // Which player are we?
	
	private enum Evaluation { LOSS, DRAW, WIN };

	private class MoveEval {
		Move move;
		Evaluation evaluation;

		public MoveEval(Move move, Evaluation evaluation) {
			this.move = move;
			this.evaluation = evaluation;
		}
	}

	@Override
	public void makeMove(long[][] board) {
		Move move = findMove(board, myPiece).move;
		board[move.getRow()][move.getCol()] = myPiece;
	}

	private MoveEval findMove(long[][] board, long toMove) {
		Move bestMove = null;
		Evaluation bestEval = Evaluation.LOSS;

		Long result = TicTacToe.getWinner(board);
		if (result != null) { // game is over
			if (result == toMove) return new MoveEval(null, Evaluation.WIN);
			else if (result == (0 - toMove)) return new MoveEval(null, Evaluation.LOSS);
			else return new MoveEval(null, Evaluation.DRAW);
		} else {
			// Find best move for piece toMove
			for (int col = 0; col < 3; col++) {
				for (int row = 0; row < 3; row++) {
					if (board[row][col] == 0) {
						// Possible move found
						long[][] possBoard = copyBoard(board);
						possBoard[row][col] = toMove;
						MoveEval tempMR = findMove(possBoard, (toMove == 1) ? -1: 1);
						Evaluation possEval = invertResult(tempMR.evaluation); // Their win is our loss, etc.
						if (possEval.ordinal() > bestEval.ordinal()) {
							bestEval = possEval;
							bestMove = new Move(toMove, col, row);
						}
					}
				}
			}
			return new MoveEval(bestMove, bestEval); 
		}
	}
	
	private Evaluation invertResult(Evaluation in) {
		if (in == Evaluation.DRAW) return Evaluation.DRAW;
		else if (in == Evaluation.WIN) return Evaluation.LOSS;
		return Evaluation.WIN;
	}
	
	private long[][] copyBoard(long[][] board) {
		long[][] newboard = new long[board.length][];
		for (int i = 0; i < board.length; i++) {
			newboard[i] = new long[board[i].length];
			for (int j = 0; j < board[i].length; j++) {
				newboard[i][j] = board[i][j];
			}
		}
		return newboard;
	}
}

