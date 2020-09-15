import java.io.*;
import java.nio.*; 

public class IpLocation2 {

    public static String PATH = "../ip.dat";
    private static IpLocation2 mInstance;
    public static IpLocation2 getInstance() {
        if (mInstance == null) {
            if(mInstance==null){
                mInstance = new IpLocation2();
            }
        }
        return mInstance;
    }
    private RandomAccessFile db;
    private long textOffset;
    private long total;

    public IpLocation2() {
        try {
            this.db = new RandomAccessFile(PATH, "r");

            this.textOffset = this.readIntLong(0);
            this.total = (this.textOffset - 4 - 256*4) / 9;
            //System.out.println(this.textOffset);
            //System.out.println(this.total);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String findLocation(String strIP){
        long ip = ipToLong(strIP);
        //System.out.println(this.longToIp(ip));

        long first = ip >> 24;
        long left,right;
        if (first == 0xff) {
            left = this.readIntLong(4+(first-1)*4)+1;
            right = this.total;
        }else{
            left = this.readIntLong(4+first*4);
            right = this.readIntLong(4+(first+1)*4)-1;
            if(right<1){
                right = this.total;
            }
        }

        int i = 0;
        while(i<24){
            long middle = new Double(Math.floor((left+right)/2)).longValue();
            if(middle==left){
                long ipOffset = 4 + 256*4 + right*9;
                long textOffset = this.readIntLong(ipOffset + 4);
                long textLength = this.readByteLong(ipOffset + 8);

                return this.readString(this.textOffset + textOffset, textLength);
            }

            //System.out.printf("%d %d %d\n", left, right, middle);
            long middleOffset = 4 + 256*4 + middle*9;
            long middleIp = this.readIntLong(middleOffset);
            if(ip<=middleIp){
                right = middle;
            }else{
                left = middle;
            }

            i++;
        }


        return "fail";
    }

    private long readIntLong(long offset){
        try {
            this.db.seek(offset);
            byte[] buffer = new byte[4];
            this.db.read(buffer);
            return bytesToLong(buffer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private long readByteLong(long offset){
        try {
            this.db.seek(offset);
            byte[] buffer = new byte[1];
            this.db.read(buffer);
            return buffer[0]&0xFF;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private String readString(long offset, long length){
        try {
            this.db.seek(new Long(offset).intValue());
            byte[] buffer = new byte[new Long(length).intValue()];
            this.db.read(buffer);
            return new String(buffer, "utf-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "";
    }

    private static int bytesToInt(byte[] b) {
        return (((b)[0] & 0xFF) | (((b)[1] << 8) & 0xFF00) | (((b)[2] << 16) & 0xFF0000) | (((b)[3] << 24) & 0xFF000000));
    }

    private static long bytesToLong(byte[] b) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[] {0, 0, 0, 0, b[3], b[2], b[1], b[0]});
        return bb.getLong();
    }

    private static long ipToLong(String strIp) {
        long[] ip = new long[4];
        int position1 = strIp.indexOf(".");
        int position2 = strIp.indexOf(".", position1 + 1);
        int position3 = strIp.indexOf(".", position2 + 1);
        ip[0] = Long.parseLong(strIp.substring(0, position1));
        ip[1] = Long.parseLong(strIp.substring(position1 + 1, position2));
        ip[2] = Long.parseLong(strIp.substring(position2 + 1, position3));
        ip[3] = Long.parseLong(strIp.substring(position3 + 1));
        return ((ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3]);
    }

    private static String longToIp(Long longIp){
        return ((longIp >> 24) & 0xFF) + "." +
                ((longIp >> 16) & 0xFF) + "." +
                ((longIp >> 8) & 0xFF) + "." +
                (longIp & 0xFF);
    }


}
