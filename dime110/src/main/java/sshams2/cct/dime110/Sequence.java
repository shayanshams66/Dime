package sshams2.cct.dime110;

public class Sequence {

	private int[] arrSeq;
	/**
	 * A	000
	 * C	001
	 * G	010
	 * T	011
	 * N	100		4
	 * -	101		5
	 */
	private int size = 0;
	private static int UNITLEN = 10;
	private static int MASK = 7;
	
	public Sequence(int _len){
		setSize(_len);
	}
	
	public Sequence(int[] _arr, int _size){
		arrSeq = _arr;
		size = _size;
	}
	
	/**
	 * 
	 * @param _str
	 * @param _st
	 * @param _ed exclude
	 */
	public void add(String _str, int _st, int _ed){
		for(;_st<_ed;_st++){
			arrSeq[size/UNITLEN] += (toHash(_str.charAt(_st))<<(size%UNITLEN)*3);
			size++;
		}
	}
	
	public void add(int _val){
		arrSeq[size/UNITLEN] += (_val<<(size%UNITLEN)*3);
		size++;
	}
	
	public int get(int _idx){
		return ((arrSeq[_idx/UNITLEN]>>(_idx%UNITLEN)*3)&MASK);
	}
	
	public int sizeNoEmpty(){
		int lenTemp = 0;
		for(int i=0;i<size;i++){
			if(((arrSeq[i/UNITLEN]>>(i%UNITLEN)*3)&MASK)!=5){
				lenTemp++;
			}
		}
		return lenTemp;
	}
	
	/**
	 * before _st with '-', _extra means no '-'
	 * @param _st
	 * @param _extra
	 * @return
	 */
	public int posWithEmpty(int _st, int _extra){
		for(;_st<size&&_extra>0;_st++){
			if(((arrSeq[_st/UNITLEN]>>(_st%UNITLEN)*3)&MASK)!=5){
				_extra--;
			}
			//if(_extra<=0)break;
		}
		return _st;
	}
	
	public int posWithEmpty(int _pos){
		int lenTemp = 0;
		int i = 0;
		for(;i<size;i++){
			if(((arrSeq[i/UNITLEN]>>(i%UNITLEN)*3)&MASK)!=5){
				lenTemp++;
				if(lenTemp>=_pos){
					break;
				}
			}
		}
		return i;
	}
	
	public String get(StringBuffer sb){
		sb.delete(0, sb.length());
		for(int i=0;i<size;i++){
			//if(((arrSeq[i/UNITLEN]>>(i%UNITLEN))&MASK)!=5){
				sb.append(toChar(((arrSeq[i/UNITLEN]>>(i%UNITLEN)*3)&MASK)));
			//}
		}
		return sb.toString();
	}
	
	public int size(){
		return size;
	}
	
	private void setSize(int _size){
		arrSeq = new int[(int)Math.ceil(((double)_size)/((double)UNITLEN))];
	}
	
	public static char toChar(int _v){
		switch(_v){
		case 0:return 'A';
		case 1:return 'C';
		case 2:return 'G';
		case 3:return 'T';
		case 4:return 'N';
		case 5:return '-';
		default: return 'N';
		}
	}
	
	/**
	 * convert from ACGT to 0123 and N is changed to 4
	 * @param _a
	 * @return
	 */
	private static int toHash(char _a){
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
		default:
			//System.out.println("Error in inputSequence: "+_a);
			//System.exit(1);
			return 4;
		}		
	}
	
	public void reverse(int[] _arr){
		for(int i=0;i<size;i++){
			_arr[i] = ((arrSeq[i/UNITLEN]>>(i%UNITLEN)*3)&MASK);
			if(_arr[i] < 4){
				_arr[i] = 3 - _arr[i];
			}
		}
		int temp;
		for(int i=0;i<size/2;i++){
			temp = _arr[i];
			_arr[i] = _arr[size - i - 1];
			_arr[size - i - 1] = temp;
		}
		for(int i=0;i<arrSeq.length;i++){
			arrSeq[i] = 0;
		}
		for(int i=0;i<size;i++){
			arrSeq[i/UNITLEN] += ((_arr[i])<<(i%UNITLEN)*3); 
		}
	}
	
	/**
	 * get the sequence into _arr (byte)
	 * @param _arr
	 * @param _st
	 * @param _ed excluded
	 * @return
	 */
	public int getSeqNoEmpty(byte[] _arr, int _st, int _ed){
		int idx = 0;
		for(;_st<_ed;_st++){
			_arr[idx] = (byte) ((arrSeq[_st/UNITLEN]>>(_st%UNITLEN)*3)&MASK);
			if(_arr[idx]<5){
				idx++;
			}
		}
		return idx;
	}
	
	/**
	 * get the sequence into _arr (int) no empty
	 * @param _arr
	 * @param _st
	 * @param _ed
	 * @return
	 */
	public int getSeqNoEmpty(int[] _arr, int _st, int _ed){
		int idx = 0;
		for(;_st<_ed;_st++){
			_arr[idx] =  ((arrSeq[_st/UNITLEN]>>(_st%UNITLEN)*3)&MASK);
			if(_arr[idx]<5){
				idx++;
			}
		}
		return idx;
	}
	
}
