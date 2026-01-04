package ch.fhnw.richards.aigs_spring_server.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import ch.fhnw.richards.aigs_spring_server.user.UserExceptionHandler;
import ch.fhnw.richards.aigs_spring_server.utility.ErrorResponse;

@ControllerAdvice
public class GameExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(UserExceptionHandler.class);

    @ExceptionHandler(value = {GameException.class})
    @ResponseBody
    ResponseEntity<ErrorResponse> gameError(GameException ex) {
        LOG.error("Game exception " , ex);
        ErrorResponse response = new ErrorResponse("Game error", ex.getMessage());
        return new ResponseEntity<ErrorResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
