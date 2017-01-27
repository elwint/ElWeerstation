import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class XMLParser {
	public static void main(String[] args) {
		int portNumber = 7789;
		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(portNumber);
			System.out.println("Started listening port on " + Integer.toString(portNumber));
		} catch (IOException e) {
			System.out.println("Could not listen on port " + Integer.toString(portNumber) + ", " + e);
			System.exit(1);
		}

		while (true) {
			try {
				Socket client = serverSocket.accept();

				//TODO: THREAD (nu max 1 client)
				DataHandler c = new DataHandler(client);
				c.run();

			} catch(IOException e) {
				System.out.println("Client connection error" + ", " + e);
			}
		}
	}
}