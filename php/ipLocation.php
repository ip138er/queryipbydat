<?php
/**
  *  ip138php ip查询
  *  注意：php版本只能使用64位
  *  能处理离线库加载进内存使用此代码
  *
  **/
ini_set("memory_limit","-1");
/**
* 
*/
class Ip138{
	
	protected static $_instance = null;
    private static $ip     = NULL;
    private static $db     = NULL;
    private static $index  = array();
    private static $textData = NULL;
    private static $idx_start = NULL;
    private static $total = 0;
    private static $ipEndAddr = array();
    private static $textOffset = array();
    private static $textLen = array();


    public static function getInstance(){
        if (!self::$_instance instanceof self){
            self::$_instance = new self();
            self::init();
        }

        return self::$_instance;
    }

    public static function query($ip){
        if (empty($ip) === TRUE){
            return 'N/A';
        }
        $nip   = gethostbyname($ip);
        $ipdot = explode('.', trim($nip));
        if ($ipdot[0] < 0 || $ipdot[0] > 255 || count($ipdot) !== 4)
        {
            return 'N/A';
        }
        $ip = ip2long($ip);
        if (self::$db === NULL)
        {
            self::init();
        }

        $end = 0;
        //self::$index 0开始索引，往前对一位偏差
        if (($ip>>24)!=0xff){
            $end = self::$index[($ip>>24)+1];
        }

        if ($end == 0){
            $end = count(self::$ipEndAddr);
        }

        $i = self::find($ip, self::$index[$ip>>24], $end);
        $addr = substr(self::$textData, self::$textOffset[$i], self::$textLen[$i]);
        return  $addr;
    }

    private static function find($ip, $left, $right){
        //使用二分法查找网络字节编码的IP地址的索引记录
        if ($right <= $left){
            return $right;
        }
        $m = intval(($left + $right) / 2);
        $new_ip = self::$ipEndAddr[$m];
        if ($ip <= $new_ip){
            return self::find($ip, $left, $m);
        }else{
            return self::find($ip, $m+1, $right);
        }
    }

    private static function init()
    {
        if (self::$db === NULL)
        {
            self::$db = fopen(__DIR__ . '/../ip.dat', 'rb');
            if (self::$db === FALSE)
            {
                throw new Exception('Invalid ip.dat file!');
            }
            self::read_data();
        }
    }

    private static function read_data(){
        //文本数据偏移值
        fseek(self::$db,0);
        self::$idx_start = unpack('I', fread(self::$db, 4))[1];
        //ip段的数量 = (文本数据偏移值 - 文本数据offset值，int4字节 - 分割索引量256*int4) / 每条记录占用字节9
        self::$total = (self::$idx_start - 4 - 256*4) / 9;
        //读取文本数据
        fseek(self::$db,self::$idx_start);
        while (!feof(self::$db)){
            self::$textData.= fread(self::$db,4096);
        }
        //分割索引值，abc.def.igh.lkm，为加快索引增加abc分割
        fseek(self::$db,0);
        for($i=0;$i<256;$i++){
            $off = 4+4*$i;          
            fseek(self::$db, $off);
            self::$index[$i] =  unpack('I', fread(self::$db, 4))[1];
        }

        //读取各ip段数据（结束值、所在文本偏移值、所在文本长度）
        for($i=0;$i<self::$total;$i++){
           $off = 4 + 256*4 + $i*9;
           fseek(self::$db,$off);
           self::$ipEndAddr[$i] =  unpack('I', fread(self::$db, 4))[1];
           fseek(self::$db,$off+4);
           self::$textOffset[$i] =  unpack('I', fread(self::$db, 4))[1];
           fseek(self::$db,$off+4+4);
           self::$textLen[$i] =  unpack('C', fread(self::$db, 1))[1];
        }

        return self::$index;
    }
    public function __destruct()
    {
        if (self::$db !== NULL && is_resource(self::$db))
        {
            fclose(self::$db);
        }
    }
}


function main(){
	$ip = '255.255.255.255';
	$ip138 = Ip138::getInstance();
	$a = $ip138->query($ip);
	$output = sprintf("%s %s\n",$ip, $a);
    echo $output;
}
main();

?>