import java.util.concurrent.ThreadPoolExecutor;

class Stats implements Runnable {
	private static int clients = 0;
	private static int measurements = 0;
	private static int db_sent = 0;

	static synchronized void inc_clients() {
		clients++;
	}

	static synchronized void dec_cients() {
		clients--;
	}

	static synchronized void inc_measurements() {
		measurements++;
	}

	static synchronized void set_db_sent(int c) {
		db_sent += c;
	}

	private static synchronized void res() {
		db_sent = 0;
		measurements = 0;
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("\nClients connected: " + Integer.toString(clients));
			System.out.println("Total threads: " + Integer.toString(java.lang.Thread.activeCount()));
			System.out.println("Active pool threads: " + Integer.toString(((ThreadPoolExecutor)XMLParser.clientProcessingPool).getActiveCount()));
			System.out.println("Parsing: " + Integer.toString(measurements) + "/s");
			System.out.println("Database insert rate: " + Integer.toString(db_sent) + "/s");
			res();
		}
	}
}