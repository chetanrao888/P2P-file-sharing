
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 
 * @author Rahul Hosmani
 * @author Chetan Rao
 * 
 */
public class PeerInterfaceRemote extends UnicastRemoteObject implements PeerInterface {
	public static int counter = 0;
	public static int counter1 = 1;
	private static final long serialVersionUID = 1L;
	String masterDir, dubDir;
	ArrayList<String> localFiles = new ArrayList<String>();
	ArrayList<String> msgIds;
	private ArrayList<fileInfo_server> files;
	int peerId;
	int currentPeerPort;
	File delfind;

	PeerInterfaceRemote(String sharedDir, String dubDir, int peerId, int currentPeerPort, ArrayList<String> localFiles)
			throws RemoteException {
		super();
		// store the constructor parameters
		this.masterDir = sharedDir;
		this.dubDir = dubDir;
		this.peerId = peerId;
		this.localFiles = localFiles;
		this.currentPeerPort = currentPeerPort;
		// to maintain list of messages seen
		msgIds = new ArrayList<String>();
		files = new ArrayList<fileInfo_server>();
	}

	public synchronized void registry(String peerId, String fileName, String portNo, String orig) {
		// TODO Auto-generated method stub
		fileInfo_server fi = new fileInfo_server(peerId, fileName, portNo, orig);

		if (portNo == null) {
			for (int i = 0; i < files.size(); i++) {
				if (files.get(i).peerId.equals(fi.peerId) && fileName.equals(files.get(i).fileName)) {
					System.out.println("\nFile: " + fi.fileName + " Deleted. Belonging to" + fi.peerId);
					files.remove(i);
				}
			}
		} else {
			this.files.add(fi);
			System.out.println(
					"\nFile Added: " + fi.fileName + " | Peer ID: " + fi.peerId + " | IP:Port Number: " + portNo);
		}
	}

	public synchronized byte[] obtain(String filename) throws RemoteException {
		byte[] fileBytes = null;
		String fullFileName = null;

		fullFileName = masterDir + "\\" + filename;
		try {
			File myFile = new File(fullFileName);
			if (!myFile.exists()) {
				fullFileName = dubDir + "\\" + filename;
				myFile = new File(fullFileName);
			}
			// create buffer with size equal to file length(size)
			fileBytes = new byte[(int) myFile.length()];
			// create buffered input stream to read bytes of data
			BufferedInputStream input = new BufferedInputStream(new FileInputStream(fullFileName));
			// read file contents into the buffer
			input.read(fileBytes, 0, fileBytes.length);
			input.close();
			// returns the buffer(file) to the client -- (upload action)
			return fileBytes;
		} catch (Exception e) {
			System.err.println("File Retrival Exception: " + e.getMessage());
			e.printStackTrace();
			return null;
		}

	}

