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
	String who;
	long version_control;

	NeighborConnectionThread(String ip, int port, String fileName, String msgId, int fromPeerId, String toPeerId,
			int ttl, String who, long version_control) {
		this.ip = ip;
		this.port = port;
		this.fromPeerId = fromPeerId;
		this.fileName = fileName;
		this.msgId = msgId;
		this.toPeerId = toPeerId;
		this.ttl = ttl;
		this.who = who;
		this.version_control = version_control;
	}

	@Override
	public void run() {
		PeerInterface peer = null;
		try {
			// Establish connection
			peer = (PeerInterface) Naming.lookup("rmi://" + ip + ":" + port + "/peerServer");
			// Call remote method query
			if (who.equals("query")) {
				hitQueryResult = peer.query(fromPeerId, msgId, fileName, ttl);
			} else {
				System.out.println("from peerid:" + fromPeerId);
				System.out.println("msgid:" + msgId);
				System.out.println("filename:" + fileName);
				System.out.println("ttl:" + ttl);
				peer.invalidation(fromPeerId, msgId, fileName, ttl, version_control);
			}
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
