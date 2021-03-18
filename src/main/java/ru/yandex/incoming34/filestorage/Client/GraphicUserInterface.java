package ru.yandex.incoming34.filestorage.Client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import ru.yandex.incoming34.filestorage.Client.Client;

public class GraphicUserInterface {

	JFrame frame = new JFrame("Cloud Storage");
	Client myClient = null;
	JTextArea textArea;

	public GraphicUserInterface(Client myClient) {
		super();
		this.myClient = myClient;
		runGUI();
	}

	public void runGUI() {

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 300);

		textArea = new JTextArea();
		// TODO: 02.03.2021
		// list targetFile - JList
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
			// System.out.println(myClient.sendFile(textArea.getText()));
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

		/*
		 * JFrame chooseFrame = new JFrame(); chooseFrame.setSize(800, 600);
		 * chooseFrame.setLayout(new BorderLayout()); Container buttons = new
		 * Container(); buttons.setLayout(new FlowLayout()); JButton choose = new
		 * JButton("Choose"); JButton cancel = new JButton("Cancel");
		 * buttons.add(choose); buttons.add(cancel); JPanel chooserPanel = new JPanel();
		 * chooseFrame.add(BorderLayout.SOUTH, buttons);
		 * chooseFrame.add(BorderLayout.CENTER, fileChooser);
		 * chooseFrame.getContentPane().add(chooserPanel);
		 */
		// chooseFrame.setVisible(true);

		/*
		 * choose.addActionListener(
		 * 
		 * // new ActionListener() {
		 * 
		 * @Override /* public void actionPerformed(ActionEvent chs) {
		 */
		fileChooser.setDialogTitle("Select file");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setVisible(true);
		int result = fileChooser.showOpenDialog(fileChooser);
		if (result == JFileChooser.APPROVE_OPTION) {
			return new File(fileChooser.getSelectedFile().toString());
		} else {
			return null;
		}

	}
}