	public HitQuery query(int fromPeerId, String msgId, String filename, int ttl) throws RemoteException {
		// Remote method for handling the search request
		ArrayList<NeighborPeers> neighborPeers = new ArrayList<NeighborPeers>();
		ArrayList<PeerDetails> peerFound = new ArrayList<PeerDetails>();
		// records trace path of the query message
		ArrayList<String> trace_path = new ArrayList<String>();
		// used to store trace path and peer which has the requested file
		HitQuery hitqueryResult = new HitQuery();
		Boolean msgDuplicate = false;
		ArrayList<NeighborConnectionThread> peerThreadsList = new ArrayList<NeighborConnectionThread>();
		synchronized (this) {
			// check for the duplicate request
			if (this.msgIds.contains(msgId) || ttl == 0) {
				System.out.println("Incoming request to peer " + peerId + ": From - " + fromPeerId
						+ " searched already in this peer- with message id - A Duplicate Request -"
						+ String.valueOf(msgId));
				msgDuplicate = true;
			}

			else {
				// reduce list size to avoid infinite size issue
				if (msgIds.size() > 100) {
					for (int c = 0; c < 50; c++)
						msgIds.remove(c);
				}
				// decrease ttl after receiving
				ttl--;
				// Store the messaged id to avoid duplicate search
				this.msgIds.add(msgId);

			}
		}
		if (msgDuplicate == false) {
			Boolean foundLocal = false;
			List<Thread> threads = new ArrayList<Thread>();
			// Search the filename in the local registry
			ArrayList<fileInfo_server> matchedFiles = new ArrayList<fileInfo_server>();
			for (int i = 0; i < this.files.size(); i++) {
				if (filename.equalsIgnoreCase(files.get(i).fileName)) {
					matchedFiles.add(files.get(i));
					foundLocal = true;
					PeerDetails temp = new PeerDetails();
					temp.hostIp = "localhost";
					temp.peerId = Integer.parseInt(files.get(i).peerId);
					temp.port = Integer.parseInt(files.get(i).portNumber);
					temp.orig = files.get(i).orig_port;
					peerFound.add(temp);
				}

			}
			hitqueryResult.peerFound.addAll(peerFound);
			hitqueryResult.pathTraversed.addAll(trace_path);
			if (foundLocal != true) {
				// file not found in the registry-- further search in other super peers
				System.out
						.println("File not found in the parent super peer-- Proceeding to search in other super peers");
				// Read configproperties file to get details of neighbor peers
				getNeighbor(neighborPeers, peerId);
				if (neighborPeers.size() == 0) {
					trace_path.add(Integer.toString(peerId));
				}

				for (int i = 0; i < neighborPeers.size(); i++) {
					String currentPeer = "peerid." + fromPeerId;
					if (neighborPeers.get(i).peerId.equals(currentPeer)) {
						// avoid sending request back to the sender
						continue;
					}
					NeighborConnectionThread ths = new NeighborConnectionThread(neighborPeers.get(i).ip,
							neighborPeers.get(i).portno, filename, msgId, peerId, neighborPeers.get(i).peerId, ttl,
							"query", 0);
					Thread ts = new Thread(ths);
					// start thread for new request
					ts.start();
					// store the instances to get the return values after all the threads finish the
					threads.add(ts);
					peerThreadsList.add(ths);

				}
				for (int i = 0; i < threads.size(); i++)
					try {
						// wait for all the request threads finish the search
						((Thread) threads.get(i)).join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

//				for (int i = 0; i < threads.size(); i++)
//					try {
//						((Thread) threads.get(i)).join();
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
				for (int i = 0; i < peerThreadsList.size(); i++) {
					HitQuery temp1 = new HitQuery();
					// Get the result of the thread request
					temp1 = (HitQuery) peerThreadsList.get(i).getValue();
					if (temp1.peerFound.size() > 0) {
						peerFound.addAll(temp1.peerFound);
					}
					for (int count = 0; count < temp1.pathTraversed.size(); count++) {
						String path = peerId + temp1.pathTraversed.get(count);
						trace_path.add(path);
					}
				}
				if (trace_path.size() == 0) {
					trace_path.add(Integer.toString(peerId));
				}
				// send the result back to the sender
				System.out.println("Query Hit: backpropogate the result to " + fromPeerId);
				for (int i = 0; i < peerFound.size(); i++) {
					System.out.println(
							"--Found at Peer" + peerFound.get(i).peerId + " on localhost:" + peerFound.get(i).port);

				}
				hitqueryResult.peerFound.addAll(peerFound);
				hitqueryResult.pathTraversed.addAll(trace_path);
			}
		}

		return hitqueryResult;

	}

	private void getNeighbor(ArrayList<NeighborPeers> neighborPeers, int peerId) {
		// Get the Neighbor peers for current peer id
		String property = null;
		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream("./src/config.properties");
			// load a properties file
			prop.load(input);
			property = "peerid." + peerId + ".neighbors";
			// get the property value and print it out
			String value = prop.getProperty(property);
			if (value != null) {
				String[] strNeighbors = value.split(",");
				for (int i = 0; i < strNeighbors.length; i++) {
					NeighborPeers tempPeer = new NeighborPeers();
					// get the peer details
					tempPeer.peerId = strNeighbors[i];
					tempPeer.ip = prop.getProperty(strNeighbors[i] + ".ip");
					tempPeer.portno = Integer.parseInt(prop.getProperty(strNeighbors[i] + ".port"));
					neighborPeers.add(tempPeer);
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/*----------------start change----------------*/
	/**
	 * broadcast invalidate message to all peers
	 */
	public void invalidation(int fromPeerId, String msgId, String filename, int ttl, long version_number)
			throws RemoteException {
		// Remote method for handling the search request
		ArrayList<NeighborPeers> neighborPeers = new ArrayList<NeighborPeers>();
		// records trace path of the query message
		ArrayList<String> trace_path = new ArrayList<String>();
		// used to store trace path and peer which has the requested file
		// HitQuery hitqueryResult = new HitQuery();
		Boolean msgDuplicate = false;
		ArrayList<NeighborConnectionThread> peerThreadsList = new ArrayList<NeighborConnectionThread>();
		long local_version_number;
		counter1++;
		synchronized (this) {
			// check for the duplicate request
			if (this.msgIds.contains(msgId) || ttl == 0) {
				System.out.println("Incoming request to peer " + peerId + ": From - " + fromPeerId
						+ " searched already in this peer- with message id - A Duplicate Request -"
						+ String.valueOf(msgId));
				msgDuplicate = true;
			}

			else {
				// reduce list size to avoid infinite size issue
				if (msgIds.size() > 100) {
					for (int c = 0; c < 50; c++)
						msgIds.remove(c);
				}
				// decrease ttl after receiving
				ttl--;
				// Store the messaged id to avoid duplicate search
				this.msgIds.add(msgId);

			}
		}
		if (dubDir.contains("downpeer4")) {
			System.out.println("check");
		}
		System.out.println("filename:" + filename);
		System.out.println("dubDir:" + dubDir);
		if (msgDuplicate == false) {
			List<Thread> threads = new ArrayList<Thread>();
			File directoryObject2 = new File(dubDir);
			String[] delfilename2 = directoryObject2.list();
			// Search the filename in the local registry
			if (!(dubDir.contains("downpeer1") | dubDir.contains("downpeer5") | dubDir.contains("downpeer9")
					| dubDir.contains("downpeer13") | dubDir.contains("downpeer17") | dubDir.contains("downpeer21")
					| dubDir.contains("downpeer25") | dubDir.contains("downpeer29") | dubDir.contains("downpeer33")
					| dubDir.contains("downpeer37"))) {
				System.out.println("Server" + delfilename2.length);
				for (int i = 0; i < delfilename2.length; i++) {
					System.out.println("display names:" + delfilename2[i]);
				}
				for (int i = 0; i < delfilename2.length; i++) {
					System.out.println("filename" + delfilename2[i] + "directory file name" + filename);
					if (filename.equalsIgnoreCase(delfilename2[i])) {
						System.err.println("Deleting stale file");
						delfind = new File(dubDir + "\\" + delfilename2[i]);
						local_version_number = delfind.lastModified();
						// discard file if stale
						if (local_version_number < version_number)
						{
							System.err.println(dubDir + "\\" + delfilename2[i] + " discarded using push approach");
							delfind.delete();
							counter++;
						}
							
							
					}

				}
			}
			// file not found in the registry-- further search in other super peers
			System.out.println("File not found in the parent super peer-- Proceeding to search in other super peers");
			// Read configproperties file to get details of neighbor peers
			getNeighbor(neighborPeers, peerId);
			if (neighborPeers.size() == 0) {
				trace_path.add(Integer.toString(peerId));
			}

			for (int i = 0; i < neighborPeers.size(); i++) {
				String currentPeer = "peerid." + fromPeerId;
				if (neighborPeers.get(i).peerId.equals(currentPeer)) {
					// avoid sending request back to the sender
					continue;
				}
				System.out.println(neighborPeers.get(i).ip + neighborPeers.get(i).portno + filename + msgId + peerId
						+ neighborPeers.get(i).peerId + ttl + "invalidate");
				NeighborConnectionThread ths = new NeighborConnectionThread(neighborPeers.get(i).ip,
						neighborPeers.get(i).portno, filename, msgId, peerId, neighborPeers.get(i).peerId, ttl,
						"invalidate", version_number);
				Thread ts = new Thread(ths);
				// start thread for new request
				ts.start();
				// store the instances to get the return values after all the threads finish the
				threads.add(ts);
				peerThreadsList.add(ths);

			}
			for (int i = 0; i < threads.size(); i++)
				try {
					// wait for all the request threads finish the search
					((Thread) threads.get(i)).join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			for (int i = 0; i < threads.size(); i++)
				try {
					((Thread) threads.get(i)).join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

		}
		System.out.println("Valid/Invalid percentage:"+counter/counter1);
		try {
			String print = "counter"+counter+"counter1"+counter1+"ans"+Integer.toString((int) counter/counter1);							
			Files.write(Paths.get("C:\\Users\\Chetan\\Google Drive\\My_PC\\OS\\HW\\HW3\\test\\AOSPA3-Gnutella\\src\\result1.txt"),
					print.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			System.err.println("Evaluation Measure" + e);

		}
	}

	/**
	 * retrieve version number of the file on the server
	 */
	public long pollServer(String fileName) throws RemoteException {

		String fullFileName = null;
		long version_number = 0;
		fullFileName = masterDir + "\\" + fileName;
		try {
			File myFile = new File(fullFileName);
			if (!myFile.exists()) {
				fullFileName = dubDir + "\\" + fileName;
				myFile = new File(fullFileName);
			}
			version_number = myFile.lastModified();
			// version number of the file
			return version_number;
		} catch (Exception e) {
			System.out.println("Client polling on server failed");
			return version_number;
		}

		/*----------------end change------------------*/

	}
}