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
	private int packetSize = 1000;

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
  			while (i < FILE_SIZE){
  				receivePacket(fr, i);
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
  	    			int numPackets = (int) (fs.getFile().length() / packetSize);
  	    			for (int i = 0; i < numPackets; i++){
  	    				sendPacket(fs, i * packetSize);
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
  	
  	private void receivePacket(FileReceiver fr, int index) throws IOException{
  		byte [] packet  = new byte [packetSize];
  		int bytesRead = fr.getIs().read(packet,index,index + packetSize); //might go over end of file?
		int current = bytesRead;

		do {
			bytesRead = fr.getIs().read(packet, current, (packet.length-current));
			if(bytesRead >= 0) {
				current += bytesRead;
			}
		} while(bytesRead > -1);

		fr.getBos().write(packet, index , index + bytesRead);	//maybe should be +packetSize
		fr.getBos().flush();
  	}
  	
  	private void sendPacket(FileSender fs, int index) throws IOException{
  		byte [] packet  = new byte [packetSize];
		fs.getBis().read(packet,index,index + packet.length);
		System.out.println("Sending packet index " + index + " from "  + sendFile + "(" + packet.length + " bytes)");
		fs.getOs().write(packet,index,index + packet.length);
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
