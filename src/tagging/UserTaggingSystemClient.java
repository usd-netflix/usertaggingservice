package tagging;

import java.io.*;
import java.net.*;

public class UserTaggingSystemClient {

	public final static String EXIT_COMMAND = "exit";

	private Socket socket;
	private BufferedReader userInput;
	private DataInputStream in;
	private DataOutputStream out;

	public UserTaggingSystemClient(String address, int port) throws UnknownHostException, IOException {
		socket = new Socket(address, port);
		userInput = new BufferedReader(new InputStreamReader(System.in));
		in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		out = new DataOutputStream(socket.getOutputStream());
	}

	public void run() throws IOException {
		String line = "";

		while (!line.equals(EXIT_COMMAND)) {
			line = userInput.readLine().trim();
			out.writeUTF(line);
			String response = "";
			try {
				response = in.readUTF();
			} catch (EOFException e) {
				// No response from the server;
			}
			if (!response.isEmpty()) {
				System.out.println(response);
			}
		}

		userInput.close();
		out.close();
		socket.close();
	}

	public static void main(String args[]) throws UnknownHostException, IOException {
		System.out.println("Attempting to connect to server...");
		UserTaggingSystemClient client;
		try {
			client = new UserTaggingSystemClient(UserTaggingSystemServer.ADDRESS, UserTaggingSystemServer.PORT);
		} catch (ConnectException e) {
			System.out.println("Failed to connect to server. Please make sure the server is running and try again.");
			return;
		}
		System.out.println("Connected to server.\n");
		System.out.println("Enter a command to get started, \"help\" to see possible commands, or \"exit\" at any time to disconnect.");
		client.run();
	}
}
