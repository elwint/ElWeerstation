import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

class DataHandler {
	private Socket client;
	private BufferedReader reader;
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

	DataHandler(Socket client) throws IOException {
		this.client = client;
		this.reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
		System.out.println("CONNECTED: " + client.getInetAddress() + ":" + client.getPort());
	}

	void run() throws IOException {
		getData();
		this.client.close();
		System.out.println("DISCONNECTED: " + client.getInetAddress() + ":" + client.getPort());
	}

	private void getData() throws IOException  {
		String ln;
		String value;
		boolean read = false;

		while ((ln = this.reader.readLine()) != null) {
			String key = ln.substring(ln.indexOf("<") + 1, ln.indexOf(">"));
			if (key.equals("MEASUREMENT")) {
				read = true;
			}

			if (!read) {
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
					default:
						break;
				}
			} catch (DateTimeParseException | NumberFormatException e) {
				System.out.println("[" + client.getInetAddress() + ":" + client.getPort() + "] Received invalid value: " + value);
			}
		}
	}
}