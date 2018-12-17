import java.util.HashMap;

public class GameData implements java.io.Serializable{

    // TODO - make create gamedata with content
    private DataType    type;
    private HashMap<String,String> content;

    public enum DataType{
        QUESTION,
        ANSWER,
        UPDATE,
        SKIP,
        FIN
    }
    GameData(DataType type){
        this.type = type;
        content = new HashMap<>();
    }

    DataType getType() {
        return type;
    }

    String getContent(String key) {
        return content.get(key);
    }

    void setContent(String key, String value){
        content.put(key,value);
    }

    @Override
    public String toString() {
        return type.toString() + ":" + content;
    }
}
