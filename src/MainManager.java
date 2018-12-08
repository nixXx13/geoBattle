import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

class MainManager {

    private boolean run;
    private Map<String,IGameManager> gameManagers = Collections.synchronizedMap(new HashMap<>());

    MainManager(){
        run = true;
    }

    void start() throws IOException{
        ServerSocket ss = new ServerSocket(4444);


        System.out.println("Server is up!");
        while(run) {

            Socket socket = ss.accept();
            ObjectOutputStream os = null;
            ObjectInputStream is = null;
            GameData initGameData = null;
            try {
                os = new ObjectOutputStream(socket.getOutputStream());
                is = new ObjectInputStream(socket.getInputStream());
                String dataConnectionStr = ConnectionUtils.readObjectInputStream(is);
                initGameData = ConnectionUtils.jsonToGameData(dataConnectionStr);
            } catch (IOException e) {
                e.printStackTrace();
                closeAll(is,os,socket);
            }
            if (initGameData!= null){

                String name     = initGameData.getContent("name");
                String roomName = initGameData.getContent("roomName");

                String type = initGameData.getContent("type");
                switch (type){
                    case "create":
                        System.out.println(roomName + ":" + name + " is creating room with size " + initGameData.getContent("roomSize") );
                        // TODO - run validators before creation
                        // 1.validate room not used , if used check if empty (game over)
                        // 2.validate player name

                        IGameManager newGameManager = initGameManagerSettings(initGameData);
                        newGameManager.clientJoined(new ServerWorker(socket, name, is, os, newGameManager , roomName),os);
                        gameManagers.put(roomName,newGameManager);
                        break;

                    case "join":
                        if(gameManagers.containsKey(roomName)){
                            System.out.println(roomName + ":" + name + " is joining room");
                            // TODO validate room is not full ..
                            IGameManager gameManager = gameManagers.get(roomName);
                            gameManager.clientJoined(new ServerWorker(socket, name, is, os, gameManagers.get(roomName), roomName),os);
                        }
                        else {
                            System.out.println( name+":no such room "+ roomName+".");
                            closeAll(is,os,socket);
                            //send packet - no such room
                        }
                        break;
                    default:
                        System.out.println(name+":unknown initGameData received.");
                        closeAll(is,os,socket);
                        //send packet - no such room
                        break;
                }

            }
        }

    }

    private IGameManager initGameManagerSettings(GameData dataConnection){

        String roomPassword = dataConnection.getContent("roomPassword");
        String roomName = dataConnection.getContent("roomName");
        int roomSize = Integer.valueOf(dataConnection.getContent("roomSize"));

        List<ServerWorker> serverWorkers = new ArrayList<>();
        List<ObjectOutputStream> osAll = new ArrayList<>();

        // data for the room
        List<GameData> data = new ArrayList<>();
        List<String> answers = new ArrayList<>();
        Main.getData(data, answers, roomSize);

        return new GameManagerImpl(this ,roomName , roomSize, osAll, serverWorkers, data, answers);
    }

    void removeGameManager(String gameManagerName){
        gameManagers.remove(gameManagerName);
        System.out.println(gameManagerName + ":was removed, current rooms - " + gameManagers.toString());
    }

    private void closeAll(Closeable st1, Closeable st2, Socket s){
        ConnectionUtils.closeStream(st1);
        ConnectionUtils.closeStream(st2);
        ConnectionUtils.closeSocket(s);
    }
}
