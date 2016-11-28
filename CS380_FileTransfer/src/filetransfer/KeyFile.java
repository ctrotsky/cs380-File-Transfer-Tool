package filetransfer;


	

	import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
	import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
	import java.io.IOException;
	import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

	//wrapper class for streams associated with receiving files
	public class KeyFile implements Closeable {
		private File file;
		private FileInputStream fis;
		private BufferedInputStream bis;
		private OutputStream os;
		private Socket sock;

		public KeyFile(String filePath) throws IOException{
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