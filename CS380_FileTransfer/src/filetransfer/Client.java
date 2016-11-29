package filetransfer;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class Client {
	private int socketPort;					// port to connect to
	private String targetIP;				// IP to connect to
	private String filePath;				// path to file to send/receive
	private String keyFilePath;
	private String authenticationFilePath;
	private int packetSize;					// packet size in bytes
	private static final int TIMEOUT_TIME = 5000;	//time until timeout in milliseconds
	private byte[] keyBytes;
	private boolean asciiArmor;
	private HashMap<String,String> authenticationInfo;
	private int retryAttempts;
	
	
	//default values for these are mostly meaningless. Set values later in Driver with setter methods. can change to initialize with parameters in constructor if you want.
	public Client(){
		socketPort = 13267;		// can change to whatever
		targetIP = "127.0.0.1"; // localhost
		filePath = "E:/Documents/SocketTesting/FileClient1/SendFile.txt";	// file to send or receive
		packetSize = 5000;	
		keyFilePath="";// only send 3 bytes by default cause test file is tiny. should be much bigger for real file.
		asciiArmor = false;
		retryAttempts = 5;
	}
  	
  	public void receiveFile(){
  	    Socket sock = null;
  	    FileReceiver fr = null;
  	    
  	    OutputStream responseOs = null;
  	    
  	    System.out.println("[RECV] Waiting to connect to receive...");
  	    
  	    try {
  	    	readKeyBytes(keyFilePath);
  	    	readAuthenticationFile(authenticationFilePath);
  	    	System.out.println("Targetip: " + targetIP);
  	    	System.out.println("socketPort: " + socketPort);
	    	sock = new Socket(targetIP, socketPort);
	    	fr = new FileReceiver(filePath, sock);
	    	responseOs = sock.getOutputStream();
	    	
	    	System.out.println("[RECV] Connected to receive!");
	    	
	    	//TODO: validate username/password here
	    	
	    	String receivedUsername = receiveString(fr);
	    	String receivedPassword = receiveString(fr);
	    	if (checkAuthentication(receivedUsername, receivedPassword)){
	    		System.out.println("[RECV] Authenticated successfully... Receiving file.");
	    		sendResponseSignal(responseOs, true);
	    		
		    	int numPackets = receiveInt(fr);	//receive the number of packets to expect
		    	System.out.println("[RECV] Num of packets to expect:" +  numPackets);
		    	receiveAllPackets(fr, responseOs, numPackets);
		    	terminateReceivingConnection(sock, fr, responseOs);
	    	}
	    	else {
	    		System.out.println("[RECV] Authentication failed. Username or password incorrect.");
	    		sendResponseSignal(responseOs, false);
	    	}
  	  	}
  	    catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}
	    catch (InterruptedException e) {
			System.err.println("InterruptedException: " + e.getMessage());
			e.printStackTrace();
		}
  	}
  	
  	private boolean checkAuthentication(String receivedUsername, String receivedPassword){
  		System.out.println("[RECV] User: " + receivedUsername + " is requesting transfer");
  		System.out.println("[RECV] Received pass: " + receivedPassword);
  		
  		if (authenticationInfo.containsKey(receivedUsername) && authenticationInfo.get(receivedUsername).equals(receivedPassword)){
  			return true;
  		}
  		return false;
  	}
  	
  	private void readAuthenticationFile(String filepath) throws FileNotFoundException{
  		Scanner read = new Scanner(new File(filepath));
  		read.useDelimiter(",");
  		authenticationInfo = new HashMap<String, String>();
  		
  		while (read.hasNext()){
  			String username = read.next();
  			String password = read.next();
  			authenticationInfo.put(username, password);
  		}
  		read.close();
  	}
  	
  	//sends the file to the connected client.
  	public void sendFile(String username, String password){
  	    ServerSocket servsock = null;
  	    Socket sock = null;
  	    FileSender fs = null;
  	    
  	    InputStream responseIs = null;
  	    
  	    try {
  	    	readKeyBytes(keyFilePath);
  	    	sock = establishConnection(servsock, socketPort);
  	    	fs = new FileSender(filePath, sock);
  			responseIs = sock.getInputStream();
  			
  			int numPackets = (int) Math.ceil(((int) fs.getFile().length())/packetSize) + 1;
  			System.out.println("[SEND] File Size: " + fs.getFile().length());
  			System.out.println("[SEND] Number of packets: " + numPackets);
  			
  			//send username and password to receiver
  			sendString(fs, username);
  			sendString(fs, password);
  			
  			//receiver will send responseSignal = true if authenticated correctly
  			boolean authenticated = checkResponseSignal(responseIs);
  			if (authenticated){
  				System.out.println("[SEND] Authenticated successfully... Sending file.");
  				//do file sending shit
	  			sendInt(fs, numPackets);	//tell receiver how many packets to expect
	  			sendAllPackets(fs, responseIs, numPackets);
  			}
  			else {
  				System.out.println("[SEND] Authentication failed. Username or password incorrect.");
  			}
  			terminateSendingConnection(servsock, sock, fs, responseIs);  			
  	    }
  		catch (IOException e) {
  			System.err.println("IOException: " + e.getMessage());
  			e.printStackTrace();
  		}
  	    catch (InterruptedException e) {
			System.err.println("InterruptedException: " + e.getMessage());
			e.printStackTrace();
		}
  	}	
  	
  	//Establishes connection. Returns Socket that is connected.
  	private Socket establishConnection(ServerSocket servsock, int port) throws IOException{
  		Socket sock;
  		System.out.println("[SEND] Waiting for connection to send...");
  		servsock = new ServerSocket(port);
  		sock = servsock.accept();
		System.out.println("[SEND] Connected to send!: " + sock);
		servsock.close();
		return sock;
  	}
  	
  	//Terminates connection by closing all streams.
  	private void terminateSendingConnection(ServerSocket servsock, Socket sock, Closeable filehandler, Closeable responseStream) throws IOException{
  		System.out.println("[SEND] Terminating sending connection...");
		if (servsock != null) servsock.close();
    	if (filehandler != null) filehandler.close();
		if (sock!=null) sock.close();
		if (responseStream != null) responseStream.close();
  	}
  	
  	//Terminates connection by closing all streams.
  	private void terminateReceivingConnection(Socket sock, Closeable filehandler, Closeable responseStream) throws IOException{
  		System.out.println("[RECV] Terminating receiving connection...");
  		if (filehandler != null) filehandler.close();
		if (sock!=null) sock.close();
		if (responseStream != null) responseStream.close();
  	}
  	
  	
  	private boolean receiveAllPackets(FileReceiver fr, OutputStream responseOs, int numPackets) throws IOException, InterruptedException{
  		//loop through receiving packets
  		byte[] receivedPacket;
  		int remainingAttempts = retryAttempts;
  		
		int i = 0;
		while (i < numPackets){
			boolean timedOut = waitForAvailable(fr.getIs(), TIMEOUT_TIME, packetSize);	//wait for full packet to arrive and be available
			if (timedOut == true){
				System.out.println("[RECV] Did not receive full packet. Timed out");
			}
			
			int nextPacketSize = receiveInt(fr);
			receivedPacket = receiveNextPacket(fr, nextPacketSize);		
			
			if (receivedPacket != null){
				System.out.println("[RECV] Received packet #" + i);
				byte[] checksum = receiveNextChecksum(fr);
							
				if (asciiArmor){				
					receivedPacket = myAsciiDecode(receivedPacket);
				}
				
				receivedPacket = XoR(receivedPacket,i);	//decrypt packet
				checksum = XoR(checksum, i);			//decrypt checksum
				
				if (checkIntegrity(receivedPacket, checksum) && !timedOut){
					System.out.println("[RECV] Packet has integrity");
					i++;
					remainingAttempts = remainingAttempts;
					//TODO: decrypt packet here
					writePacketToFile(fr, receivedPacket, packetSize);
					sendResponseSignal(responseOs, true);		//let sender know packet was successfully received
				}
				else{
					System.out.println("[RECV] Problem with transmission.");
					if (remainingAttempts > 0){
						remainingAttempts--;
						System.out.println("[RECV] Signaling sender to try again.");
						System.out.println("[RECV] Remaining attempts: " + remainingAttempts);
						sendResponseSignal(responseOs, false);	//let sender know packet was not correct, need to resend packet	
					}
					else {
						System.out.println("[RECV] Out of attempts. Quitting receiving.");
						return false;
					}		
				}
			}
		}
		System.out.println("[RECV] Finished receiving file");
		return true;
  	}
  	
  	private boolean sendAllPackets(FileSender fs, InputStream responseIs, int numPackets) throws IOException, InterruptedException{
  		boolean moveToNextPacket = true;
		boolean timedOut = false;
		boolean successfulReceive;
		byte[] packet = null;
		byte[] checksum = null;
		
		//loop through sending each packet
		int i = 0;
		int remainingAttempts = retryAttempts;
		while (i < numPackets){
			System.out.println("[SEND] Sending packet #" + i);
			if (moveToNextPacket){	
				packet = prepareNextPacket(fs);
				checksum = checksumPacketBytes(packet);
				packet=XoR(packet,i); //encrypt packet
				checksum=XoR(checksum,i); //encrypt checksum
				i++;
				remainingAttempts = retryAttempts;
			}		
			
			if (asciiArmor){
				
//				System.out.println("PACKET BEFORE ASCII (Send):");
//				printByteArray(packet);
				
//				System.out.println("Default Encode:");
//				printByteArray(Base64.getEncoder().encode(packet));
				
				packet= myAsciiEncode(packet);
				
//				System.out.println("PACKET AFTER ASCII (Send):");
//				printByteArray(packet);
			}
						
			sendInt(fs, packet.length);
			sendPacket(fs, packet);
			sendChecksum(fs, checksum);
			
			timedOut = waitForAvailable(responseIs, TIMEOUT_TIME, 1);	//wait until 1 byte arrives (signal that last packet was successful)
			successfulReceive = checkResponseSignal(responseIs);		//resolve that byte to a boolean
			if (successfulReceive && !timedOut){						//if packet was received successfully and signal did not time out, send next packet. Otherwise send same packet again.
				moveToNextPacket = true;
			}
			else {
				System.out.println("[SEND] Last packet not received correctly...");
				if (remainingAttempts > 0){
					moveToNextPacket = false;
					remainingAttempts--;
					System.out.println("[SEND] Remaining Attempts: " + remainingAttempts);
				}
				else {
					System.out.println("[SEND] Out of attempts. Quitting sending.");
					return false;
				}
			}
		}
		return true;
  	}
  	
  	
  	//should make this return false if timed out
  	private boolean waitForAvailable(InputStream is, int attempts, int reqBytes) throws IOException, InterruptedException{
			//wait for full packet to arrive before continuing
			int curAttempt = 0;
			while (is.available() < reqBytes && curAttempt < attempts){
				curAttempt++;
				Thread.sleep(1);
			}
			if (curAttempt >= attempts){
				System.out.println("Timed out...");
				return true;
			}
			return false;
  	}
  	
  	
  	//returns packet if it was received. else returns null.
  	private byte[] receiveNextPacket(FileReceiver fr, int expectedSize) throws IOException{
		byte [] packet  = new byte [expectedSize];
  		int bytesRead;
  		
  		if ((bytesRead = fr.getIs().read(packet)) > 0){
  			return packet;
  		}	
  		return null;
  	}
  	
  	private void sendResponseSignal(OutputStream os, boolean successful) throws IOException{
  		byte[] result = new byte[1];
  		if (successful){
  			result[0] = 1;
  		}
  		else {
  			result[0] = 0;
  		}
  		os.write(result,0,1);	//only one byte for boolean
		os.flush();
  	}
  	
  	private boolean checkResponseSignal(InputStream is) throws IOException{
  		byte[] result = new byte [1];
  		is.read(result);
  		if (result[0] == 0){
  			return false;	//signal was false
  		}
  		return true;		//signal was true
  	}
  	
  	private void writePacketToFile(FileReceiver fr, byte[] packet, int bytesRead) throws IOException{
  		fr.getBos().write(packet, 0, bytesRead);
  		fr.getBos().flush();
  	}
  	
  	//receives and returns the next hash from the connected client
  	private byte[] receiveNextChecksum(FileReceiver fr) throws IOException{
  		byte [] hash  = new byte [4];
  		
  		fr.getIs().read(hash);
  		
  		return (hash);
  	}
  	
  	//sends the next packet to the connected client, then returns the packet.
  	//returns packet so it can be used to sendHashedPacket() in sendFile.
  	private byte[] sendPacket(FileSender fs, byte[] packet) throws IOException{
  		
  		fs.getOs().write(packet,0,packet.length);
		fs.getOs().flush();
		
		return packet;		
  	}
  	
  	private byte[] prepareNextPacket(FileSender fs) throws IOException{
  		byte [] packet  = new byte [packetSize];
		fs.getBis().read(packet,0,packetSize);
		return packet;
  	}
   	
  	
  	//sends the hash to the connected client.
  	private void sendChecksum(FileSender fs, byte[] checksum) throws IOException{
		fs.getOs().write(checksum,0,checksum.length);		
		fs.getOs().flush();	
  	}
  	
  	//sends an integer to the receiver
  	private void sendInt(FileSender fs, int numPackets) throws IOException{	
  		byte[] intBytes = ByteBuffer.allocate(4).putInt(numPackets).array();	
  		
  		fs.getOs().write(intBytes,0,intBytes.length);
		fs.getOs().flush();
  	}
  	
  	//receives byte array of int and returns int
  	private int receiveInt(FileReceiver fr) throws IOException{	
  		byte [] intBytes  = new byte [4];
  	
  		fr.getIs().read(intBytes);

  		return (ByteBuffer.wrap(intBytes).getInt());
  	}
  	
  	//sends a string to the receiver
  	private void sendString(FileSender fs, String string) throws IOException{	
  		System.out.println("[SEND] Sending string: " + string);
  		byte[] stringBytes = string.getBytes();
  		
  		sendInt(fs, stringBytes.length);					//tell receiver how many bytes to expect
  		fs.getOs().write(stringBytes,0,stringBytes.length);	//send the string
		fs.getOs().flush();
  	}
  	
  	//receives bytes of string and returns that string
  	private String receiveString(FileReceiver fr) throws IOException{	
  		byte [] stringBytes  = new byte [receiveInt(fr)];	//receive how many bytes string will be
  	
  		fr.getIs().read(stringBytes);

  		String result = new String(stringBytes, Charset.forName("UTF-8"));
  		return result;
  	}
  	
  	
  	//returns a byte array of the hash of the given packet.
  	private byte[] checksumPacketBytes(byte[] packet){
  		int hash = 0;
  		
  		for (byte b : packet){
  			hash = hash *31 ^ b;
  		}
  		
  		byte[] result = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(hash).array();	
  		return result;
  	}
  	
  	//compares the received hash to the calculated hash of the received packet. Returns true if the packet has integrity. Returns false if the packet has been tampered with.
  	private boolean checkIntegrity(byte[] receivedPacket, byte[] receivedHash){		
  		byte[] hash = checksumPacketBytes(receivedPacket); 		

  		if (Arrays.equals(hash, receivedHash)){
  			return true;
  		}
  		return false;
  	}
  	
  	public void setFilePath(String filePath){
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
  	
  	public void setAuthenticationFilePath(String filePath){
  		this.authenticationFilePath = filePath;
  	}
  	
  	public void setPacketSize(int packetSize){
  		this.packetSize = packetSize;
  	}
  	
  	public void setAsciiArmored(boolean asciiArmor){
  		this.asciiArmor = asciiArmor;
  	}
  	
  	public void setRetryAttempts(int retryAttempts){
  		this.retryAttempts = retryAttempts;
  	}
  	
  	
  	//For testing only. Delete when done.
  	private void printByteArray(byte[] bytes){
  		System.out.println("=================");
  		for (byte b : bytes){
  			System.out.println(b);
  		}
  		System.out.println("=================");
  	}
  	
  	private byte[] XoR(byte[] a, int packetNumber) throws IOException
    {
        byte[] c= new byte[a.length];
        byte[] b= keyBytes;
        int eof= keyBytes.length;
        
       int j= packetNumber*packetSize;

        for(int i=0;i<a.length;i++)
        {
        	if(j>=eof)
        		j=j%eof;
        	
            c[i] = (byte) (a[i] ^ b[j]);
            j++;
        }

        return c;

    }

	private void readKeyBytes(String fileName) throws IOException
	{
  		KeyFile kf= new KeyFile(fileName);
  		System.out.println("Key File size:" + kf.getFile().length());
  		keyBytes = new byte[(int) kf.getFile().length()];
  		kf.getFis().read(keyBytes, 0, (int)(kf.getFile().length()));

  	}
	public static byte[] asciiEncode2(byte[] bArray) throws IOException{
        String dictionary = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwqyx0123456789+/";
        String asciiData = "";
        String[] ascii = new String[4];			//For breaking 3 bytes into 4 sets of 6 bits
        byte[] armouredBArray;
        
        int len = bArray.length;
		
  		for(int i = 0; i < len; i+=3){
  			String s = "";
  			String temp = Integer.toBinaryString(0xFF & bArray[i]);
  			while (temp.length() < 8)
  				temp = "0" + temp;
  			s += temp;
  			if(i+1  < len){
  				temp = Integer.toBinaryString(0xFF & bArray[i+1]);
	  			while (temp.length() < 8)
	  				temp = "0" + temp;
	  			s += temp;
  			}
  			if(i+2  < len){
  				temp = Integer.toBinaryString(0xFF & bArray[i+2]);
	  			while (temp.length() < 8)
	  				temp = "0" + temp;
	  			s += temp;
  			}
  			//if (s.length() == 24)
  				//System.out.println("Number in bits:" + s);
  			 if(s.length() == 8)
  				s += "0000000000000000";
  			else if(s.length() == 16)
  			  	s += "00000000";
  			  	
  	        int y = 0;
  	        int counter = 0;
  	        while (y < ascii.length) {
  	        	String a = "";
  	        	while (counter < s.length() ){
  	        		a += s.charAt(counter);
  	        		counter++;
  	        		if (counter % 6 == 0)
  	        			break;
              }
              ascii[y] = a;
              y++;
          }
  	        
  	        
          //System.out.println("\nGroup in 6 bits:");
          //for(int j = 0; j < ascii.length; j++) {
          //    System.out.println(ascii[j]);
          //}
          //System.out.println("\nInteger Values:");
          for(int j = 0; j < ascii.length; j++) {
              int index = Integer.parseInt(ascii[j], 2);
              //System.out.print("Int number: " + index);
              //System.out.println("\t\tBase 64: " + dictionary.charAt(index));
              asciiData += dictionary.charAt(index);
          }
  		}
  		armouredBArray = asciiData.getBytes();
  		
  		return armouredBArray;
  		
  	}
	
	public  byte[] asciiDecode(byte[] bArray){
    	

		  byte[] decoded = Base64.getDecoder().decode(bArray);
		  //System.out.println(new String(decoded));
		
		  String asciiData = new String(decoded);
		

        char cur;
        int base64Val;
        String finalBinaryString = "";
        
        
         String dictionary = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwqyx0123456789+/";
	       
        for(int i = 0; i < asciiData.length(); i++){
      	  
            cur = asciiData.charAt(i);
            
            base64Val = dictionary.indexOf(cur);
            
            String binaryString = Integer.toBinaryString(base64Val);
            
            while (binaryString.length() < 6)
          	  binaryString = "0" + binaryString;
            
            finalBinaryString += binaryString;
            
            
        }
        //System.out.println(finalBinaryString);
        bArray = new BigInteger(finalBinaryString, 2).toByteArray();
        
        return bArray;
	}
	
	public byte[] myAsciiEncode(byte[] bArray){
		return Base64.getEncoder().encode(bArray);
	}
	
	public byte[] myAsciiDecode(byte[] bArray){
		return Base64.getDecoder().decode(bArray);
	}
	
}
  	//TODO: (in no particular order)
  	//1. Send packet size to receiver before sending packets. Don't rely on receiver to hardcode correct packet size.
  	//2. XOR encrypt sent packets with key file
  	//3. Signal sender to terminate connection when successfully received file
  	//4. Retry if packet is invalid (hash doesn't match)
  	//			-- use mark() if correct, use reset() if invalid.
    //5. Make separate method for writing received packet to file. Don't save packet unless it has been checked for integrity.
  	//6. Authenticate with username and password
  	//7. Terminate connection if connection lost.
  	//8. Improve integrity hashing algorithm?
  	//9. Last sent packet should cut off after last used byte. Currently will send with empty bytes if full packet size isn't used.
  	//10. make waitForReceived return false if timed out. Use this to retry send if timed out

