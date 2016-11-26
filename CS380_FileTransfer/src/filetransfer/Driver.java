package filetransfer;

import java.io.IOException;

public class Driver {

	public static void main(String[] args) throws IOException {
		Client client1 = new Client();
		client1.setSendFile("E:/Documents/SocketTesting/FileClient1/SendFile.txt");
		
		Client client2 = new Client();
		client2.setReceiveFile("E:/Documents/SocketTesting/FileClient2/ReceiveFile.txt");
		
		client1.sendFile();
		client2.receiveFile();
	}
}
