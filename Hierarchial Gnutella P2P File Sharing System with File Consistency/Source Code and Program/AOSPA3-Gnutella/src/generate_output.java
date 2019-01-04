import java.io.File;

public class generate_output {
	public static void main(String args[]) {
		int i;
//		File df = new File("C:\\Users\\Chetan\\Google Drive\\My_PC\\OS\\HW\\HW3\\test\\01_rao_chetan_pa2\\Source Code and Program\\AOSPA2-Gnutella\\src\\downpeer2\\b.txt");
//		if(df.exists())
//		{
//			df.delete();
//		}
		
//		for(i=1;i<41;i++)
//		{
//			System.out.println("peerid."+ i +".ip=127.0.0.1");
//			System.out.println("peerid."+i+".port="+i+"000");
//			System.out.println("peerid."+ i +".masterDir=\\\\src\\\\peer"+ i);
//			System.out.println("peerid."+ i +".dubDir=\\\\src\\\\downpeer"+ i);
//		}
		for(i=1;i<41;i++)
		{
			if(i%4 == 1)
			{
				System.out.print("| dubDir.contains(\"downpeer"+i+"\")");
			}
			//new File(System.getProperty("user.dir") + "\\src\\downpeer"+i).mkdirs();
			
		}
//		long version_number = System.currentTimeMillis();
//		System.out.println(version_number);
//		for(i=5;i<31;i++)
//		{
//			System.out.println("peerid."+i+".neighbors=");
//		}
//		for(i=1;i<40;i++)
//		{
//			if(i==37)
//			{
//				i=i+3;
//				continue;
//			}
//			System.out.print("peerid."+i+",");
//			i=i+3;
//		}
//		System.out.println("Working Directory = " +
//	              System.getProperty("user.dir"));
//		autoupdate();
		
	}
	
//	private static void autoupdate() {
//		new java.util.Timer().schedule(new java.util.TimerTask() {
//
//			public void run() {
//				System.out.println("Run every 10 second");
//			}
//		}, 0, 10000);
//	}

}
