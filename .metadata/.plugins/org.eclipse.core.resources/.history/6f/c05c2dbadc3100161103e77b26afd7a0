package cap3;

public class ReadG {

	private int id;
	private int idContig;
	private int idxST;
	private int[] rawBases;
	private static int UNITLEN = 10;
	private static int MASK = 7;
	private int sizeRawBases = 0;
	private int[][] deleteEdit;//[0] position; [1] how many insert (position) from reads not on the contig
	//(D)deletion is not on the contig but on the sequence (not show in the edit)
	//only record D
	private int[][] insertEdit;
	//(I)insertion is on the contig not on the sequence (not record)
	
	public static int[] arrCommon = new int[5000];
	public static int[] arrCopy = new int[5000];
	public static int sizeArrCommon = 0;
	public static StringBuffer sb = new StringBuffer();
	
	public ReadG(String line) {
		int i = 0;
		id = 0;
		for(i=0;i<line.length();i++){
			if(line.charAt(i)==' ')break;
			id = id*10 + ((charToInt(line.charAt(i))));
		}
		i++;
		idContig = 0;
		for(;i<line.length();i++){
			if(line.charAt(i)==' ')break;
			idContig = idContig*10 + ((charToInt(line.charAt(i))));
		}
		i++;
		boolean neg = false;
		if(line.charAt(i)=='-'){
			neg = true;
			i++;
		}
		for(;i<line.length();i++){
			if(line.charAt(i)==' ')break;
			idxST = idxST*10 + ((charToInt(line.charAt(i))));
		}
		if(neg)idxST = -idxST;
		i++;
		int spaceNum = 3;
		for(;i<line.length();i++){
			if(line.charAt(i)==' '){
				spaceNum--;
				if(spaceNum==0)break;
			}	
		}
		//read the raw sequence
		sizeArrCommon = 0;
		i++;
		for(;i<line.length();i++){
			if(line.charAt(i)==' ')
				break;
			arrCommon[sizeArrCommon] = charToInt(line.charAt(i));
			sizeArrCommon++;
		}
		rawBases = new int[((int)Math.ceil(((double)sizeArrCommon)/((double)UNITLEN)))];
		sizeRawBases = 0;
		for(int j=0;j<sizeArrCommon;j++){
			rawBases[sizeRawBases/UNITLEN] += (arrCommon[j]<<(sizeRawBases%UNITLEN)*3);
			sizeRawBases++;
		}
		i++;
		for(i++;i<line.length();i++){
			if(line.charAt(i)==' ')break;
		}
		//get the edit
		sb.delete(0, sb.length());
		i++;
		for(;i<line.length();i++){
			if(line.charAt(i)==' ')	break;
			sb.append(line.charAt(i));
		}
		//apply the edit
		this.setEdit(sb.toString());
	}

	private void add(int _val){
		rawBases[sizeRawBases/UNITLEN] += (_val<<(sizeRawBases%UNITLEN)*3);
		sizeRawBases++;
	}


	/**
	 * add _num "-" to the sequence starting at _pos 
	 * @param _arr
	 * @param _size
	 * @param _pos
	 * @param _num
	 * @return the new length of the sequence
	 */
	public static int addEmpty(int[] _arr, int _size, int _pos, int _num){
		for(int i=_size-1;i>=_pos;i--){
			_arr[i+_num] = _arr[i];
		}
		for(int i=0;i<_num;i++){
			_arr[_pos+i] = 5;
		}
		_size += _num;
		return _size;
	}
	
	/**
	 * change the sequence of read
	 * and change the start position of read
	 * @param _contigEdit
	 * @param _idxSTContig
	 */
	public void applyEdit(byte[] _contigEdit, int _idxSTContig){
		//get raw-bases into arr-common
		sizeArrCommon = 0;
		for(int i=0;i<sizeRawBases;i++){
			arrCommon[sizeArrCommon] = getBase(i);
			sizeArrCommon++;
		}
		//apply
		int posDelete = 0;
		int posInsert = 0;
		int idx = 0;
		int idxEdit = 0;
		for(int i=0;i<sizeArrCommon;){
			if(_contigEdit[idxEdit+idxST-_idxSTContig]>0){
				if(posDelete<deleteEdit.length&&deleteEdit[posDelete][0]==idxEdit){
					for(int j=0;j<deleteEdit[posDelete][1];j++){
						arrCopy[idx] = arrCommon[i];
						idx++;
						i++;
					}
					for(int j=deleteEdit[posDelete][1];j<_contigEdit[idxEdit+idxST-_idxSTContig];j++){
						arrCopy[idx] = 5;
						idx++;
					}
					posDelete++;
					arrCopy[idx] = arrCommon[i];
					idx++;
					i++;
					idxEdit++;
				}else if(posInsert<insertEdit.length&&insertEdit[posInsert][0]==idxEdit){
					for(int j=0;j<_contigEdit[idxEdit+idxST-_idxSTContig];j++){
						arrCopy[idx] = 5;
						idx++;
					}
					for(int j=0;j<insertEdit[posInsert][1];j++){
						arrCopy[idx] = 5;
						idx++;
						idxEdit++;
					}
					posInsert++;
				}else{
					for(int j=0;j<_contigEdit[idxEdit+idxST-_idxSTContig];j++){
						arrCopy[idx] = 5;
						idx++;
					}
					arrCopy[idx] = arrCommon[i];
					idx++;
					i++;
					idxEdit++;
				}
			}else{
				if(posInsert<insertEdit.length&&insertEdit[posInsert][0]==idxEdit){
					for(int j=0;j<insertEdit[posInsert][1];j++){
						arrCopy[idx] = 5;
						idx++;
						idxEdit++;
					}
					posInsert++;
				}else{
					arrCopy[idx] = arrCommon[i];
					idx++;
					i++;
					idxEdit++;
				}
			}
		}
		//change to int[] sequence
		this.rawBases = new int[((int)Math.ceil(((double)idx)/((double)UNITLEN)))];
		this.sizeRawBases = 0;
		for(int i=0;i<idx;i++){
			this.add(arrCopy[i]);
		}
		//change the idxST
		sizeArrCommon = 0;
		for(int i=0;i<idxST-_idxSTContig;i++){
			sizeArrCommon += _contigEdit[i];
		}
		this.idxST += sizeArrCommon;
	}
	
