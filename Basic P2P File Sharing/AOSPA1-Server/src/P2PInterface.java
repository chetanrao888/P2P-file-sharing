/**
 * 
 */


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * @author Rahul Hosmani
 * @author Chetan Rao
 *
 */
public interface P2PInterface extends Remote {
	
	/**
	 *  This method is used to register the peer in the indexing server
	 * @param peerId  Identifier of the peer in the network
	 * @param fileName File name
	 * @param portNo The port number of the peer
	 * @param srcDir  The Shared directory of the peer which is visible to all the peers connected to the network
	 * @throws RemoteException
	 */
	public void registry(String peerId, String fileName,String portNo,String srcDir)throws RemoteException;
	/**
	 *  This method is used to search the file in the indexing server
	 * @param filename The name of the file which is searched in the indexing server
	 * @return  Returns a array list of the matched files in the indexing server 
	 * @throws RemoteException
	 */
	public ArrayList<FileInfo> search(String filename)throws RemoteException; 

}
