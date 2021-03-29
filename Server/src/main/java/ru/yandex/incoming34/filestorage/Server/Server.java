package ru.yandex.incoming34.filestorage.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import auxiliary.AuxiliaryMethods;

public class Server {
	static InetSocketAddress hostAddress = new InetSocketAddress(1237);
	private static int tramsmission;
	final static int DEFAULT_PORT = 1237;

	public static void main(String[] args) throws IOException {
		int port = DEFAULT_PORT;

		System.out.println("Server starting ... listening on port " + port);
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		ServerSocket serverSocket = serverSocketChannel.socket();
		serverSocket.bind(new InetSocketAddress(port));
		serverSocketChannel.configureBlocking(false);
		Selector selector = Selector.open();
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

		while (true) {
			int numReadyChannels = selector.select();
			if (numReadyChannels == 0)
				continue;
			Set<SelectionKey> selectedKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = selectedKeys.iterator();

			while (iterator.hasNext()) {
				ServerSocketChannel server = null;
				SelectionKey oneKey = iterator.next();
				if (!oneKey.isValid()) {
					continue;
				}
				if (oneKey.isAcceptable()) {
					server = (ServerSocketChannel) oneKey.channel();
					SocketChannel client = server.accept();

					if (client == null) {
						continue;
					}
					client.configureBlocking(false);
					client.register(selector, (SelectionKey.OP_READ | SelectionKey.OP_WRITE),
							new ClientHandler(client));
					System.out.println("Accepted " + server + " Client: " + client);

				} else {
					if (oneKey.isReadable()) {
						SocketChannel client = (SocketChannel) oneKey.channel();
						ByteBuffer buffer = ByteBuffer.allocate(256);
						client.read(buffer);
						if (!Objects.isNull(buffer)) {
							String command = AuxiliaryMethods.readStringFromByteBuffer(buffer);
							command = AuxiliaryMethods.leaveOnlyMeaningfullSymbols(command);
							buffer.clear();
							if (command.length() != 0) {
								System.out.println("Command: " + command);
								ClientHandler clientHandler = (ClientHandler) oneKey.attachment();
								System.out.println("clientHandler: " + clientHandler.getClass());
								clientHandler.handleCommand(command);
							}
						}

					} else if (oneKey.isWritable()) {
						SocketChannel client = (SocketChannel) oneKey.channel();
						if (tramsmission != 1) {
							client.write(auxiliary.AuxiliaryMethods.convertStringToByteBuffer("You have connected to server " + Server.class));
							tramsmission++;
						}

					}
				}

				iterator.remove();
			}
		}
	}
}