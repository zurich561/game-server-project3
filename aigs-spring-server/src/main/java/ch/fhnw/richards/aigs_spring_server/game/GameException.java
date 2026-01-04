package ch.fhnw.richards.aigs_spring_server.game;

@SuppressWarnings("serial")
public class GameException extends RuntimeException {
	GameException(String message) {
		super(message);
	}
}
