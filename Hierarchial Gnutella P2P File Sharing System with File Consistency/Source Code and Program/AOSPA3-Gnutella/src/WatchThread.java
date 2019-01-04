
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.rmi.Naming;
import java.util.List;
import java.util.Properties;

/**
 * 
 * @author Rahul Hosmani
 * @author Chetan Rao
 * 
 */
public class WatchThread extends Thread {

	Path myDir;
	String path;
	WatchService watcher;
	private String portNo;
	private String peerId;
	boolean sysCond = true;
	int searchCounter = 0;
	int ttl = 10;

	/**
	 * 
	 * @param path    Poll for changes (Create/ Delete) in the shared directory of
	 *                the client.
	 * @param port_No Port number of the client
	 * @param peer_Id Peer id of the client
	 */
	WatchThread(String path, String port_No, String peer_Id) {
		try {
			myDir = Paths.get(path);
			this.path = path;
			// create watch service for the directory dirName
			watcher = myDir.getFileSystem().newWatchService();
			// register types of events to be watched (create / delete)
			myDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY);
			portNo = port_No;
			peerId = peer_Id;
		} catch (java.nio.file.NoSuchFileException ed) {
			System.err.println("Directory name invalid. Process Halted!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (sysCond) {
			try {
				String property = null;
				String approach = null;
				// obtain server reference to register files as and when they are downloaded and
				// de-register files when they are deleted
				PeerInterface hello = (PeerInterface) Naming.lookup("rmi://localhost:" + portNo + "/peerServer");
				// token representing the registration of a watchable object with a WatchService
				WatchKey watchKey = watcher.take();
				Properties prop = new Properties();
				FileInputStream input = new FileInputStream("./src/config.properties");
				// load a properties file
				prop.load(input);
				property = "approach";
				approach = prop.getProperty(property);
				// list to maintain poll of events
				List<WatchEvent<?>> events = watchKey.pollEvents();
				for (WatchEvent<?> event : events) {
					// this event is called when a new file is created / downloaded
//					if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {			
//						//when a new file is added / created. Register the file on the server
//						System.err.println("File created");
//						hello.registry(peerId, event.context().toString(), portNo);						 
//					}
					// watch for delete events
					if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
						// unregister the deleted file on the server
						System.err.println("File Deleted watch thread");
						hello.registry(peerId, event.context().toString(), null, null);
					}
					/*----------------------start change--------------------*/
					// watch for modification events
					if (approach.equals("push")) {
						if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY && path.contains("peer")) {
							// unregister the deleted file on the server
							System.err.println("File modified");
							long version_number = System.currentTimeMillis();
							System.out.println(event.context().toString());
							searchCounter++;
							String msgId = "Peer" + peerId + ".Search" + searchCounter;
							// server push message- sent if a file is modified by the server
							hello.invalidation(Integer.parseInt(peerId), msgId, event.context().toString(), ttl,
									version_number);
						}
					}
					/*----------------------end change--------------------*/
				}
				// after an event is serviced, go back to ready state
				watchKey.reset();
			} catch (java.rmi.ConnectIOException ed) {
				sysCond = false;
				System.err.println("Server is either offline / Wrong IP address provided by user");
			} catch (Exception e) {
				sysCond = false;
				System.out.println("Error: " + e.toString());
			}
		}
	}
}