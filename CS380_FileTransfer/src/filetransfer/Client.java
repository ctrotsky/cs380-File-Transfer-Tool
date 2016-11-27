package filetransfer;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Client {
	private int socketPort;					// port to connect to
	private String targetIP;				// IP to connect to
	private String filePath;				// path to file to send/receive
	private String keyFilePath = "";		// not yet implemented. Will be used to XOR encrypt send packets.
	private int packetSize;					// packet size in bytes
	private static final int TIMEOUT_TIME = 5000;	//time until timeout in milliseconds
	
	//default values for these are mostly meaningless. Set values later in Driver with setter methods. can change to initialize with parameters in constructor if you want.
	public Client(){
		socketPort = 13267;		// can change to whatever
		targetIP = "127.0.0.1"; // localhost
		filePath = "E:/Documents/SocketTesting/FileClient1/SendFile.txt";	// file to send or receive
		packetSize = 3;			// only send 3 bytes by default cause test file is tiny. should be much bigger for real file.
	}
  	
  	public void receiveFile(){
  	    Socket sock = null;
  	    FileReceiver fr = null;
  	    
  	    OutputStream responseOs = null;
  	    
  	    try {
	    	sock = new Socket(targetIP, socketPort);
	    	fr = new FileReceiver(filePath, sock);
	    	responseOs = sock.getOutputStream();
	    	
	    	//TODO: validate username/password here
	    	receiveAllPackets(fr, responseOs);
	    	terminateReceivingConnection(sock, fr, responseOs);
  	  	}
  	    catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
		}
	    catch (InterruptedException e) {
			System.err.println("InterruptedException: " + e.getMessage());
		}
  	}
  	
  	//sends the file to the connected client.
  	public void sendFile(){
  	    ServerSocket servsock = null;
  	    Socket sock = null;
  	    FileSender fs = null;
  	    
  	    InputStream responseIs = null;
  	    
  	    try {
  	    	sock = establishConnection(servsock, socketPort);
  	    	fs = new FileSender(filePath, sock);
  			responseIs = sock.getInputStream();
  			
  			int numPackets = (int) Math.ceil(((int) fs.getFile().length())/packetSize) + 1;
  			System.out.println("File Size: " + fs.getFile().length());
  			System.out.println("Number of packets: " + numPackets);
  			
  			sendAllPackets(fs, responseIs, numPackets);
  			terminateSendingConnection(servsock, sock, fs, responseIs);
  			
  	    }
  		catch (IOException e) {
  			System.err.println("IOException: " + e.getMessage());
  		}
  	    catch (InterruptedException e) {
			System.err.println("InterruptedException: " + e.getMessage());
		}
  	}	
  	
  	//Establishes connection. Returns Socket that is connected.
  	private Socket establishConnection(ServerSocket servsock, int port) throws IOException{
  		Socket sock;
  		System.out.println("Waiting for connection...");
  		servsock = new ServerSocket(port);
  		sock = servsock.accept();
		System.out.println("Accepted connection: " + sock);
		servsock.close();
		return sock;
  	}
  	
  	//Terminates connection by closing all streams.
  	private void terminateSendingConnection(ServerSocket servsock, Socket sock, Closeable filehandler, Closeable responseStream) throws IOException{
  			if (servsock != null) servsock.close();
	    	if (filehandler != null) filehandler.close();
			if (sock!=null) sock.close();
			if (responseStream != null) responseStream.close();
  	}
  	
  //Terminates connection by closing all streams.
  	private void terminateReceivingConnection(Socket sock, Closeable filehandler, Closeable responseStream) throws IOException{
	    	if (filehandler != null) filehandler.close();
			if (sock!=null) sock.close();
			if (responseStream != null) responseStream.close();
  	}
  	
  	
  	private void receiveAllPackets(FileReceiver fr, OutputStream responseOs) throws IOException, InterruptedException{
  		//loop through receiving packets
  		byte[] receivedPacket;
  		
		int i = 0;
		do {
			boolean timedOut = waitForAvailable(fr.getIs(), TIMEOUT_TIME, packetSize);	//wait for full packet to arrive and be available
			if (timedOut == true){
				System.out.println("Did not receive full packet. Timed out");
			}
			
			receivedPacket = receiveNextPacket(fr);		
			if (receivedPacket != null){
				System.out.println("Received packet #" + i);
				byte[] hash = receiveNextChecksum(fr);
				if (checkIntegrity(receivedPacket, hash) && !timedOut){
					System.out.println("Packet has integrity");
					i++;
					//ROCKY WRITE XOR DECRYPTION METHOD CALL HERE. Have it modify receivedPacket array to be decrypted.
					writePacketToFile(fr, receivedPacket, packetSize);
					signalPacketReceived(responseOs, true);		//let sender know packet was successfully received
				}
				else{
					System.out.println("Packet needs to be resent");
					signalPacketReceived(responseOs, false);	//let sender know packet was not correct, need to resend packet
				}
			}
			else {
				//notify receiver of successful completion of file transfer
				//notify receiver to terminate connection
			}
		} while(receivedPacket != null);
  	}
  	
  	private void sendAllPackets(FileSender fs, InputStream responseIs, int numPackets) throws IOException, InterruptedException{
  		boolean moveToNextPacket = true;
		boolean timedOut = false;
		boolean successfulReceive;
		byte[] packet = null;
		
		//loop through sending each packet
		int i = 0;
		while (i < numPackets){
			System.out.println("Sending packet #" + i);
			if (moveToNextPacket){	
				packet = prepareNextPacket(fs);
				i++;
			}	
		sendPacket(fs, packet);				
		sendChecksum(fs, packet);
		timedOut = waitForAvailable(responseIs, TIMEOUT_TIME, 1);		//wait until 1 byte arrives (signal that last packet was successful)
		successfulReceive = checkSignal(responseIs);				//resolve that byte to a boolean
		if (successfulReceive && !timedOut){						//if packet was received successfully and signal did not time out, send next packet. Otherwise send same packet again.
				moveToNextPacket = true;
			}
			else {
				System.out.println("last packet not received correctly, retrying");
				moveToNextPacket = false;
			}
		}
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
  	private byte[] receiveNextPacket(FileReceiver fr) throws IOException{
  		byte [] packet  = new byte [packetSize];
  		int bytesRead;
  		
  		if ((bytesRead = fr.getIs().read(packet)) > 0){
  			return packet;
  		}	
 			
		return null;
  	}
  	
  	private void signalPacketReceived(OutputStream os, boolean successful) throws IOException{
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
  	
  	private boolean checkSignal(InputStream is) throws IOException{
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
		fs.getOs().write(packet,0,packetSize);
		fs.getOs().flush();
		
		return packet;		
  	}
  	
  	private byte[] prepareNextPacket(FileSender fs) throws IOException{
  		byte [] packet  = new byte [packetSize];
		fs.getBis().read(packet,0,packetSize);
		return packet;
  	}
   	
  	
  	//hashes the given packet, and sends the hash to the connected client.
  	private void sendChecksum(FileSender fs, byte[] packet) throws IOException{
  		byte[] hashBytes = hashPacketBytes(packet);
  		
  		
		fs.getOs().write(hashBytes,0,hashBytes.length);
		
		fs.getOs().flush();
		
  	}
  	
  	//returns a byte array of the hash of the given packet.
  	private byte[] hashPacketBytes(byte[] packet){
  		int hash = 0;
  		
  		for (byte b : packet){
  			hash = hash *31 ^ b;
  		}
  		
  		byte[] result = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(hash).array();	
  		return result;
  	}
  	
  	//compares the received hash to the calculated hash of the received packet. Returns true if the packet has integrity. Returns false if the packet has been tampered with.
  	private boolean checkIntegrity(byte[] receivedPacket, byte[] receivedHash){	
  		byte[] hash = hashPacketBytes(receivedPacket);

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
  	
  	public void setPacketSize(int packetSize){
  		this.packetSize = packetSize;
  	}
  	
  	
  	//For testing only. Delete when done.
  	private void printByteArray(byte[] bytes){
  		System.out.println("=================");
  		for (byte b : bytes){
  			System.out.println(b);
  		}
  		System.out.println("=================");
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
}
