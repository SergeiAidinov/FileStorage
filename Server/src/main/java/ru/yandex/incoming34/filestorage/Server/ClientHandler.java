package ru.yandex.incoming34.filestorage.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UTFDataFormatException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import auxiliary.AuxiliaryMethods;

/**
 * Обработчик входящих клиентов
 */
public class ClientHandler /* implements Runnable */ {
	// private final Socket socket;
	private DataOutputStream out;
	private DataInputStream in;
	private ByteChannel writeUtilityChannel;
	private ByteChannel readUtilityChannel;
	InetSocketAddress hostAddress;//
	int port = 1237;
	SocketChannel servedClient;

	public ClientHandler(SocketChannel client) {
		servedClient = client;
		hostAddress = new InetSocketAddress(auxiliary.Constants.hostName, auxiliary.Constants.port);
		// try {
		// writeUtilityChannel = SocketChannel.open(hostAddress);
		// readUtilityChannel = SocketChannel.open(hostAddress);
		// out = new DataOutputStream(server.getOutputStream());
		// in = new DataInputStream(server.getInputStream());
		System.out.println("ClientHandler created!");
		/*
		 * } catch (IOException ex) {
		 * Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
		 * }
		 */
		// run();

	}

	public void handleCommand(String command) {
		if (Objects.isNull(command)) {
			return;
		}
		command = AuxiliaryMethods.leaveOnlyMeaningfullSymbols(command);
		if (command.length() == 0) {
			return;
		}
		System.out.println("In handleCommand: " + command + " " + command.length());
		String statement = command.substring(0, 3);
		String operand = null;
		if (command.length() > 3) {
			operand = command.substring(3, command.length());
		}

		System.out.println(statement + " " + operand);

		switch (statement) {

		case "UPL": {
			performUpload(operand);
			break;
		}
		case "RMV": {
			System.out.println("Received command REMOVE");
			performRemove(operand);
			break;
		}

		case "DNL": {
			try {
				performDownload(operand);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
		case "LST": {
			showListOfFiles();
			break;
		}
		default: {
			System.out.println("Unknown command.");
		}

		}

	}

	private void performUpload(String operand) {

		Path targetPath = Paths.get("/media/sergei/Linux/ServerFiles" + File.separator + operand);
		System.out.println("targetPath: " + targetPath);
		File targetFile = new File(targetPath.toString());
		
		try {
			
		if (!targetFile.exists()) {
				targetFile.createNewFile();
		}
		targetFile.setWritable(true);
		FileChannel targetFileChannel = FileChannel.open(targetPath, StandardOpenOption.WRITE);
		ByteBuffer buffer = ByteBuffer.allocate(256);
		long receivedBytes = 0;
		long anotherLong = AuxiliaryMethods.readLongFromChannel(servedClient);
		System.out.println("Expecting file of " + anotherLong + " bytes.");
		while ((true)) {
			servedClient.read(buffer);
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
	}
		
	

	private void performRemove(String filename) { // working
		if (Objects.isNull(filename)) {
			return;
		}
		filename = AuxiliaryMethods.handleInputFromTextArea(filename);
		System.out.println("performRemove()");
		File file = new File("/media/sergei/Linux/ServerFiles" + File.separator + filename);
		if (Objects.isNull(file) || file.getName().length() == 0) {
			instatntWriningIntoStream("There is no such a file");
			return;
		}
		if (file.exists()) {
			boolean fileDeleted = file.delete();
			if (fileDeleted) {
				instatntWriningIntoStream("File " + file.getName() + " was deleted.");
			} else {
				instatntWriningIntoStream("File " + file.getName() + " was NOT deleted.");
			}
		} else {
			instatntWriningIntoStream("File does not exist.");
		}
	}

	private void performDownload(String operand) throws IOException {
		System.out.println("performDownload() BEGIN with operand " + operand);
		File sourceFile = new File("/media/sergei/Linux/ServerFiles" + File.separator + operand);
		System.out.println(sourceFile);
		Path sourcePath = Paths.get("/media/sergei/Linux/ServerFiles" + File.separator + sourceFile.getName());
		System.out.println("sourcePath: " + sourcePath);
		FileSystem fileSystem = FileSystems.getDefault();
		System.out.println(fileSystem);
		FileChannel sourceChannel = FileChannel.open(sourcePath);
		System.out.println("sourceChannel: " + sourceChannel);

		// ByteChannel destinationChannel = SocketChannel.open(hostAddress);
		// ByteChannel auxiliaryChannel = SocketChannel.open(hostAddress);
		ByteBuffer buffer = ByteBuffer.allocate(256);
		long sizeOfsourceFile = sourceFile.length();
		System.out.println("Transmitting file " + sourceFile + " of " + sizeOfsourceFile + " bytes " + "from "
				+ sourceChannel + " to " + servedClient);

		buffer.flip();
		long transmittedBytes = 0;

		auxiliary.AuxiliaryMethods.writeLongToChannel(sizeOfsourceFile, servedClient);

		while (transmittedBytes < sizeOfsourceFile) {
			sourceChannel.read(buffer);
			buffer.flip();
			transmittedBytes += buffer.limit();
			servedClient.write(buffer);
			buffer.clear();

			/*
			 * long response = AuxiliaryMethods.readLongFromChannel(servedClient);
			 * System.out.println("RecievedBytes: " + response); while (true) { if
			 * (transmittedBytes > response) { continue; } }
			 */

		}

		sourceChannel.close();
		// destinationChannel.close();
		// auxiliaryChannel.close();
		System.out.println("performDownload() FINISHED. Transmitted " + transmittedBytes + " bytes.");
	}

	private long calculateQuantityOfBuffers(File oneFile, ByteBuffer oneBuffer) {
		long qtyBuffers = oneFile.length() / oneBuffer.capacity();
		if (qtyBuffers * oneBuffer.capacity() < oneFile.length()) {
			qtyBuffers++;
		}
		return qtyBuffers;
	}

	private void showListOfFiles() {
		File dir = new File("/media/sergei/Linux/ServerFiles/");
		File[] allFiles = dir.listFiles();
		StringBuffer listOfFiles = new StringBuffer();
		for (int i = 0; i < allFiles.length; i++) {
			String oneFile = allFiles[i].getName();
			listOfFiles.append(oneFile).append("\n");
			System.out.println(oneFile);
		}
		// instatntWriningIntoStream(listOfFiles.toString());
		String list = listOfFiles.toString();
		// list = AuxiliaryMethods.leaveOnlyMeaningfullSymbols(list);
		ByteBuffer byteBuffer = AuxiliaryMethods.convertStringToByteBuffer(list);
		byteBuffer.limit(list.length());
		try {
			servedClient.write(byteBuffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void instatntWriningIntoStream(String message) {
		System.out.println(message);
		ByteBuffer buffer = auxiliary.AuxiliaryMethods.convertStringToByteBuffer(message);

		// out.writeUTF(message);
		// out.flush();

	}

	/*
	 * @Override public void run() { // TODO Auto-generated method stub
	 * 
	 * }
	 */
}
