
import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 * @author Rahul Hosmani
 * @author Chetan Rao
 *
 */
public class FileInfo_initial implements Serializable {

	String speerId;
	String sfileName;
	String sportNumber;
	String ssourceDirectoryName;
	ArrayList<String> neigh = new ArrayList<String>();
	/**
	 * 	File Information Constructor
	 * @param peerId Peer id of this client
	 * @param fileName FileName 
	 * @param portNumber Port of the client
	 * @param sourceDirectoryName Shared folder of the client
	 */
	public FileInfo_initial(String peerId,String fileName, String portNumber, String sourceDirectoryName, ArrayList neigh) {
		this.speerId = peerId;
		this.sfileName = fileName;
		this.sportNumber = portNumber;
		this.ssourceDirectoryName = sourceDirectoryName;
		this.neigh = neigh;
	}
	
	public FileInfo_initial(String sourceDirectoryName) {
		this.ssourceDirectoryName = sourceDirectoryName;
	}

}
