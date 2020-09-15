using System;
using System.IO;
using System.Text;
using System.Threading.Tasks;

namespace csharp
{
    public class IPLocation : IDisposable
    {
        private Stream _raf = null;

        private long _textOffset = 0;
        private long _total = 0;


        public IPLocation(string dbFile)
        {
            _raf = new FileStream(dbFile, FileMode.Open, FileAccess.Read, FileShare.Read);

             _raf.Seek(0, SeekOrigin.Begin);
            byte[] buffer = new byte[4];
            _raf.Read(buffer, 0, buffer.Length);
            _textOffset = Utils.GetIntLong(buffer, 0);
            _total = (_textOffset - 4 - 256 * 4) / 9;

            //Console.WriteLine("{0} {1}", _textOffset, _total);
        }

        private long readIntData(long offset)
        {
             _raf.Seek(offset, SeekOrigin.Begin);
            byte[] buffer = new byte[4];
            _raf.Read(buffer, 0, buffer.Length);
            return Utils.GetIntLong(buffer, 0);
        }

        private int readByteData(long offset)
        {
            _raf.Seek(offset, SeekOrigin.Begin);
            byte[] buffer = new byte[1];
            _raf.Read(buffer, 0, buffer.Length);
            return Utils.GetIntByte(buffer, 0);
        }

        public string Find(string ip)
        {
            long iplong = Utils.Ip2long(ip);
            string region = "N/A";

            long left = 0;
            long right = 0;
            long first = (long)((iplong >> 24)&0xFF);
            if(first == 0xff) {
                left = readIntData(4+(first-1)*4)+1;
                right = _total;
            }else{
                left = readIntData(4+first*4);
                right = readIntData(4+(first+1)*4)-1;
                if(right<1){
                    right = _total;
                }
            }
            //Console.WriteLine("{0} {1} {2}", first, left, right);

            int i=0;
            while(i<23){
                long middle = (long)Math.Floor((double)((left+right)/2));
                if(middle == left){
                    long ipOffset = 4 + 256*4 + right*9;
                    long textOffset = readIntData(ipOffset+4);
                    int textLength = readByteData(ipOffset+8);

                    _raf.Seek((long)(_textOffset+textOffset), SeekOrigin.Begin);
                    byte[] buffer = new byte[textLength];
                    _raf.Read(buffer, 0, buffer.Length);
                    region = Encoding.UTF8.GetString(buffer, 0, buffer.Length);

                    break;
                }

                //Console.WriteLine("{0} {1} {2}", left, right, middle);
                long middleOffset = 4 + 256*4 + middle*9;
                long middleIp = readIntData(middleOffset);
                if(iplong <= middleIp){
                    right = middle;
                }else{
                    left = middle;
                }
                i++;
            }

            return region;
        }

        public void Close()
        {
            _raf.Close();
        }

        public void Dispose()
        {
            Close();
        }
    }

}