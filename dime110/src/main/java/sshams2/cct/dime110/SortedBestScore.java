package sshams2.cct.dime110;

import java.util.Arrays;


public class SortedBestScore {

	private Edge[] arr = new Edge[10];
	private boolean[] arrKeep;
	private int size = 0;
	private int idx = 0;
	public int count = 0;
	
	public void add(Edge _bestScore){
		if(size>=arr.length){
			Edge[] arrNew = new Edge[arr.length*3/2];
			for(int i=0;i<arr.length;i++)arrNew[i] = arr[i];
			arr = arrNew;
		}
		arr[size] = _bestScore;
		size++;
	}
	
	public Edge pop(){
		if(idx>=size)return null;
		for(;idx<size;idx++){
			if(arrKeep[idx]){
				arrKeep[idx] = false;
				count++;
				return arr[idx];
			}
		}
		return null;
	}
	
	public int remove(int _id1, boolean _rev1, byte _STED1,
			int _id2, boolean _rev2, byte _STED2){
		for(int i=idx;i<size;i++){
			if(arrKeep[i]){
				if(arr[i].testIdEnd(_id1, _rev1, _STED1)){
					arrKeep[i] = false;
					count++;
					continue;
				}else if(arr[i].testIdEnd(_id2, _rev2, _STED2)){
					arrKeep[i] = false;
					count++;
				}
			}
		}
		return count;
	}
	
	public void setSTED(Contig[] _arr){
		for(int i=0;i<size;i++){
			arr[i].setSTED(_arr[arr[i].ID1()], _arr[arr[i].ID2()]);
		}
	}
	
	public int size(){
		return size;
	}
	
	public void sort(){
		Arrays.sort(arr, 0, size);
		arrKeep = new boolean[size];
		for(int i=0;i<size;i++){
			arrKeep[i] = true;
		}
	}
	
	
	
}