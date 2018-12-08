import java.io.ObjectOutputStream;

public interface IGameManager {

    void initGame( String clientId);

    void turnFinished();

    GameData getTurnGameData();

    String getTurnGameDataAnswer();

    void clientExit(String id);

    void updateScore(String clientID ,int turnScore);

    void clientJoined(ServerWorker serverWorker , ObjectOutputStream os);
}
