package ch.fhnw.richards.aigs_spring_server.gameEngines.TicTacToe;

import java.util.HashMap;

import ch.fhnw.richards.aigs_spring_server.game.Game;
import ch.fhnw.richards.aigs_spring_server.gameEngines.GameEngine;

/**
 * Game board encoding: -1 = O, 0 = empty, 1 = X
 * 
 * Game difficulty: 1 or less = random-player, 2 = decent player, 3 or more =
 * optimal player
 */
public class TicTacToe implements GameEngine {
	@Override
	public Game newGame(Game game) {
		game.setBoard(new long[3][3]);
		game.setResult(false);
		return game;
	}

	@Override
	public Game move(Game game, HashMap<String, String> move) {
		int row = Integer.parseInt(move.get("row"));
		int col = Integer.parseInt(move.get("col"));

		// Only accept the player's move if it is valid
		if (game.getBoard()[row][col] == 0) {
			game.getBoard()[row][col] = 1;
			game.setResult(getResult(game.getBoard()));

			if (!game.getResult()) {
				// Make our move.
				ttt_ai player = (game.getDifficulty() <= 1) ? new RandomPlayer() : new MinimaxPlayer();
				player.makeMove(game.getBoard());
			}
			game.setResult(getResult(game.getBoard()));
		}
		return game;
	}

	/**
	 * @return true if the game is over
	 */
	private boolean getResult(long[][] board) {
		return getWinner(board) != null;
	}

	/**
	 * Test whether the game is over.
	 * 
	 * @return null if still playing, 0 if cat's game, otherwise 1 or -1 for the
	 *         winner
	 */
	static Long getWinner(long[][] board) {
		int numEmptyCells = 0;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (board[i][j] == 0)
					numEmptyCells++;
			}
		}

		long winner = 0;
		long rowPiece = 0;
		long colPiece = 0;
		for (int i = 0; i < 3; i++) {
			rowPiece = board[0][i];
			colPiece = board[i][0];
			for (int j = 1; j < 3; j++) {
				if (board[j][i] != rowPiece)
					rowPiece = 0;
				if (board[i][j] != colPiece)
					colPiece = 0;
			}
			if (rowPiece != 0)
				winner = rowPiece;
			if (colPiece != 0)
				winner = colPiece;
		}

		long diag1 = board[0][0];
		long diag2 = board[2][0];
		for (int i = 1; i < 3; i++) {
			if (board[i][i] != diag1)
				diag1 = 0;
			if (board[2 - i][i] != diag2)
				diag2 = 0;
		}
		if (diag1 != 0)
			winner = diag1;
		if (diag2 != 0)
			winner = diag2;

		if (numEmptyCells == 0)
			return 0l;
		else if (winner != 0)
			return winner;
		else
			return null;
	}
}
