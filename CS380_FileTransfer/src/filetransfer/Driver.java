package filetransfer;

import java.io.IOException;
import java.util.Scanner;


public class Driver {

	public static void main(String[] args) throws IOException, InterruptedException {
//		
		Client c = new Client();
		String filePath;
		String keyPath;
		String username;
		String password;
		String ip;
		int port=22;
		int as=0;
		boolean ascii;
		int sr=0;
		
		Scanner scan=new Scanner(System.in);
		
		System.out.print("enter 1 for recieve client, 2 for send: ");
		sr= scan.nextInt();
		scan.nextLine();
		System.out.print("Enter Target File Path for file: ");
		filePath= scan.nextLine();
		System.out.print("Enter file path for key: ");
		keyPath=scan.nextLine();
		System.out.print("Enter IP of other client: ");
		ip=scan.nextLine();
		System.out.print("enter 1 for Ascii Encode, any # for no: ");
		as=scan.nextInt();
		if(as==1)
			ascii=true;
		else 
			ascii=false;
		if (sr==1)
		{
		
			c.setFilePath(filePath);
			c.setKeyFile(keyPath);
			c.setTargetIP(ip);
			c.setSocketPort(port);
			c.setAsciiArmored(ascii);
			c.receiveFile();
			
			
		}
		
		else if(sr==2)
		{
			scan.nextLine();
			System.out.print("Enter Username: ");
			username=scan.nextLine();
			System.out.print("Enter Password: ");
			password=scan.nextLine();
			c.setFilePath(filePath);
			c.setKeyFile(keyPath);
			c.setTargetIP(ip);
			c.setSocketPort(port);
			c.setAsciiArmored(ascii);
			c.sendFile();
			
		}
		
		else 
		{
			System.out.println("not a valid input");
		}
		
		
		
		
		
		
		
		//Leave uncommented on computer that will send file.
		/*Client sendClient = new Client();
		sendClient.setFilePath("C:/cs380/sample.avi");
		sendClient.setKeyFile("2");
		sendClient.setTargetIP("192.168.45.128");
		sendClient.setSocketPort(22);
		sendClient.setPacketSize(50000);
		sendClient.setAsciiArmored(false);
		sendClient.sendFile();
		*/
		//Leave uncommented on computer that will receive file.
		//Client receiveClient = new Client();
		//receiveClient.setFilePath("C:/cs380/sample.avi");
		//receiveClient.setKeyFile("C:/cs380/C00130.pdf");
		//receiveClient.setTargetIP("192.168.1.6");
		//receiveClient.setSocketPort(13267);
		//receiveClient.setPacketSize(55000);
		//receiveClient.receiveFile();
	}
}
