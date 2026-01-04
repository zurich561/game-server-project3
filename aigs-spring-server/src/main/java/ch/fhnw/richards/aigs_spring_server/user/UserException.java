package ch.fhnw.richards.aigs_spring_server.user;

@SuppressWarnings("serial")
public class UserException extends RuntimeException {
	UserException(String message) {
		super(message);
	}
}