	public static int charToInt(char _x){
		switch(_x){
		case '0':return 0;
		case '1':return 1;
		case '2':return 2;
		case '3':return 3;
		case '4':return 4;
		case '5':return 5;
		case '6':return 6;
		case '7':return 7;
		case '8':return 8;
		case '9':return 9;
		default:
			System.out.println("error");
			return -1;
		}
	}
	
	public int getIdxST(){
		return this.idxST;
	}
	
	public int getIdContig(){
		return this.idContig;
	}
	
	public int getId(){
		return this.id;
	}
	
	public int[] getSeq(){
		return this.rawBases;
	}
	
	public int getSizeSeq(){
		return this.sizeRawBases;
	}
	
	public int[][] getInsertEdit(){
		return this.insertEdit;
	}
	
	public int[][] getDeleteEdit(){
		return this.deleteEdit;
	}
	
	private int getBase(int _idx){
		return ((rawBases[_idx/UNITLEN]>>(_idx%UNITLEN)*3)&MASK);
	}
	
	private static int tempVal;
	private static int tempIdxD;
	private static int tempIdxI;
	
	public void setEdit(String _str){
		int len = 0;
		for(int i=0;i<_str.length();i++){
			if(_str.charAt(i)=='D')len++;
		}
		deleteEdit = new int[len][2];
		len = 0;
		for(int i=0;i<_str.length();i++){
			if(_str.charAt(i)=='I')len++;
		}
		insertEdit = new int[len][2];
		String[] arr = _str.split("-");
		len = 0;
		tempIdxD = 0;
		tempIdxI = 0;
		int i = 0;
		if(arr[0].charAt(arr[0].length()-1)=='D'){
			deleteEdit[tempIdxD][0] = 0;
			tempVal = Integer.parseInt(arr[0].substring(0, arr[0].length()-1));
			deleteEdit[tempIdxD][1] = tempVal;
			tempIdxD++;
			i++;
		}else if(arr[0].charAt(arr[0].length()-1)=='I'){
			tempVal = Integer.parseInt(arr[0].substring(0, arr[0].length()-1));
			insertEdit[tempIdxI][0] = len;
			insertEdit[tempIdxI][1] = tempVal;
			tempIdxI++;
			i++;
		}
		for(;i+1<arr.length;i+=2){
			len += Integer.parseInt(arr[i].substring(0, arr[i].length()-1));
			tempVal = Integer.parseInt(arr[i+1].substring(0, arr[i+1].length()-1));
			if(arr[i+1].charAt(arr[i+1].length()-1)=='I'){
				insertEdit[tempIdxI][0] = len;
				insertEdit[tempIdxI][1] = tempVal;
				len += tempVal;
				tempIdxI++;
			}else{
				deleteEdit[tempIdxD][0] = len;
				deleteEdit[tempIdxD][1] = tempVal;
				tempIdxD++;
			}
		}
	}
	
	public void setIdContig(int _id){
		this.idContig = _id;
	}
	
	public void setIdxST(int _idx){
		this.idxST = _idx;
	}
	
	public void shape(int _idxStContig){
		if(this.idxST<_idxStContig){
			int sizeNew = 0;
			int[] rawBasesNew = new int[((int)Math.ceil(((double)(sizeRawBases - _idxStContig + idxST))/((double)UNITLEN)))];
			for(int i = _idxStContig - idxST; i < sizeRawBases; i++){
				rawBases[sizeNew/UNITLEN] += (this.getBase(i)<<(sizeRawBases%UNITLEN)*3);
				sizeNew++;
			}
			this.rawBases = rawBasesNew;
			this.sizeRawBases = sizeNew;
			this.idxST = _idxStContig;
		}
	}
	
	public void toPrint(){
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<this.sizeRawBases;i++)
			sb.append(((rawBases[i/UNITLEN]>>(i%UNITLEN)*3)&MASK));
		System.out.println(sb.toString());
	}
	
	public static void main(String[] args){
		String test = "16 48287 0 447 221322120312113333331020201321100111302201332022131301330331223322222231030122020302221332033311330012111333220001332011331210311310232202033211220200011012010313310110311321102330012220231211012132302332313232023321001200311300011121000033313312200043311221122234132222220302230042233000123011334112131023031003201000201331124101323101133122233221403322223220310303323020222302120113313203132123122010000431222300332410210140311132223331231203031 443 22132212031211333333102020132110011130220133202213130133033122332222223103012202030222133203331133001211133322000133201133121031131023220203321122020001101201031331011013113211302330012223023121101213230233231323202332100120031130001112100003331331220003311221122323132222220302230022330001230113311213102303100320100020132311210132310113312223322103322223220310303323020222302120113313203132123122010000312223003321021010311132223331231203031 452 168N-1D-7N-1D-10N-1D-65N-1I-10N-1D-2N-1I-16N-1I-15N-1I-25N-1D-4N-1I-21N-1I-56N-1I-11N-1I-6N-1I-22N -265.933";
		ReadG read = new ReadG(test);
		read.setIdxST(0);
	}

	public void setId(int _id) {
		this.id = _id;
	}
	
}
