import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class ServerWorker implements Runnable{

    private ObjectInputStream is;
    private ObjectOutputStream os;
    private Socket socket;
    private String clientName;
    private String roomName;
    private IGameManager gameManager;

    ServerWorker(Socket socket, String clientName, ObjectInputStream is, ObjectOutputStream os, IGameManager gameManager , String roomName) {
        this.gameManager    = gameManager;
        this.clientName     = clientName;
        this.roomName       = roomName;
        this.socket         = socket;
        this.is             = is;
        this.os             = os;
    }

    @Override
    public void run() {
        try {
            gameManager.initGame(clientName);
            GameData clientResponse = readClient();

            while (clientResponse != null) {
                GameData.DataType type = clientResponse.getType();

                if (type == GameData.DataType.FIN){
                    String reason = clientResponse.getContent("reason");
                    if (reason != null && reason.equals("game finished")){
                        // Game ended as expected
                        System.out.println(roomName + ":ServerWorker,run: client "+ clientName + " exited properly");
                    }
                    else{
                        // Client initiated exit, notifying gameManager of client exit
                        gameManager.clientExit(clientName);
                    }
                    break;
                }
                if (type == GameData.DataType.ANSWER) {
                    //calc score of this turn
                    String turnCorrectAnswer = gameManager.getTurnGameDataAnswer();
                    double turnScore = calculateScore(clientResponse, turnCorrectAnswer);
                    gameManager.updateScore(clientName, turnScore);

                    // sending client response with correct answer
                    GameData correctAnswer = new GameData(GameData.DataType.ANSWER);
                    correctAnswer.setContent("answer", turnCorrectAnswer);
                    sendClient(correctAnswer);
                    System.out.println(roomName + ":ServerWorker,run: client" + clientName + " answered: " + clientResponse.getContent("answer") + ":" + clientResponse.getContent("timeCoeffient") +"(correct answer '" + turnCorrectAnswer + "' )" + ".this turn score: " + turnScore);

                    gameManager.turnFinished();
                }
                clientResponse = readClient();
            }
        }catch (IOException se){
            System.out.println(roomName + ":ServerWorker,run: error in main loop - terminating");
            gameManager.clientExit(clientName);
        }
        finally {
            terminate();
        }
    }

    private double calculateScore(GameData clientResponse, String correctAnswer){
        String clientAnswer = clientResponse.getContent("answer");
        double clientTimeCoefficient = Double.valueOf(clientResponse.getContent("timeCoeffient"));
        if (clientAnswer.equals(correctAnswer)){
            return clientTimeCoefficient;
        }
        return 0;
    }

    void terminate(){
        System.out.println(roomName + ":ServerWorker,terminate: trying to close output/input streams and socket of client " + clientName);
        ConnectionUtils.closeStream(os);
        ConnectionUtils.closeStream(is);
        ConnectionUtils.closeSocket(socket);
    }

    String getId() {
        return clientName;
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
            throw new IOException(roomName + ":ServerWorker,readClient: received null from client");
        }
        return m;
    }
}