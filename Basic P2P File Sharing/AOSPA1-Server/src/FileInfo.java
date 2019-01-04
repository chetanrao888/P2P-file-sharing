

import java.io.Serializable;

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
	
	/**
	 * 	File Information Constructor
	 * @param peerId Peer id of this client
	 * @param fileName FileName 
	 * @param portNumber Port of the client
	 * @param sourceDirectoryName Shared folder of the client
	 */
	public FileInfo(String peerId,String fileName, String portNumber, String sourceDirectoryName) {
		this.peerId = peerId;
		this.fileName = fileName;
		this.portNumber = portNumber;
		this.sourceDirectoryName = sourceDirectoryName;
	}

}
