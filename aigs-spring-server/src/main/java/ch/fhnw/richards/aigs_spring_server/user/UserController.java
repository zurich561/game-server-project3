package ch.fhnw.richards.aigs_spring_server.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.*;

import ch.fhnw.richards.aigs_spring_server.utility.Token;

@RestController
@CrossOrigin(origins = "*") // Allow cross-origin requests (necessary for web clients)
public class UserController {
	private static UserRepository repository;

	UserController(UserRepository repository) {
		UserController.repository = repository;
	}

	// Register a User
	@PostMapping("/users/register")
	User registerUser(@RequestBody User User) {
		Optional<User> oldUser = repository.findById(User.getUserName());
		if (oldUser.isEmpty()) {
			User.setUserExpiry(LocalDateTime.now().plusDays(7));
			repository.save(User);
			User.setPassword(""); // Remove password before sending user-object
			return User;
		} else {
			throw new UserException("'" + User.getUserName() + "' already exists");
		}
	}

	// Login a User
	// Passwords should be encrypted on the client. For testing, we can omit
	// encryption. It doesn't matter to the server.
	@PostMapping("/users/login")
	User loginUser(@RequestBody User User) {
		Optional<User> oldUser = repository.findById(User.getUserName());
		if (oldUser.isPresent() && oldUser.get().getPassword().equals(User.getPassword())) {
			User.setUserExpiry(LocalDateTime.now().plusDays(7));
			User.setToken(Token.generate());
			repository.save(User);
			User.setPassword(""); // Remove password before sending user-object
			return User;
		} else {
			throw new UserException("Wrong password for user '" + User.getUserName() + "'");
		}
	}

	// Logout a User
	@PostMapping("/users/logout")
	User logoutUser(@RequestBody User User) {
		Optional<User> oldUser = repository.findById(User.getUserName());
		if (oldUser.isPresent()) {
			User thisUser = oldUser.get();
			thisUser.setToken(null);
			repository.save(thisUser);
			User.setPassword(""); // Remove any password before sending user-object
			return User;
		} else {
			throw new UserException("\"" + User.getUserName() + "\" does not exist");
		}
	}

	// --- The following user methods are for debugging only
	// --- A real web service would not

	// List all Users
	@GetMapping("/users")
	List<User> all() {
		return repository.findAll();
	}

	// Read a User
	@GetMapping("users/{userName}")
	User one(@PathVariable String userName) {
		return repository.findById(userName)
				.orElseThrow(() -> new UserException("\"" + userName + "\" does not exist"));
	}

	// --- Ping functionality

	@GetMapping("/ping")
	String ping() {
		return "{ \"ping\":\"success\" }";
	}

	// --- Getter method for the repository
	
	// User data needs checked from various places
	public static UserRepository getRepository() {
		return repository;
	}
}
