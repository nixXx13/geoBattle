import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.List;

import com.google.gson.Gson;

class ConnectionUtils {

    static void sendClient(ObjectOutputStream os, GameData gameData) throws IOException {
        Gson gson = new Gson();
        String s = gson.toJson(gameData);
        os.writeObject(s);

        PrintStream ps = new PrintStream(os);
        if (ps.checkError()){
            throw new IOException("Error sending client with objectStream " + os.toString());
        }
    }

    // sendAllClients is used for updates hence it doesnt throw any exceptions.
    // issues of client connection are handled in dedicated thread of client
    static void sendAllClients(List<ObjectOutputStream> clients, GameData s) {
        System.out.println("sendAllClients:sending " + s);
        for (ObjectOutputStream os : clients) {
            try{
                sendClient(os, s);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    static void closeClient(Socket socket){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static GameData readClient(ObjectInputStream is) throws IOException {
        GameData m = null;
        Gson gson = new Gson();
        try {
            String s = (String) is.readObject();
            m = gson.fromJson(s,GameData.class);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return m;
    }
}
