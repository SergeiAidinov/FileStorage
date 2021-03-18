package ru.yandex.incoming34.filestorage.Client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextArea;

public class GraphicUserInterface {

	JFrame frame = new JFrame("Cloud Storage");
	Client myClient = null;
	private JTextArea textArea;

	public GraphicUserInterface(Client myClient) {
		super();
		this.myClient = myClient;
		runGUI();
	}

	public void runGUI() {

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 300);

		textArea = new JTextArea();
		JButton uploadButton = new JButton("Upload");
		JButton deleteButton = new JButton("Delete");
		JButton downloadButton = new JButton("Download");
		JButton listOfFiles = new JButton("List");
		frame.getContentPane().add(BorderLayout.CENTER, textArea);
		Container container = new Container();
		container.setLayout(new FlowLayout());
		container.add(uploadButton);
		container.add(deleteButton);
		container.add(downloadButton);
		container.add(listOfFiles);
		frame.getContentPane().add(BorderLayout.SOUTH, container);
		frame.setVisible(true);
		uploadButton.addActionListener(upl -> {
			System.out.println(myClient.chooseAndSendFile());
		});
		deleteButton.addActionListener(dlt -> {
			System.out.println(myClient.deleteFile(textArea.getText()));
		});
		downloadButton.addActionListener(dnl -> {
			System.out.println(myClient.downloadFile(textArea.getText()));
		});
		listOfFiles.addActionListener(lst -> {
			System.out.println(myClient.showListOfFiles());
		});

	}

	public File openFileChooser() {

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogTitle("Select file");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setVisible(true);
		int result = fileChooser.showOpenDialog(fileChooser);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = new File(fileChooser.getSelectedFile().toString());
			System.out.println("Choosen file is " + file);
			return file;
		} else {
			return null;
		}

	}

	public void informUser(String message) {
		textArea.setText(message);
	}

}
