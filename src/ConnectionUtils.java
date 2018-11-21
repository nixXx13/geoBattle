import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import com.google.gson.Gson;

// TODO - improve exception handling
// client exits unexpectedly

class ConnectionUtils {

    static void sendClient(ObjectOutputStream os, GameData gameData){
        Gson gson = new Gson();
        String s = gson.toJson(gameData);
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
        Gson gson = new Gson();
        try {
            String s = (String) is.readObject();
            m = gson.fromJson(s,GameData.class);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return m;
    }
}
