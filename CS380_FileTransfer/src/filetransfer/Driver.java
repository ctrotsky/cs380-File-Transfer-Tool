package filetransfer;

import java.util.Scanner;

public class Driver {
	
	//Must be simple text file with usernames and passwords seperated by comma
	private final String authenticationFilePath = "C:/Users/Colin/Documents/FileTransfer/Client2/Authentication.txt";
	

	public static void main(String[] args){
		
//		ThreadRunner sendClient = new ThreadRunner();
//		sendClient.setFilePath("C:/Users/Colin/Documents/FileTransfer/Client1/Hangman.zip");
//		sendClient.setKeyFile("C:/Users/Colin/Documents/FileTransfer/Client1/keyfile.txt");
//		sendClient.setTargetIP("10.110.123.221");
//		sendClient.setSocketPort(22);
//		sendClient.setPacketSize(8000);
//		sendClient.setToSend();
//		sendClient.setUsername("catrotter");
//		sendClient.setPassword("pass1");
//		sendClient.setAsciiArmored(false);
//		
//		ThreadRunner receiveClient = new ThreadRunner();
//		receiveClient.setFilePath("C:/Users/Colin/Documents/FileTransfer/Client2/Hangman.zip");
//		receiveClient.setKeyFile("C:/Users/Colin/Documents/FileTransfer/Client2/keyfile.txt");
//		receiveClient.setTargetIP("10.110.123.221");
//		receiveClient.setSocketPort(22);
//		receiveClient.setPacketSize(8000);
//		receiveClient.setToReceive();
//		receiveClient.setAsciiArmored(false);
//		receiveClient.setAuthenticationFilePath("C:/Users/Colin/Documents/FileTransfer/Client2/Authentication.txt");
//		
//		Thread sendThread = new Thread(sendClient);
//		Thread receiveThread = new Thread(receiveClient);
//		sendThread.start();
//		receiveThread.start();
		
		
		
		createClient();
	}
	
	public static void createClient(){
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
		
		System.out.println("Select mode: ");
		System.out.println("[1] Receive: ");
		System.out.println("[2] Send: ");
		sr= scan.nextInt();
		scan.nextLine();
		System.out.print("Enter Target File Path for file: ");
		filePath= scan.nextLine();
		System.out.print("Enter file path for key: ");
		keyPath=scan.nextLine();
		System.out.print("Enter IP of other client: ");
		ip=scan.nextLine();
		System.out.print("Enter 1 for Ascii Encode, any # for no: ");
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
			c.setAuthenticationFilePath("C:/Users/Colin/Documents/FileTransfer/Client2/Authentication.txt");
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
			
			
			System.out.println("Send with error?");
			System.out.println("[1] Yes");
			System.out.println("[2] No");
			as=scan.nextInt();
			if (as == 1){
				c.sendFileWithError(username, password);
			}
			else {
				c.sendFile(username, password);
			}
		}
		
		else 
		{
			System.out.println("not a valid input");
		}
	}
}
