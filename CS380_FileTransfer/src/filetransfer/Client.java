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

public class Client {
	private int socketPort = 13267;      // you may change this
	private String targetIP = "127.0.0.1";  // localhost
	private String receiveFile = "E:/Documents/SocketTesting/FileClient1/ReceiveFile.txt";
	private String sendFile = "E:/Documents/SocketTesting/FileClient1/SendFile.txt";
	private String keyFile = "";
	private int packetSize = 3;

  	public final static int FILE_SIZE = 6022386; // file size temporary hard coded
                                               // should bigger than the file to be downloaded

  	
  	public void receiveFile() throws IOException {
  		int bytesRead;
  		int current = 0;
  		FileReceiver fr = null;
  		Socket sock = null;
  		try {
  			sock = new Socket(targetIP, socketPort);
  			System.out.println("Connecting...");

  			// receive file
  			byte [] mybytearray  = new byte [FILE_SIZE];
  			fr = new FileReceiver(receiveFile, sock);
  			InputStream is = sock.getInputStream();
  			bytesRead = is.read(mybytearray,0,mybytearray.length);
  			current = bytesRead;
		
  			int i = 0;
  			while (receiveNextPacket(fr)){
  				System.out.println("receiving shit");
  				i+=packetSize;
  			}
  			System.out.println("File " + receiveFile + " downloaded (" + current + " bytes read)");
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
  	    			fs = new FileSender(sendFile, sock);
  	    			int numPackets = (int) Math.ceil(((int) fs.getFile().length())/packetSize) + 1;
  	    			System.out.println("file size: " + fs.getFile().length());
  	    			System.out.println("number of fucking packets: " + numPackets);
  	    			for (int i = 0; i < numPackets; i++){
  	    				System.out.println("sending fucking packet " + i);
  	    				sendNextPacket(fs);
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
  	
  	private boolean receiveNextPacket(FileReceiver fr) throws IOException{
  		byte [] packet  = new byte [packetSize];
  		int bytesRead;
  		boolean packetReceived = false;
  		
  		while ((bytesRead = fr.getIs().read(packet)) > 0){
  			fr.getBos().write(packet, 0, bytesRead);
  			packetReceived = true;
  		}
  		
		fr.getBos().flush();
		return packetReceived;
  	}
  	
  	private void sendNextPacket(FileSender fs) throws IOException{
  		byte [] packet  = new byte [packetSize];
		fs.getBis().read(packet,0,packetSize);
		fs.getOs().write(packet,0,packetSize);
		fs.getOs().flush();
		System.out.println("Done.");
  	}
  	
  	public void setSendFile(String filePath){
  		sendFile = filePath;
  	}
  	
  	public void setReceiveFile(String filePath){
  		receiveFile = filePath;
  	}
  	
  	public void setTargetIP(String ip){
  		targetIP = ip;
  	}
  	
  	public void setSocketPort(int port){
  		socketPort = port;
  	}
  	
  	public void setKeyFile(String filePath){
  		keyFile = filePath;
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
