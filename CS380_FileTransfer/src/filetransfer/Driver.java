package filetransfer;

import java.io.IOException;

public class Driver {

	public static void main(String[] args) {
		//Leave uncommented on computer that will send file.
		Client sendClient = new Client();
		sendClient.setFilePath("E:/Documents/SocketTesting/FileClient1/SendFile.txt");
		sendClient.setTargetIP("192.168.1.13");
		sendClient.setSocketPort(13267);
		sendClient.setPacketSize(3);
		sendClient.sendFile();
		
//		//Leave uncommented on computer that will receive file.
//		Client receiveClient = new Client();
//		receiveClient.setFilePath("C:/Users/Colin/Documents/FileTransfer/Client2/ReceiveFile.txt");
//		receiveClient.setTargetIP("192.168.1.83");
//		receiveClient.setSocketPort(13267);
//		receiveClient.setPacketSize(5000);
//		receiveClient.receiveFile();
	}
}
