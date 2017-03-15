import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.Timestamp;

class DataHandler implements Runnable {
	private final Socket client;
	private final DatabaseHandler db_handler;
	private BufferedReader reader = null;

	private boolean disconnected = false;
	private String ln;
	private String key;
	private String value;
	private boolean read = false;

	private int stationID;
	private char[] date = new char[10];
	private char[] time = new char[8];
	private Timestamp timestamp;
	private float temperature;
	private float dewPoint;
	private float airPressureStation;
	private float airPressureSeaLevel;
	private float visibility;
	private float windSpeed;
	private float rain;
	private float snow;
	private int events; // Binary
	private float cloudAmount;
	private int windDirection;

	DataHandler(Socket client, DatabaseHandler db_handler) throws IOException {
		this.client = client;
		this.db_handler = db_handler;
	}

	public void run() {
		try {
			if (this.reader == null) {
				this.reader = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
			}
			read();
			if (this.disconnected) {
				// System.out.println("DISCONNECTED: " + client.getInetAddress() + ":" + client.getPort());
				Stats.dec_cients();
				this.client.close();
			} else {
				XMLParser.clientProcessingPool.submit(this);
			}
		} catch (IOException e) {
			System.out.println("Client connection error" + ", " + e);
		}
	}

	private void read() throws IOException {
		while (true) {
			ln = reader.readLine();
			if (ln == null) {
				disconnected = true;
				return;
			}

			try {
				key = ln.substring(ln.indexOf("<") + 1, ln.indexOf(">"));
			} catch (IndexOutOfBoundsException e) {
				continue;
			}

			if (key.equals("/WEATHERDATA")) {
				break;
			}

			if (key.equals("MEASUREMENT")) {
				read = true;
				continue;
			} else if (key.trim().isEmpty() || !read) {
				continue;
			}

			if (key.equals("/MEASUREMENT")) {
				if (stationID == -10000 || timestamp == null) {
					System.out.println("[" + client.getInetAddress() + ":" + client.getPort() + "] Missing required data, dropping measurement");
				} else {
					// System.out.println("[" + client.getInetAddress() + ":" + client.getPort() + "] Successfully received data for station: " + Integer.toString(stationID));
					Stats.inc_measurements();
					if (stationID == 786630 || stationID == 786660) {
						db_handler.prepareData(stationID, timestamp, temperature, dewPoint, airPressureStation, airPressureSeaLevel, visibility, windSpeed, rain, snow, events, cloudAmount, windDirection);
					}
				}
				read = false;

				stationID = -10000;
				date[0] = 0;
				time[0] = 0;
				timestamp = null;
				temperature = -10000;
				dewPoint = -10000;
				airPressureStation = -10000;
				airPressureSeaLevel = -10000;
				visibility = -10000;
				windSpeed = -10000;
				rain = -10000;
				snow = -10000;
				events = -10000;
				cloudAmount = -10000;
				windDirection = -10000;
				continue;
			}

			try {
				value = ln.substring(ln.indexOf(">") + 1, ln.indexOf("<", ln.indexOf(">")));
			} catch (IndexOutOfBoundsException e) {
				continue;
			}

			if (value.trim().isEmpty()) {
				continue;
			}

			try {
				switch (key) {
					case "STN":
						stationID = Integer.parseInt(value);
						break;
					case "DATE":
						if (time[0] != 0)
							timestamp = Timestamp.valueOf(String.valueOf(value) + " " + String.valueOf(time));
						else
							date = value.toCharArray();
						break;
					case "TIME":
						if (date[0] != 0) {
							timestamp = Timestamp.valueOf(String.valueOf(date) + " " + String.valueOf(value));
						} else
							time = value.toCharArray();
						break;
					case "TEMP":
						temperature = Float.parseFloat(value);
						break;
					case "DEWP":
						dewPoint = Float.parseFloat(value);
						break;
					case "STP":
						airPressureStation = Float.parseFloat(value);
						break;
					case "SLP":
						airPressureSeaLevel = Float.parseFloat(value);
						break;
					case "VISIB":
						visibility = Float.parseFloat(value);
						break;
					case "WDSP":
						windSpeed = Float.parseFloat(value);
						break;
					case "PRCP":
						rain = Float.parseFloat(value);
						break;
					case "SNDP":
						snow = Float.parseFloat(value);
						break;
					case "FRSHTT":
						events = Integer.parseInt(value, 2);
						break;
					case "CLDC":
						cloudAmount = Float.parseFloat(value);
						break;
					case "WNDDIR":
						windDirection = Integer.parseInt(value);
						break;
					default:
						System.out.println("[" + client.getInetAddress() + ":" + client.getPort() + "] Unknown key: " + key);
						break;
				}
			} catch (IllegalArgumentException e) {
				System.out.println("[" + client.getInetAddress() + ":" + client.getPort() + "] Received invalid value: " + value + ", " + e);
			}
		}
	}
}