public class GameData {

    private DataType    type;
    private String      content;

    public enum DataType{
        QUESTION,
        ANSWER,
        UPDATE,
        SKIP
    }
    public GameData(DataType type,String content){
        this.type = type;
        this.content=content;
    }

    public DataType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return type.toString() + ":" + content;
    }
}
