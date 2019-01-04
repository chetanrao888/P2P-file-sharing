
/**
 * 
 */

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author Rahul Hosmani
 * @author Chetan Rao
 *
 */
public class P2PDownload extends UnicastRemoteObject implements ClientInterface {
	
	private static final long serialVersionUID = 1L;
	private String directoryName;

	protected P2PDownload(String dirName) throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
		directoryName = dirName;
	}

	@Override
	public byte[] retrieve(String fileName) throws RemoteException {
		// TODO Auto-generated method stub
		try {
			File file = new File(directoryName + "/" + fileName);
			int size = (int) file.length();
			//create buffer with size equal to file length(size)
			byte buffer[] = new byte[size];								
			 //create buffered input stream to read bytes of data
			BufferedInputStream input = new BufferedInputStream(new FileInputStream(directoryName + "//" + fileName));  
			//read file contents into the buffer
			input.read(buffer, 0, buffer.length);					
			input.close();
			//returns the buffer(file) to the client -- (upload action)
			return (buffer);											
		} catch (Exception e) {
			System.err.println("File Retrival Exception: " + e.getMessage());
			e.printStackTrace();
			return null;
		}

	}

}
