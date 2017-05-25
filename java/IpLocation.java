import java.io.*;
import java.util.Arrays;

public class IpLocation {

    public static String PATH = "../ip.dat";
    private static IpLocation mInstance;
    static byte[] lock=new byte[0];
    public static IpLocation getInstance() {
        if (mInstance == null) {
            synchronized (lock){
                if(mInstance==null){
                    mInstance = new IpLocation();
                }
            }
        }
        return mInstance;
    }


    private int index[];
    private long ipEndArr[];
    private long textOffset[];
    private long textLen[];
    private byte[] textData;
    private int cout;

    public IpLocation() {
        index = new int[256];
        initIpLocation();
    }

    private void initIpLocation() {
        byte[] data = readFile();
        int len = (int) (new File(PATH)).length();
        byte[] buf = readByte(4, 0, data);
        long textOffset = B2IL(buf);
        this.cout = (int)((textOffset - 4 - 256 * 4) / 9);
        for (int i = 0; i < 256; i++) {
            int offset = 4 + i * 4;
            byte[] temp = readByte(4, offset, data);
            this.index[i] =(int) B2IL(temp);
        }
        this.textData = readByte((int)(len - textOffset),(int)(textOffset), data);
        this.ipEndArr = new long[this.cout];//
        this.textOffset = new long[this.cout];
        this.textLen = new long[this.cout];
        for (int i = 0; i < this.cout; i++) {
            int offset = 4 + 1024 + i * 9;
            byte[] temp = readByte(4, offset, data);
            this.ipEndArr[i] = toUint(B2IL(temp));
            temp = readByte(4, offset + 4, data);
            this.textOffset[i] =toUint(B2IL(temp));
            this.textLen[i] = toUint(data[offset + 8]);
        }

    }


    public synchronized String findLocation(String strIP) {
        long ip = ipToLong(strIP);
        int end = 0;
        if (ip >> 24 != 0xff) {
            end = this.index[(int) ((ip >> 24) + 1)];
        }
        if (end == 0) {
            end = this.cout;
        }
        int index = findIndexOffset(ip, this.index[(int)( ip >> 24)], end, this);
        byte[] res = readByte((int)this.textLen[index], (int)this.textOffset[index], this.textData);
        return byteToString(res);
    }


    private static String byteToString(byte[] data) {
        String result = "";
        try {
            result = new String(data, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static int findIndexOffset(long ip, int start, int end, IpLocation location) {
        while (start < end) {
            int mid = (start + end) / 2;
            if (ip > location.ipEndArr[mid]) {
                start = mid + 1;
            } else {
                end = mid;
            }
        }
        if (location.ipEndArr[end] >= ip) {
            return end;
        }
        return start;
    }


    private  static byte[] readByte(int cout, int offset, byte[] source) {
        return Arrays.copyOfRange(source, offset, (offset + cout));
    }

    private static byte[] readFile() {
        byte[] res = null;
        try {
            File file = new File(PATH);
            res = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            fis.read(res);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;

    }

    private static long B2IL(byte[] b) {
        return (((b)[0] & 0xFF) | (((b)[1] << 8) & 0xFF00) | (((b)[2] << 16) & 0xFF0000) | (((b)[3] << 24) & 0xFF000000));
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
        return toUint ((ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3]);
    }
    static long MAX = (1L << 32) - 1;

    /**
     * 转化成C语言 uint 类型
     * @param value
     * @return uint
     */
    private static long toUint(long value){
        return value&MAX;
    }


}
