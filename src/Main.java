import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private final static int NUM_PLAYERS = 2;

    public static void main(String[] args) throws IOException{
        ObjectOutputStream[] osAll = new ObjectOutputStream[NUM_PLAYERS];
        ServerSocket ss = new ServerSocket(4444);
        System.out.println("Server is up!");
        Socket socket0 = ss.accept();
        System.out.println("player0 connected");
        Socket socket1 = ss.accept();
        System.out.println("player1 connected");
        List<GameData> data         = new ArrayList();
        List<String> answers        = new ArrayList();
        getData(data,answers);
        IGameManager gameManager = new GameManagerImpl(NUM_PLAYERS,osAll , data ,answers);

        Thread p1 = new Thread(new ServerWorker(socket0, osAll ,0 , gameManager));
        Thread p2 = new Thread(new ServerWorker(socket1, osAll ,1 , gameManager));
        p1.start();
        p2.start();

    }

    private static void getData(List data,List answers) {
        for(int i = 0 ; i<10 ; i++) {
            data.add(new GameData(GameData.DataType.QUESTION , "1+"+i));
            answers.add(String.valueOf(i+1));
        }
    }

}

