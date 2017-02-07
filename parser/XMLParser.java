import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.*;

public class XMLParser {
	static final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);

	public static void main(String[] args) {
		int serverPort = 0;
		String dbHost = null;
		String dbPort = null;
		String dbName = null;
		String dbUser = null;
		String dbPass = null;
		final String sql = "INSERT INTO Measurement VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		InputStream file = XMLParser.class.getResourceAsStream("config.properties");
		if (file != null) {
			try {
				Properties conf = new Properties();
				conf.load(file);
				serverPort = Integer.parseInt(conf.getProperty("SERVER_PORT"));
				dbHost = conf.getProperty("DB_HOST");
				dbPort = conf.getProperty("DB_PORT");
				dbName = conf.getProperty("DB_NAME");
				dbUser = conf.getProperty("DB_USER");
				dbPass = conf.getProperty("DB_PASS");
			} catch (Exception e) {
				System.out.println("Can't load config file config.properties, " + e);
			} finally {
				try {
					file.close();
				} catch (IOException e) {
					System.out.println("Can't load config file config.properties, " + e);
				}
			}
		} else {
			System.out.println("Can't find config file config.properties");
			System.exit(1);
		}

		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Cannot find PostgreSQL JDBC Driver");
			System.exit(1);
		}

		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(serverPort);
			System.out.println("Started listening port on " + Integer.toString(serverPort));
		} catch (IOException e) {
			System.out.println("Could not listen on port " + Integer.toString(serverPort) + ", " + e);
			System.exit(1);
		}

		//TODO: Meerdere database connecties? (if necessary)
		DatabaseHandler db_handler = new DatabaseHandler(dbHost, dbPort, dbName, dbUser, dbPass, sql);
		new Thread(db_handler).start();
		new Thread(new Stats()).start();

		while (true) {
			try {
				Socket clientSocket = serverSocket.accept();
				BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				// System.out.println("CONNECTED: " + client.getInetAddress() + ":" + client.getPort());
				Stats.inc_clients();
				clientProcessingPool.submit(new DataHandler(clientSocket, reader, db_handler));
			} catch(IOException e) {
				System.out.println("Client connection error" + ", " + e);
			}
		}
	}
}