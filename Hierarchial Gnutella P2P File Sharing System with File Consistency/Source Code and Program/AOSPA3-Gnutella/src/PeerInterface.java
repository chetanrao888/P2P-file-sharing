import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 
 * @author Rahul Hosmani
 * @author Chetan Rao
 * 
 */
public interface PeerInterface extends Remote {

	public byte[] obtain(String filename) throws RemoteException;

	public HitQuery query(int fromPeerId, String msgId, String fileName, int ttl) throws RemoteException;

	public void registry(String peerId, String fileName, String portNo, String orig) throws RemoteException;

	public void invalidation(int fromPeerId, String msgId, String filename, int ttl, long version_number)
			throws RemoteException;

	public long pollServer(String fileName) throws RemoteException;
}
