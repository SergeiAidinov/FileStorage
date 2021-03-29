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
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
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
	private ByteChannel writeUtilityChannel;
	private ByteChannel readUtilityChannel;
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
		System.out.println("String: " + AuxiliaryMethods.readStringFromByteBuffer(buffer));
	}

	private void runClient() {
		run();

	}

	protected String sendFile(File sourceFile) {
		System.out.println(sourceFile);
		System.out.println("Sending file: " + sourceFile);
		try {
			long sizeOfsourceFile = sourceFile.length();
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

			buffer.flip();
			long transmittedBytes = 0;

			while (transmittedBytes < sizeOfsourceFile) {
				sourceChannel.read(buffer);
				buffer.flip();
				client.write(buffer);
				transmittedBytes += buffer.limit();
				buffer.clear();
			}
			sourceChannel.close();
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
				buffer.clear();
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
			ByteBuffer byteBuffer = ByteBuffer.allocate(256);
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

		File file = gui.openFileChooser();

		if (Objects.isNull(file)) {
			return "There is no such a file!";
		}
		sendFile(file);
		return "Choosen  file: " + file;
		// }

	}

}
