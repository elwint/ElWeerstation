import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.*;

public class XMLParser {
	static final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);

	// 10 threads voor het parsen (thread pool)
	// 4 threads voor het verzenden van data naar de database (elke 500 ms)
	// 1 thread voor het accepteren van connecties
	// 1 thread voor stats
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
			serverSocket = new ServerSocket(serverPort, 800);
			System.out.println("Started listening port on " + Integer.toString(serverPort));
		} catch (IOException e) {
			System.out.println("Could not listen on port " + Integer.toString(serverPort) + ", " + e);
			System.exit(1);
		}

		DatabaseHandler[] db_handlers = {
				new DatabaseHandler(dbHost, dbPort, dbName, dbUser, dbPass, sql),
				new DatabaseHandler(dbHost, dbPort, dbName, dbUser, dbPass, sql),
				new DatabaseHandler(dbHost, dbPort, dbName, dbUser, dbPass, sql),
				new DatabaseHandler(dbHost, dbPort, dbName, dbUser, dbPass, sql)
		};
		new Thread(db_handlers[0]).start();
		new Thread(db_handlers[1]).start();
		new Thread(db_handlers[2]).start();
		new Thread(db_handlers[3]).start();
		new Thread(new Stats()).start();

		((ThreadPoolExecutor)clientProcessingPool).prestartAllCoreThreads();
		int i = 0;
		while (true) {
			if (i > 3) i = 0;
			try {
				Socket clientSocket = serverSocket.accept();
				// System.out.println("CONNECTED: " + client.getInetAddress() + ":" + client.getPort());
				Stats.inc_clients();
				clientProcessingPool.submit(new DataHandler(clientSocket, db_handlers[i]));
				i++;
			} catch(IOException e) {
				System.out.println("Client connection error" + ", " + e);
			}
		}
	}
}