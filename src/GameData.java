import java.util.HashMap;

public class GameData implements java.io.Serializable{

    private DataType    type;
    private HashMap<String,String> content;

    public enum DataType{
        QUESTION,
        ANSWER,
        UPDATE,
        SKIP
    }
    public GameData(DataType type){
        this.type = type;
        content = new HashMap<>();
    }

    public DataType getType() {
        return type;
    }

    public String getContent(String key) {
        return content.get(key);
    }

    public void setContent(String key, String value){
        content.put(key,value);
    }

    @Override
    public String toString() {
        return type.toString() + ":" + content;
    }
}
