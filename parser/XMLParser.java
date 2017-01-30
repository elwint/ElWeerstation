import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class XMLParser {
	public static void main(String[] args) {
		Connection con = null;
		String dbHost = "127.0.0.1";
		String dbPort = "5432";
		String dbDatabase = "school";
		String dbUser = "postgres";
		String dbPass = "";
		int serverPort = 7789;

		try {
			Class.forName("org.postgresql.Driver");
			con = DriverManager.getConnection("jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbDatabase, dbUser, dbPass);
		} catch (ClassNotFoundException e) {
			System.out.println("Cannot find PostgreSQL JDBC Driver");
			System.exit(1);
		} catch (SQLException e) {
			System.out.println("Database server connection failed, " + e);
			System.exit(1);
		}

		if (con != null) {
			System.out.println("Connected successfully to database server");
		} else {
			System.out.println("Database server connection failed");
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

		while (true) {
			try {
				Socket client = serverSocket.accept();

				//TODO: THREAD (nu max 1 client)
				DataHandler c = new DataHandler(client, con);
				c.start();

			} catch(IOException e) {
				System.out.println("Client connection error" + ", " + e);
			}
		}
	}
}