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
import java.util.Iterator;

public class Server {
	static InetSocketAddress hostAddress = new InetSocketAddress(1237);
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
			int n = selector.select();
			if (n == 0)
				continue;
			Iterator iterator = selector.selectedKeys().iterator();
			while (iterator.hasNext()) {
				SocketChannel socketChannel = null;
				SelectionKey key = (SelectionKey) iterator.next();
				if (key.isAcceptable()) {
					
					socketChannel = ((ServerSocketChannel) key.channel()).accept();
					if (socketChannel == null) {
						continue;
					}
					System.out.println("Accepted " + socketChannel);
					
					
				}
				
				if (key.isValid()) {
					System.out.println(auxiliary.AuxiliaryMethods.readLongFromChannel(socketChannel));
				}
				socketChannel.close();
				iterator.remove();
				System.out.println("Cycle!");
			}
		}
	}
}