
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * 
 * @author Rahul Hosmani
 * @author Chetan Rao
 * 
 */

public class Peer {

	/**
	 * Method to get the list of all the local files in the directory
	 * 
	 * @param sharedDir  The directory path
	 * @param localFiles Array list of all the files in the directory
	 */
	public static void register_file(String sharedDir, ArrayList<String> localFiles, int neighport, int port,
			int peerid) {
		try {
			PeerInterface hello = (PeerInterface) Naming.lookup("rmi://localhost:" + neighport + "/peerServer");
			File directoryObject = new File(sharedDir);
			File newfind;
			String filename;
			String peerId = Integer.toString(peerid);
			String portNo = Integer.toString(port);
			String[] filesList = directoryObject.list();
			for (int i = 0; i < filesList.length; i++) {
				newfind = new File(filesList[i]);
				filename = newfind.getName();
				// Storing the file name in arrayList
				hello.registry(peerId, filename, portNo);
				localFiles.add(filename);
			}
		} catch (Exception e) {
			System.out.println("issue with registering file with server" + e);
		}

	}

	/**
	 * Method to Run peer as a Server
	 * 
	 * @param peerId     Peer id of the server
	 * @param port       Port number of the server
	 * @param sharedDir  Shared directory path
	 * @param localFiles Arraylist of local files
	 */
	public static void runPeerServer(int peerId, int port, String sharedDir, ArrayList<String> localFiles) {
		try {
			LocateRegistry.createRegistry(port);
			PeerInterface stub = new PeerInterfaceRemote(sharedDir, peerId, port, localFiles);
			Naming.rebind("rmi://localhost:" + port + "/peerServer", stub);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Method to get the list of neighboring peers
	 * 
	 * @param neighborPeers Arraylist of neighbor peers
	 * @param peerId        Peer ID of the peer
	 */
	public static void getNeighborPeers(ArrayList<NeighborPeers> neighborPeers, int peerId) {

		String property = null;
		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream("./src/config.properties");
			prop.load(input);
			property = "peerid." + peerId + ".neighbors";

			String[] strNeighbors = prop.getProperty(property).split(",");

			for (int i = 0; i < strNeighbors.length; i++) {
				NeighborPeers tempPeer = new NeighborPeers();
				tempPeer.peerId = strNeighbors[i];
				tempPeer.ip = prop.getProperty(strNeighbors[i] + ".ip");
				tempPeer.portno = Integer.parseInt(prop.getProperty(strNeighbors[i] + ".port"));
				neighborPeers.add(tempPeer);
			}

		} catch (Exception e) {
			e.printStackTrace();
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

	/**
	 * Method to download file
	 * 
	 * @param searchResult_Peers
	 * @param peerId             Peer ID of the peer
	 * @param fileName           File name
	 * @param Path               Shared directory path
	 * @throws IOException
	 */
	public static void download(ArrayList<PeerDetails> searchResult_Peers, int peerId, String fileName, String Path)
			throws IOException {
		// Download functionality
		int peer_count = 0;
		int port = 0;
		String Host = null;
		OutputStream os = null;
		while (peer_count < searchResult_Peers.size()) {
			if (peerId == searchResult_Peers.get(peer_count).peerId) {
				port = searchResult_Peers.get(peer_count).port;
				Host = searchResult_Peers.get(peer_count).hostIp;
				break;
			}
			peer_count++;
		}

		System.out.println("Downloading from " + Host + ":" + port);
		// Get an object for peer server to download the file.
		PeerInterface PeerServer = null;
		try {
			PeerServer = (PeerInterface) Naming.lookup("rmi://localhost:" + port + "/peerServer");
			File destFile = new File(Path);
			System.out.println("file " + fileName);
			if (!destFile.exists()) {
				destFile.createNewFile();
			}

			os = new FileOutputStream(Path + "\\" + fileName);
			byte[] buffer = PeerServer.obtain(fileName);
			os.write(buffer, 0, buffer.length);
			System.out.println("File copied");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String args[]) {

		String sharedDir, dubDir;
		ArrayList<String> localFiles = new ArrayList<String>();
		List<Thread> threadInstancesList = new ArrayList<Thread>();
		int port;
		int peerid;
		int searchCounter = 0;
		int choice;
		Boolean bExit = false;
		ArrayList<NeighborPeers> neighborPeers = new ArrayList<NeighborPeers>();
		String searchFileName;
		ArrayList<PeerDetails> searchResult_Peers = new ArrayList<PeerDetails>();
		ArrayList<NeighborConnectionThread> neighborConnThreadList = new ArrayList<NeighborConnectionThread>();
		int download_peerid;

		try {
			Scanner sc = new Scanner(System.in);

			// Collect peerid and port from the user.
			System.out.println("Please enter the Peer ID as configured in the Configuration Properties File");
			peerid = Integer.parseInt(sc.nextLine());

			String property, property2 = null;
			Properties prop = new Properties();
			InputStream input = null;
			int neighborport;
			input = new FileInputStream("./src/config.properties");
			// load a properties file
			prop.load(input);
			property = "peerid." + peerid + ".port";
			
			port = Integer.parseInt(prop.getProperty(property));

			System.out.println("Session for peer id: " + peerid + " started...");
			//
			// Get the directory to share with other peers.
			property = "peerid." + peerid + ".shareDir";
			property2 = "peerid." + peerid + ".dubDir";
			sharedDir = System.getProperty("user.dir") + prop.getProperty(property);
			dubDir = System.getProperty("user.dir") + prop.getProperty(property2);
			// added
			property = "peerid." + peerid + ".neighbors";

			String[] strNeighbors = prop.getProperty(property).split(",");
			neighborport = Integer.parseInt(prop.getProperty(strNeighbors[0] + ".port"));

			runPeerServer(peerid, port, sharedDir, localFiles);
			if (strNeighbors.length > 1) {
				System.out.println("Acting as super peer");
			} else {
				new WatchThread(sharedDir, Integer.toString(neighborport), Integer.toString(peerid)).start();
				// ended
				// Get the filenames that existed locally
				register_file(sharedDir, localFiles, neighborport, port, peerid);
				//
				// Run peer as a server on a specific port -- may be only for super peer

				// User Menu -- if else --- non super peers have access to the below code
				while (true) {
					System.out.println(
							"Welcome to search and download:\nOptions available: \n1. Search/download file\n2. Test the Average Response Time for a leaf node performing multiple sequential search Requests");
					choice = Integer.parseInt(sc.nextLine());
					switch (choice) {
					case 1:
						// Clear the previous search contents
						neighborPeers.clear();
						threadInstancesList.clear();
						neighborConnThreadList.clear();
						searchResult_Peers.clear();
						// Get Neighbor peers
						getNeighborPeers(neighborPeers, peerid);
						System.out.println("Enter file name to search:");
						searchFileName = sc.nextLine();
						// Generate unique message id
						++searchCounter;
						String msgId = "Peer" + peerid + ".Search" + searchCounter;
						System.out.println("Message id for search: " + msgId);
						// define ttl- time to leave for a query message
						int ttl = 10;
						NeighborConnectionThread connectionThread = new NeighborConnectionThread(
								neighborPeers.get(0).ip, neighborPeers.get(0).portno, searchFileName, msgId, peerid,
								neighborPeers.get(0).peerId, ttl);
						Thread threadInstance = new Thread(connectionThread);
						threadInstance.start();
						// Save connection thread instances
						threadInstancesList.add(threadInstance);
						neighborConnThreadList.add(connectionThread);
						// Wait until child threads finished execution
						((Thread) threadInstancesList.get(0)).join();
						// Get hit query result of all the neighbor peers
						System.out.println("Paths searched are:");
						HitQuery hitQueryResult = (HitQuery) neighborConnThreadList.get(0).getValue();
						if (hitQueryResult.peerFound.size() > 0) {
							// Save the neighbor peer result
							searchResult_Peers.addAll(hitQueryResult.peerFound);
						}
						// Display the paths in which search performed
						for (int count = 0; count < hitQueryResult.pathTraversed.size(); count++) {
							String path = peerid + hitQueryResult.pathTraversed.get(count);
							System.out.println("Search Path: " + path);
						}
						if (searchResult_Peers.size() == 0) {
							System.err.println(searchFileName + " File not found in the network");
						} else {
							// Display the peers list where the searchFilename file
							// existed.
							// call method for download functionality

							System.out.println(searchFileName + " File found in the network at below peers");
							for (int i = 0; i < searchResult_Peers.size(); i++) {
								System.out.println("Found at Peer with peer ID: " + searchResult_Peers.get(i).peerId
										+ " , running on port:" + searchResult_Peers.get(i).port);
							}

						}
						System.out.println("Enter peer ID to download the file from:");
						download_peerid = Integer.parseInt(sc.nextLine());
						if (searchResult_Peers.size() > 0) {
							download(searchResult_Peers, download_peerid, searchFileName, sharedDir);
						}
						break;
					case 2:
						// Option to test Average Response Time
						// Clear the previous search contents
						int count = 0;
						System.out.println("How many iterations??");
						count = Integer.parseInt(sc.nextLine());
						neighborPeers.clear();
						threadInstancesList.clear();
						neighborConnThreadList.clear();
						searchResult_Peers.clear();
						// Get Neighbor peers
						getNeighborPeers(neighborPeers, peerid);
						System.out.println("Enter file name to search:");
						searchFileName = sc.nextLine();
						// Generate unique message id
						++searchCounter;
						String msgId1 = "Peer" + peerid + ".Search" + searchCounter;
						System.out.println("Message id for search: " + msgId1);
						// define ttl- time to leave for a query message
						int ttl1 = 10;
						long starttime = 0;
						long endtime = 0;
						for (int j = 0; j < count; j++) {
							starttime = System.nanoTime();
							NeighborConnectionThread connectionThread1 = new NeighborConnectionThread(
									neighborPeers.get(0).ip, neighborPeers.get(0).portno, searchFileName, msgId1,
									peerid, neighborPeers.get(0).peerId, ttl1);
							Thread threadInstance1 = new Thread(connectionThread1);
							threadInstance1.start();
							// Save connection thread instances
							threadInstancesList.add(threadInstance1);
							neighborConnThreadList.add(connectionThread1);
							// Wait until child threads finished execution
							((Thread) threadInstancesList.get(0)).join();
							// Get hit query result of all the neighbor peers
							HitQuery hitQueryResult1 = (HitQuery) neighborConnThreadList.get(0).getValue();
							endtime = System.nanoTime() - starttime;

						}
						System.out.println("average response time taken in nano seconds:" + endtime / (count));
						break;
					default:
						bExit = true;
					}
					if (bExit) {
						// End of the client session
						System.exit(1);
						break;
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

}
