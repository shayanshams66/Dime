package cap3;

import java.util.ArrayList;

public class Cluster {

	private ArrayList<Contig> arrContig = new ArrayList<Contig>();
	
	
	public void addContig(Contig _contig){
		arrContig.add(_contig);
	}
	
	public Contig getContig(){
		return arrContig.get(arrContig.size()-1);
	}
	
	public Contig getContig(int _id){
		if(arrContig.get(_id - 1).getId()==_id){
			return arrContig.get(_id - 1);
		}else{
			System.out.println("error");
			return null;
		}
	}
	
	public Contig get(int _idx){
		return arrContig.get(_idx);
	}
	
	public int size(){
		return arrContig.size();
	}
}
