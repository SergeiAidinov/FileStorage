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

/**
 * Обработчик входящих клиентов
 */
public class ClientHandler implements Runnable {
	//private final Socket socket;
	private DataOutputStream out;
	private DataInputStream in;
	private ByteChannel writeUtilityChannel;
	private ByteChannel readUtilityChannel;
	InetSocketAddress hostAddress;// 
	int port = 1237;
	

	public ClientHandler(ServerSocketChannel server) {
		//this.socket = server;
		hostAddress = new InetSocketAddress(auxiliary.Constants.hostName, auxiliary.Constants.port);
		try {
			writeUtilityChannel = SocketChannel.open(hostAddress);
			readUtilityChannel = SocketChannel.open(hostAddress);
			//out = new DataOutputStream(server.getOutputStream());
			//in = new DataInputStream(server.getInputStream());
			System.out.println("ClientHandler created!");
		} catch (IOException ex) {
			Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	@Override
	public void run() {
		String command;
		try {
			while (true) {

				//try {
					//command = in.readUTF();
					command = auxiliary.AuxiliaryMethods.readStringFromChannel(readUtilityChannel);
					System.out.println("command: " + command);
				/*}  catch (EOFException eofEx) {
					continue;
				} */

				switch (command) {
				case "upload": {
					performUpload();
					break;
				}
				case "remove": {
					performRemove();
					break;
				}

				case "download": {
					performDownload();
					break;
				}
				case "listOfFiles": {
					showListOfFiles();
					break;
				}

				}
			}
		}

		catch (IOException ex) {
			Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void performUpload() {

		FileOutputStream fos = null;
		try {
			File fileFromStream = new File("/media/sergei/Linux/ServerFiles" + File.separator + in.readUTF());
			File file = new File("/media/sergei/Linux/ServerFiles" + File.separator + fileFromStream.getName());
			System.out.println("Uploaning file: " + file);
			if (!file.exists()) {
				file.createNewFile();
			}
			long size = in.readLong();
			fos = new FileOutputStream(file);
			byte[] buffer = new byte[256];
			for (int i = 0; i < (size + 255) / 256; i++) {
				int read = in.read(buffer);
				fos.write(buffer, 0, read);
			}
			fos.close();
			instatntWriningIntoStream(file.getName() + " succesfully uploaded to server.");
		} catch (Exception e) {

		}
	}

	private void performRemove() {
		try {
			File file = new File("/media/sergei/Linux/ServerFiles" + File.separator + in.readUTF());
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

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		catch (IOException ex) {
			Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void performDownload() throws IOException {
		System.out.println("performDownload() BEGIN");
		File sourceFile = new File("/media/sergei/Linux/ServerFiles" + File.separator + in.readUTF());
		System.out.println(sourceFile);
		Path sourcePath = Paths.get("/media/sergei/Linux/ServerFiles" + File.separator + sourceFile.getName());
		System.out.println("sourcePath: " + sourcePath);
		FileSystem fileSystem = FileSystems.getDefault();
		System.out.println(fileSystem);
		FileChannel outputChannel = FileChannel.open(sourcePath);
		System.out.println("outputChannel: " + outputChannel);
		
		
		ByteChannel destinationChannel = SocketChannel.open(hostAddress);
		ByteChannel auxiliaryChannel = SocketChannel.open(hostAddress);
		ByteBuffer buffer = ByteBuffer.allocate(256);
		long sizeOfsourceFile = sourceFile.length();
		System.out.println("Transmitting file " + sourceFile + " of " + sizeOfsourceFile + " bytes " + "from "
				+ outputChannel + " to " + destinationChannel);

		buffer.flip();
		long transmittedBytes = 0;
		long bytesToTransmit = outputChannel.size();
		auxiliary.AuxiliaryMethods.writeLongToChannel(bytesToTransmit, auxiliaryChannel);
		while (transmittedBytes < bytesToTransmit) {
			outputChannel.read(buffer);
			buffer.flip();
			transmittedBytes += buffer.limit();
			destinationChannel.write(buffer);
			buffer.clear();
		}
		outputChannel.close();
		destinationChannel.close();
		auxiliaryChannel.close();
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
		instatntWriningIntoStream(listOfFiles.toString());

	}

	private void instatntWriningIntoStream(String message) {
		try {
			out.writeUTF(message);
			out.flush();
		} catch (IOException e) {
			Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, e);
		}

	}
}