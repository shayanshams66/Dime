package mer;

public class AlignReadSet {

	public AlignRead[] arrLeftReadContig;
	public int sizeLeft;
	public AlignRead[] arrRightReadContig;
	public int sizeRight;
	
	public static int sizeRead = 3000;
	
	public int[] hm = new int[5000];
	public int stHm = 1000;
	public int[] interCon = new int[5000];
	
	public AlignReadSet(){
		arrLeftReadContig = new AlignRead[30];
		for(int i=0;i<arrLeftReadContig.length;i++){
			arrLeftReadContig[i] = new AlignRead(sizeRead);
		}
		arrRightReadContig = new AlignRead[30];
		for(int i=0;i<arrRightReadContig.length;i++){
			arrRightReadContig[i] = new AlignRead(sizeRead);
		}
		sizeLeft = 0;
		sizeRight = 0;
	}
	
	public int align(Alignment _aligner, byte[] _concensus, int _sizeCon){
		for(int i=0;i<sizeLeft;i++) arrLeftReadContig[i].align(_aligner, _concensus, _sizeCon);
		for(int i=0;i<sizeRight;i++) arrRightReadContig[i].align(_aligner, _concensus, _sizeCon);
		for(int i=0;i<hm.length;i++)hm[i] = 0;
		for(int i=0;i<sizeLeft;i++)arrLeftReadContig[i].count(hm);
		for(int i=0;i<sizeRight;i++)arrRightReadContig[i].count(hm);
		int idx = 0;
		for(int i=0;i<stHm;i++){
			if(hm[i]>0){
				for(int j=0;j<hm[i];j++){
					interCon[idx] = 5;
					idx++;
				}
			}
		}
		for(int i=stHm;i<hm.length;i++){
			if(hm[i]>0){
				if(i-stHm<_sizeCon){
					interCon[idx] = _concensus[i-stHm];
					idx++;
				}else{
					interCon[idx] = 5;
					idx++;
				}
				for(int j=1;j<hm[i];j++){
					interCon[idx] = 5;
					idx++;
				}
			}
		}
		for(int i=0;i<sizeLeft;i++) arrLeftReadContig[i].apply(hm);
		for(int i=0;i<sizeRight;i++) arrRightReadContig[i].apply(hm);
		return idx;
	}
	
	/**
	 * get reads all connected to the contigs
	 * @param _stL
	 */
	public void connectReadLeft(int _stL){
		for(int i=0;i<sizeLeft;i++)
			arrLeftReadContig[i].connectToReadLeft(_stL);
		/*for(int i=0;i<sizeRight;i++)
			arrRightReadContig[i].connectToReadRight(_stL);*/
	}
	
	public void connectReadRight(int _stL){
		/*for(int i=0;i<sizeLeft;i++)
			arrLeftReadContig[i].connectToReadLeft(_stL);*/
		for(int i=0;i<sizeRight;i++)
			arrRightReadContig[i].connectToReadRight(_stL);
	}
	
	/**
	 * clean the size of left and right array
	 */
	public void clean(){
		sizeLeft = 0;
		sizeRight = 0;
	}
	
	public int[] getInterCon(){
		return interCon;
	}
	
	/**
	 * get the hit and miss on _idx
	 * @param _idx
	 * @param _consensus
	 * @param _arr
	 */
	public void getHitMiss(int _idx, int[] _consensus, int[] _arr){
		for(int i=0;i<_arr.length;i++)_arr[i] = 0;
		int x = _consensus[_idx];
		int y = 0;
		for(int i=0;i<sizeLeft;i++){
			y = arrLeftReadContig[i].get(_idx);
			if(y==-1){
				_arr[3]++;
				continue;
			}else if(y==x){
				_arr[0]++;
			}else{
				_arr[1]++;
			}
		}
		for(int i=0;i<sizeRight;i++){
			y = arrRightReadContig[i].get(_idx);
			if(y==-1){
				_arr[3]++;
				continue;
			}else if(y==x){
				_arr[0]++;
			}else{
				_arr[1]++;
			}
		}
	}
	
	/**
	 * get count on 0, 1, 2, 3, 4, 5
	 * @param _idx
	 * @param _consensus
	 * @param _arr
	 */
	public void getPosCount(int _idx, int[] _consensus, int[] _arr){
		for(int i=0;i<_arr.length;i++)_arr[i] = 0;
		int y = 0;
		for(int i=0;i<sizeLeft;i++){
			y = arrLeftReadContig[i].get(_idx);
			if(y==-1){
				continue;
			}
			_arr[y]++;
		}
		for(int i=0;i<sizeRight;i++){
			y = arrRightReadContig[i].get(_idx);
			if(y==-1){
				continue;
			}
			_arr[y]++;
		}
		y = -1;
		int largest = -1;
		for(int i=0;i<6;i++){
			if(largest<_arr[i]){
				largest = _arr[i];
				y = i;
			}
		}
		_consensus[_idx] = y;
	}
	
	public AlignRead popLeft(){
		if(sizeLeft>=arrLeftReadContig.length){
			AlignRead[] arrNew = new AlignRead[arrLeftReadContig.length*3/2];
			for(int i=0;i<arrLeftReadContig.length;i++)arrNew[i] = arrLeftReadContig[i];
			for(int i=arrLeftReadContig.length;i<arrNew.length;i++)arrNew[i] = new AlignRead(sizeRead);
			arrLeftReadContig = arrNew;
		}
		sizeLeft++;
		return arrLeftReadContig[sizeLeft-1];
	}
	
	public AlignRead popRight(){
		if(sizeRight>=arrRightReadContig.length){
			AlignRead[] arrNew = new AlignRead[arrRightReadContig.length*3/2];
			for(int i=0;i<arrRightReadContig.length;i++)arrNew[i] = arrRightReadContig[i];
			for(int i=arrRightReadContig.length;i<arrNew.length;i++)arrNew[i] = new AlignRead(sizeRead);
			arrRightReadContig = arrNew;
		}
		sizeRight++;
		return arrRightReadContig[sizeRight-1];
	}
	
	
	public void testLenRead(int _size){
		if(arrLeftReadContig[0].seq.length<_size){
			for(int i=0;i<arrLeftReadContig.length;i++){
				arrLeftReadContig[i].resize(_size);
			}
			for(int i=0;i<arrRightReadContig.length;i++){
				arrRightReadContig[i].resize(_size);
			}
		}
	}
	
}
