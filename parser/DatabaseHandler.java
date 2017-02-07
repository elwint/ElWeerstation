import java.sql.*;

public class DatabaseHandler implements Runnable {
	private PreparedStatement pst = null;
	private boolean db_data = false;

	DatabaseHandler(String dbHost, String dbPort, String dbDatabase, String dbUser, String dbPass, String sql) {
		try {
			Connection con  = DriverManager.getConnection("jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbDatabase, dbUser, dbPass);
			if (con != null) {
				pst = con.prepareStatement(sql);
				// pst.setQueryTimeout(1);
				con.setAutoCommit(false);
				System.out.println("Connected successfully to database server");
			} else {
				System.out.println("Database server connection failed");
				System.exit(1);
			}
		} catch (SQLException e) {
			System.out.println("Database server connection failed, " + e);
			System.exit(1);
		}
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (db_data) {
				sendData();
				db_data = false;
			}
		}
	}

	private synchronized void sendData() {
		try {
			int[] c = pst.executeBatch();
			pst.getConnection().commit();
			Stats.set_db_sent(c.length);
		} catch (SQLException e) {
			System.out.println("SQL Error, " + e);
		}
	}

	synchronized void prepareData(Integer stationID, Timestamp timestamp, Float temperature, Float dewPoint, Float airPressureStation, Float airPressureSeaLevel, Float visibility, Float windSpeed, Float rain, Float snow, Integer events, Float cloudAmount, Integer windDirection) {
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
			pst.addBatch();
			db_data = true;
		} catch (SQLException e) {
			System.out.println("SQL Error, " + e);
		}
	}
}
