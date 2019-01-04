
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
	private ArrayList<FileInfo> files;

	protected IndexingServer() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
		files = new ArrayList<FileInfo>();
	}

	@Override
	public synchronized void registry(String peerId, String fileName, String portNo, String srcDir)
			throws RemoteException {
		// TODO Auto-generated method stub
		FileInfo fi = new FileInfo(peerId, fileName, portNo, srcDir);

		if (portNo == null) {
			for(int i=0; i<files.size(); i++)
			{
				if(peerId == fi.peerId & fileName == fi.fileName)
				{					
					System.out.println("\nFile: " + fi.fileName + " from Source Directory: " + fi.sourceDirectoryName + " Deleted. Belonging to" +fi.peerId);
					files.remove(i);
				}					
			}			
		} else {
			this.files.add(fi);
			System.out.println("\nFile Added: " + fi.fileName + " | Peer ID: " + fi.peerId + " | IP:Port Number: " + portNo
					+ " | Source Directory: " + fi.sourceDirectoryName);
		}
	}

	@Override
	public ArrayList<FileInfo> search(String filename) throws RemoteException {
		// TODO Auto-generated method stub
		ArrayList<FileInfo> matchedFiles = new ArrayList<FileInfo>();

		for (int i = 0; i < this.files.size(); i++) {
			if (filename.equalsIgnoreCase(files.get(i).fileName))
				matchedFiles.add(files.get(i));
		}
		return (matchedFiles);
	}

}
