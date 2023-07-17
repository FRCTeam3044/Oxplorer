import pathfinding.Pathfinder;

public class Main {
    public static WebSocketWrapper ws_server;
    public static Pathfinder pathfinder;
    public static void main(String[] args) {
        try {
            int port = 5800; 
            ws_server = new WebSocketWrapper(port);
            pathfinder = new Pathfinder();
            
            ws_server.start();
            System.out.println("Pathfinding ws started on port: " + ws_server.getPort());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
