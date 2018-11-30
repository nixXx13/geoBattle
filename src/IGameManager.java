public interface IGameManager {

    public void initGame( int clientId);

    public void turnFinished();

    public GameData getTurnGameData();

    public String getTurnGameDataAnswer();

    void updateScore(int clientID ,int turnScore);
}
