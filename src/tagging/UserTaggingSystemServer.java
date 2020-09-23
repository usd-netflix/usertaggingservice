package tagging;

import java.io.*;
import java.net.*;

public class UserTaggingSystemServer {

	final static String ADDRESS = "127.0.0.1";
	final static int PORT = 5000;

	// Server runs indefinitely, so no need to explicitly close this resource.
	@SuppressWarnings("resource")
	public static void listen(int port) throws IOException {
		// Start server and wait for a connection
		
		ServerSocket server;
		try {
			server = new ServerSocket(port);
		} catch (BindException e) {
			System.out.println(
					"Another resource is already connected to this endpoint. Please terminate other instances of this server before starting a new one.");
			return;
		}
		
		System.out.println("Waiting for clients to connect...");
		while (true) {
			Socket socket = server.accept();
			System.out.println("Client connected.");
			new UserTaggingSystemServerThread(socket).start();
		}
	}

	public static void main(String args[]) throws IOException {
		System.out.println("Server started.");
		listen(PORT);
	}

}
