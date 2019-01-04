
import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 * @author Rahul Hosmani
 * @author Chetan Rao
 *
 */
public class FileInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	String peerId;
	String fileName;
	String portNumber;
	String sourceDirectoryName;
	Integer message_id;
	Integer ttl;
	ArrayList<String> neigh = new ArrayList<String>();
	
	/**
	 * 	File Information Constructor
	 * @param peerId Peer id of this client
	 * @param fileName FileName 
	 * @param portNumber Port of the client
	 * @param sourceDirectoryName Shared folder of the client
	 */
	public FileInfo(String peerId,String fileName, String portNumber, String sourceDirectoryName, Integer message_id) {
		this.peerId = peerId;
		this.fileName = fileName;
		this.portNumber = portNumber;
		this.sourceDirectoryName = sourceDirectoryName;
		this.message_id = message_id;
	}
	
	public FileInfo(String sourceDirectoryName) {
		this.sourceDirectoryName = sourceDirectoryName;
	}

}
