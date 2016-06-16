package sshams2.cct.dime3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Spikes {
	
	public static int maxgap = 10;
	public static int hit_spikes_NoError = 7;
	
	private int size = 0;
	private static int defaultSize = 4000;
	private Map<Integer, Integer> cast = new HashMap<Integer, Integer>(defaultSize);
	private short[][] arrLocs;
	private int defNLoc = 0;
	private int row = 0;
	
	private int last = 0;
	private int hits_per_spike;
	private ArrayList<Integer> pruned = new ArrayList<Integer>();
	
	private SortArrayInt arrSpikes = new SortArrayInt(1000);
	
	public Spikes(int _nKmer, int _nLoc){
		arrLocs = new short[_nKmer][_nLoc];
		defNLoc = _nLoc;
	}
	
	public void add(int _kmer, short _loc){
		if(cast.containsKey(_kmer)){
			row = cast.get(_kmer);
			this.addLoc(row, _loc);
		}else{
			if(size>=this.arrLocs.length){
				this.reSize();
			}
			cast.put(_kmer, size);
			this.addLoc(size, _loc);
			size++;
		}
	}
	
	private void addLoc(int _row, short _loc){
		if(this.arrLocs[_row]==null){
			this.reSizeCollum(_row);
		}
		short len = (short) (this.arrLocs[_row][0] + 1);
		if(len>=this.arrLocs[_row].length){
			this.reSizeCollum(_row);
		}
		this.arrLocs[_row][len] = _loc;
		this.arrLocs[_row][0] = (short) (len);
	}
	
	public void addSpikes(int _kmer, short _loc){
		if(this.cast.containsKey(_kmer)){
			row = this.cast.get(_kmer);
			int len = this.arrLocs[row][0];
			for(int i=0;i<len;i++){
				this.arrSpikes.add(_loc - this.arrLocs[row][i+1]);
			}
		}
		/*if(this.arrSpikes.size()>40000){
			System.out.println("error in size of spike 4");
			System.exit(1);
		}*/
	}
	
	public void clear(){
		for(int i=0;i<arrLocs.length;i++){
			if(arrLocs[i]==null)continue;
			this.arrLocs[i][0] = 0;
		}
		size = 0;
		this.cast.clear();
		this.clearSpikes();
	}
	
	public void clearSpikes(){
		this.arrSpikes.clear();
	}
	
	public ArrayList<Integer> pruneSpikes(){
		pruned.clear();
		if(this.arrSpikes.size()==0)return pruned;
		arrSpikes.addLarge();
		last = this.arrSpikes.get(0);
		hits_per_spike = 0;
		for(int i=0;i<this.arrSpikes.size();i++){
			if(this.arrSpikes.get(i)-last>maxgap){
				if(hits_per_spike>hit_spikes_NoError){
					pruned.add(last);
				}
				last = this.arrSpikes.get(i);
				hits_per_spike = 1;
			}else{
				hits_per_spike++;
			}
		}
		arrSpikes.clear();
		/*if(pruned.size()>2000){
			System.out.println("error in size of spike 3");
			System.exit(1);
		}*/
		return pruned;
	}
	
	private void reSize(){
		short[][] arrNew = new short[this.arrLocs.length*3/2][];
		for(int i=0;i<this.arrLocs.length;i++){
			arrNew[i] = this.arrLocs[i];
		}
		this.arrLocs = arrNew;
		/*if(this.arrLocs.length>10000){
			System.out.println("error in size of spike 1");
			System.exit(1);
		}*/
	}
	
	private void reSizeCollum(int _idx){
		if(this.arrLocs[_idx]==null){
			this.arrLocs[_idx] = new short[defNLoc];
		}else{
			short[] arrNew = new short[this.arrLocs[_idx].length*3/2];
			for(int i=0;i<this.arrLocs[_idx].length;i++){
				arrNew[i] = this.arrLocs[_idx][i];
			}
			this.arrLocs[_idx] = arrNew;
		}
		/*if(this.arrLocs[_idx].length>200){
			System.out.println("error in size of spike 2");
			System.exit(1);
		}*/
	}
	
	/*public static void main(String[] args){
		Spikes obj = new Spikes();
		for(int i=10000;i<10020;i++){
			for(int j=0;j<800;j++){
				obj.add(i, j);
			}
		}
		for(int i=10021;i<10023;i++){
			for(int j=0;j<1020;j++)obj.add(i, j);
		}
		obj.pruneSpikes();
	}*/
	
}


