package tw.imonkey.plc2go;


public class Message {
    private String message;
    private Long timeStamp;
    public Message(String message,Long timeStamp) {

        this.message=message;
        this.timeStamp=timeStamp;
    }
    public Message(){}
    public String getMessage() {
        return message;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }
}
