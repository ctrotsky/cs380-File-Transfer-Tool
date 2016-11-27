package filetransfer;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

//Wrapper class for objects needed for sending files
public class FileSender implements Closeable{
	private File file;
	private FileInputStream fis;
	private BufferedInputStream bis;
	private OutputStream os;
	private Socket sock;

	public FileSender(String filePath, Socket sock) throws IOException{
		file = new File(filePath);
		fis = new FileInputStream(file);
		bis = new BufferedInputStream(fis);
		os = sock.getOutputStream();
	}

	public File getFile() {
		return file;
	}

	public FileInputStream getFis() {
		return fis;
	}

	public BufferedInputStream getBis() {
		return bis;
	}

	public OutputStream getOs() {
		return os;
	}

	@Override
	public void close() throws IOException {
		fis.close();
		bis.close();
		os.close();
	}
	
	
}
