import java.io.Serializable;

public class fileInfo_server implements Serializable {

	private static final long serialVersionUID = 1L;
	String peerId;
	String fileName;
	String portNumber;
	String orig_port;
	long version_number;
	String fullFileName;

	/**
	 * File Information Constructor
	 * 
	 * @param peerId              Peer id of this client
	 * @param fileName            FileName
	 * @param portNumber          Port of the client
	 * @param sourceDirectoryName Shared folder of the client
	 */
	public fileInfo_server(String peerId, String fileName, String portNumber, String orig, long version_number,
			String fullFileName) {
		this.peerId = peerId;
		this.fileName = fileName;
		this.portNumber = portNumber;
		this.orig_port = orig;
		this.version_number = version_number;
		this.fullFileName = fullFileName;
		// this.sourceDirectoryName = sourceDirectoryName;
	}

	public fileInfo_server(String peerId, String fileName, String portNumber, String orig) {
		this.peerId = peerId;
		this.fileName = fileName;
		this.portNumber = portNumber;
		this.orig_port = orig;
		// this.sourceDirectoryName = sourceDirectoryName;
	}

	public fileInfo_server() {

	}
}
