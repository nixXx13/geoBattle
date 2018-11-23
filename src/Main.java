import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// TODO - implement private and public rooms - generalization of loop in main

public class Main {

    private final static int NUM_PLAYERS = 1;

    public static void main(String[] args) throws IOException{

        ServerSocket ss = new ServerSocket(4444);


        // gameManagers keys are game rooms names
//        HashMap<String,IGameManager> gameManagers= new HashMap<>();
//        List<IGameManager> gameManagers = new ArrayList<>();

        while(true) {
            System.out.println("Server is up!");
            // setting up variables needed for game
            ServerWorker[] serverWorkers = new ServerWorker[NUM_PLAYERS];
            ObjectOutputStream[] osAll = new ObjectOutputStream[NUM_PLAYERS];
            List<GameData> data = new ArrayList();
            List<String> answers = new ArrayList();
            getData(data, answers);
            IGameManager gameManager = new GameManagerImpl(NUM_PLAYERS, osAll, data, answers);
//            gameManagers.add(gameManager);

            for (int i = 0; i < NUM_PLAYERS; i++) {
                Socket socket = ss.accept();
                try {
                    ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
                    osAll[i] = os;
                    serverWorkers[i] = new ServerWorker(socket, i, is, os, gameManager);
                    System.out.println(String.format("player %d connected", i));

                } catch (IOException e) {
                    e.printStackTrace();
                    ConnectionUtils.closeClient(socket);
                    i--;
                }
            }

            // all players connected - start game
            for (int i = 0; i < NUM_PLAYERS; i++) {
                Thread p = new Thread(serverWorkers[i]);
                p.start();
            }

        }

    }

    /*
    TODO - Fetch data from DB
     */
    private static void getData(List data,List answers) {
        for(int i = 0 ; i<10 ; i++) {
            GameData questionGameData = new GameData(GameData.DataType.QUESTION);
            questionGameData.setContent("question", "1+"+i);
            questionGameData.setContent("pAnswer1", "" +(i+1));
            questionGameData.setContent("pAnswer2", ""+i);
            data.add(questionGameData);
            answers.add(String.valueOf(i+1));
        }
    }

}

