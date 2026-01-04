package ch.fhnw.richards.aigs_spring_server.gameEngines.TicTacToe;

public class Move {
	private long piece; // 1 is the player, -1 is the computer
	private int col;
	private int row;
	
	public Move(long piece, int col, int row) {
		this.piece = piece;
		this.col = col;
		this.row = row;
	}
	
	public long getPiece() {
		return piece;
	}
	public int getCol() {
		return col;
	}
	public int getRow() {
		return row;
	}
}

