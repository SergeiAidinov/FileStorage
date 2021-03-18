package ru.yandex.incoming34.filestorage.Server;

import java.awt.List;
import java.io.*;
import static java.lang.String.valueOf;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Обработчик входящих клиентов
 */
public class ClientHandler implements Runnable {
	private final Socket socket;
	private DataOutputStream out;
	private DataInputStream in;

	public ClientHandler(Socket socket) {
		this.socket = socket;
		try {
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
		} catch (IOException ex) {
			Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	@Override
	public void run() {
		String command;
		try {
			while (true) {

				try {
					command = in.readUTF();
				} catch (EOFException eofEx) {
					continue;
				}

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
			File file = new File("/media/sergei/Linux/ServerFiles" + File.separator + in.readUTF());
			file = file.getCanonicalFile();
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

			//out.writeUTF("DONE");
		} catch (Exception e) {

		}
	}

	private void performRemove() {
		File file;
		try {
			file = new File("/media/sergei/Linux/ServerFiles" + File.separator + in.readUTF());
			if (Objects.isNull(file) || file.getName().length() == 0) {
				return;
			}
			if (file.exists()) {
				boolean fileDeleted = file.delete();
				if (fileDeleted) {
					out.writeUTF("File " + file + " was deleted.");
				} else {
					out.writeUTF("File " + file + " was NOT deleted.");
				}
			} else {
				out.writeUTF("File does not exist.");
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		catch (IOException ex) {
			Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void performDownload() {
		File file;
		FileInputStream fis = null;
		try {
			file = new File("/media/sergei/Linux/ServerFiles" + File.separator + in.readUTF());
			if (Objects.isNull(file) || file.getName().length() == 0) {
				out.writeLong(0);
				return;
			}
			if (file.exists()) {
				fis = new FileInputStream(file);
				long length = file.length();
				out.writeLong(length);
				int read = 0;
				byte[] buffer = new byte[256];
				while ((read = fis.read(buffer)) != -1) {
					out.write(buffer, 0, read);
				}
				out.flush();
				String status = in.readUTF();
				out.writeUTF("download - Server" + file);
				fis.close();

			} else {
				out.writeLong(0);
				System.out.println("File does not exist");
				fis.close();
			}
		} catch (NullPointerException nlex) {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException ex) {
					Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			nlex.printStackTrace();
		}

		catch (IOException ex) {
			Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void showListOfFiles() {
		try {
			File dir = new File("/media/sergei/Linux/ServerFiles/");
			File[] allFiles = dir.listFiles();
			StringBuffer listOfFiles = new StringBuffer();
			for (int i = 0; i < allFiles.length; i++) {
				String oneFile = allFiles[i].getName();
				listOfFiles.append(oneFile).append("\n");
				System.out.println(oneFile);
			}
			out.writeUTF(listOfFiles.toString());
		} catch (IOException ex) {
			Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
		}

	}
}
