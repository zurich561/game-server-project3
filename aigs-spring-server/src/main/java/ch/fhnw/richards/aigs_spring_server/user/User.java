package ch.fhnw.richards.aigs_spring_server.user;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "users")
public class User {
	@Id
	@Column(name = "username")
	private String userName;
	@Column(name = "password")
	private String password;
	@Column(name = "userexpiry")
	private LocalDateTime userExpiry;
	@Column(name = "token")
	private String token;

	public User() {
	}

	public User(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public LocalDateTime getUserExpiry() {
		return userExpiry;
	}

	public void setUserExpiry(LocalDateTime userExpiry) {
		this.userExpiry = userExpiry;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof User)) return false;
		User u = (User) o;
		return (this.userName.equals(u.userName));
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.userName);
	}

	@Override
	public String toString() {
		return "User{" + "userName=" + this.userName + '}';
	}

}
