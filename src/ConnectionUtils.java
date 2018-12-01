import java.io.*;
import java.net.Socket;

import com.google.gson.Gson;

class ConnectionUtils {


    static void sendObjectOutputStream(ObjectOutputStream os, String s) throws IOException {
        os.writeObject(s);

        PrintStream ps = new PrintStream(os);
        if (ps.checkError()){
            throw new IOException("Error sending client with objectStream " + os.toString());
        }
    }

    static String readObjectInputStream(ObjectInputStream is) throws IOException {
        String s = null;
        try {
            s = (String) is.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return s;
    }

    static void closeStream(Closeable s ){
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void closeSocket(Socket socket){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String gameDataToJson(GameData gameData){
        Gson gson = new Gson();
        return gson.toJson(gameData);
    }

    static GameData jsonToGameData(String json){
        Gson gson = new Gson();
        return gson.fromJson(json, GameData.class);
    }
}
