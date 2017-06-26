package tw.imonkey.plc2go;

public class RegisterPLC {
    // Register address:{name:name;message:message;timeStamp:timeStamp}
    private String name ;
    private String message ;
    private Long timeStamp;
    public RegisterPLC(){
    }
    public RegisterPLC(String name,  String message,Long timeStamp){
        this.name=name;
        this.message=message;
        this.timeStamp=timeStamp;
    }
    public Long getTimeStamp() {
        return timeStamp;
    }
    public String getName(){return name;}
    public String getMessage(){return message;}
}
