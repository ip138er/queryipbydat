using System;

namespace csharp
{
    public class IPInValidException : Exception
    {
        const string ERROR_MSG = "IP Illigel. Please input a valid IP.";
        public IPInValidException() : base(ERROR_MSG) { }
    }
    
    internal static class Utils
    {
        /// <summary>
        /// Get a int from a byte array start from the specifiled offset.
        /// </summary>
        public static long GetIntLong(byte[] b, int offset)
        {
            return (
                ((b[offset++] & 0x000000FFL)) |
                ((b[offset++] << 8) & 0x0000FF00L) |
                ((b[offset++] << 16) & 0x00FF0000L) |
                ((b[offset] << 24) & 0xFF000000L)
            );
        }

        public static int GetIntByte(byte[] b, int offset)
        {
            return (
                (b[offset] & 0x000000FF)
            );
        }

        public static long Ip2long(string ip)
        {
            string[] p = ip.Split('.');
            if (p.Length != 4) throw new IPInValidException();

            foreach (string pp in p)
            {
                if (pp.Length > 3) throw new IPInValidException();
                if (!int.TryParse(pp, out int value) || value > 255)
                {
                    throw new IPInValidException();
                }
            }
            var bip1 = long.TryParse(p[0], out long ip1);
            var bip2 = long.TryParse(p[1], out long ip2);
            var bip3 = long.TryParse(p[2], out long ip3);
            var bip4 = long.TryParse(p[3], out long ip4);

            if (!bip1 || !bip2 || !bip3 || !bip4
                || ip4 > 255 || ip1 > 255 || ip2 > 255 || ip3 > 255
                || ip4 < 0 || ip1 < 0 || ip2 < 0 || ip3 < 0)
            {
                throw new IPInValidException();
            }
            long p1 = ((ip1 << 24) & 0xFF000000);
            long p2 = ((ip2 << 16) & 0x00FF0000);
            long p3 = ((ip3 << 8) & 0x0000FF00);
            long p4 = ((ip4 << 0) & 0x000000FF);
            return ((p1 | p2 | p3 | p4) & 0xFFFFFFFFL);
        }

        public static string Long2ip(long ip)
        {
            return $"{(ip >> 24) & 0xFF}.{(ip >> 16) & 0xFF}.{(ip >> 8) & 0xFF}.{ip & 0xFF}";
        }
    }

}