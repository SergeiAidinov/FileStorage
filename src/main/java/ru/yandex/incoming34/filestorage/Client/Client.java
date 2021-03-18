package ru.yandex.incoming34.filestorage.Client;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client implements Runnable {
	private final Socket socket;
	private final DataInputStream in;
	private final DataOutputStream out;
	private final GraphicUserInterface gui;

	public Client() throws IOException {
		gui = new GraphicUserInterface(this);
		socket = new Socket("localhost", 1235);
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
	}

	private void runClient() {
		run();

	}

	protected String sendFile(File filename) {
		System.out.println("Sending file: " + filename);
		FileInputStream fis = null;
		if (filename.length() == 0) {
			return ("File name needed.");

		}
		try {
			File file = filename;
			if (file.exists()) {
				System.out.println("Transmitting file: " + filename.getName());
				out.writeUTF("upload");
				out.writeUTF(filename.toString());
				long length = file.length();
				out.writeLong(length);
				fis = new FileInputStream(file);
				int read = 0;
				byte[] buffer = new byte[256];
				while ((read = fis.read(buffer)) != -1) {
					out.write(buffer, 0, read);
					System.out.print('.');
				}
				out.flush();
				String status = "String";
				fis.close();
				System.out.println("File transmitted.");
				return status;
			} else {
				return "File " + file + " does not exists";

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Some error";
	}

	public static void main(String[] args) throws IOException {

		new Client();
	}

	protected String deleteFile(String filename) {
		String status = null;
		try {
			out.writeUTF("remove");
			out.writeUTF(filename);
			status = in.readUTF();
			gui.informUser(status);
		} catch (IOException ex) {
			ex.printStackTrace();

		}
		return status;
	}

	protected String downloadFile(String filename) {
		System.out.println("downloadFile BEGIN");
		InetSocketAddress serverAddress = new InetSocketAddress("localhost", 1235);
        try (SocketChannel socketChannel = SocketChannel.open(serverAddress)) {

            RandomAccessFile file = new RandomAccessFile("/media/sergei/Linux/ClientFiles" + 
            File.separator + filename, "rw");
            FileChannel channel = file.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(256);

            int bytesRead = channel.read(buffer);
            while (bytesRead > -1) {
            	System.out.print('.');
                buffer.flip();
                while (buffer.hasRemaining()) { 
                    socketChannel.write(buffer);
                }
                
                bytesRead = channel.read(buffer);
                file.write(bytesRead);
                buffer.clear();
            }
            
            
            file.close();
        }
			//position += channel.transferFrom(channel, position, size);
 catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			//fileWriter.transfer(channel, size);
			
			
			//fileWriter.transfer(channel, size);
			
			
			//fileWriter.transfer(channel, size);
			//fileWriter.close();
			/*
			 * if (!file.exists()) { file.createNewFile();
			 * System.out.println("Created file: " + file); }
			 */
			// byte[] buffer = new byte[256];
			// int read = 0;
			/*
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			BufferedWriter writer = Files.newBufferedWriter(targetPath, Charset.forName("UTF-8"));
			
			 * long size = in.readLong(); for (int i = 0; i < (size + 255) / 256; i++) {
			 * read = in.read(buffer);
			 * 
			 * }
			 
			OutputStream os = new FileOutputStream(sourcePath.toFile());
			Files.copy(targetPath, os);
			// Files.write(targetPath, sourcePath);
			fileOutputStream.flush();
			fileOutputStream.close();
			*/
			System.out.println("downloadFile FINISHED");

		

		gui.informUser("DONE" /* filename + " succesfully downloaded to client's" */);
		return "Downloaded file " + filename;
	}

	protected String showListOfFiles() {

		try {

			try {
				out.writeUTF("listOfFiles");
			} catch (IOException ex) {
				Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
			}
			String filesList = in.readUTF();
			gui.informUser(null);
			gui.informUser(filesList);

		} catch (IOException ex) {
			Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
		}
		return "Received list of files on server.";
	}

	@Override
	public void run() {

	}

	public String chooseAndSendFile() {
		File currentFile = gui.openFileChooser();
		if (!Objects.isNull(currentFile)) {
			sendFile(currentFile);
			try {
				String serverAnawer = in.readUTF();
				gui.informUser(serverAnawer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "Choosen file: " + currentFile;
		} else {
			return "File was not selected.";
		}

	}

}
