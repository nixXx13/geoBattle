import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {

        MainManager mainManager = new MainManager();
        mainManager.start();

    }

    /*
    TODO - Fetch data from DB
     */
    static void getData(List<GameData> data, List<String> answers, int numPlayers) {
        for(int i = 0 ; i<4 ; i++) {
            GameData questionGameData = new GameData(GameData.DataType.QUESTION);
            questionGameData.setContent("question", "1+"+i);
            questionGameData.setContent("pAnswer0", ""+i);
            questionGameData.setContent("pAnswer1", ""+(i+1));
            questionGameData.setContent("pAnswer2", "" +(i+2));
            questionGameData.setContent("pAnswer3", "" +(i+3));
            data.add(questionGameData);
            answers.add(String.valueOf(i+1));
        }
    }

}

