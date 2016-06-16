package sshams2.cct.dime9;

/**
 * contig 1,20,489,ACGT-NX
 * @author Xuan
 *
 */
public class SequenceCompact {
	
	private int id;
	private int[] seq;//one integer stores 10 bases
	//private int len;
	private int size;
	private int st = -1;
	private int ed = -1;
	private static int UNITLEN = 10;
	private static int MASK = 7;
	
	/**
	 * A	000
	 * C	001
	 * G	010
	 * T	011
	 * N	100		4
	 * -	101		5
	 */
	
	public SequenceCompact(int _id, String _seq, int _st, int _ed, int _stSeq){
		id = _id;
		st = _st;
		ed = _ed;
		size = _seq.length() - _stSeq;
		seq = new int[(int)Math.ceil(((double)size)/10.0)];
		for(int i=0;i<size;i++){
			seq[i/UNITLEN] += (toHash(_seq.charAt(i+_stSeq))<<(i%UNITLEN)*3);
		}
	}
	
	public int getId(){
		return id;
	}
	
	public int getST(){
		return st;
	}
	
	public int getED(){
		return ed;
	}
	
	public int size(){
		return size;
	}
	
	public int toIntArray(int[] _arr){
		
		for(int i=0;i<size;i++){
			//if(((arrSeq[i/UNITLEN]>>(i%UNITLEN))&MASK)!=5){
				_arr[i] = ((seq[i/UNITLEN]>>(i%UNITLEN)*3)&MASK);
			//}
		}
		
		return size;
	}
	
	public int toIntArrayRev(int[]_arr){
		toIntArray(_arr);
		for(int i=0;i<size/2;i++){
			if(_arr[i]<4)_arr[i] = 3 -_arr[i];
			int j = size-i-1;
			if(_arr[j]<4)_arr[j] = 3 - _arr[j];
			int k = _arr[j];
			_arr[j] = _arr[i];
			_arr[i] = k;
		}
		if(size%2!=0){
			int i = size/2;
			if(_arr[i]==5){
			}else if(_arr[i]!=4)_arr[i] = 3 - _arr[i];
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
		case 5:return '-';
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
		case '-':return 5;
		case 'R':return 4;
		case 'Y':return 4;
		case 'W':return 4;
		default:
			//System.out.println("Error in inputSequence: "+_a);
			//System.exit(1);
			return 4;
		}		
	}
	
	public static void main(String[] args){
		String x1 = "2,2,3,ACGTC-CCACGTC-CCACGTC-CCACGTC-CCACGTC-CC";
		SequenceCompact obj = new SequenceCompact(2, x1, 2, 3, 6);
		int[] x3 = new int[1000];
		obj.toIntArray(x3);
		for(int i=0;i<obj.size();i++){
			System.out.print(SequenceCompact.toChar(x3[i]));
		}
	}

}
