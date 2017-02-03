import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class XMLParser {
	public static void main(String[] args) {
		String dbHost = "127.0.0.1";
		String dbPort = "5432";
		String dbDatabase = "school";
		String dbUser = "postgres";
		String dbPass = "";
		String sql = "INSERT INTO Measurement VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		int serverPort = 7789;

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

		//TODO: Connection pool
		Connection con = null;
		try {
			con = DriverManager.getConnection("jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbDatabase, dbUser, dbPass);
			if (con != null) {
				System.out.println("Connected successfully to database server");
			} else {
				System.out.println("Database server connection failed");
				System.exit(1);
			}
		} catch (SQLException e) {
			System.out.println("Database server connection failed, " + e);
			System.exit(1);
		}

		Thread debug = new Thread(() -> {
			try {
				while (true) {
					System.out.println("Active threads: " + Integer.toString(java.lang.Thread.activeCount()));
					Thread.sleep(1000);
				}
			} catch(InterruptedException v) {
				System.out.println(v);
			}
		});
		debug.start();

		while (true) {
			try {
				Socket client = serverSocket.accept();
				Thread c = new DataHandler(client, con, sql);
				c.start();
			} catch(IOException | SQLException e) {
				System.out.println("Client connection error" + ", " + e);
			}
		}
	}
}