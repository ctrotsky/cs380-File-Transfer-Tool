package filetransfer;

import java.io.IOException;

public class Driver {

	public static void main(String[] args) throws IOException {
		Client myClient = new Client();
		myClient.setSendFile("E:/Documents/SocketTesting/FileClient1/SendFile.txt");
		myClient.setTargetIP("127.0.0.1");
		myClient.setSocketPort(13267);
	}
}
