package tw.imonkey.plc2go;

import java.nio.ByteBuffer;



public class ByteUtil {
    int byte2int(byte[] bytes){
        return ByteBuffer.wrap(bytes).getInt();
    }
    byte[] int2byte(int I){
        return  ByteBuffer.allocate(4).putInt(I).array();
    }
    long byte2long(byte[] bytes){
        return ByteBuffer.wrap(bytes).getLong();
    }
    byte[] int2long(long L){
        return  ByteBuffer.allocate(8).putLong(L).array();
    }
    float byte2float(byte[] bytes){
        return ByteBuffer.wrap(bytes).getFloat();
    }
    byte[] float2byte(float F){
        return  ByteBuffer.allocate(4).putFloat(F).array();
    }
    double byte2double(byte[] bytes){
        return ByteBuffer.wrap(bytes).getDouble();
    }
    byte[] double2byte(float D){
        return  ByteBuffer.allocate(8).putDouble(D).array();
    }
}
