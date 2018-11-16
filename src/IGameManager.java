public interface IGameManager {

    public void initGame( int clientId);

    public void turnFinished();

    public boolean isClientTurn(int clientId);

    public GameData getTurnGameData();

    public String getTurnGameDataAnswer();

    void updateScore(int clientID ,int turnScore);
}
