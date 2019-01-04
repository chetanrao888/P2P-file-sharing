
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.rmi.Naming;
import java.util.List;

/**
 * 
 * @author Rahul Hosmani
 * @author Chetan Rao
 * 
 */
public class WatchThread extends Thread {

	Path myDir;
	WatchService watcher;
	private String portNo;
	private String peerId;
	private String dirName;
	boolean sysCond=true;
/**
 * 
 * @param path Poll for changes (Create/ Delete) in the shared directory of the client. 
 * @param port_No Port number of the client
 * @param peer_Id Peer id of the client
 */
	WatchThread(String path, String port_No, String peer_Id) {
		try {
			myDir = Paths.get(path);
			dirName = path;
			//create watch service for the directory dirName
			watcher = myDir.getFileSystem().newWatchService();													
			//register types of events to be watched (create / delete)
			myDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
			portNo = port_No;
			peerId = peer_Id;
		} catch(java.nio.file.NoSuchFileException ed) 
		{
			System.err.println("Directory name invalid. Process Halted!!");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (sysCond) {
			try {			
				//obtain server reference to register files as and when they are downloaded and de-register files when they are deleted
				P2PInterface hello = (P2PInterface) Naming.lookup("rmi://" + Client.serverIp + ":1099/Hello");		
				//token representing the registration of a watchable object with a WatchService
				WatchKey watchKey = watcher.take();																					
				//list to maintain poll of events
				List<WatchEvent<?>> events = watchKey.pollEvents();																											
				for (WatchEvent<?> event : events) {
					//this event is called when a new file is created / downloaded
					if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {			
						//when a new file is added / created. Register the file on the server
						hello.registry(peerId, event.context().toString(), portNo, dirName);						 
					}
					//watch for delete events
					if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
						//unregister the deleted file on the server
						hello.registry(peerId, event.context().toString(), null, dirName);							
					}
				}
				//after an event is serviced, go back to ready state
				watchKey.reset();									
			}catch(java.rmi.ConnectIOException ed)
			{
				sysCond = false;
				System.err.println("Server is either offline / Wrong IP address provided by user");
			}
			catch (Exception e) {
				sysCond = false;
				System.out.println("Error: " + e.toString());
			}
		}
	}
}