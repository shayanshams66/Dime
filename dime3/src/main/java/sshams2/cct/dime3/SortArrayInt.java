package sshams2.cct.dime3;

public class SortArrayInt {
	
	private int[] arr;
	private int size;
	
	private int low = 0;
	private int high = 0;
	private int mid = 0;
	private boolean flagEqual = false;
	
	private static int LARGE = Integer.MAX_VALUE/2;
	
	public SortArrayInt(int _n){
		size = 0;
		arr = new int[_n];
	}

	public void add(int _val){
		if(size>=arr.length){
			reSize();
		}
		low = 0; high = size - 1;
		flagEqual = false;
		while(low<=high){
			mid = (low + high)/2;
			if(arr[mid]<_val){
				low = mid + 1;
			}else if(arr[mid]>_val){
				high = mid - 1;
			}else{
				flagEqual = true;
				break;
			}
		}
		if(flagEqual){
			for(int i=size;i>mid;i--){
				arr[i] = arr[i-1];
			}
			arr[mid] = _val;
			size++;
		}else{
			for(int i=size;i>low;i--){
				arr[i] = arr[i-1];
			}
			arr[low] = _val;
			size++;
		}
	}
	
	public void addLarge(){
		if(size>=arr.length){
			reSize();
		}
		arr[size] = LARGE;
		size++;
	}
	
	public void clear(){
		size = 0;
	}
	
	public int get(int _idx){
		return arr[_idx];
	}
	
	public int size(){
		return size;
	}
	
	private void reSize(){
		int[] arrNew = new int[this.arr.length*3/2];
		for(int i=0;i<this.arr.length;i++){
			arrNew[i] = arr[i];
		}
		arr = arrNew;
	}
}
