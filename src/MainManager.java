import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

class MainManager {

    private List<String> questionRaw;
    private boolean run;
    private Map<String,IGameManager> gameManagers = Collections.synchronizedMap(new HashMap<>());

    MainManager(){
        run = true;

        questionRaw = new ArrayList<>();
        readFile();

        List<List<GameData>> data = new ArrayList<>();
        List<List<String>> answers = new ArrayList<>();
        getData(data, answers, 2);


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
                        System.out.println(initGameData.toString());
                        closeAll(is,os,socket);
                        //send packet - no such room
                        break;
                }

            }
        }

    }

    private void readFile(){
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    "C:\\Users\\Nir\\IdeaProjects\\testServer\\src\\qs.txt"));
            String line = reader.readLine();
            while (line != null) {
                System.out.println(line);
                questionRaw.add(line);
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private IGameManager initGameManagerSettings(GameData dataConnection){

        String roomPassword = dataConnection.getContent("roomPassword");
        String roomName = dataConnection.getContent("roomName");
        int roomSize = Integer.valueOf(dataConnection.getContent("roomSize"));

        List<ServerWorker> serverWorkers = new ArrayList<>();
        List<ObjectOutputStream> osAll = new ArrayList<>();

        // data for the room
        List<List<GameData>> data = new ArrayList<>();
        List<List<String>> answers = new ArrayList<>();
        getData(data, answers, roomSize);

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

        /*
    TODO - Fetch data from DB
     */
    void getData(List<List<GameData>> data, List<List<String>> answers, int numPlayers) {

        // TODO - organize
        int QUESTION_NUMBER = 5;
        int COUNTRIES_NUMBER = 50;

        List<Integer> indexes = new ArrayList<>();
        for ( int i = 0 ; i < COUNTRIES_NUMBER ; i++){
            indexes.add(i);
        }
        Collections.shuffle(indexes);

        for ( int i=0 ; i<numPlayers  ; i++) {
            List<GameData> playerData = new ArrayList<>();
            List<String> playerAnswers = new ArrayList<>();
            data.add(playerData);
            answers.add(playerAnswers);
        }

        for ( int j = 0 ; j<QUESTION_NUMBER; j++){

            int id = indexes.get(j);
            String qRaw = questionRaw.get(id);
            String[] sQRaw = qRaw.split(",");

            GameData questionGameData5 = new GameData(GameData.DataType.QUESTION);
            questionGameData5.setContent("question", sQRaw[0]);
            questionGameData5.setContent("pAnswer0", sQRaw[1]);
            questionGameData5.setContent("pAnswer1", sQRaw[2]);
            questionGameData5.setContent("pAnswer2", sQRaw[3]);
            questionGameData5.setContent("pAnswer3", sQRaw[4]);

            //filling q's for player i
            for ( int i=0 ; i<numPlayers  ; i++){
                data.get(i).add(questionGameData5);
                answers.get(i).add(sQRaw[1]);

            }
        }

    }
}
