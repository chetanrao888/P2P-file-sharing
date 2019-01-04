import java.io.Serializable;



public class fileInfo_server implements Serializable {
	
	private static final long serialVersionUID = 1L;
	String peerId;
	String fileName;
	String portNumber;
	
	
	/**
	 * 	File Information Constructor
	 * @param peerId Peer id of this client
	 * @param fileName FileName 
	 * @param portNumber Port of the client
	 * @param sourceDirectoryName Shared folder of the client
	 */
	public fileInfo_server(String peerId,String fileName, String portNumber) {
		this.peerId = peerId;
		this.fileName = fileName;
		this.portNumber = portNumber;
		//this.sourceDirectoryName = sourceDirectoryName;
	}
}
