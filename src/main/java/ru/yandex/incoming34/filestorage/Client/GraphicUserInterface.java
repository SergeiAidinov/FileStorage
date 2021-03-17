package ru.yandex.incoming34.filestorage.Client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import ru.yandex.incoming34.filestorage.Client.Client;

public class GraphicUserInterface {
	
	
	JFrame frame = new JFrame("Cloud Storage");
	Client myClient = null;
	JTextArea ta;
	
	public GraphicUserInterface(Client myClient) {
		super();
		this.myClient = myClient;
	}

	public void runGUI() {
		
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
		System.out.println(myClient.sendFile(ta.getText()));
	});
            deleteButton.addActionListener(dlt -> {
                System.out.println(myClient.deleteFile(ta.getText()));
            });
            downloadButton.addActionListener(dnl -> {
                System.out.println(myClient.downloadFile(ta.getText()));
            });
            listOfFiles.addActionListener(lst -> {
                System.out.println(myClient.showListOfFiles());
            });
            
}
}

