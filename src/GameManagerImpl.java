import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO - implement game finished

public class GameManagerImpl implements IGameManager {

    private List<GameData>              turnsData;
    private List<String>                turnsDataAnswer;
    private final int                   NUM_PLAYERS;
    private List<ObjectOutputStream>    clientsOs;
    private List<ServerWorker>          clients;
    private HashMap<String,Integer>     scores ;

    private int allPlayersReady;

    public GameManagerImpl(int numPlayers , List<ObjectOutputStream> clientsOs , List<ServerWorker> clients , List<GameData> turnsData , List<String> turnsDataAnswer){
        this.clientsOs          = clientsOs;
        this.clients            = clients;
        this.NUM_PLAYERS        = numPlayers;
        this.turnsData          = turnsData;
        this.turnsDataAnswer    = turnsDataAnswer;
        this.allPlayersReady    = 0;
        scores = new HashMap<>();
    }

    @Override
    public void initGame(int clientId) {
        //init user score
        scores.put(String.valueOf(clientId),0);

        allPlayersReady +=1;
        if ( allPlayersReady == NUM_PLAYERS ){
            nextTurn();
        }
    }

    @Override
    public void turnFinished() {

        System.out.println("client" + clients.get(0).getId() +" finished his turn");
        GameData update = collectUpdateStatus();
        ConnectionUtils.sendAllClients(clientsOs,update);

        // preparing data for next turn
        pushFirstToLast(clientsOs);
        pushFirstToLast(clients);
        turnsData.remove(0);
        turnsDataAnswer.remove(0);

        if (turnsData.isEmpty()){
            GameData summary = collectSummary();
            ConnectionUtils.sendAllClients(clientsOs,summary);
            return;
        }

        nextTurn();
    }

    @Override
    public GameData getTurnGameData() {
        return turnsData.get(0);
    }

    @Override
    public String getTurnGameDataAnswer() {
        return turnsDataAnswer.get(0);
    }

    @Override
    public void updateScore(int clientID, int turnScore) {
        int oldScore = scores.get(String.valueOf(clientID));
        scores.put(String.valueOf(clientID),oldScore+turnScore);
    }

    private void nextTurn(){
        GameData currTurnData = getTurnGameData();
        try {
            ConnectionUtils.sendClient(clientsOs.get(0),currTurnData);
            System.out.println("sent client '" + clients.get(0).getId() +"':" + currTurnData);
        } catch (IOException e) {
            // TODO 1- check if player is the only one left
            // TODO 2- check if any GameData is left
            e.printStackTrace();
            System.out.println("Client '" + clients.get(0).getId() +"' doesnt response.client removed.");
            // TODO 3- get client to properly close is,os and socket
            clientsOs.remove(0);
            clients.remove(0);

            nextTurn();
        }
    }

    // collect all users scores and place them in GameData
    private GameData collectUpdateStatus() {

        GameData updateGameData = new GameData(GameData.DataType.UPDATE);
        updateGameData.setContent("update", getScoreSummary());
        return updateGameData;
    }

    private GameData collectSummary() {
        GameData summary = new GameData(GameData.DataType.FIN);
        summary.setContent("summary" , getScoreSummary());
        return summary;
    }

    private String getScoreSummary(){
        String scoresSummary = "";          // TODO - replace with StringBuilder
        for (Map.Entry<String,Integer> sc : scores.entrySet()){
            scoresSummary += String.format("player%s:%d  ",sc.getKey(),sc.getValue());
        }
        return scoresSummary;
    }

    private void pushFirstToLast(List l){
        Object obj = l.get(0);
        l.remove(0);
        l.add(obj);
    }

}
