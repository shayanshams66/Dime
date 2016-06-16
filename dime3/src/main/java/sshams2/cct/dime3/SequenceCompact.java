package sshams2.cct.dime3;

public class SequenceCompact {
	
	private int id;
	private int[] seq;//one integer stores 10 bases
	private int len;
	private int size;
	private static int mask = 7;
	
	public SequenceCompact(int _id, String _seq, int _st){
		id = _id;
		size = _seq.length() - _st;
		len = size/10;
		if(size%10>0)len++;
		seq = new int[len];
		int j = 0;
		for(int i=0;i<size;i++){
			j = i/10;
			seq[j] = add(_seq.charAt(i+_st), seq[j]);
		}
	}
	
	private int add(char _base, int _val){
		_val = _val<<3;
		_val += toHash(_base);
		return _val;
	}
	
	public int getId(){
		return id;
	}
	
	public int getSize(){
		return size;
	}
	
	private int getVal(int _idx, int _idy){
		_idy = (10 - _idy - 1)*3;
		
		return ((seq[_idx]>>_idy)&mask);
	}
	
	private int getVal(int _idx, int _idy, int _total){
		_idy = (_total - _idy - 1)*3;
		return ((seq[_idx]>>_idy)&mask);
	}
	
	public int toIntArray(int[] _arr){
		int j = 0;
		for(int i=0;i<len-1;i++){
			for(int k=0;k<10;k++){
				_arr[j] = getVal(i, k);
				j++;
				if(j>=size)break;
			}
			if(j>=size)break;
		}
		int i = size%10;
		i = i==0?10:i;
		for(int k=0;k<10;k++){
			_arr[j] = getVal(len-1,k,i);
			j++;
			if(j>=size)break;
		}
		return size;
	}
	
	public int toIntArrayRev(int[]_arr){
		toIntArray(_arr);
		for(int i=0;i<size/2;i++){
			if(_arr[i]!=4)_arr[i] = 3 -_arr[i];
			int j = size-i-1;
			if(_arr[j]!=4)_arr[j] = 3 - _arr[j];
			int k = _arr[j];
			_arr[j] = _arr[i];
			_arr[i] = k;
		}
		if(size%2!=0){
			int i = size/2;
			if(_arr[i]!=4)_arr[i] = 3 - _arr[i];
		}
		return size;
	}
	
	public static char toChar(int _v){
		switch(_v){
		case 0:return 'A';
		case 1:return 'C';
		case 2:return 'G';
		case 3:return 'T';
		case 4:return 'N';
		default:return 'X';
		}
	}
	
	/**
	 * convert from ACGT to 0123 and N is changed to 4
	 * @param _a
	 * @return
	 */
	private int toHash(char _a){
		switch (_a){
		case 'A':
		case 'a':return 0;
		case 'C':
		case 'c':return 1;
		case 'G':
		case 'g':return 2;
		case 'T':
		case 't':return 3;
		case 'N':
		case 'n':return 4;
		case 'R':return 4;
		case 'Y':return 4;
		case 'W':return 4;
		default:
			//System.out.println("Error in inputSequence: "+_a);
			//System.exit(1);
			return 4;
		}		
	}

	public void clean() {
		// TODO Auto-generated method stub
		id = -1;
		len = 0;
		size = 0;
	}
	
	/*public static void main(String[] args){
		String x1 = "232";
		String x2 = "NGTGTATATATATATATATATATATATATATATATATATATATATATATATATATATATATATATATACTACTACGTACGTACGTACGTACGTACGTACGTACGACGACGACGACGACGACGACGACGACGACGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAG";
		SequenceCompact obj = new SequenceCompact(x1, x2);
		int[] x3 = new int[1000];
		obj.toIntArray(x3);
		for(int i=0;i<obj.getSize();i++){
			System.out.print(SequenceCompact.toChar(x3[i]));
		}
	}*/

}
