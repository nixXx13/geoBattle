import java.io.IOException;
        import java.io.ObjectOutputStream;

class connectionUtils {

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
            connectionUtils.sendClient(os, s);
        }
    }
}
