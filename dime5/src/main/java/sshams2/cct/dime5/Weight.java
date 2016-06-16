package sshams2.cct.dime5;

public class Weight {

	//id1<id2
	private int id1;
	private int id2;
	private byte val;
	
	public Weight(int _id1, int _id2, byte _val){
		id1 = _id1;
		id2 = _id2;
		val = _val;
	}
	
	public int getId(int _id){
		if(id1==_id){
			return id2;
		}else if(id2==_id){
			return id1;
		}else{
			System.out.println("error in Weight");
			return -1;
		}
	}
	
	public int getId2(){
		return id2;
	}
	
	public byte getVal(){
		return val;
	}
}
