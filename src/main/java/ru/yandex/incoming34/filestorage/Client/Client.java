package ru.yandex.incoming34.filestorage.Client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
	private final Socket socket;
	private final DataInputStream in;
	private final DataOutputStream out;
        JTextArea ta;

	public Client() throws IOException {
		new ClientGUI(this);
		socket = new Socket("localhost", 1235);
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		runClient();
	}

	private void runClient() {
		
	/*
		JFrame frame = new JFrame("Cloud Storage");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 300);

		ta = new JTextArea();
		// TODO: 02.03.2021
		// list targetFile - JList
		JButton uploadButton = new JButton("Upload");
                JButton deleteButton = new JButton("Delete");
                JButton downloadButton = new JButton("Download");
                JButton listOfFiles = new JButton("List");
		frame.getContentPane().add(BorderLayout.CENTER, ta);
                Container container = new Container();
                container.setLayout(new FlowLayout());
		container.add(uploadButton);
                container.add(deleteButton);
                container.add(downloadButton);
                container.add(listOfFiles);
                frame.getContentPane().add(BorderLayout.SOUTH, container);
		frame.setVisible(true);
		uploadButton.addActionListener(upl -> {
			System.out.println(sendFile(ta.getText()));
		});
                deleteButton.addActionListener(dlt -> {
                    System.out.println(deleteFile(ta.getText()));
                });
                downloadButton.addActionListener(dnl -> {
                    System.out.println(downloadFile(ta.getText()));
                });
                listOfFiles.addActionListener(lst -> {
                    System.out.println(showListOfFiles());
                });
      */          
	} 

	protected String sendFile(String filename) {
            FileInputStream fis = null;
            if (filename.length() == 0) {
                return("File name needed.");
                
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
        ta.setText(null);
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
                ta.setText(null);
                ta.setText(filesList);
                
                
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        return "Received list of files on server.";
    }
    
}
