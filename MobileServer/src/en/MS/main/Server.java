package en.MS.main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
	private ServerSocket serverSocket;
	private ArrayList<Connection> connections = new ArrayList<Connection>();
	
    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(7777);
    }
    
    public void open() {
    	startConnections();
    	startMessageParsing();
    }
  
	private void startConnections()  {
        Thread connector = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                	while (!serverSocket.isClosed()) {
	                    Socket socket = serverSocket.accept();
	                    synchronized (connections) {
	                    	Connection newConnection = new Connection(socket);
	                    	connections.add(newConnection);
	                    	newConnection.startListening();
	                    	System.out.println("Connection made with " + newConnection.getName() + "!");
						}
	                    
                	}
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        connector.start();
    }
	
	private void startMessageParsing() {
		Thread parser = new Thread(new Runnable() {
			public void run() {
				while (!serverSocket.isClosed()) {
					synchronized(connections) {
						for (int i = 0; i < connections.size(); i++) {
							Connection c = connections.get(i);
							if (!c.isAlive()) {
				    			connections.remove(c);
				    			i--;
				    			continue;
				    		}
							synchronized(c.messageQueue) {
								if (!c.messageQueue.isEmpty()) {
									parse(c, c.messageQueue.get(0));
									c.messageQueue.remove(0);
								}
							}
						}
					}
				}
			}
		});
		parser.start();
	}
	
	private void parse(Connection c, String message) {
		if (message.equals("ping")) {
			c.send("pong");
		} else if (message.equals("/closed")) {
			try {
				c.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println(c.getName() + ": " + message);
			broadCast(c.getName() + ": " + message);
		}
	}
    
    public void broadCast(String message) {
    	synchronized (connections) {
	    	for (int i = 0; i < connections.size(); i++) {
	    		Connection c = connections.get(i);
	    		if (!c.isAlive()) {
	    			connections.remove(c);
	    			i--;
	    			continue;
	    		}
	    		c.send(message);
	    	}
    	}
    }
    
    public void close() throws IOException {
    	serverSocket.close();
        for (Connection c:connections) {
        	c.close();
        }
    }
   
}
