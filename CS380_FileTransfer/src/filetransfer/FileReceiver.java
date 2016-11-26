package filetransfer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

//wrapper class for streams associated with receiving files
public class FileReceiver {
	private FileOutputStream fos;
	private BufferedOutputStream bos;
	private InputStream is;
	private Socket sock;

	public FileReceiver(String filePath, Socket sock) throws IOException{
		fos = new FileOutputStream(filePath);
		bos = new BufferedOutputStream(fos);
		is = sock.getInputStream();
		this.sock = sock;
	}
	
	public FileOutputStream getFos() {
		return fos;
	}

	public BufferedOutputStream getBos() {
		return bos;
	}

	public InputStream getIs() {
		return is;
	}

	public Socket getSock() {
		return sock;
	}
	
}
