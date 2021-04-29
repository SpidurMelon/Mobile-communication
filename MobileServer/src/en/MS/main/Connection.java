package en.MS.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Connection {
	private String name;
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	public ArrayList<String> messageQueue = new ArrayList<String>();
	
	public Connection(Socket socket) {
		this.socket = socket;
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			name = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void startListening() {
		Thread listener = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                	while (socket.isConnected()) {
                		String receivedMessage = null;
                		try {
                			receivedMessage = in.readLine();
                		} catch(IOException e) {
                			System.out.println("Connection with " + name + " got closed somehow (Ignore this message if you closed it yourself)");
                			break;
                		}
                		
                		if (receivedMessage != null) {
	                		synchronized (messageQueue) {
	                			messageQueue.add(receivedMessage);
	                		}
                		} else {
                			System.out.println("OutputStream got closed on " + name + "'s side");
                			close();
                			break;
                		}
                	}
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
		listener.start();
	}
	
	public void send(String message) {
		out.println(message);
	}
	
	public boolean isAlive() {
		return !socket.isClosed();
	}
	
	public String getName() {
		return name;
	}
	
	public void close() throws IOException {
		System.out.println(name + " closed the connection.");
        socket.close();
        out.close();
        in.close();
	}
}
