import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

class DataHandler {
	private Socket client;
	private BufferedReader reader;
	private Connection db;
	private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
	private DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH);
	private Integer stationID = null;
	private LocalDate date = null;
	private LocalTime time = null;
	private Float temperature = null;
	private Float dewPoint = null;
	private Float airPressureStation = null;
	private Float airPressureSeaLevel = null;
	private Float visibility = null;
	private Float windSpeed = null;
	private Float rain = null;
	private Float snow = null;
	private Integer events = null; // binair
	private Float cloudAmount = null;
	private Integer windDirection = null;

	DataHandler(Socket client, Connection con) throws IOException {
		this.client = client;
		this.reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
		this.db = con;
		System.out.println("CONNECTED: " + client.getInetAddress() + ":" + client.getPort());
	}

	void start() throws IOException {
		run();
		this.client.close();
		System.out.println("DISCONNECTED: " + client.getInetAddress() + ":" + client.getPort());
	}

	private void run() throws IOException  {
		String ln;
		String key;
		String value;
		boolean read = false;

		while ((ln = this.reader.readLine()) != null) {
			try {
				key = ln.substring(ln.indexOf("<") + 1, ln.indexOf(">"));
			} catch (IndexOutOfBoundsException e) {
				continue;
			}

			if (key.equals("MEASUREMENT")) {
				read = true;
				continue;
			} else if (key.trim().isEmpty() || !read) {
				continue;
			}

			if (key.equals("/MEASUREMENT")) {
				if (stationID == null || date == null || time == null) {
					System.out.println("[" + client.getInetAddress() + ":" + client.getPort() + "] Missing required data, drop measurement");
				} else {
					System.out.println("[" + client.getInetAddress() + ":" + client.getPort() + "] Successfully received data for station: " + Integer.toString(stationID));
					//TODO: SQL Magic?
					System.out.println(date);
					System.out.println(time);
					System.out.println(temperature);
					System.out.println(dewPoint);
					System.out.println(airPressureStation);
					System.out.println(airPressureSeaLevel);
					System.out.println(visibility);
					System.out.println(windSpeed);
					System.out.println(rain);
					System.out.println(snow);
					if (events != null) {
						System.out.println(Integer.toBinaryString(events));
					} else {
						System.out.println("null");
					}
					System.out.println(cloudAmount);
					System.out.println(windDirection);
				}
				read = false;

				stationID = null;
				date = null;
				time = null;
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
						date = LocalDate.parse(value, dateFormat);
						break;
					case "TIME":
						time = LocalTime.parse(value, timeFormat);
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
			} catch (DateTimeParseException | NumberFormatException e) {
				System.out.println("[" + client.getInetAddress() + ":" + client.getPort() + "] Received invalid value: " + value);
			}
		}
	}

	private void query(String sql) {
		Statement stmt = null;
		try {
			stmt = this.db.createStatement();
			stmt.executeUpdate(sql);
		} catch (SQLException e ) {
			System.out.println("[" + client.getInetAddress() + ":" + client.getPort() + "] SQL Error, " + e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}