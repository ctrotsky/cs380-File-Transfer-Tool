package filetransfer;

import java.io.IOException;

public class Driver {

	public static void main(String[] args) throws IOException, InterruptedException {
		//Leave uncommented on computer that will send file.
		Client sendClient = new Client();
		sendClient.setFilePath("E:/Documents/SocketTesting/FileClient1/SendFile.txt");
		sendClient.setKeyFile("E:/Documents/SocketTesting/FileClient1/keyfile.txt");
		sendClient.setTargetIP("192.168.1.13");
		sendClient.setSocketPort(13267);
		sendClient.setPacketSize(5000);
		sendClient.sendFile();
		
//		//Leave uncommented on computer that will receive file.
//		Client receiveClient = new Client();
//		receiveClient.setFilePath("C:/Users/Colin/Documents/FileTransfer/Client2/ReceiveFile.txt");
//		sendClient.setKeyFile("C:/Users/Colin/Documents/FileTransfer/Client2/keyfile.txt");
//		receiveClient.setTargetIP("192.168.1.83");
//		receiveClient.setSocketPort(13267);
//		receiveClient.setPacketSize(3);
//		receiveClient.receiveFile();
	}
}
