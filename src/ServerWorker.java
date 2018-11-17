import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class ServerWorker implements Runnable{

    private ObjectInputStream is;
    private ObjectOutputStream os;
    private Socket socket;
    private int id;
    private IGameManager gameManager;


    public ServerWorker(Socket socket , int id , ObjectInputStream is , ObjectOutputStream os , IGameManager gameManager) {
        this.id = id;
        this.socket = socket;
        this.gameManager = gameManager;
        this.is = is;
        this.os = os;
    }

    @Override
    public void run() {

        gameManager.initGame(id);
        GameData clientResponse = ConnectionUtils.readClient(is);

        while ( clientResponse != null) {
            if(gameManager.isClientTurn(id)){
                if (clientResponse.getType() == GameData.DataType.ANSWER){

                    //calc score of this turn
                    String turnCorrectAnswer = gameManager.getTurnGameDataAnswer();
                    int turnScore = calculateScore(clientResponse.getContent(),turnCorrectAnswer);
                    gameManager.updateScore(id, turnScore);
                    System.out.println("client" + id +" answered: " + clientResponse.getContent() + "(correct answer '" + turnCorrectAnswer +"' )");

                    // sending client response with correct answer
                    GameData correctAnswer = new GameData(GameData.DataType.ANSWER , turnCorrectAnswer );
                    ConnectionUtils.sendClient(os,correctAnswer);
                    System.out.println("client" + id +" this turn score: " + turnScore);
                }
                gameManager.turnFinished();
            }
            clientResponse = ConnectionUtils.readClient(is);
        }
        ConnectionUtils.closeClient(socket);
    }


    private int calculateScore(String clientAnswer,String correctAnswer){
        if (clientAnswer.equals(correctAnswer)){
            return 1;
        }
        return 0;
    }
}