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
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ru.yandex.incoming34.filestorage.Client.GraphicUserInterface;



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

		filename.trim();
		System.out.println((byte) filename.charAt(filename.length() - 1));
		Pattern pattern = Pattern.compile("\n");
		Matcher matcher = pattern.matcher(filename);
		filename = matcher.replaceAll("");
		System.out.println((byte) filename.charAt(filename.length() - 1));

		System.out.println("Fetching: " + filename);
		int iter = 0;

		long receivedBytes = 0;
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

			System.out.println("sourceChannel: " + sourceChannel);
			FileChannel targetFileChannel = FileChannel.open(targetPath, StandardOpenOption.WRITE,
					StandardOpenOption.APPEND);
			System.out.println("Reading from channel " + sourceChannel + " into " + sourceChannel);
			System.out.println("sourceChannel: " + sourceChannel.isOpen() + " " + sourceChannel.isConnected() + " "
					+ sourceChannel.getRemoteAddress());

			ByteBuffer buffer = ByteBuffer.allocate(256);

			int lastByte = 0;
			long qtyBuffers = in.readLong();
			for (long i = 0; i < qtyBuffers + 1; i++) {
				buffer.clear();
				sourceChannel.read(buffer);
				iter++;
				buffer.flip();
				receivedBytes += buffer.limit();
				targetFileChannel.write(buffer);
			}

			serverSocketChannel.close();
			targetFileChannel.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("downloadFile END. Received " + receivedBytes + " bytes in " + iter + " iteration.");

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
