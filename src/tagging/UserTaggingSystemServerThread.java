package tagging;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import tagging.parsing.CommandParser;
import tagging.parsing.CommandParserException;

public class UserTaggingSystemServerThread extends Thread {

	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;

	public UserTaggingSystemServerThread(Socket socket) throws IOException {
		this.socket = socket;
		in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		out = new DataOutputStream(socket.getOutputStream());
	}

	public void run() {
		String line = "";
		try {
			while (!line.equals(UserTaggingSystemClient.EXIT_COMMAND)) {
				try {
					line = in.readUTF();
					String response = CommandParser.parseLine(line);
					out.writeUTF(response);
				} catch (CommandParserException e) {
					out.writeUTF(e.getMessage());
				}
			}
			System.out.println("Client disconnected.");
			socket.close();
			out.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
