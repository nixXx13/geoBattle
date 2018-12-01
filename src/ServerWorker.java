import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class ServerWorker implements Runnable{

    private ObjectInputStream is;
    private ObjectOutputStream os;
    private Socket socket;
    private int id;
    private IGameManager gameManager;

    ServerWorker(Socket socket, int id, ObjectInputStream is, ObjectOutputStream os, IGameManager gameManager) {
        this.gameManager    = gameManager;
        this.id             = id;
        this.socket         = socket;
        this.is             = is;
        this.os             = os;
    }

    @Override
    public void run() {
        try {
            gameManager.initGame(id);
            GameData clientResponse = readClient();

            while (clientResponse != null) {
                GameData.DataType type = clientResponse.getType();

                if (type == GameData.DataType.FIN){
                    String reason = clientResponse.getContent("reason");
                    if (reason != null && reason.equals("game finished")){
                        System.out.println("ServerWorker,run: client "+ id + " exited properly");
                    }
                    else{
                        // Client initiated exit, notifying gameManager of client exit
                        gameManager.notifyClientExit(id);
                    }
                    break;
                }
                if (type == GameData.DataType.ANSWER) {

                    //calc score of this turn
                    String turnCorrectAnswer = gameManager.getTurnGameDataAnswer();
                    String clientAnswer = clientResponse.getContent("answer");
                    int turnScore = calculateScore(clientAnswer, turnCorrectAnswer);
                    gameManager.updateScore(id, turnScore);
                    System.out.println("ServerWorker,run: client" + id + " answered: " + clientAnswer + "(correct answer '" + turnCorrectAnswer + "' )");

                    // sending client response with correct answer
                    GameData correctAnswer = new GameData(GameData.DataType.ANSWER);
                    correctAnswer.setContent("answer", turnCorrectAnswer);
                    sendClient(correctAnswer);
                    System.out.println("ServerWorker,run: client" + id + " this turn score: " + turnScore);
                    gameManager.turnFinished();
                }
                clientResponse = readClient();
            }
        }catch (IOException se){
            System.out.println("ServerWorker,run: error in main loop - terminating");
            gameManager.notifyClientExit(id);
        }
        finally {
            terminate();
        }
    }

    private int calculateScore(String clientAnswer, String correctAnswer){
        if (clientAnswer.equals(correctAnswer)){
            return 1;
        }
        return 0;
    }

    void terminate(){
        System.out.println("ServerWorker,terminate: trying to close output stream of client " + id);
        ConnectionUtils.closeStream(os);
        System.out.println("ServerWorker,terminate: trying to close input stream of client " + id);
        ConnectionUtils.closeStream(is);
        System.out.println("ServerWorker,terminate: trying to close socket of client " + id);
        ConnectionUtils.closeSocket(socket);
    }

    int getId() {
        return id;
    }

    void sendClient(GameData gameData) throws IOException {
        String s = ConnectionUtils.gameDataToJson(gameData);
        ConnectionUtils.sendObjectOutputStream(os,s);
    }

    private GameData readClient() throws IOException {
        GameData m;
        String input = ConnectionUtils.readObjectInputStream(is);
        if (input!=null) {
            m = ConnectionUtils.jsonToGameData(input);
        }else{
            throw new IOException("ServerWorker,readClient: received null from client");
        }
        return m;
    }
}