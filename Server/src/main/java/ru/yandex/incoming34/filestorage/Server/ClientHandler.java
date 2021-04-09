package ru.yandex.incoming34.filestorage.Server;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import auxiliary.AuxiliaryMethods;

/**
 * Обработчик входящих клиентов
 */
public class ClientHandler {
	InetSocketAddress hostAddress;//
	int port = 1237;
	SocketChannel servedClient;
	private boolean busy = false;
	ServerSocket auxiliaryServerSocket = null;

	public ClientHandler(SocketChannel client) {

		servedClient = client;
		hostAddress = new InetSocketAddress(auxiliary.Constants.hostName, auxiliary.Constants.port);
		try {
			servedClient.configureBlocking(false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("ClientHandler created!");
	}

	public boolean getBusy() {
		return busy;
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
			System.out.println("Received command UPLOAD");
			busy = true;
			receiveFileFromClient(operand);
			break;
		}
		case "RMV": {
			System.out.println("Received command REMOVE");
			busy = true;
			performRemove(operand);
			break;
		}
		case "DNL": {
			busy = true;
			sendFileToClient(operand);
			break;
		}
		case "LST": {
			busy = true;
			showClientListOfFiles();
			break;
		}
		default: {
			System.out.println("Unknown command.");
		}
		}
	}

	private void receiveFileFromClient(String operand) {
		ByteBuffer buffer = ByteBuffer.allocate(256);
		long lengthOfExpectedFile = 0;
		while (lengthOfExpectedFile == 0) {
			lengthOfExpectedFile = AuxiliaryMethods.readLongFromChannel(servedClient);
		}
		Path targetPath = Paths.get("/media/sergei/Linux/ServerFiles" + File.separator + operand);
		System.out.println("targetPath: " + targetPath);
		File targetFile = new File(targetPath.toString());

		try {

			if (!targetFile.exists()) {
				targetFile.createNewFile();
			}
			targetFile.setWritable(true);
			FileChannel targetFileChannel = FileChannel.open(targetPath, StandardOpenOption.WRITE);
			long receivedBytes = 0;

			System.out.println("Expecting file of " + lengthOfExpectedFile + " bytes.");
			while ((true)) {
				buffer.clear();
				servedClient.read(buffer);
				buffer.flip();
				receivedBytes += buffer.limit();
				targetFileChannel.write(buffer);

				if (receivedBytes >= lengthOfExpectedFile) {
					break;
				}
			}
			targetFileChannel.close();
			System.out.println("downloadFile END. Received " + receivedBytes + " bytes.");
			busy = false;
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
		busy = false;
	}

	private void sendFileToClient(String operand)  {
		System.out.println("performDownload() BEGIN with operand " + operand);
		File sourceFile = new File("/media/sergei/Linux/ServerFiles" + File.separator + operand);
		System.out.println("sourceFile: " + sourceFile);
		Path sourcePath = Paths.get("/media/sergei/Linux/ServerFiles" + File.separator + sourceFile.getName());
		System.out.println("sourcePath: " + sourcePath);
		// FileSystem fileSystem = FileSystems.getDefault();
		// System.out.println(fileSystem);
		FileChannel sourceChannel;
		try {
			sourceChannel = FileChannel.open(sourcePath);
		
		System.out.println("sourceChannel: " + sourceChannel);
		ByteBuffer buffer = ByteBuffer.allocate(256);
		//ByteBuffer auxiliaryBuffer = ByteBuffer.allocate(128);
		long sizeOfsourceFile = sourceFile.length();
		//AuxiliaryMethods.writeLongToChannel(sizeOfsourceFile, servedClient);
		//auxiliaryBuffer.putLong(sizeOfsourceFile);
		//servedClient.write(auxiliaryBuffer);
		System.out.println("Transmitting file " + sourceFile + " of " + sizeOfsourceFile + " bytes " + "from "
				+ sourceChannel + " to " + servedClient);

		long transmittedBytes = 0;

		auxiliary.AuxiliaryMethods.writeLongToChannel(sizeOfsourceFile, servedClient);
		
		String response = null;
		
		while (true) {
			
			buffer.clear();
			
			if(sizeOfsourceFile - transmittedBytes < 256) {
				buffer = ByteBuffer.allocate((int)(sizeOfsourceFile - transmittedBytes));
			}
			
			servedClient.write(buffer);
			transmittedBytes += buffer.limit();
			System.out.println("Transmitting buffer of " + buffer.limit() + " bytes.");
			//buffer.rewind();
			//buffer.clear();
			System.out.println("Transmitted: " + transmittedBytes + " bytes.");
			if (transmittedBytes > sizeOfsourceFile ) {
				break;
			}
			

		}
		
		
		buffer.clear();
		sourceChannel.close();
		
		System.out.println("performDownload() FINISHED. Transmitted " + transmittedBytes + " bytes.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void informClientOfBufferSize(ByteBuffer buffer) {
		String info = "BFO" + buffer.limit();
		System.out.println("info: " + info);
		ByteBuffer auxiliaryBuffer = auxiliary.AuxiliaryMethods.convertStringToByteBuffer(info);
		try {
			servedClient.write(auxiliaryBuffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		auxiliaryBuffer.clear();
		
		
		
	}

	private void awaitForRequest() {
		String response = null;
		while (true) {
			ByteBuffer auxiliaryBuffer = ByteBuffer.allocate(128);
			auxiliaryBuffer.clear();
			try {
				servedClient.read(auxiliaryBuffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			auxiliaryBuffer.flip();
			auxiliaryBuffer.rewind();
			response = AuxiliaryMethods.readStringFromByteBuffer(auxiliaryBuffer);
			if (response.equals("SNB")) {
				System.out.println(response);
				break;
			}
		}
		
	}

	private long calculateQuantityOfBuffers(File oneFile, ByteBuffer oneBuffer) {
		long qtyBuffers = oneFile.length() / oneBuffer.capacity();
		if (qtyBuffers * oneBuffer.capacity() < oneFile.length()) {
			qtyBuffers++;
		}
		return qtyBuffers;
	}

	private void showClientListOfFiles() {
		File dir = new File("/media/sergei/Linux/ServerFiles/");
		File[] allFiles = dir.listFiles();
		StringBuffer listOfFiles = new StringBuffer();
		for (int i = 0; i < allFiles.length; i++) {
			String oneFile = allFiles[i].getName();
			listOfFiles.append(oneFile).append("\n");
			System.out.println(oneFile);
		}
		String list = listOfFiles.toString();
		ByteBuffer byteBuffer = AuxiliaryMethods.convertStringToByteBuffer(list);
		byteBuffer.limit(list.length());
		try {
			servedClient.write(byteBuffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		busy = false;
	}

	private void instatntWriningIntoStream(String message) {
		System.out.println(message);
		ByteBuffer buffer = auxiliary.AuxiliaryMethods.convertStringToByteBuffer(message);
	}
}
