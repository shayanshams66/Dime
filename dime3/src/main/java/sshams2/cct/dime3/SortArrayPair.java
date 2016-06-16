package sshams2.cct.dime3;

/**
 * use for kmer and id to replace hashmap
 * @author Xuan
 *
 */
public class SortArrayPair {
	
	private int size = 0;
	private int[] arrIds;
	private short[] arrVals;
	
	private int low = 0;
	private int high = 0;
	private int mid = 0;
	private boolean flagEqual = false;
	
	public SortArrayPair(int _size){
		arrIds = new int[_size];
		arrVals = new short[_size];
	}
	
	/**
	 * add a pair of id and value to the list, if not in the list then create a new element ,
	 * if exist then add up the new value
	 * @param _id
	 * @param _val
	 */
	public void add(int _id, short _val){
		if(size>=arrIds.length){
			resize();
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
			arrVals[mid] += _val;
		}else{
			for(int i=size;i>low;i--){
				arrIds[i] = arrIds[i-1];
				arrVals[i] = arrVals[i-1];
			}
			arrIds[low] = _id;
			arrVals[low] = _val;
			size++;
		}
	}
	
	/**
	 * resize the size of list to 0
	 */
	public void clear(){
		size = 0;
	}
	
	public int getId(int _idx){
		return arrIds[_idx];
	}
	
	public int getSize(){
		return size;
	}
	
	/**
	 * return 0 means the give id is not in the list
	 * @param _id
	 * @return
	 */
	public short inArray(int _id){
		low = 0; high = size - 1;
		flagEqual = false;
		while(low<high){
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
			return arrVals[mid];
		}else{
			return 0;
		}
	}
	
	public short getVal(int _idx){
		return arrVals[_idx];
	}
	
	private void resize(){
		int[] arrIdsNew = new int[arrIds.length*3/2];
		short[] arrValsNew = new short[arrVals.length*3/2];
		for(int i=0;i<arrIds.length;i++){
			arrIdsNew[i] = arrIds[i];
			arrValsNew[i] = arrVals[i];
		}
		arrIds = arrIdsNew;
		arrVals = arrValsNew;
	}
	
	
	/*public static void main(String[] args){
		int[] x1 = {1,22,4,5,8,7,91,1};
		short[] x2 = {1,1,1,1,1,1,1,2};
		SortArrayPair obj = new SortArrayPair(2);
		for(int i=0;i<x1.length;i++){
			obj.add(x1[i], x2[i]);
		}
		System.out.println(obj.getSize());
	}
*/
}
