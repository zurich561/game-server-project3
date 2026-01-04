# AIGS Spring Server

The AI Game Server (AIGS) is a server that supports one or more games, and provides AI players to play against human opponents. In the basic version, only TicTacToe is implemented. However, game modules can easily be added. Student projects can either add new game modules or create clients for existing games.

## Building this project in IntelliJ

- Open the View menu and select : Tool-Windows : Maven
- Under Lifecycle, choose "package"

The build information will be displayed in the console. Assuming that the build is successful, near the
end will be a line "Building jar" with a link to the finished JAR file. This will be in the "target"
subdirectory of the project. You may wish to rename the JAR file to something simpler, like "AIGS.jar".

## Running the server

The server runs as a standalone Java application. By default, it uses port 50005.
To run the server from the command line, enter: <code>java -jar aigs-server.jar</code>

To run the server as a permanent service <code>xyz</code>
- Create a new account that will run the service: <code>adduser xyz</code>
- Copy the JAR file to <code>/home/xyz</code> and change the ownership to <code>xyz:xyz</code>
- Create a new service script for the service in <code>/usr/local/bin</code>
- Create a new systemd service definition in: <code>/etc/systemd/system/xyz.service</code>
- Tell systemd about this service by running: <code>systemctl enable xyz</code>
- Now you can use: <code>service xyz start/stop/status/restart</code>

## License

This is open source software, licensed under the BSD 3-clause license.
