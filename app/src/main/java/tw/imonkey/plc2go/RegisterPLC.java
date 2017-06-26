package tw.imonkey.plc2go;



public class RegisterPLC {
    private String Register ;
    private String name ;
    private String message ;
    private Long timeStamp;
    public RegisterPLC(){
    }
    public RegisterPLC(String Register,String name,  String message,Long timeStamp){
        this.Register=Register;
        this.name=name;
        this.message=message;
        this.timeStamp=timeStamp;
    }
    public Long getTimeStamp() {
        return timeStamp;
    }
    public String getRegister(){return Register;}
    public String getName(){return name;}
    public String getMessage(){return message;}
}
