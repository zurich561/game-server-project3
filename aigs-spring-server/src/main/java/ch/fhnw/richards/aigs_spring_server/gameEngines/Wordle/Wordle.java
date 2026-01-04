package ch.fhnw.richards.aigs_spring_server.gameEngines.Wordle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import ch.fhnw.richards.aigs_spring_server.game.Game;
import ch.fhnw.richards.aigs_spring_server.gameEngines.GameEngine;
import ch.fhnw.richards.aigs_spring_server.gameEngines.TicTacToe.MinimaxPlayer;
import ch.fhnw.richards.aigs_spring_server.gameEngines.TicTacToe.RandomPlayer;
import ch.fhnw.richards.aigs_spring_server.gameEngines.TicTacToe.ttt_ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ch.fhnw.richards.aigs_spring_server.gameEngines.Wordle.Service;

public class Wordle implements GameEngine {
    Service service = new Service();
    private int wordLength;

    @Override
    /*  Loris: Initializes the game and board size, depending on difficulty
            Easy        -> 6 x 5 (default)
            Medium      -> 6 x 7
            Hard        -> 6 x 9
            Ludacris    -> 6 x 12
    */
	public Game newGame(Game game) {
        switch (game.getDifficulty().intValue()) {
            case 1:
                game.setBoard(new long[6][5]);
                wordLength = 5;
                break;
            case 2:
                game.setBoard(new long[6][7]);
                wordLength = 7;
                break;
            case 3:
                game.setBoard(new long[6][9]);
                wordLength = 9;
                break;         
            case 4:
                game.setBoard(new long[6][12]);
                wordLength = 12;
                break;       
            default:
                game.setBoard(new long[6][5]);
                break;
        }
		game.setResult(null);

        // set game options:
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> wordleOpts = new HashMap<>();
        String json = null;

        try {
            wordleOpts.put("solutionWord", service.GenerateWord(wordLength));
            wordleOpts.put("guesses", new ArrayList<List<Map<String, String>>>());
            wordleOpts.put("wordLength", wordLength);
            wordleOpts.put("round", 0);
            wordleOpts.put("maxTries", 6);
            json = mapper.writeValueAsString(wordleOpts);
        } catch (Exception e) {
            e.printStackTrace();
        }

        game.setOptions(json);

		return game;
	}

    @Override
	public Game move(Game game, HashMap<String, String> move) {
        // Check if the word exists
        try {
            service.CheckWordExistence(move.get("guess"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // add the guess and evaluate if guess is correct
        game = service.addGuess(game, move.get("guess"));

		return game;
	}

}
