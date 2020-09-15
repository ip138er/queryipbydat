using System;

namespace csharp
{
    class Program
    {
        static void Main(string[] args)
        {
            var _search = new IPLocation(Environment.CurrentDirectory + @"/../ip.dat");
           	string region = _search.Find("110.81.112.0");
            //string region = _search.Find("152.32.193.0");

            Console.WriteLine(region);
        }
    }
}
