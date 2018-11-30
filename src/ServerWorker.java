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
        this.id = id;
        this.socket = socket;
        this.gameManager = gameManager;
        this.is = is;
        this.os = os;
    }

    @Override
    public void run() {
        try {
            gameManager.initGame(id);
            GameData clientResponse = ConnectionUtils.readClient(is);

            while (clientResponse != null) {
                GameData.DataType type = clientResponse.getType();

                if (type == GameData.DataType.FIN){
                    System.out.println("Game finished.client" + id + " exited properly");
                    break;
                }
                if (type == GameData.DataType.ANSWER) {

                    //calc score of this turn
                    String turnCorrectAnswer = gameManager.getTurnGameDataAnswer();
                    String clientAnswer = clientResponse.getContent("answer");

                    int turnScore = calculateScore(clientAnswer, turnCorrectAnswer);
                    gameManager.updateScore(id, turnScore);
                    System.out.println("client" + id + " answered: " + clientAnswer + "(correct answer '" + turnCorrectAnswer + "' )");

                    // sending client response with correct answer
                    GameData correctAnswer = new GameData(GameData.DataType.ANSWER);
                    correctAnswer.setContent("answer", turnCorrectAnswer);
                    ConnectionUtils.sendClient(os, correctAnswer);
                    System.out.println("client" + id + " this turn score: " + turnScore);
//                    }
                    gameManager.turnFinished();
                }
                clientResponse = ConnectionUtils.readClient(is);
            }
        }catch (IOException se){
            // TODO - do something here!
        }
        finally {
            // TODO - close is,os and socket properly
            ConnectionUtils.closeClient(socket);
        }
    }

    private int calculateScore(String clientAnswer, String correctAnswer){
        if (clientAnswer.equals(correctAnswer)){
            return 1;
        }
        return 0;
    }

    int getId() {
        return id;
    }
}