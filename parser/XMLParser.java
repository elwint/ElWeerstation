import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;

public class XMLParser {
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException{
        int portNumber = 7789;
        int connections = 0;
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Started listening port on " + Integer.toString(portNumber));
        } catch (IOException e) {
            System.out.println("Could not listen on port " + Integer.toString(portNumber) + ", " + e);
            System.exit(1);
        }

        while(true) {
            Socket client;
            client = serverSocket.accept();

            //TODO: THREAD (nu max 1 client)
            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            connections += 1;
            System.out.println("CONNECTED: " + client.getInetAddress() + ":" + client.getPort());
            System.out.println("Total connected: " + Integer.toString(connections));

            StringBuilder data = new StringBuilder(); // Fastest way to read data
            String ln;
            boolean read = false;
            int total = 0;
            while((ln = br.readLine()) != null) {

                switch(ln = ln.replaceAll("\\s+","")) {
                    case "<MEASUREMENT>":
                        read = true;
                        continue;
                    case "</MEASUREMENT>":
                        read = false;
                        break;
                    default:
                        if(read) {
                            data.append(ln);
                        }
                        continue;
                }
                total += 1;
                System.out.println(data.toString());
                System.out.println("[" + client.getPort() + "] Succesfully received meassurement data, total: " + Integer.toString(total));
                //TODO: Parser
                //TODO: SQL Magic?

                data.setLength(0); // Clear buffer
            }

            connections -= 1;
            client.close();
            System.out.println("DISCONNECTED: " + client.getInetAddress() + ":" + client.getPort());
            System.out.println("Total connected: " + Integer.toString(connections));
        }

//		File inputFile = new File("output.xml");
//		SAXParserFactory factory = SAXParserFactory.newInstance();
//		SAXParser saxParser = factory.newSAXParser();
//		MyHandler myHandler = new MyHandler();
//		saxParser.parse(inputFile, myHandler);
	}
}