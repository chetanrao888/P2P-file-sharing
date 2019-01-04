import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

/**
 * 
 * @author Rahul Hosmani
 * @author Chetan Rao
 *
 */
public class Client implements Runnable {

	String clientPortNo;
	String dirName;
	String peerId;
	String tag;
	// public static String serverIp;
	double starttime;
	double endtime;
	double responsetime = 0;
	int choice;
	ArrayList<String> neigh = new ArrayList<String>();
	Scanner sc = new Scanner(System.in);
	private Scanner sc3;

	Client() {
	}

	Client(String pno, String dName, String peerId, ArrayList<String> neigh, String tag) {
		this.clientPortNo = pno;
		this.dirName = dName;
		this.peerId = peerId;
		this.neigh = neigh;
		this.tag = tag;
	}

	@Override
	public void run() {

		// Watch clients directory for changes (Create/Delete file)
		// new WatchThread(dirName, clientPortNo, peerId).start();

		int flag = 0, number = 0;
		try {
			if (tag.equals("super")) {
				System.out.println("Super peer" + peerId);

			} else {
				// Lookup for server object on RMI and obtain reference
				P2PInterface hello = (P2PInterface) Naming.lookup("rmi://127.0.0.1" + tag + "/Fileserver");

				// Register all files of the client with indexing server present in the shared
				// directory
				registerFile(dirName, peerId, hello);
				while (true) {
					System.out.println(
							"Welcome to search and download:\nOptions available: \n1. Search/download file\n2. Test the Average Response Time for a single client performing multiple sequential search Requests");
					choice = sc.nextInt();
					switch (choice) {
					case 1:
						// Option to Search and Download the file
						flag = 0;
						lookUp(hello, flag, number);
						break;
					case 2:
						// Option to test Average Response Time
						System.out.println("Number of Sequential requests: ");
						number = sc.nextInt();
						flag = 1;
						lookUp(hello, flag, number);
						System.out.println("Average Response time for " + number + " sequential search requests: "
								+ responsetime / number + " nanoseconds.");
					default:
						break;
					}
				}
			}

		} catch (Exception e) {
			System.err.println("Client Exception" + e);
		}

	}

	/**
	 * This method registers all the files present in the directory.
	 * 
	 * @param dirName Directory Name
	 * @param peerId  Client ID
	 * @param hello   Object Reference of the server
	 * @throws RemoteException
	 */
	public void registerFile(String dirName, String peerId, P2PInterface hello) throws RemoteException {
		File dirList = new File(dirName);
		String[] dirArray = dirList.list();
		int counter = 0;
		while (counter < dirArray.length) {
			File currFile = new File(dirArray[counter]);
			try {
				// register files with the client
				hello.registry(peerId, currFile.getName(), clientPortNo, dirName, neigh);
			} catch (RemoteException ex) {
				System.err.println("Error while registering file:" + ex);
			}
			counter++;
		}
	}

	/**
	 * This method is used to search the file in the indexing server
	 * 
	 * @param hello  Object reference of the server
	 * @param flag   Keeps track of users choice (Search and Download OR Evaluate
	 *               Average Response of multiple search requests)
	 * @param number Number of Sequential Requests
	 * @throws MalformedURLException
	 * @throws NotBoundException
	 * @throws IOException
	 */
	public void lookUp(P2PInterface hello, int flag, int number)
			throws MalformedURLException, NotBoundException, IOException {
		String fname;
		String pdest = null;
		sc3 = new Scanner(System.in);
		ArrayList<FileInfo_initial> arr = new ArrayList<FileInfo_initial>();
		System.out.println("Enter the file name to be searched");
		fname = sc3.nextLine();

		if (flag == 0) {
			while (fname != null) {
				// Search the given file in the indexing server
				arr = hello.search(fname);
				if (arr.size() == 0) {
					System.out.println("File not Found");
					return;

				} else {
					for (int i = 0; i < arr.size(); i++) {
						System.out.println("Peer ID's having the given file are: " + arr.get(i).speerId);
					}
					System.out.println("From which client do you wish to download the content:");
					peerId = sc.next();
					for (int i = 0; i < arr.size(); i++) {
						if (peerId.equals(arr.get(i).speerId)) {
							pdest = arr.get(i).sportNumber;
						}
					}
					// Download the File
					downloadFile(pdest, fname);
					break;
				}
			}

		} else {
			// Calculating the response time of the search request
			for (int i = 0; i < number; i++) {
				starttime = System.nanoTime();
				arr = hello.search(fname);
				endtime = System.nanoTime() - starttime;
				responsetime = +endtime;
			}

		}
	}

	/**
	 * This method is used to download the file from the peer.
	 * 
	 * @param pdest IP address and port number of the client which has the file
	 *              ready for download
	 * @param fname File name of downloading file
	 * @throws NotBoundException
	 * @throws RemoteException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public void downloadFile(String pdest, String fname)
			throws NotBoundException, RemoteException, MalformedURLException, IOException {
		String target = dirName;
		OutputStream os = null;

		try {
			// To used to obtain a reference of the Peer which has the file to be downloaded
			ClientInterface peerServer = (ClientInterface) Naming.lookup("rmi://" + pdest + "/FileServer");

			File destFile = new File(target);
			System.out.println("file " + destFile);
			if (!destFile.exists()) {
				destFile.createNewFile();
			}

			os = new FileOutputStream(target + "\\" + fname);
			byte[] buffer = peerServer.retrieve(fname);
			os.write(buffer, 0, buffer.length);
			System.out.println("File copied");
		} catch (Exception e) {
			System.err.println("Failed to download:");
		} finally {
			os.close();
		}

	}

	public static void main(String[] args) {
		String portno = null;
		String peerId = null;
		String tag = null;
		String directoryName = null;
		ArrayList<String> neighbourlist = new ArrayList<String>();
		Properties prop = new Properties();
		InputStream input = null;
		String read = null;
		String neighbours = null;
		Scanner sc1 = new Scanner(System.in);
		try {
			input = new FileInputStream("C:\\Users\\cheta\\Desktop\\aos\\final\\AOSPA1-Client\\src\\config.properties");
			prop.load(input);
			System.out.println("Enter peer ID:");
			read = sc1.nextLine();
			peerId = prop.getProperty("peer." + read + ".id");
			portno = prop.getProperty("peer." + read + ".portno");
			directoryName = prop.getProperty("peer." + read + ".directory");
			neighbours = prop.getProperty("peer." + read + ".neighbours");
			tag = prop.getProperty("peer." + read + ".super");
			String[] arrOfStr = neighbours.split(",");

			for (int i = 0; i < arrOfStr.length; i++)
				neighbourlist.add(arrOfStr[i]);

			System.out.println(neighbourlist);
			// Creating a registry instance on the local host
			LocateRegistry.createRegistry(Integer.parseInt(portno));
			if (tag.equals("super")) {
				P2PInterface hello = new IndexingServer();
				// bind name of the object with the RMI registry
				Naming.rebind("rmi://127.0.0.1:" + portno + "/FileServer", hello);
			}

		} catch (java.rmi.ConnectException er) {
			System.err.println("Server offline / Wrong ip address!!");
		} catch (Exception e) {
			System.err.println("FileServer exception: " + e.getMessage());
			e.printStackTrace();
		}
		try {
			new Client(InetAddress.getLocalHost().getHostAddress() + ":" + portno, directoryName, peerId, neighbourlist,
					tag).run();
		} catch (UnknownHostException e) {
			e.toString();
		}
		sc1.close();
		System.out.println("System exited\nPlease start client again");

	}
}
