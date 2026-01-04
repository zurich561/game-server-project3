package ch.fhnw.richards.aigs_spring_server.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
	List<User> findByToken(String token);
}
