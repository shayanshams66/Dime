package sshams2.cct.dime3;

public class SortedEdges {
	
	private Edge[] arr;
	private int[] arrIds;
	private int size;
	
	private int low = 0;
	private int high = 0;
	private int mid = 0;
	private boolean flagEqual = false;
	
	public SortedEdges(int _size){
		arr = new Edge[_size];
		for(int i=0;i<arr.length;i++){
			arr[i] = new Edge();
		}
		arrIds = new int[_size];
	}
	
	public void add(int _id, Edge _edge){
		if(size>=arr.length){
			reSize();
		}
		low = 0; high = size - 1;
		flagEqual = false;
		while(low<=high){
			mid = (low + high)/2;
			if(arrIds[mid]<_id){
				low = mid + 1;
			}else if(arrIds[mid]>_id){
				high = mid - 1;
			}else{
				flagEqual = true;
				break;
			}
		}
		if(flagEqual){
			System.out.println("Error in SortedEdges");
			System.exit(1);
		}else{
			for(int i=size;i>low;i--){
				arrIds[i] = arrIds[i-1];
				arr[i].set(arr[i-1]);
			}
			arrIds[low] = _id;
			arr[low].set(_edge);
			size++;
		}
	}
	
	public void clear(){
		size = 0;
	}
	
	public Edge get(int _idx){
		return arr[_idx];
	}
	
	private void reSize(){
		Edge[] arrNew = new Edge[arr.length*3/2];
		int[] arrIdsNew = new int[arr.length*3/2];
		for(int i=0;i<arr.length;i++){
			arrNew[i] = arr[i];
			arrIdsNew[i] = arrIds[i];
		}
		for(int i=arr.length;i<arrNew.length;i++){
			arrNew[i] = new Edge();
		}
		arr = arrNew;
		arrIds = arrIdsNew;
	}
	
	public int Size(){
		return size;
	}

}
