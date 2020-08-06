<?php
/**
  *  ip138 php ip查询
  *
  **/
class Ip138{
    protected static $_instance = null;
    private static $ip     = NULL;
    private static $db     = NULL;
    private static $textOffset = 0;
    private static $total = 0;

    public static function getInstance() {
        if (!self::$_instance instanceof self){
            self::$_instance = new self();
            self::init();
        }

        return self::$_instance;
    }

    private static function init() {
        if (self::$db === NULL) {
            self::$db = fopen(__DIR__ . '/../ip.dat', 'rb');
            if (self::$db === FALSE){
                throw new Exception('Invalid ip.dat file!');
            }
        }
    }

    public static function query($ip) {
        if (empty($ip) === TRUE){
            return 'N/A';
        }
        //域名获取ip
        $ip = gethostbyname($ip);
        if (!preg_match('#^(25[0-5]|2[0-4]\d|[0-1]?\d?\d)(\.(25[0-5]|2[0-4]\d|[0-1]?\d?\d)){3}$#', $ip)){
            return 'N/A';
        }
        $iplong = ip2long($ip);
        // 初始化
        if (self::$db === NULL){
            self::init();
        }

        $text = self::find($iplong);

        return  $text;
    }

    private static function find($iplong){
        // 文本偏移量（4字节） + 索引区（256*4字节，第一段开始的记录位置） + 记录区（4字节结束ip+4字节文本偏移量+1字节文本长度） + 数据区
        fseek(self::$db,0);
        self::$textOffset = unpack('I', fread(self::$db, 4))[1];
        
        //ip段的数量
        self::$total = (self::$textOffset - 4 - 256*4) / 9;

        //分割索引值，abc.def.igh.lkm，为加快索引增加abc分割
        $first = $iplong>>24;
        if($first==0){
            fseek(self::$db, 4+($first)*4);
            $left = unpack('I', fread(self::$db, 4))[1];
            fseek(self::$db, 4+($first+1)*4);
            $right = unpack('I', fread(self::$db, 4))[1]-1;
        }elseif($first==255){
            fseek(self::$db, 4+($first-1)*4);
            $left = unpack('I', fread(self::$db, 4))[1]+1;
            $right = self::$total;
        }else{
            fseek(self::$db, 4+($first)*4);
            $left = unpack('I', fread(self::$db, 4))[1];
            fseek(self::$db, 4+($first+1)*4);
            $right = unpack('I', fread(self::$db, 4))[1]-1;
        }

        //读取各ip段数据（结束值、所在文本偏移值、所在文本长度）
        $i = 0;
        $text = 'N/A';
        while($i<24){//2^24二叉树查找最多16777216条数据
            $middle = floor(($left+$right)/2);
            if($middle==$left){
                $ipOffset = 4 + 256*4 + $right*9;
                fseek(self::$db, $ipOffset+4);
                $textOffset =  unpack('I', fread(self::$db, 4))[1];
                fseek(self::$db, $ipOffset+4+4);
                $textLength =  unpack('C', fread(self::$db, 1))[1];
                //var_dump(long2ip($middleIplong).' = '.$middle.' = '.$ipOffset);

                fseek(self::$db, self::$textOffset+$textOffset);
                $text =  fread(self::$db, $textLength);
                break;
            }

            $middleOffset = 4 + 256*4 + $middle*9;
            fseek(self::$db, $middleOffset);
            $middleIplong =  unpack('I', fread(self::$db, 4))[1];
            if($iplong <= $middleIplong){
                $right = $middle;
            }else{
                $left = $middle;
            }
            $i++;
        }

        return $text;
    }

    public function __destruct() {
        if (self::$db !== NULL && is_resource(self::$db))
        {
            fclose(self::$db);
        }
    }
}


function main(){
	$ip138 = Ip138::getInstance();
    var_dump($ip138->query('0.0.0.0'));
    var_dump($ip138->query('255.255.255.255'));
    var_dump($ip138->query('152.32.193.0'));
    var_dump($ip138->query('1.0.2.1'));
    var_dump($ip138->query('110.81.112.0'));

}

main();

?>