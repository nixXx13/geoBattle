public interface IGameManager {

    public void initGame( int clientId);

    public void turnFinished();

    public boolean isClientTurn(int clientId);

    public GameData getTurnData();

    public String getTurnDataAnswer();
}
