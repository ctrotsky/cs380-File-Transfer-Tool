package filetransfer;

import java.io.IOException;

public class Driver {

	public static void main(String[] args) throws IOException, InterruptedException {
//		//Leave uncommented on computer that will send file.
//		Client sendClient = new Client();
//		sendClient.setFilePath("E:/Documents/SocketTesting/FileClient1/SendFile.txt");
//		sendClient.setKeyFile("E:/Documents/SocketTesting/FileClient1/keyfile.txt");
//		sendClient.setTargetIP("192.168.1.13");
//		sendClient.setSocketPort(13267);
//		sendClient.setPacketSize(8000);
//		sendClient.sendFile();
//		
		
//		//Leave uncommented on computer that will receive file.
//		Client receiveClient = new Client();
//		receiveClient.setFilePath("C:/Users/Colin/Documents/FileTransfer/Client2/RockyFile.avi");
//		receiveClient.setKeyFile("C:/Users/Colin/Documents/FileTransfer/Client2/C00130.pdf");
//		receiveClient.setTargetIP("10.110.35.113");
//		receiveClient.setSocketPort(22);
//		receiveClient.setPacketSize(5000);
//		receiveClient.receiveFile();
//		
		ThreadRunner sendClient = new ThreadRunner();
		sendClient.setFilePath("C:/Users/Colin/Documents/FileTransfer/Client1/Jet1.mp4");
		sendClient.setKeyFile("C:/Users/Colin/Documents/FileTransfer/Client1/keyfile.txt");
		sendClient.setTargetIP("10.110.123.221");
		sendClient.setSocketPort(13267);
		sendClient.setPacketSize(40);
		sendClient.setToSend();
		sendClient.setAsciiArmored(true);
		
		ThreadRunner receiveClient = new ThreadRunner();
		receiveClient.setFilePath("C:/Users/Colin/Documents/FileTransfer/Client2/Jet1.mp4");
		receiveClient.setKeyFile("C:/Users/Colin/Documents/FileTransfer/Client2/keyfile.txt");
		receiveClient.setTargetIP("10.110.123.221");
		receiveClient.setSocketPort(13267);
		receiveClient.setPacketSize(40);
		receiveClient.setToReceive();
		receiveClient.setAsciiArmored(true);
		
		Thread sendThread = new Thread(sendClient);
		Thread receiveThread = new Thread(receiveClient);
		sendThread.start();
		receiveThread.start();
	}
}
