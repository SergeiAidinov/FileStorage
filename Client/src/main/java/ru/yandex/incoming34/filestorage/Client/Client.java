package ru.yandex.incoming34.filestorage.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client implements Runnable {
	private final Socket socket;
	private final DataInputStream in;
	private final DataOutputStream out;
	private final GraphicUserInterface gui;
	// private int iter;

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
		filename = auxiliary.AuxiliaryMethods.handleInputFromTextArea(filename);

		System.out.println("Fetching: " + filename);
		int iter = 0;

		try {
			out.writeUTF("download");
			out.writeUTF(filename);
			Path targetPath = Paths.get("/media/sergei/Linux/ClientFiles" + File.separator + filename);
			System.out.println("targetPath: " + targetPath);
			File targetFile = new File(targetPath.toString());
			targetFile.setWritable(true);
			if (!targetFile.exists()) {
				targetFile.createNewFile();
			}
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().bind(new InetSocketAddress(1237));
			System.out.println();

			SocketChannel sourceChannel = serverSocketChannel.accept();
			SocketChannel auxiliaryChannel = serverSocketChannel.accept();

			System.out.println("sourceChannel: " + sourceChannel);
			FileChannel targetFileChannel = FileChannel.open(targetPath, StandardOpenOption.WRITE,
					StandardOpenOption.APPEND);
			System.out.println("Reading from channel " + sourceChannel + " into " + sourceChannel);
			System.out.println("sourceChannel: " + sourceChannel.isOpen() + " " + sourceChannel.isConnected() + " "
					+ sourceChannel.getRemoteAddress());

			ByteBuffer buffer = ByteBuffer.allocate(256);
			ByteBuffer tempBuffer = ByteBuffer.allocate(256);

			long receivedBytes = 0;
			long appreciatingBytes = in.readLong();
			
			// ===================
			
			auxiliaryChannel.read(tempBuffer);
			tempBuffer.flip();
			byte[] tempByte = tempBuffer.array();
			System.out.println("tempByte: " + Arrays.toString(tempByte));
			long anotherLong = auxiliary.AuxiliaryMethods.convertByteArrayToLong(tempByte);
			System.out.println("anotherLong: " + anotherLong + " receivedBytes: " + receivedBytes);
			// ==========================
			tempBuffer.clear();
			while ((receivedBytes != anotherLong)) {
				buffer.clear();
				sourceChannel.read(buffer);
				iter++;
				buffer.flip();
				receivedBytes += buffer.limit();
				targetFileChannel.write(buffer);
				if (receivedBytes >= anotherLong) {
					break;
				}
				//System.out.println(iter);
			}
			sourceChannel.close();
			serverSocketChannel.close();
			targetFileChannel.close();
			auxiliaryChannel.close();
			buffer = null;
			System.out.println("downloadFile END. Received " + receivedBytes + " bytes in " + iter + " iteration.");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return filename;

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
