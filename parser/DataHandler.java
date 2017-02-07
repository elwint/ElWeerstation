import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.sql.*;

class DataHandler implements Runnable {
	private final Socket client;
	private final BufferedReader reader;
	private final DatabaseHandler db_handler;
	private boolean disconnected = false;
	private Integer stationID = null;
	private String date = null;
	private String time = null;
	private Timestamp timestamp = null;
	private Float temperature = null;
	private Float dewPoint = null;
	private Float airPressureStation = null;
	private Float airPressureSeaLevel = null;
	private Float visibility = null;
	private Float windSpeed = null;
	private Float rain = null;
	private Float snow = null;
	private Integer events = null; // Binary
	private Float cloudAmount = null;
	private Integer windDirection = null;

	DataHandler(Socket client, BufferedReader reader, DatabaseHandler db_handler) {
		this.client = client;
		this.reader = reader;
		this.db_handler = db_handler;
	}

	public void run() {
		try {
			read(this.reader);
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

	private void read(BufferedReader reader) throws IOException  {
		String ln;
		String key;
		String value;
		boolean read = false;

		while (true) {
			ln = reader.readLine();
			if (ln == null) {
				disconnected = true;
				return;
			}

			// System.out.println(ln);
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
				if (stationID == null || timestamp == null) {
					System.out.println("[" + client.getInetAddress() + ":" + client.getPort() + "] Missing required data, drop measurement");
				} else {
					// System.out.println("[" + client.getInetAddress() + ":" + client.getPort() + "] Successfully received data for station: " + Integer.toString(stationID));
					Stats.inc_measurements();
					db_handler.prepareData(stationID, timestamp, temperature, dewPoint, airPressureStation, airPressureSeaLevel, visibility, windSpeed, rain, snow, events, cloudAmount, windDirection);
				}
				read = false;

				stationID = null;
				date = null;
				time = null;
				timestamp = null;
				temperature = null;
				dewPoint = null;
				airPressureStation = null;
				airPressureSeaLevel = null;
				visibility = null;
				windSpeed = null;
				rain = null;
				snow = null;
				events = null;
				cloudAmount = null;
				windDirection = null;
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
						if (time != null)
							timestamp = Timestamp.valueOf(value + " " + time);
						date = value;
						break;
					case "TIME":
						if (date != null)
							timestamp = Timestamp.valueOf(date + " " + value);
						time = value;
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