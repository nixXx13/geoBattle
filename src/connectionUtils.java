import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

// TODO - improve exception handling
// client exits unexpectedly

class ConnectionUtils {

    static void sendClient(ObjectOutputStream os, GameData s){
        try {
            os.writeObject(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void sendAllClients(ObjectOutputStream[] clients, GameData s) {
        System.out.println("sendAllClients:sending " + s);
        for (ObjectOutputStream os : clients) {
            sendClient(os, s);
        }
    }

    static void closeClient(Socket socket){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static GameData readClient(ObjectInputStream is) {
        GameData m = null;
        try {
            m = (GameData) is.readObject();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return m;
    }
}
