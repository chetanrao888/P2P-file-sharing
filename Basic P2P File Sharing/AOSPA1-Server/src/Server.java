import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 * @author Rahul Hosmani
 * @author Chetan Rao
 *
 */
public class Server implements Runnable {

	@Override
	public void run() {

		try {
			LocateRegistry.createRegistry(1099);
			P2PInterface hello = new IndexingServer();
			Naming.rebind("rmi://" + InetAddress.getLocalHost().getHostAddress() + ":1099/Hello", hello);
			System.out.println("Indexing Server is Ready");
			System.out.println("IP Address of Server: " + InetAddress.getLocalHost().getHostAddress());

		} catch (Exception e) {

			System.err.println("Server Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Server().run();
	}

}
