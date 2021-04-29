package com.test.test;

import android.content.Context;
import android.os.Message;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public abstract class Client {
    private String ip, name;
    private int port;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private ArrayList<String> messageQueue = new ArrayList<String>();
    private boolean open = false;

    public Client(String ip, int port, final String name) {
        this.ip = ip;
        this.port = port;
        this.name = name;
        socket = new Socket();
    }

    public void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket.connect(new InetSocketAddress(ip, port), 3000);

                    open = true;

                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream(), true);
                    startListening();
                    startParsing();

                    send(name);

                    System.out.println("Connection success!");
                } catch (SocketTimeoutException socketTimeoutException) {
                    System.err.println("Connection refused.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startListening()  {
        Thread listener = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (isOpenAndWell()) {
                        String receivedMessage = null;
                        try {
                            receivedMessage = in.readLine();
                        } catch(IOException e) {
                            System.out.println("Connection got closed somehow (Ignore this message if you closed it yourself)");
                            close();
                            break;
                        }
                        if (receivedMessage != null) {
                            synchronized (messageQueue) {
                                messageQueue.add(receivedMessage);
                            }
                        } else {
                            System.err.println("OutputStream got closed on the servers side");
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

    private void startParsing() {
        Thread parser = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isOpenAndWell()) {
                    synchronized (messageQueue) {
                        if (!messageQueue.isEmpty()) {
                            parse(messageQueue.get(0));
                            messageQueue.remove(0);
                        }
                    }
                }
            }
        });
        parser.start();
    }

    public abstract void parse(String message);

    public void send(final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isOpenAndWell()) {
                    out.println(message);
                }
            }
        }).start();
    }

    public boolean connectionEstablished() {
        if (socket == null) {
            return false;
        }
        return socket.isConnected();
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isOpenAndWell() {
        return connectionEstablished() && isOpen();
    }

    public void close() throws IOException {
        if (isOpen()) {
            send("/closed");
            System.out.println("Connection closed.");
            socket.close();
            out.close();
            in.close();

            open = false;
        }
    }
}
