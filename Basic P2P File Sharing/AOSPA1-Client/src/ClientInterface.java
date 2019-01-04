
/**
 * 
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Rahul Hosmani
 * @author Chetan Rao
 *
 */


public interface ClientInterface extends Remote {

	//Method invoked by remote object to download file
	public byte[] retrieve(String fileName) throws RemoteException;        

}
