package filetransfer;

public class ThreadRunner implements Runnable {
	
	Client myClient = new Client();
	boolean sendMode = true;

	public void setFilePath(String filePath){
		myClient.setFilePath(filePath);
  	}
  	
  	public void setTargetIP(String ip){
  		myClient.setTargetIP(ip);
  	}
  	
  	public void setSocketPort(int port){
  		myClient.setSocketPort(port);
  	}
  	
  	public void setKeyFile(String filePath){
  		myClient.setKeyFile(filePath);
  	}
  	
  	public void setPacketSize(int packetSize){
  		myClient.setPacketSize(packetSize);
  	}
  	
  	public void setToSend(){
  		sendMode = true;
  	}
  	
  	public void setToReceive(){
  		sendMode = false;
  	}
  	
  	public void setAsciiArmored(boolean asciiArmor){
  		myClient.setAsciiArmored(asciiArmor);
  	}
	
	
	@Override
	public void run() {
		if (sendMode){
			myClient.sendFile();
		}
		else{
			myClient.receiveFile();
		}
	}

	
	
}
