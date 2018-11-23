import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

// TODO - implement game finished

public class GameManagerImpl implements IGameManager {

    private List<GameData>  turnsData;
    private List<String>    turnsDataAnswer;
    private int         currentTurn;
    private final int   NUM_PLAYERS;
    private ObjectOutputStream[] clients;
    private int[] scores ;

    public GameManagerImpl(int numPlayers , ObjectOutputStream[] clients , List<GameData> turnsData , List<String> turnsDataAnswer){
        this.currentTurn        = 0;
        this.clients            = clients;
        this.NUM_PLAYERS        = numPlayers;
        this.turnsData          = turnsData;
        this.turnsDataAnswer    = turnsDataAnswer;

        scores = new int[NUM_PLAYERS];
        for (int i = 0; i<NUM_PLAYERS;i++){
            scores[i] = 0;
        }
    }

    @Override
    public void initGame(int clientId) {
        if(isClientTurn(clientId)){
            nextTurn();
        }
    }

    @Override
    public void turnFinished() {
        System.out.println("client" + currentTurn%NUM_PLAYERS +" finished his turn");
        GameData update = getUpdate();
        ConnectionUtils.sendAllClients(clients,update);
        currentTurn +=1;
        nextTurn();
    }

    @Override
    public boolean isClientTurn(int clientId) {
        return currentTurn%NUM_PLAYERS == clientId%NUM_PLAYERS;
    }

    @Override
    public GameData getTurnGameData() {
        return turnsData.get(currentTurn);
    }

    @Override
    public String getTurnGameDataAnswer() {
        return turnsDataAnswer.get(currentTurn);
    }

    @Override
    public void updateScore(int clientID, int turnScore) {
        scores[clientID] += turnScore;
    }

    private void nextTurn(){
        int nextClientId = currentTurn%NUM_PLAYERS;
        GameData currTurnData = getTurnGameData();
        try {
            ConnectionUtils.sendClient(clients[nextClientId],currTurnData);
        } catch (IOException e) {
            e.printStackTrace();
            // TODO - should fix new players indexes
        }
        System.out.println("sent client " + nextClientId +":" + currTurnData);
    }


    // collect all users scores and place them in GameData
    private GameData getUpdate() {
        String scoresSummary = "";          // TODO - replace with StringBuilder
        for (int i = 0;i<scores.length;i++){
            scoresSummary += String.format("player%d:%d  ",i,scores[i]);
        }
        GameData updateGameData = new GameData(GameData.DataType.UPDATE);
        updateGameData.setContent("update", scoresSummary);
        return updateGameData;
    }

}
