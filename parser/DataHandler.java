import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

class DataHandler extends Thread {
	private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
	private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH);
	private Socket client;
	private BufferedReader reader;
	private PreparedStatement pst;
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
	private Integer events = null; // Binary
	private Float cloudAmount = null;
	private Integer windDirection = null;

	DataHandler(Socket client, Connection con, String sql) throws IOException, SQLException {
		this.client = client;
		this.reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
		this.pst = con.prepareStatement(sql);
		System.out.println("CONNECTED: " + client.getInetAddress() + ":" + client.getPort());
	}

	public void run() {
		try {
			read();
			this.client.close();
			this.pst.close();
		} catch (IOException | SQLException e) {
			System.out.println("Client connection error" + ", " + e);
		} finally {
			System.out.println("DISCONNECTED: " + client.getInetAddress() + ":" + client.getPort());
		}
	}

	private void read() throws IOException  {
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
					// System.out.println("[" + client.getInetAddress() + ":" + client.getPort() + "] Successfully received data for station: " + Integer.toString(stationID));

					// Comment sendData om geen data te verzenden naar de database
					sendData(stationID, Timestamp.valueOf(LocalDateTime.of(date, time)), temperature, dewPoint, airPressureStation, airPressureSeaLevel, visibility, windSpeed, rain, snow, events, cloudAmount, windDirection);
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

	private void sendData(Integer stationID, Timestamp timestamp, Float temperature, Float dewPoint, Float airPressureStation, Float airPressureSeaLevel, Float visibility, Float windSpeed, Float rain, Float snow, Integer events, Float cloudAmount, Integer windDirection) {
		try {
			pst.setInt(1, stationID);
			pst.setTimestamp(2, timestamp);
			if (temperature != null)
				pst.setFloat(3, temperature);
			else
				pst.setNull(3, Types.FLOAT);
			if (dewPoint != null)
				pst.setFloat(4, dewPoint);
			else
				pst.setNull(4, Types.FLOAT);
			if (airPressureStation != null)
				pst.setFloat(5, airPressureStation);
			else
				pst.setNull(5, Types.FLOAT);
			if (airPressureSeaLevel != null)
				pst.setFloat(6, airPressureSeaLevel);
			else
				pst.setNull(6, Types.FLOAT);
			if (rain != null)
				pst.setFloat(7, rain);
			else
				pst.setNull(7, Types.FLOAT);
			if (snow != null)
				pst.setFloat(8, snow);
			else
				pst.setNull(8, Types.FLOAT);
			if (cloudAmount != null)
				pst.setFloat(9, cloudAmount);
			else
				pst.setNull(9, Types.FLOAT);
			if (visibility != null)
				pst.setFloat(10, visibility);
			else
				pst.setNull(10, Types.FLOAT);
			if (events != null)
				pst.setInt(11, events);
			else
				pst.setNull(11, Types.INTEGER);
			if (windDirection != null)
				pst.setInt(12, windDirection);
			else
				pst.setNull(12, Types.INTEGER);
			if (windSpeed != null)
				pst.setFloat(13, windSpeed);
			else
				pst.setNull(13, Types.FLOAT);
			pst.setQueryTimeout(1);
			pst.executeUpdate();
		} catch (SQLTimeoutException e) {
			System.out.println("[" + client.getInetAddress() + ":" + client.getPort() + "] SQL Error, query execution is taking too long (timeout = 1 second)" );
		} catch (SQLException e ) {
			System.out.println("[" + client.getInetAddress() + ":" + client.getPort() + "] SQL Error, " + e);
		}
	}
}