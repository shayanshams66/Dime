package sshams2.cct.dime5;

public class BinaryMap {

	private int[] sortedArray;
	private int[] indexArray;
	private int size = 0;
	private int low;
	private int mid;
	private int high;
	private boolean flagEqual;
	
	public BinaryMap(int _size){
		sortedArray = new int[_size];
		indexArray = new int[_size];
	}
	
	public void add(int _id, int _idx){
		if(size>=sortedArray.length){
			reSize();
		}
		low = 0; high = size - 1;
		flagEqual = false;
		while(low<=high){
			mid = (low + high)/2;
			if(sortedArray[mid]<_id){
				low = mid + 1;
			}else if(sortedArray[mid]>_id){
				high = mid - 1;
			}else{
				flagEqual = true;
				break;
			}
		}
		if(flagEqual){
			for(int i=size;i>mid;i--){
				sortedArray[i] = sortedArray[i-1];
				indexArray[i] = indexArray[i-1];
			}
			sortedArray[mid] = _id;
			indexArray[mid] = _idx;
			size++;
		}else{
			for(int i=size;i>low;i--){
				sortedArray[i] = sortedArray[i-1];
				indexArray[i] = indexArray[i-1];
			}
			sortedArray[low] = _id;
			indexArray[low] = _idx;
			size++;
		}
	}
	
	public int getSorted(int _idx){
		return sortedArray[_idx];
	}
	
	public int getIndexArray(int _idx){
		return indexArray[_idx];
	}
	
	public int Size(){
		return size;
	}
	
	private void reSize(){
		int[] arrNew1 = new int[sortedArray.length*3/2];
		int[] arrNew2 = new int[sortedArray.length*3/2];
		for(int i=0;i<sortedArray.length;i++){
			arrNew1[i] = sortedArray[i];
			arrNew2[i] = indexArray[i];
		}
		sortedArray = arrNew1;
		indexArray = arrNew2;
	}
}
