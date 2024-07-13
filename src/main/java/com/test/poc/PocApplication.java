package com.test.poc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Base64;
import java.util.Random;

@SpringBootApplication
public class PocApplication {

	public static void main(String[] args) throws Exception{
		// Create the HTTP request string with proper line endings
		String key = Base64.getEncoder().encodeToString("randomkey12345".getBytes());

		String request = "GET /poc/socket HTTP/1.1\r\n" +
				"Host: localhost\r\n" +
				"Upgrade: websocket\r\n" +
				"Connection: Upgrade\r\n" +
				"Sec-WebSocket-Key: " + key + "\r\n" +
				"Sec-WebSocket-Version: 13\r\n" +
				"\r\n";

		// Wrap the request string into a ByteBuffer
		ByteBuffer buffer = ByteBuffer.wrap(request.getBytes());

		// Open a SocketChannel and connect to the server
		SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", 9095));

		if (client.isConnected()) {
			// Write the request to the server
			client.write(buffer);
			buffer.clear();

			// Read the response from the server
			ByteBuffer responseBuffer = ByteBuffer.allocate(2024);
			int bytesRead = client.read(responseBuffer);
			responseBuffer.flip();
			byte[] responseData = new byte[bytesRead];
			responseBuffer.get(responseData);
			String response = new String(responseData).trim();
			System.out.println("response=" + response);
			responseBuffer.clear();

			// Check the response for the "101 Switching Protocols" status
//			if (!response.contains("101 Switching Protocols")) {
//				throw new RuntimeException("WebSocket handshake failed: " + response);
//			}

			sendTextFrameWithMask(client, "Pong");

			// Read the server's response
			ByteBuffer responseBuffer2 = ByteBuffer.allocate(1024);
			int bytesRead2 = client.read(responseBuffer2);
			responseBuffer2.flip();
			byte[] responseData2 = new byte[bytesRead2];
			responseBuffer2.get(responseData2);
			String response2 = new String(responseData2).trim();
			System.out.println("Server Response: " + response2);
		}

		// Close the client connection
		client.close();

//		SpringApplication.run(PocApplication.class, args);
	}

	private static void sendTextFrameWithMask(SocketChannel client, String message) throws Exception {
		byte[] messageBytes = message.getBytes();
		byte[] maskingKey = new byte[4];
		new Random().nextBytes(maskingKey);  // Generate a random masking key

		ByteBuffer textFrame = ByteBuffer.allocate(2 + messageBytes.length + 4);  // 2 bytes for header, length of message, and 4 bytes for masking key

		// Frame Header
		textFrame.put((byte) 0x81);  // FIN bit set (1), RSV1, RSV2, RSV3 bits cleared (0), Opcode 0x1 for text frame
		textFrame.put((byte) (0x80 | messageBytes.length));  // Mask bit set (1), Payload length

		// Masking Key
		textFrame.put(maskingKey);

		// Apply masking to the payload
		for (int i = 0; i < messageBytes.length; i++) {
			textFrame.put((byte) (messageBytes[i] ^ maskingKey[i % 4]));
		}

		textFrame.flip();  // Prepare for sending

		// Write the Text Frame to the server
		client.write(textFrame);
	}
}
