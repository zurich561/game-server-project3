package ch.fhnw.richards.aigs_spring_server.game;

import java.util.HashMap;
import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.fhnw.richards.aigs_spring_server.gameEngines.GameEngine;
import ch.fhnw.richards.aigs_spring_server.utility.Token;

@RestController
@CrossOrigin(origins = "*") // Allow cross-origin requests (necessary for web clients)
public class GameController {
	private static GameRepository repository;

	GameController(GameRepository repository) {
		GameController.repository = repository;
	}

	// User creates a new game
	@PostMapping("/game/new")
	Game newGame(@RequestBody Game game) {
		// First, check that the token is valid
		if (Token.validate(game.getToken())) {
			// Get game engine
			GameEngine ge = GameEngine.getGameEngine(game.getGameType());
			if (ge != null) {
				game = ge.newGame(game);
				return repository.save(game);
			} else {
				throw new GameException("Invalid game type");
			}
		} else {
			throw new GameException("Invalid token");
		}
	}

	// User quits a game
	@PostMapping("/game/quit")
	Game quitGame(@RequestBody Game game) {
		// We only care about the token
		if (Token.validate(game.getToken())) {
			repository.delete(game);
			game.setResult(false); // Game is over
			return game;
		} else {
			throw new GameException("Invalid token");
		}
	}

	// User makes a move - the format differs by game, so we just pass a map to the
	// game engine
	@PostMapping("/game/move")
	Game gameMove(@RequestBody String json) throws JsonProcessingException {
		// Convert incoming JSON to a map, then fetch the value of the token-property
		ObjectMapper mapper = new ObjectMapper();
		HashMap<String, String> map = mapper.readValue(json, HashMap.class);

		// Check the token for validity
		String token = map.get("token");
		if (Token.validate(token)) {
			// Get game object
			Game game = repository.findById(token).get();
			if (game != null) {
				// Get game engine
				GameEngine ge = GameEngine.getGameEngine(game.getGameType());
				if (ge != null) {
					// Pass map, get back updated game (or null, if move was invalid)
					game = ge.move(game, map);
					if (game != null) {
						return repository.save(game);
					} else {
						throw new GameException("Invalid move");
					}
				} else {
					throw new GameException("Invalid game type");
				}

			} else {
				throw new GameException("Game not found");
			}
		} else {
			throw new GameException("Invalid token");
		}
	}

	// --- The following methods are for debugging - a real web service would not
	// offer them ---

	// List all Games
	@GetMapping("/games")
	List<Game> all() {
		return repository.findAll();
	}

	// ||Game data needs checked from various places
	public static GameRepository getRepository() {
		return repository;
	}

}
