import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManagerImpl implements IGameManager {

    private List<GameData>              turnsData;
    private List<String>                turnsDataAnswer;
    private final int                   NUM_PLAYERS;
    private List<ObjectOutputStream>    clientsOs;
    private List<ServerWorker>          clients;
    private HashMap<String,Integer>     scores ;

    private int allPlayersReady;

    GameManagerImpl(int numPlayers, List<ObjectOutputStream> clientsOs, List<ServerWorker> clients, List<GameData> turnsData, List<String> turnsDataAnswer){
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

        // Sending all clients scores update
        System.out.println("GameManagerImpl,turnFinished: client" + clients.get(0).getId() +" finished his turn");
        GameData update = collectUpdateStatus();
        sendAllClients(update);

        // pushing client that finished turn to end of queue
        pushFirstToLast(clientsOs);
        pushFirstToLast(clients);

        // TODO - refactor - turnsData should be separated for each player
        // removing question from question queue
        turnsData.remove(0);
        turnsDataAnswer.remove(0);

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
    public void notifyClientExit(int id) {
        if (clients.get(0).getId()==id){
            // client exited during his turn, removing it and initiating next turn
            System.out.println( "GameManagerImpl,notifyClientExit: client '" + id +"' exited during own turn, removing client");
            removeCurrentClient();

            // TODO - refactor - turnsData should be separated for each player
            if (!turnsData.isEmpty()) {
                turnsData.remove(0);
                turnsDataAnswer.remove(0);
            }
            nextTurn();
        }
    }

    @Override
    public void updateScore(int clientID, int turnScore) {
        int oldScore = scores.get(String.valueOf(clientID));
        scores.put(String.valueOf(clientID),oldScore+turnScore);
    }

    private void nextTurn(){

        //checking if there is a next turn -
        // game ended? any questions left?
        if (turnsData.isEmpty()){
            GameData summary = collectSummary();
            sendAllClients(summary);
            return;
        }
        // any clients left?
        if (clients.size() == 0){
            return;
        }

        // next turn is available
        GameData currTurnData = getTurnGameData();
        try {
            clients.get(0).sendClient(currTurnData);
            System.out.println("GameManagerImpl,nextTurn: sent client '" + clients.get(0).getId() +"':" + currTurnData);
        } catch (IOException e) {
            // TODO 1- bug / feature - check if player is the only one left
            e.printStackTrace();
            System.out.println("GameManagerImpl,nextTurn: client '" + clients.get(0).getId() +"' doesnt response.client removed.");
            clients.get(0).terminate(); // force os,is,socket close
            removeCurrentClient();

            nextTurn();
        }
    }

    private void removeCurrentClient(){
        int clientName = clients.get(0).getId();
        clientsOs.remove(0);
        clients.remove(0);

        // TODO - is this update should be here
        GameData update = new GameData(GameData.DataType.UPDATE);
        update.setContent("update" , "Client '" + clientName +"' disconnected" );
        sendAllClients(update);
    }

    // collect all users scores and place them in GameData
    private GameData collectUpdateStatus() {

        GameData updateGameData = new GameData(GameData.DataType.UPDATE);
        updateGameData.setContent("scores", getScoreSummary());
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
            scoresSummary += String.format("player%s:%d ",sc.getKey(),sc.getValue());
        }
        return scoresSummary;
    }

    private void pushFirstToLast(List l){
        Object obj = l.get(0);
        l.remove(0);
        l.add(obj);
    }

    private void sendAllClients(GameData s) {
        // sendAllClients is used for updates hence it doesnt throw any exceptions.
        // issues of client connection are handled in dedicated thread of client
        System.out.println("GameManagerImpl,sendAllClients: sending " + s);
        String json = ConnectionUtils.gameDataToJson(s);
        for (ObjectOutputStream os : clientsOs) {
            try{
                ConnectionUtils.sendObjectOutputStream(os, json);
            }catch (IOException e){
                // if send fails it will be handled and closed later
                //e.printStackTrace();
            }
        }
    }
}
