import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 * @author Rahul Hosmani
 * @author Chetan Rao
 * 
 */
public class HitQuery implements Serializable {

	private static final long serialVersionUID = 1L;
	public ArrayList<PeerDetails> peerFound = new ArrayList<PeerDetails>();
	public ArrayList<String> pathTraversed = new ArrayList<String>();
}
