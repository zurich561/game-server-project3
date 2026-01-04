package ch.fhnw.richards.aigs_spring_server.game;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "games")
public class Game {
	@Id
	@Column(name = "token")
	private String token;
	@Column(name = "gameType")
	GameType gameType;
	@Column(name = "difficulty")
	Long difficulty;
	@Column(name = "options", length=2048)
	String options;
	@Column(name = "board", length=2048)
	long[][] board;
	@Column(name = "result")
	Boolean result; // true = won, false = lost, null = playing

	public Game() {
	}

	public Game(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public GameType getGameType() {
		return gameType;
	}

	public void setGameType(GameType gameType) {
		this.gameType = gameType;
	}

	public Long getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(Long difficulty) {
		this.difficulty = difficulty;
	}

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	public long[][] getBoard() {
		return board;
	}

	public void setBoard(long[][] board) {
		this.board = board;
	}
	
	public Boolean getResult() {
		return result;
	}

	public void setResult(Boolean result) {
		this.result = result;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Game)) return false;
		Game g = (Game) o;
		return (this.token.equals(g.token));
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.token);
	}

	@Override
	public String toString() {
		return "Game{" + "token=" + this.token + ", gameType= " + gameType + "}";
	}
}
