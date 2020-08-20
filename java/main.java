public class main{
	
	public static void main(String[] args){
		
		//javac main.java IpLocation2.java ipLocation.java && java main

		//方法一载入内存查找
		IpLocation location=IpLocation.getInstance();
		String address=location.findLocation("117.132.195.99");
		System.out.println(address);

		//方法二文件查找
		IpLocation2 location2=IpLocation2.getInstance();
		String address2;
		address2=location2.findLocation("0.0.0.0");
		System.out.println(address2);
		address2=location2.findLocation("255.255.255.255");
		System.out.println(address2);
		address2=location2.findLocation("152.32.193.0");
		System.out.println(address2);
		address2=location2.findLocation("1.0.2.1");
		System.out.println(address2);
		address2=location2.findLocation("110.81.112.0");
		System.out.println(address2);

	}
	
}
