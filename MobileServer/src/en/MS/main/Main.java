package en.MS.main;

import java.io.IOException;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		Server server = null;
		try {
			server = new Server(7777);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Opened server...");
		server.open();
		
		System.out.println("Enter something to broadcast...");
		Scanner scan = new Scanner(System.in);
		while (true) {
			String message = scan.nextLine();
			if (message.startsWith("/")) {
				if (message.equals("/quit")) {
					break;
				}
			} else {
				server.broadCast(message);
			}
		}
		
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
