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

  	public final static int FILE_SIZE = 6022386; // file size temporary hard coded
                                               // should bigger than the file to be downloaded

  	
  	public void receiveFile() throws IOException {
  		int bytesRead;
  		int current = 0;
  		FileOutputStream fos = null;
  		BufferedOutputStream bos = null;
  		Socket sock = null;
  		try {
  			sock = new Socket(targetIP, socketPort);
  			System.out.println("Connecting...");

  			// receive file
  			byte [] mybytearray  = new byte [FILE_SIZE];
  			InputStream is = sock.getInputStream();
  			fos = new FileOutputStream(receiveFile);
  			bos = new BufferedOutputStream(fos);
  			bytesRead = is.read(mybytearray,0,mybytearray.length);
  			current = bytesRead;

  			do {
  				bytesRead =
  						is.read(mybytearray, current, (mybytearray.length-current));
  				if(bytesRead >= 0) current += bytesRead;
  			} while(bytesRead > -1);

  			bos.write(mybytearray, 0 , current);
  			bos.flush();
  			System.out.println("File " + receiveFile + " downloaded (" + current + " bytes read)");
  		}
  		finally {
  			if (fos != null) fos.close();
  			if (bos != null) bos.close();
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
  	    			fs = new FileSender(new File (sendFile), sock);
  	    			File myFile = new File (sendFile);
  	    			byte [] mybytearray  = new byte [(int)myFile.length()];
  	    			fs.getBis().read(mybytearray,0,mybytearray.length);
  	    			System.out.println("Sending " + sendFile + "(" + mybytearray.length + " bytes)");
  	    			fs.getOs().write(mybytearray,0,mybytearray.length);
  	    			fs.getOs().flush();
  	    			System.out.println("Done.");
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
  	
  	private void sendPacket(FileSender fs, int index){
  		
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
