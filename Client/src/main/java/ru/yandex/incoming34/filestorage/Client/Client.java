package ru.yandex.incoming34.filestorage.Client;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import com.sun.security.ntlm.Server;

import auxiliary.AuxiliaryMethods;

//import auxiliary.AuxiliaryMethods;

public class Client implements Runnable {
	InetSocketAddress hostAddress;
	private final GraphicUserInterface gui;
	SocketChannel client = null;

	public Client() throws IOException {
		hostAddress = new InetSocketAddress(auxiliary.Constants.hostName, auxiliary.Constants.port);
		gui = new GraphicUserInterface(this);
		client = SocketChannel.open();
		client.connect(hostAddress);
		ByteBuffer buffer = ByteBuffer.allocate(256);
		client.read(buffer);
		buffer.flip();
		System.out.println("You have connected to server" + Server.class);
	}

	protected String sendFileToServer(File sourceFile) {
		System.out.println(sourceFile);
		System.out.println("Sending file: " + sourceFile);
		try {
			long sizeOfsourceFile = sourceFile.length();
			System.out.println("sizeOfsourceFile: " + sizeOfsourceFile);
			client.write(auxiliary.AuxiliaryMethods.convertStringToByteBuffer("UPL" + sourceFile.getName()));
			auxiliary.AuxiliaryMethods.writeLongToChannel(sizeOfsourceFile, client);
			Path sourcePath = Paths.get(sourceFile.getAbsolutePath());
			System.out.println("sourcePath: " + sourcePath);
			FileSystem fileSystem = FileSystems.getDefault();
			System.out.println(fileSystem);
			FileChannel sourceChannel = FileChannel.open(sourcePath);
			System.out.println("sourceChannel: " + sourceChannel);
			ByteBuffer buffer = ByteBuffer.allocate(256);
			System.out.println("Uploading file " + sourceFile + " of " + sizeOfsourceFile + " bytes " + "from "
					+ sourceChannel + " to " + client);

			buffer.clear();
			long transmittedBytes = 0;

			while (transmittedBytes < sizeOfsourceFile) {
				sourceChannel.read(buffer);
				buffer.flip();
				client.write(buffer);
				transmittedBytes += buffer.limit();
				buffer.clear();
			}
			sourceChannel.close();
			buffer.clear();
			System.out.println("FINISHED. Transmitted " + transmittedBytes + " bytes.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
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

	protected String receiveFileFromServer(String filename) {
		System.out.println("downloadFile BEGIN");
		filename = auxiliary.AuxiliaryMethods.handleInputFromTextArea(filename);
		try {
			client.write(auxiliary.AuxiliaryMethods.convertStringToByteBuffer("DNL" + filename));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Fetching: " + filename + " " + filename.length());
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
			long expectedLengthOfFile = auxiliary.AuxiliaryMethods.readLongFromChannel(client);
			System.out.println("Expecting file of " + expectedLengthOfFile + " bytes.");
			ByteBuffer auxiliaryBuffer = ByteBuffer.allocate(3);
			auxiliaryBuffer = auxiliary.AuxiliaryMethods.convertStringToByteBuffer("SNB");
			while (true) {

				 
					auxiliaryBuffer = auxiliary.AuxiliaryMethods.convertStringToByteBuffer("SNB");
					client.write(auxiliaryBuffer);
					System.out.println("SNB");

				

				buffer.clear();
				client.read(buffer);
				// buffer.compact();
				if (expectedLengthOfFile - receivedBytes < 256) {
					buffer.limit((int) (expectedLengthOfFile - receivedBytes));
				}
				buffer.flip();
				ByteBuffer tempBuffer = buffer;
				//tempBuffer.flip();
				tempBuffer.compact();
				if (tempBuffer.limit() == buffer.limit()) {
					receivedBytes += buffer.limit();
					targetFileChannel.write(buffer);
				}

				//buffer.clear();
				System.out.println("Received " + receivedBytes + " bytes.");
				if (receivedBytes >= expectedLengthOfFile) {
					System.out.println("BREAK");
					//auxiliaryBuffer.clear();
					break;
				}
			}
			auxiliaryBuffer.clear();
			buffer.clear();
			targetFileChannel.close();
			System.out.println("FILE DOWNLOADED. Received " + receivedBytes + " bytes.");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return filename;

	}

	protected String showListOfFiles() {
		ByteBuffer byteBuffer = ByteBuffer.allocate(256);
		try {
			client.write(auxiliary.AuxiliaryMethods.convertStringToByteBuffer("LST"));

			client.read(byteBuffer);
			byteBuffer.flip();
			gui.informUser(auxiliary.AuxiliaryMethods.readStringFromByteBuffer(byteBuffer));
			System.out.println(auxiliary.AuxiliaryMethods.readStringFromByteBuffer(byteBuffer));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byteBuffer.clear();
		return "Received list of files on server.";
	}

	public void run() {

	}

	public String chooseAndSendFile() {

		File file = gui.openFileChooser();

		if (Objects.isNull(file)) {
			return "There is no such a file!";
		}
		sendFileToServer(file);
		return "Choosen  file: " + file;
		// }

	}

}
