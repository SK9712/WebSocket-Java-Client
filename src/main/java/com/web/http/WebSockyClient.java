package com.web.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Base64;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebSockyClient {

    private SocketChannel client;

    private URL webSocketUrl;

    private Queue<String> responseData;

    public WebSockyClient(String url) {
        try {
            webSocketUrl = new URL(url);
            client = SocketChannel.open(new InetSocketAddress(webSocketUrl.getHost(), webSocketUrl.getPort()));
            responseData = new LinkedList<>();
            establishChannel();
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> readResponseFrame());
        } catch (Exception ex){
            ex.printStackTrace();
            throw new RuntimeException("Unable to establish connection: " + ex.getMessage());
        }
    }

    private void establishChannel() throws IOException {
        String key = Base64.getEncoder().encodeToString("randomkey12345".getBytes());
        String request = "GET /poc/socket HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Upgrade: websocket\r\n" +
                "Connection: Upgrade\r\n" +
                "Sec-WebSocket-Key: " + key + "\r\n" +
                "Sec-WebSocket-Version: 13\r\n" +
                "\r\n";
        ByteBuffer buffer = ByteBuffer.wrap(request.getBytes());
        client.write(buffer);
    }

    private void readResponseFrame() {
        while(true) {
            try {
                ByteBuffer buffer = ByteBuffer.allocate(2024);
                if (client.isOpen()) {
                    // Read data from the SocketChannel
                    int bytesRead = client.read(buffer);
                    if (bytesRead > 0) {
                        buffer.flip();
                        byte[] data = new byte[buffer.remaining()];
                        buffer.get(data);
                        String response = new String(data);
                        System.out.println("Received response: " + response);
                        responseData.add(response);
                        buffer.clear();
                    } else if (bytesRead == -1) {
                        // The server has closed the connection
                        System.out.println("Server closed the connection.");
                        client.close();
                        break;
                    }
                } else {
                    System.out.println("SocketChannel is closed.");
                    break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                break;
            }
        }
    }

    public String responsePoll() {
        return !responseData.isEmpty() ? responseData.poll() : null;
    }

    public boolean sendText(String message) throws Exception {
        WebSockyOP.sendTextFrameWithMask(client, message);
        return true;
    }

    public boolean sendTextWithoutMask(String message) throws Exception {
        WebSockyOP.sendTextFrameWithoutMask(client, message);
        return true;
    }

    public boolean ping() throws Exception {
        WebSockyOP.sendPingFrame(client);
        return true;
    }

    public boolean pong() throws Exception {
        WebSockyOP.sendPongFrame(client);
        return true;
    }

    public boolean sendLargeText(String message) throws Exception {
        WebSockyOP.sendLargeTextMessage(client, message);
        return true;
    }

    public boolean close() throws Exception {
        WebSockyOP.sendCloseFrame(client);
        client.close();
        return true;
    }
}
