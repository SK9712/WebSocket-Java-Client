package com.web.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;

public class WebSockyOP {

    public static void sendPingFrame(SocketChannel client) throws IOException {
        ByteBuffer frame = ByteBuffer.allocate(2 + 4);  // 2 bytes for header, 4 bytes for masking key

        // Frame Header
        frame.put((byte) 0x89);  // FIN bit set (1), RSV1, RSV2, RSV3 bits cleared (0), Opcode 0x9 for Ping
        frame.put((byte) 0x80);  // Mask bit set (1), Payload length 0 (no payload)

        // Masking Key
        byte[] maskingKey = new byte[4];
        new Random().nextBytes(maskingKey);
        frame.put(maskingKey);

        frame.flip();
        client.write(frame);
        System.out.println("Sent Ping frame without payload");
    }

    public static void sendCloseFrame(SocketChannel client) throws IOException {
        ByteBuffer frame = ByteBuffer.allocate(2 + 4);

        // Frame Header
        frame.put((byte) 0x88);  // FIN bit set (1), RSV1, RSV2, RSV3 bits cleared (0), Opcode 0x8 for Close
        frame.put((byte) 0x80);  // Mask bit set (1), Payload length 0 (no payload)

        // Masking Key
        byte[] maskingKey = new byte[4];
        new Random().nextBytes(maskingKey);
        frame.put(maskingKey);

        frame.flip();
        client.write(frame);
        System.out.println("Sent Close frame");
    }

    public static void sendTextFrameWithMask(SocketChannel client, String message) throws Exception {
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

    public static void sendLargeTextMessage(SocketChannel client, String message) throws IOException {
        byte[] payload = message.getBytes();

        int frameSize = 10; // Define the size of each frame's payload
        int offset = 0;

        // Send the first frame
        int length = Math.min(frameSize, payload.length - offset);
        sendContinueFrame(client, payload, offset, length, true, false); // FIN = false, Opcode = Text
        offset += length;

        // Send continuation frames
        while (offset < payload.length) {
            length = Math.min(frameSize, payload.length - offset);
            sendContinueFrame(client, payload, offset, length, offset + length == payload.length, true); // FIN = true if it's the last frame, Opcode = Continuation
            offset += length;
        }
    }

    private static void sendContinueFrame(SocketChannel client, byte[] payload, int offset, int length, boolean fin, boolean continuation) throws IOException {
        ByteBuffer frame = ByteBuffer.allocate(2 + length + 4);

        // Frame Header
        byte opcode = continuation ? (byte) 0x0 : (byte) 0x1; // Opcode 0x0 for Continuation, 0x1 for Text
        byte finBit = (byte) (fin ? 0x80 : 0x00); // FIN bit set or not
        frame.put((byte) (finBit | opcode));
        frame.put((byte) (0x80 | length)); // Mask bit set (1), Payload length

        // Masking Key
        byte[] maskingKey = new byte[4];
        new Random().nextBytes(maskingKey);
        frame.put(maskingKey);

        // Apply masking to the payload
        for (int i = offset; i < offset + length; i++) {
            frame.put((byte) (payload[i] ^ maskingKey[(i - offset) % 4]));
        }

        frame.flip();
        client.write(frame);
        System.out.println("Sent " + (continuation ? "Continuation" : "Text") + " frame, FIN: " + fin);
    }

    public static void sendTextFrameWithoutMask(SocketChannel client, String message) throws IOException {
        byte[] payload = message.getBytes();
        ByteBuffer frame = ByteBuffer.allocate(2 + payload.length);

        // Frame Header
        frame.put((byte) 0x81);  // FIN bit set (1), RSV1, RSV2, RSV3 bits cleared (0), Opcode 0x1 for Text
        frame.put((byte) payload.length);  // Mask bit not set (0), Payload length

        // Payload
        frame.put(payload);

        frame.flip();
        client.write(frame);
        System.out.println("Sent Text frame without mask");
    }

    public static void sendPongFrame(SocketChannel client) throws IOException {
        ByteBuffer frame = ByteBuffer.allocate(2 + 4);  // 2 bytes for header, 4 bytes for masking key

        // Frame Header
        frame.put((byte) 0x8A);  // FIN bit set (1), RSV1, RSV2, RSV3 bits cleared (0), Opcode 0xA for Pong
        frame.put((byte) 0x80);  // Mask bit set (1), Payload length 0 (no payload)

        // Masking Key
        byte[] maskingKey = new byte[4];
        new Random().nextBytes(maskingKey);
        frame.put(maskingKey);

        frame.flip();
        client.write(frame);
        System.out.println("Sent Pong frame");
    }

    private static void sendBinaryFrame(SocketChannel client) throws IOException {
        byte[] payload = "Binary data".getBytes();
        ByteBuffer frame = ByteBuffer.allocate(2 + payload.length + 4);

        // Frame Header
        frame.put((byte) 0x82);  // FIN bit set (1), RSV1, RSV2, RSV3 bits cleared (0), Opcode 0x2 for Binary
        frame.put((byte) (0x80 | payload.length));  // Mask bit set (1), Payload length

        // Masking Key
        byte[] maskingKey = new byte[4];
        new Random().nextBytes(maskingKey);
        frame.put(maskingKey);

        // Apply masking to the payload
        for (int i = 0; i < payload.length; i++) {
            frame.put((byte) (payload[i] ^ maskingKey[i % 4]));
        }

        frame.flip();
        client.write(frame);
        System.out.println("Sent Binary frame");
    }
}
