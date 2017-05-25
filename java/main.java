public class main{
	
	public static void main(String[] args){
		
		IpLocation location=IpLocation.getInstance();
		String address=location.findLocation("117.25.157.102");
		System.out.println(address);

	}
	
}
