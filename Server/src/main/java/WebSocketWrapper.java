import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Base64;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import pathfinding.Pathfinder.PathfindSnapMode;
import pathfinding.Structures.Path;
import pathfinding.Structures.ImpossiblePathException;
import pathfinding.Structures.Node;

public class WebSocketWrapper extends WebSocketServer {

    public WebSocketWrapper(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println(
            conn.getRemoteSocketAddress().getAddress().getHostAddress() + " connected.");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println(conn + " disconnected");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println(conn + ": " + message);
        if(message.startsWith("sp")){
            String[] split = message.split(" ");
            
            double[] start_raw = decodeLocation(split[1]);
            double[] end_raw = decodeLocation(split[2]);

            boolean isRed = split[3].equals("t");
            int snapMode_raw = Integer.parseInt(split[4]);
            PathfindSnapMode snapMode = PathfindSnapMode.values()[snapMode_raw];

            double[] dynamicVertices_raw = decodeLocation(split[5]);
            Node start = new Node(start_raw[0], start_raw[1]);
            Node end = new Node(end_raw[0], end_raw[1]);

            ArrayList<Node> dynamicVertices = new ArrayList<>();
            for(int i = 0; i < dynamicVertices_raw.length; i += 2){
                dynamicVertices.add(new Node(dynamicVertices_raw[i], dynamicVertices_raw[i+1]));
            }
            try {
                Path path = Main.pathfinder.generatePath(start, end, snapMode, isRed, dynamicVertices);
                conn.send("path " + encodeLocation(path.toDoubleArray()));
            } catch (ImpossiblePathException e) {
                conn.send("ipe");
            } catch (Exception e) {
                conn.send("err " + e.getMessage());
            }
        }
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        System.out.println(conn + ": " + message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Server started!");
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

    private static double[] decodeLocation(String base64Encoded) {
        return byteToDoubleArray(Base64.getDecoder().decode(base64Encoded));
    }
    private static double[] byteToDoubleArray(byte[] bytes) {
        DoubleBuffer buf = ByteBuffer.wrap(bytes).asDoubleBuffer();
        double[] doubleArray = new double[buf.limit()];
        buf.get(doubleArray);
        return doubleArray;
    }
    private static String encodeLocation(double[] doubleArray) {
        return Base64.getEncoder().encodeToString(doubleToByteArray(doubleArray));
    }

    private static byte[] doubleToByteArray(double[] doubleArray) {
        ByteBuffer buf = ByteBuffer.allocate(Double.SIZE / Byte.SIZE * doubleArray.length);
        buf.asDoubleBuffer().put(doubleArray);
        return buf.array();
    }

}