import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * 
 * @author Rahul Hosmani
 * @author Chetan Rao
 * 
 */

public class NeighborConnectionThread extends Thread {
	// Handle's connection with neighbor peers

	int port;
	String ip;
	String fileName;
	String msgId;
	int fromPeerId;
	int fromPeerPort;
	ArrayList<PeerDetails> filesFoundat = new ArrayList<PeerDetails>();
	HitQuery hitQueryResult = new HitQuery();
	String toPeerId;
	int ttl;

	NeighborConnectionThread(String ip, int port, String fileName, String msgId, int fromPeerId, String toPeerId, int ttl) {
		this.ip = ip;
		this.port = port;
		this.fromPeerId = fromPeerId;
		this.fileName = fileName;
		this.msgId = msgId;
		this.toPeerId = toPeerId;
		this.ttl = ttl;
	}

	@Override
	public void run() {
		PeerInterface peer = null;
		try {
			// Establish connection
			peer = (PeerInterface) Naming.lookup("rmi://" + ip + ":" + port + "/peerServer");
			// Call remote method query
			hitQueryResult = peer.query(fromPeerId, msgId, fileName, ttl);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Unable to connect to " + toPeerId + " : " + e.getMessage());
		}

	}

	public HitQuery getValue() {
		// Return hit query result
		return hitQueryResult;
	}

}
