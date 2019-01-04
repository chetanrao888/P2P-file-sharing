
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

/**
 * 
 * @author Rahul Hosmani
 * @author Chetan Rao
 *
 */
public class IndexingServer extends UnicastRemoteObject implements P2PInterface {

	private static final long serialVersionUID = 1L;
	private ArrayList<FileInfo_initial> files;

	protected IndexingServer() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
		files = new ArrayList<FileInfo_initial>();
	}

	@Override
	public synchronized void registry(String peerId, String fileName, String portNo, String srcDir, ArrayList<String> neigh)
			throws RemoteException {
		// TODO Auto-generated method stub
		FileInfo_initial fi = new FileInfo_initial(peerId, fileName, portNo, srcDir, neigh);

		if (portNo == null) {
			for(int i=0; i<files.size(); i++)
			{
				if(peerId == fi.speerId & fileName == fi.sfileName)
				{					
					System.out.println("\nFile: " + fi.sfileName + " from Source Directory: " + fi.ssourceDirectoryName + " Deleted. Belonging to" +fi.speerId);
					files.remove(i);
				}					
			}			
		} else {
			this.files.add(fi);
			System.out.println("\nFile Added: " + fi.sfileName + " | Peer ID: " + fi.speerId + " | IP:Port Number: " + portNo
					+ " | Source Directory: " + fi.ssourceDirectoryName);
		}
	}

	@Override
	public ArrayList<FileInfo_initial> search(String filename) throws RemoteException {
		// TODO Auto-generated method stub
		ArrayList<FileInfo_initial> matchedFiles = new ArrayList<FileInfo_initial>();

		for (int i = 0; i < this.files.size(); i++) {
			if (filename.equalsIgnoreCase(files.get(i).sfileName))
				matchedFiles.add(files.get(i));
		}
		return (matchedFiles);
	}

}
