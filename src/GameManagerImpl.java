import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManagerImpl implements IGameManager {

    private MainManager                 mainManager;
    private List<GameData>              turnsData;
    private List<String>                turnsDataAnswer;
    private final int                   NUM_PLAYERS;
    private int                         playersJoined;
    private int                         playersReady;
    private String                      roomName;
    private List<ObjectOutputStream>    clientsOs;
    private List<ServerWorker>          clients;
    private HashMap<String,Integer>     scores ;

    GameManagerImpl(MainManager mainManager , String roomName , int numPlayers, List<ObjectOutputStream> clientsOs, List<ServerWorker> clients, List<GameData> turnsData, List<String> turnsDataAnswer){
        this.mainManager        = mainManager;
        this.clientsOs          = clientsOs;
        this.clients            = clients;
        this.NUM_PLAYERS        = numPlayers;
        this.turnsData          = turnsData;
        this.turnsDataAnswer    = turnsDataAnswer;
        this.roomName           = roomName;
        this.playersReady    = 0;
        this.playersJoined   = 0;
        scores = new HashMap<>();
    }

    @Override
    public void initGame(String clientName) {
        //init user score
        scores.put(clientName,0);

        playersReady +=1;
        if ( playersReady == NUM_PLAYERS ){
            nextTurn();
        }
    }

    @Override
    public void turnFinished() {

        // Sending all clients scores update
        GameData update = getGameStatus();
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
    public void clientExit(String id) {
        if (clients.get(0).getId().equals(id)){
            // client exited during his turn, removing it and initiating next turn
            System.out.println( roomName + ":GameManagerImpl,clientExit: client '" + id +"' exited during own turn, removing client");
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
    public void clientJoined(ServerWorker serverWorker ,ObjectOutputStream os){
        // updating other players new player joined
        GameData update = new GameData(GameData.DataType.UPDATE);
        update.setContent("update" , "Client '" + serverWorker.getId() +"' joined" );
        sendAllClients(update);

        clientsOs.add(os);
        clients.add(serverWorker);

        playersJoined +=1;
        if ( playersJoined == NUM_PLAYERS){
            for (int i = 0; i < NUM_PLAYERS; i++) {
                Thread p = new Thread(clients.get(i));
                p.start();
            }
        }
    }

    @Override
    public void updateScore(String clientID, int turnScore) {
        int oldScore = scores.get(clientID);
        scores.put(clientID,oldScore+turnScore);
    }

    private void nextTurn(){

        //checking if there is a next turn -
        // game ended? any questions left?
        if (turnsData.isEmpty()){
            GameData summary = getGameFinishedSummary();
            sendAllClients(summary);
            mainManager.removeGameManager(roomName);
            return;
        }
        // any clients left?
        if (clients.size() == 0){
            mainManager.removeGameManager(roomName);
            return;
        }

        // next turn is available
        GameData currTurnData = getTurnGameData();
        try {
            clients.get(0).sendClient(currTurnData);
            System.out.println(roomName + ":GameManagerImpl,nextTurn: sent client '" + clients.get(0).getId() +"':" + currTurnData);
        } catch (IOException e) {
            // TODO 1- bug / feature - check if player is the only one left
            e.printStackTrace();
            System.out.println(roomName + ":GameManagerImpl,nextTurn: client '" + clients.get(0).getId() +"' doesnt response.client removed.");
            clients.get(0).terminate();
            removeCurrentClient();

            nextTurn();
        }
    }

    private void removeCurrentClient(){
        String clientName = clients.get(0).getId();
        clientsOs.remove(0);
        clients.remove(0);

        // TODO - is this update should be here
        GameData update = new GameData(GameData.DataType.UPDATE);
        update.setContent("update" , "Client '" + clientName +"' disconnected" );
        sendAllClients(update);
    }

    // collect all users scores and place them in GameData
    private GameData getGameStatus() {

        GameData updateGameData = new GameData(GameData.DataType.UPDATE);
        updateGameData.setContent("scores", getScoreSummary());
        return updateGameData;
    }

    private GameData getGameFinishedSummary() {
        GameData summary = new GameData(GameData.DataType.FIN);
        summary.setContent("summary" , getScoreSummary());
        return summary;
    }

    private String getScoreSummary(){
        String scoresSummary = "";          // TODO - replace with StringBuilder
        for (Map.Entry<String,Integer> sc : scores.entrySet()){
            scoresSummary += String.format("%s:%d ",sc.getKey(),sc.getValue());
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
        System.out.println(roomName + ":GameManagerImpl,sendAllClients: sending " + s);
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
