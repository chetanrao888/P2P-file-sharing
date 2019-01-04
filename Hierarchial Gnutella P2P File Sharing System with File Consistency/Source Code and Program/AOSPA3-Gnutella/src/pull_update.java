import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.Naming;

/**
 * 
 * @author Chetan Rao
 * @author Rahul Hosmani
 *
 */
//pull update- client maintains consistency by polling server
public class pull_update extends Thread {
	public String orig_port;
	public String fileName;
	public long version_number;
	public String fullFileName;
	public String refresh;
	public static int ttr;
	public static int counter,counter1;

	pull_update(String orig_port, String fileName, long version_number, String fullFileName, String refresh, int ttr) {
		this.orig_port = orig_port;
		this.fileName = fileName;
		this.version_number = version_number;
		this.fullFileName = fullFileName;
		this.refresh = refresh;
		pull_update.ttr = ttr;

	}

	public void run() {
		// check for stale files at ttr intervals
		autoupdate(orig_port, fileName, version_number, refresh, fullFileName);
//		

	}

	/**
	 * 
	 * @param orig_port      -- File belonging to peer
	 * @param fileName       -- filename of the downloaded file
	 * @param version_number -- version number associated with the file
	 * @param refresh        -- if file is stale, downloads modified version
	 * @param fullFileName   -- file name with path
	 */
	private static void autoupdate(String orig_port, String fileName, long version_number, String refresh,
			String fullFileName) {
		new java.util.Timer().schedule(new java.util.TimerTask() {

			public void run() {
				try {
					counter1++;
					PeerInterface PeerServer = null;
					long temp;
					FileOutputStream os;
					PeerServer = (PeerInterface) Naming.lookup("rmi://localhost:" + orig_port + "/peerServer");
					temp = PeerServer.pollServer(fileName);
					// check for stale files
					if (temp > version_number) {
						counter++;
						System.err.println(fullFileName+" discarded using pull approach");
						File n = new File(fullFileName);
						n.delete();
						if (refresh.equals("Yes")) {
							os = new FileOutputStream(fullFileName);
							byte[] buffer = PeerServer.obtain(fileName);
							os.write(buffer, 0, buffer.length);
							os.close();
						}

					}
				} catch (Exception e) {
					System.out.println("Push Failed");
				}
				try {
					String print = "counter" + counter + "counter1" + counter1 + "ans" + counter/counter1;							
					Files.write(Paths.get("C:\\Users\\Chetan\\Google Drive\\My_PC\\OS\\HW\\HW3\\test\\AOSPA3-Gnutella\\src\\result2.txt"),
							print.getBytes(), StandardOpenOption.APPEND);
				} catch (IOException e) {
					System.err.println("Evaluation Measure" + e);

				}
			}
		}, 0, ttr);
	}
}
