package ru.yandex.incoming34.filestorage.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import auxiliary.AuxiliaryMethods;

public class Client implements Runnable {
	// private final SocketChannel socket;
	// private final DataInputStream in;
	// private final DataOutputStream out;
	private ByteChannel writeUtilityChannel;
	private ByteChannel readUtilityChannel;
	InetSocketAddress hostAddress;
	private final GraphicUserInterface gui;
	SocketChannel client = null;

	public Client() throws IOException {
		hostAddress = new InetSocketAddress(auxiliary.Constants.hostName, auxiliary.Constants.port);
		gui = new GraphicUserInterface(this);
		// socket = new Socket("localhost", 1237);
		client = SocketChannel.open();
		client.connect(hostAddress);

		/*
		 * in = new DataInputStream(socket.getInputStream()); out = new
		 * DataOutputStream(socket.getOutputStream()); SocketChannel sc =
		 * SocketChannel.open(); InetSocketAddress addr = new
		 * InetSocketAddress("localhost", port); sc.connect(addr);
		 */
		writeUtilityChannel = client.open();

		readUtilityChannel = client.open();
		long lg = (long) (Math.random() * 10000000);
		// auxiliary.AuxiliaryMethods.writeLongToChannel(lg, client);
		// auxiliary.AuxiliaryMethods.writeStringToChannel("Hello, developer", client);
		// client.write(auxiliary.AuxiliaryMethods.convertStringToByteBuffer("Hello
		// developer!"));
		// client.write(auxiliary.AuxiliaryMethods.convertStringToByteBuffer("Hello lazy
		// developer!"));
		ByteBuffer buffer = ByteBuffer.allocate(256);
		client.read(buffer);
		buffer.flip();
		System.out.println("String: " + AuxiliaryMethods.readStringFromByteBuffer(buffer));
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
				long length = file.length();
				fis = new FileInputStream(file);
				int read = 0;
				byte[] buffer = new byte[256];
				while ((read = fis.read(buffer)) != -1) {
					System.out.print('.');
				}
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
			client.write(auxiliary.AuxiliaryMethods.convertStringToByteBuffer("RMV" + filename));
			gui.informUser(status);
		} catch (IOException ex) {
			ex.printStackTrace();

		}
		return status;
	}

	protected String downloadFile(String filename) {
		System.out.println("downloadFile BEGIN");
		filename = auxiliary.AuxiliaryMethods.handleInputFromTextArea(filename);
		try {
			client.write(auxiliary.AuxiliaryMethods.convertStringToByteBuffer("DNL" + filename));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Fetching: " + filename);
		try {
			Path targetPath = Paths.get("/media/sergei/Linux/ClientFiles" + File.separator + filename);
			System.out.println("targetPath: " + targetPath);
			File targetFile = new File(targetPath.toString());
			targetFile.setWritable(true);
			if (!targetFile.exists()) {
				targetFile.createNewFile();
			}
			FileChannel targetFileChannel = FileChannel.open(targetPath, StandardOpenOption.WRITE);
			ByteBuffer buffer = ByteBuffer.allocate(256);
			long receivedBytes = 0;
			long anotherLong = AuxiliaryMethods.readLongFromChannel(client);
			System.out.println("Expecting file of " + anotherLong + " bytes.");
			while ((true)) {
				client.read(buffer);
				buffer.flip(); 
				receivedBytes += buffer.limit();

				targetFileChannel.write(buffer);

				if (receivedBytes >= anotherLong) {
					break;
				}

			}

			targetFileChannel.close();
			System.out.println("downloadFile END. Received " + receivedBytes + " bytes.");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return filename;

	}

	protected String showListOfFiles() {

		try {
			client.write(auxiliary.AuxiliaryMethods.convertStringToByteBuffer("LST"));
			ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
			client.read(byteBuffer);
			byteBuffer.flip();
			gui.informUser(auxiliary.AuxiliaryMethods.readStringFromByteBuffer(byteBuffer));
			System.out.println(AuxiliaryMethods.readStringFromByteBuffer(byteBuffer));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Received list of files on server.";
	}

	public void run() {

	}

	public String chooseAndSendFile() {
		/*
		 * File currentFile = gui.openFileChooser(); if (!Objects.isNull(currentFile)) {
		 * sendFile(currentFile); try { String serverAnawer = in.readUTF();
		 * gui.informUser(serverAnawer); } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); } return "Choosen file: " +
		 * currentFile; } else {
		 */
		return "File was not selected.";
		// }

	}

}
