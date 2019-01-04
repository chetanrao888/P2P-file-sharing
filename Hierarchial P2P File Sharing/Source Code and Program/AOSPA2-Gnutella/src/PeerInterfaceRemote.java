
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
	private static final long serialVersionUID = 1L;
	String sharedDirectory;
	ArrayList<String> localFiles = new ArrayList<String>();
	ArrayList<String> msgIds;
	private ArrayList<fileInfo_server> files;
	int peerId;
	int currentPeerPort;

	PeerInterfaceRemote(String sharedDir, int peerId, int currentPeerPort, ArrayList<String> localFiles)
			throws RemoteException {
		super();
		// store the constructor parameters
		this.sharedDirectory = sharedDir;
		this.peerId = peerId;
		this.localFiles = localFiles;
		this.currentPeerPort = currentPeerPort;
		// to maintain list of messages seen
		msgIds = new ArrayList<String>();
		files = new ArrayList<fileInfo_server>();
	}

	public synchronized void registry(String peerId, String fileName, String portNo) {
		// TODO Auto-generated method stub
		fileInfo_server fi = new fileInfo_server(peerId, fileName, portNo);

		if (portNo == null) {
			for (int i = 0; i < files.size(); i++) {
				if (peerId == fi.peerId & fileName == fi.fileName) {
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
		String fullFileName = sharedDirectory + "/" + filename;
		try {
			File myFile = new File(fullFileName);
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
				//reduce list size to avoid infinite size issue
				if(msgIds.size()>100)
				{
					for(int c=0;c<50;c++)
						msgIds.remove(c);
				}
				//decrease ttl after receiving
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
					peerFound.add(temp);
				}

			}
			hitqueryResult.peerFound.addAll(peerFound);
			hitqueryResult.pathTraversed.addAll(trace_path);
			if (foundLocal != true) {
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
					NeighborConnectionThread ths = new NeighborConnectionThread(neighborPeers.get(i).ip,
							neighborPeers.get(i).portno, filename, msgId, peerId, neighborPeers.get(i).peerId, ttl);
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

}
