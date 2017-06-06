package tw.imonkey.plc2go;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Message {
    private String message;
    private String memberEmail;
    private Long timeStamp;

    public Message(String memberEmail,String message,Long timeStamp) {
        this.memberEmail= memberEmail;
        this.message=message;
        this.timeStamp=timeStamp;
    }
    public Message(){}

    public String getmemberEmail() {
        return memberEmail;
    }
    public String getMessage() {
        return message;
    }
    public Long getTimeStamp() {
        return timeStamp;
    }
}
