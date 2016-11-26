package filetransfer;

import java.io.IOException;
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
	
	//default values for these are mostly meaningless. Set values later in Driver with setter methods. can change to initialize with parameters in constructor if you want.
	public Client(){
		socketPort = 13267;		// can change to whatever
		targetIP = "127.0.0.1"; // localhost
		filePath = "E:/Documents/SocketTesting/FileClient1/SendFile.txt";	// file to send or receive
		packetSize = 3;			// only send 3 bytes by default cause test file is tiny. should be much bigger for real file.
	}
  	
	//receives the file from the connected client.
  	public void receiveFile() throws IOException {
  		FileReceiver fr = null;
  		Socket sock = null;
  		try {
  			sock = new Socket(targetIP, socketPort);
  			System.out.println("Connecting...");

  			// receive file
  			fr = new FileReceiver(filePath, sock);
  			byte[] receivedPacket = null;
  			
  			//loop through receiving packets
  			int i = 0;
  			do {
  				receivedPacket = receiveNextPacket(fr);
  				if (receivedPacket != null){
  					System.out.println("Received packet #" + i);
  					i++;
  					byte[] hash = receiveNextHash(fr);
  					if (checkIntegrity(receivedPacket, hash)){
  						//Valid packet. carry on.
  						System.out.println("Packet has integrity");
  					}
  					else{
  						//Invalid packet
  						System.out.println("Packet has does not match given hash!");
  					}
  					//invalid packet handling not fully implemented yet.
  					//use fr.getIs().mark if correct. use fr.getIs().reset if incorrect. Reset will move back to last mark. Try for number of retries.
  				}
  				else {
  					//notify receiver of successful completion of file transfer
  					//notify receiver to terminate connection
  				}
  			} while(receivedPacket != null);
  			
  			
  			System.out.println("File " + filePath + " downloaded (" + i * packetSize + " bytes read)");
  		}
  		finally {
  			if (fr.getFos() != null) fr.getFos().close();
  			if (fr.getBos() != null) fr.getBos().close();
  			if (sock != null) sock.close();
  		}
  	}
  	
  	//sends the file to the connected client.
  	public void sendFile() throws IOException{
  	    ServerSocket servsock = null;
  	    Socket sock = null;
  	    FileSender fs = null;
  	    try {
  	    	servsock = new ServerSocket(socketPort);
  	    	while (true) {	//TODO: only loop until termination of connection from receiver
  	    		System.out.println("Waiting...");
  	    		try {
  	    			sock = servsock.accept();
  	    			System.out.println("Accepted connection: " + sock);
  	    			// send file
  	    			fs = new FileSender(filePath, sock);
  	    			int numPackets = (int) Math.ceil(((int) fs.getFile().length())/packetSize) + 1;
  	    			System.out.println("File Size: " + fs.getFile().length());
  	    			System.out.println("Number of packets: " + numPackets);
  	    			
  	    			//loop through sending packets
  	    			for (int i = 0; i < numPackets; i++){
  	    				System.out.println("Sending packet #" + i);
  	    				byte[] packet = sendNextPacket(fs);	//send packet
  	    				sendHashedPacket(fs, packet);		//send hash of that packet for checking integrity
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
  	
  	//returns packet if it was received. else returns null.
  	private byte[] receiveNextPacket(FileReceiver fr) throws IOException{
  		byte [] packet  = new byte [packetSize];
  		int bytesRead;
  		boolean packetReceived = false;
  		
  		if ((bytesRead = fr.getIs().read(packet)) > 0){
  			fr.getBos().write(packet, 0, bytesRead);
  			packetReceived = true;
  		}	
 		
		fr.getBos().flush();
		if (packetReceived){
			return packet;
		}
		else {
			return null;
		}
  	}
  	
  	//receives and returns the next hash from the connected client
  	private byte[] receiveNextHash(FileReceiver fr) throws IOException{
  		byte [] hash  = new byte [4];
  		
  		fr.getIs().read(hash);
  		
  		return (hash);
  	}
  	
  	//sends the next packet to the connected client, then returns the packet.
  	//returns packet so it can be used to sendHashedPacket() in sendFile.
  	private byte[] sendNextPacket(FileSender fs) throws IOException{
  		byte [] packet  = new byte [packetSize];
		fs.getBis().read(packet,0,packetSize);
		fs.getOs().write(packet,0,packetSize);
		fs.getOs().flush();
		return packet;		
  	}
  	
  	//hashes the given packet, and sends the hash to the connected client.
  	private void sendHashedPacket(FileSender fs, byte[] packet) throws IOException{
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
  	
  	
}
