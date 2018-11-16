import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class ServerWorker implements Runnable{

    private ObjectInputStream is;
    private ObjectOutputStream os;
    private ObjectOutputStream[] osAll;
    private Socket socket;
    private int id;
    private IGameManager gameManager;


    public ServerWorker(Socket socket , ObjectOutputStream[] osAll , int id , IGameManager gameManager) {
        this.id = id;
        this.osAll = osAll;
        this.socket = socket;
        this.gameManager = gameManager;
        try {
            os = new ObjectOutputStream(socket.getOutputStream());
            is = new ObjectInputStream(socket.getInputStream());
            connectionUtils.sendClient( os,new GameData(GameData.DataType.UPDATE,"Welcome player" + id));
            osAll[id] = os;
        } catch (IOException e) {
            e.printStackTrace();
            closeClient();
        }

    }
    @Override
    public void run() {

        gameManager.initGame(id);
        GameData clientResponse = readClient();

        while ( clientResponse != null) {
            if(gameManager.isClientTurn(id)){
                if (clientResponse.getType() == GameData.DataType.ANSWER){
                    GameData correctAnswer = new GameData(GameData.DataType.ANSWER , gameManager.getTurnDataAnswer() );
                    System.out.println("got answer from client " + id + ", sending correct answer " + correctAnswer);
                    connectionUtils.sendClient(os,correctAnswer);
                }
                gameManager.turnFinished();
            }
            clientResponse = readClient();
        }
        closeClient();
    }

    private void closeClient(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private GameData readClient() {
        GameData m = null;
        try {
            m = (GameData) is.readObject();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return m;
    }

}