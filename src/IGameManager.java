public interface IGameManager {

    void initGame( int clientId);

    void turnFinished();

    GameData getTurnGameData();

    String getTurnGameDataAnswer();

    void notifyClientExit(int id);

    void updateScore(int clientID ,int turnScore);
}
