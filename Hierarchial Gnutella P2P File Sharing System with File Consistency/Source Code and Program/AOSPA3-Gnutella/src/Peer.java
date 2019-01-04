
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

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
				hello.registry(peerId, filename, portNo, portNo);
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
	public static void runPeerServer(int peerId, int port, String sharedDir, String dubDir,
			ArrayList<String> localFiles) {
		try {
			LocateRegistry.createRegistry(port);
			PeerInterface stub = new PeerInterfaceRemote(sharedDir, dubDir, peerId, port, localFiles);
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

	/*--------------------------------start change---------------------------*/
	public static void download(ArrayList<PeerDetails> searchResult_Peers, int peerId, String fileName, String Path,
			String server_port, ArrayList<fileInfo_server> downloaded_files, int self_peerid, int ttr)
			throws IOException {
		// Download functionality
		int peer_count = 0;
		int port = 0;
		String Host = null;
		OutputStream os = null;
		String orig_port = null;
		fileInfo_server info = new fileInfo_server();
		while (peer_count < searchResult_Peers.size()) {
			if (peerId == searchResult_Peers.get(peer_count).peerId) {
				port = searchResult_Peers.get(peer_count).port;
				Host = searchResult_Peers.get(peer_count).hostIp;
				orig_port = searchResult_Peers.get(peer_count).orig;
				info.fileName = fileName;
				info.orig_port = orig_port;
				info.peerId = Integer.toString(peerId);
				info.portNumber = Integer.toString(port);
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
			String fullFileName = Path + "\\" + fileName;
			os = new FileOutputStream(Path + "\\" + fileName);
			byte[] buffer = PeerServer.obtain(fileName);
			os.write(buffer, 0, buffer.length);
			long version_number = System.currentTimeMillis();
			info.fullFileName = fullFileName;
			info.version_number = version_number;
			downloaded_files.add(info);
			os.close();
			PeerInterface hello = (PeerInterface) Naming
					.lookup("rmi://localhost:" + Integer.parseInt(server_port) + "/peerServer");
			hello.registry(Integer.toString(self_peerid), fileName, Integer.toString(port), orig_port);
			String property;
			String approach;
			Properties prop = new Properties();
			FileInputStream input = new FileInputStream("./src/config.properties");
			// load a properties file
			prop.load(input);
			property = "approach";
			approach = prop.getProperty(property);
			// polls server files- if pull based approach is configured
			if (approach.equals("pull")) {
				pull_update pull_thread = new pull_update(info.orig_port, fileName, version_number, fullFileName, "No",
						ttr);
				Thread threadInstance = new Thread(pull_thread);
				threadInstance.start();

			}
			System.err.println("File copied");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*--------------------------------end change---------------------------*/
	public static void main(String args[]) {

		/*--------- start change ----------*/
		String masterDir;
		String dubDir;
		String approach;
		ArrayList<fileInfo_server> downloaded_files = new ArrayList<fileInfo_server>();
		/*--------- end change ----------*/
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
		ArrayList<PeerDetails> searchResult_Peers1 = new ArrayList<PeerDetails>();
		ArrayList<NeighborConnectionThread> neighborConnThreadList = new ArrayList<NeighborConnectionThread>();
		int download_peerid;
		// time to refresh
		String ttr = null;

		try {
			Scanner sc = new Scanner(System.in);

			// Collect peerid and port from the user.
			System.out.println("Please enter the Peer ID as configured in the Configuration Properties File");
			peerid = Integer.parseInt(sc.nextLine());

			String property = null;
			Properties prop = new Properties();
			InputStream input = null;
			int neighborport;
			input = new FileInputStream("./src/config.properties");
			// load a properties file
			prop.load(input);
			property = "peerid." + peerid + ".port";
			port = Integer.parseInt(prop.getProperty(property));

			System.out.println("Session for peer id: " + peerid + " started...");
			// Get the directory to share with other peers.
			// master directory
			property = "peerid." + peerid + ".masterDir";
			masterDir = System.getProperty("user.dir") + prop.getProperty(property);
			/*--------- start change ----------*/
			// directory downloaded files
			property = "peerid." + peerid + ".dubDir";
			dubDir = System.getProperty("user.dir") + prop.getProperty(property);
			ttr = prop.getProperty("ttr");
			/*--------- end change ----------*/

			// added
			property = "peerid." + peerid + ".neighbors";

			approach = prop.getProperty("approach");

			String[] strNeighbors = prop.getProperty(property).split(",");
			neighborport = Integer.parseInt(prop.getProperty(strNeighbors[0] + ".port"));

			runPeerServer(peerid, port, masterDir, dubDir, localFiles);
			if (strNeighbors.length > 1) {
				System.out.println("Acting as super peer");
			} else {
				new WatchThread(masterDir, Integer.toString(neighborport), Integer.toString(peerid)).start();
				/*----start change----*/
				new WatchThread(dubDir, Integer.toString(neighborport), Integer.toString(peerid)).start();
				/*----end change----*/
				// ended
				// Get the filenames that existed locally
				register_file(masterDir, localFiles, neighborport, port, peerid);
				// Run peer as a server on a specific port -- may be only for super peer

				// User Menu -- if else --- non super peers have access to the below code
				while (true) {
					if (approach.equals("pull")) {
						System.out.println(
								"Welcome to search and download:\nOptions available: \n1. Search/download file\n2. Test the Average Response Time for a leaf node performing multiple sequential search Requests\n3. Refresh downloaded files");

					} else {
						System.out.println(
								"Welcome to search and download:\nOptions available: \n1. Search/download file\n2. Test the Average Response Time for a leaf node performing multiple sequential search Requests\n");

					}
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
								neighborPeers.get(0).peerId, ttl, "query", 0);
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
							download(searchResult_Peers, download_peerid, searchFileName, dubDir,
									Integer.toString(neighborport), downloaded_files, peerid, Integer.parseInt(ttr));
						}
						break;
					case 2:
						// Option to test Average Response Time
						// Clear the previous search contents
						String modify = null;
						int count = 0;
						System.out.println("How many iterations??");
						count = Integer.parseInt(sc.nextLine());
						System.out.println("Should the peer simulate file modification or query-download??");
						modify = sc.nextLine();
						neighborPeers.clear();
						threadInstancesList.clear();
						neighborConnThreadList.clear();
						searchResult_Peers1.clear();
						// Get Neighbor peers
						getNeighborPeers(neighborPeers, peerid);						
						// Generate unique message id
						++searchCounter;
						String msgId1 = "Peer" + peerid + ".Search" + searchCounter;
						System.out.println("Message id for search: " + msgId1);
						// define ttl- time to leave for a query message
						int ttl1 = 100;
						long starttime = 0;
						long endtime = 0;
						Thread.sleep(15000);
						for (int j = 0; j < count; j++) {							
							starttime = System.nanoTime();
							if (modify.equals("No")) {
								int index = ThreadLocalRandom.current().nextInt(0, 10);
								searchFileName = "file" + index + ".txt";
								NeighborConnectionThread connectionThread1 = new NeighborConnectionThread(
										neighborPeers.get(0).ip, neighborPeers.get(0).portno, searchFileName, msgId1,
										peerid, neighborPeers.get(0).peerId, ttl1, "query", 0);
								Thread threadInstance1 = new Thread(connectionThread1);
								threadInstance1.start();
								// Save connection thread instances
								threadInstancesList.add(threadInstance1);
								neighborConnThreadList.add(connectionThread1);
								// Wait until child threads finished execution
								((Thread) threadInstancesList.get(0)).join();
//								 Get hit query result of all the neighbor peers
								HitQuery hitQueryResult1 = (HitQuery) neighborConnThreadList.get(0).getValue();
								if (hitQueryResult1.peerFound.size() > 0) {
									// Save the neighbor peer result
									searchResult_Peers1.addAll(hitQueryResult1.peerFound);
								}
								download_peerid = ThreadLocalRandom.current().nextInt(1, searchResult_Peers1.size());
								if (searchResult_Peers1.size() > 0) {
									download(searchResult_Peers1, download_peerid, searchFileName, dubDir,
											Integer.toString(neighborport), downloaded_files, peerid,
											Integer.parseInt(ttr));
								}
								endtime = System.nanoTime() - starttime;
							} else {								
								try {
									int generate = ThreadLocalRandom.current().nextInt(0, 10);
									Files.write(Paths.get(masterDir + "\\file" + generate + ".txt"),
											"append text".getBytes(), StandardOpenOption.APPEND);
								} catch (IOException e) {
									System.err.println("Evaluation Measure" + e);

								}
							}

						}
						try {
							String print = Integer.toString((int) (endtime / (count)));							
							Files.write(Paths.get("C:\\Users\\Chetan\\Google Drive\\My_PC\\OS\\HW\\HW3\\test\\AOSPA3-Gnutella\\src\\result.txt"),
									print.getBytes(), StandardOpenOption.APPEND);
						} catch (IOException e) {
							System.err.println("Evaluation Measure" + e);

						}
						System.out.println("average response time taken in nano seconds:" + endtime / (count));						
						break;
					/*--------------------------------start change---------------------------*/
					// this case discards all stale files belonging to a peer and downloads modified
					// files
					case 3:
						List<Thread> threads = new ArrayList<Thread>();
						ArrayList<pull_update> peerThreadsList = new ArrayList<pull_update>();
						for (int j = 0; j < downloaded_files.size(); j++) {
							pull_update t = new pull_update(downloaded_files.get(j).portNumber,
									downloaded_files.get(j).fileName, downloaded_files.get(j).version_number,
									downloaded_files.get(j).fullFileName, "Yes", Integer.parseInt(ttr));
							Thread threadInstance1 = new Thread(t);
							threadInstance1.start();
							threads.add(threadInstance1);
							peerThreadsList.add(t);
						}
						// (info.orig_port, fileName, version_number,
						// fullFileName, "No");
						for (int i = 0; i < threads.size(); i++)
							try {
								// wait for all the request threads finish the search
								((Thread) threads.get(i)).join();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						break;
					/*--------------------------------end change---------------------------*/
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
			sc.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

}
