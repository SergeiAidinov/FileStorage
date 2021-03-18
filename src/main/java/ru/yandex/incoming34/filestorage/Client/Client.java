package ru.yandex.incoming34.filestorage.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client implements Runnable {
	private final Socket socket;
	private final DataInputStream in;
	private final DataOutputStream out;
	private final GraphicUserInterface gui;
	// JTextArea textArea;

	public Client() throws IOException {
		gui = new GraphicUserInterface(this);
		socket = new Socket("localhost", 1235);
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		// runClient();
	}

	private void runClient() {
		run();

	}

	protected String sendFile(String filename) {
		System.out.println("Sending file: " + filename);
		FileInputStream fis = null;
		if (filename.length() == 0) {
			return ("File name needed.");

		}
		try {
			File file = new File("/media/sergei/Linux/ClientFiles/" + File.separator + filename);
			if (file.exists()) {
				out.writeUTF("upload");
				out.writeUTF(filename);
				long length = file.length();
				out.writeLong(length);
				fis = new FileInputStream(file);
				int read = 0;
				byte[] buffer = new byte[256];
				while ((read = fis.read(buffer)) != -1) {
					out.write(buffer, 0, read);
				}
				out.flush();
				String status = in.readUTF();
				fis.close();
				return status;
			} else {
				return "File " + file + " does not exists";

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Something error";
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

		} catch (IOException ex) {
			ex.printStackTrace();

		}
		return status;
	}

	protected String downloadFile(String filename) {
		gui.textArea.setText(null);
		Path targetPath = Paths.get("/media/sergei/Linux/ClientFiles/" + File.separator + filename);
		Path sourcePath = Paths.get("/media/sergei/Linux/ServerFiles/" + File.separator + filename);
		try (OutputStream outputStream = new FileOutputStream(targetPath.toFile())) {
			Files.copy(sourcePath, outputStream);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
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
			gui.textArea.setText(null);
			gui.textArea.setText(filesList);

		} catch (IOException ex) {
			Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
		}
		return "Received list of files on server.";
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	public String chooseAndSendFile() {
		gui.openFileChooser();
		return "Chooser";
	}

}
