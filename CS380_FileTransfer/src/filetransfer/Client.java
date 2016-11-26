package filetransfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Client {
	private int socketPort;					// port to connect to
	private String targetIP;				// IP to connect to
	private String filePath;				// path to file to send/receive
	private String keyFilePath = "";		// not yet implemented
	private int packetSize;					// packet size in bytes
	
	//default values for these are mostly meaningless. Set values later in Driver with setter methods. can change to initialize with parameters in constructor if you want.
	public Client(){
		socketPort = 13267;		// can change to whatever
		targetIP = "127.0.0.1"; // localhost
		filePath = "E:/Documents/SocketTesting/FileClient1/SendFile.txt";	// file to send or receive
		packetSize = 3;
	}
  	
  	public void receiveFile() throws IOException {
  		FileReceiver fr = null;
  		Socket sock = null;
  		try {
  			sock = new Socket(targetIP, socketPort);
  			System.out.println("Connecting...");

  			// receive file
  			fr = new FileReceiver(filePath, sock);
  			boolean receivedPacket = false;
  			int i = 0;
  			do {
  				receivedPacket = receiveNextPacket(fr);
  				if (receivedPacket){
  					System.out.println("received shit packet " + i);
  					i++;
  					//check integrity here
  					//use mark if correct. use reset if incorrect. Reset will move back to last mark. Try for number of retries.
  				}
  				else {
  					//notify receiver of successful completion of file transfer
  					//notify receiver to terminate connection
  				}
  			} while(receivedPacket);
  			
  			System.out.println("File " + filePath + " downloaded (" + i * packetSize + " bytes read)");
  		}
  		finally {
  			if (fr.getFos() != null) fr.getFos().close();
  			if (fr.getBos() != null) fr.getBos().close();
  			if (sock != null) sock.close();
  		}
  	}
  	
  	public void sendFile() throws IOException{
  	    ServerSocket servsock = null;
  	    Socket sock = null;
  	    FileSender fs = null;
  	    try {
  	    	servsock = new ServerSocket(socketPort);
  	    	while (true) {
  	    		System.out.println("Waiting...");
  	    		try {
  	    			sock = servsock.accept();
  	    			System.out.println("Accepted connection : " + sock);
  	    			// send file
  	    			fs = new FileSender(filePath, sock);
  	    			int numPackets = (int) Math.ceil(((int) fs.getFile().length())/packetSize) + 1;
  	    			System.out.println("file size: " + fs.getFile().length());
  	    			System.out.println("number of fucking packets: " + numPackets);
  	    			for (int i = 0; i < numPackets; i++){
  	    				System.out.println("sending fucking packet " + i);
  	    				byte[] packet = sendNextPacket(fs);
  	    				//sendHashedPacket(fs, packet);
  	    			}			
  	    		}
  	    		finally {
  	    			if (fs.getBis() != null) fs.getBis().close();
  	    			if (fs.getOs() != null) fs.getOs().close();
  	    			if (sock!=null) sock.close();
  	    		}
  	    	}
  	    }
  	    finally {
  	    	if (servsock != null) servsock.close();
  	    }
  	}
  	
  	//returns true if there was a packet to receive.
  	private boolean receiveNextPacket(FileReceiver fr) throws IOException{
  		byte [] packet  = new byte [packetSize];
  		int bytesRead;
  		boolean packetReceived = false;
  		
  		if ((bytesRead = fr.getIs().read(packet)) > 0){
  			fr.getBos().write(packet, 0, bytesRead);
  			packetReceived = true;
  		}	
 		
		fr.getBos().flush();
		return packetReceived;
  	}
  	
  	//returns packet so it can be used to sendHashedPacket in sendFile.
  	private byte[] sendNextPacket(FileSender fs) throws IOException{
  		byte [] packet  = new byte [packetSize];
		fs.getBis().read(packet,0,packetSize);
		fs.getOs().write(packet,0,packetSize);
		fs.getOs().flush();
		System.out.println("Packet sent.");
		return packet;		
  	}
  	
  	private void sendHashedPacket(FileSender fs, byte[] packet) throws IOException{
  		byte[] hashBytes = hashPacketBytes(packet);
  		fs.getBis().read(hashBytes,0,hashBytes.length);
		fs.getOs().write(packet,0,hashBytes.length);
		fs.getOs().flush();
		System.out.println("Hash sent.");
  	}
  	
  	private byte[] hashPacketBytes(byte[] packet){
  		int hash = 0;
  		
  		for (byte b : packet){
  			hash = hash *31 ^ b;
  		}
  		
  		return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(hash).array();		
  	}
  	
  	public void setSendFile(String filePath){
  		this.filePath = filePath;
  	}
  	
  	public void setReceiveFile(String filePath){
  		this.filePath = filePath;
  	}
  	
  	public void setTargetIP(String ip){
  		targetIP = ip;
  	}
  	
  	public void setSocketPort(int port){
  		socketPort = port;
  	}
  	
  	public void setKeyFile(String filePath){
  		keyFilePath = filePath;
  	}
  	
  	public void setPacketSize(int packetSize){
  		this.packetSize = packetSize;
  	}
  	
//  	public Socket establishConnection() throws IOException {
//  		Socket sock = null;
//  		
//  		try {
//  			sock = new Socket(SERVER, SOCKET_PORT);
//  			System.out.println("Connecting...");
//  		}
//  		finally {
//  			if (sock != null) sock.close();
//  		}
//  		return sock;
//  	}
}
