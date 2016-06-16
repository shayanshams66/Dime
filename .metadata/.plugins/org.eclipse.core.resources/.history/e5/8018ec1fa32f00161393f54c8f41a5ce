package mer;


public class Alignment {

	private int maxalen;
	private int maxrlen;
	private byte[] buffA;
	private int lenA = 0;
	private byte[] R;
	private int lenR = 0;
	private double[][] FM1;
	private int[][] BM1;
	private double[] vals = new double[3];
	private int m_band = 15;
	
	private static final double DBL_MAX = Double.MAX_VALUE;

	private double dx = 0;
	private double dy = 0;
	private double sx = 0;
	private double[][] p = new double[5][5];
	private int[] si = {-1, 0, -1};
	private int[] sj = {0, -1, -1};
	
	private double score;
	private byte[] align = new byte[3000];
	private int outStrLen = 0;
	private int maxeditlen;
	private byte[] edit = null;
	private int editLen = 0;
	private byte[] editR = new byte[3000];
	private int outLenEditR = 0;
	
	private byte[] consensus = new byte[3000];
	private int lenConsensus = 0;
	
	public static int extend = 5;
	
	public int ei;
	
	private int startA = 0;
	private int endA = 0;
	private int startR = 0;
	private int endR = 0;
	private static StringBuffer strTemp = new StringBuffer();
	private static char line = '-';
	private static String enter = "\n";
	
	private int idxStR = 0;//the first index of R (included)
	private int idxEdR = 0;//the last index of R (included)
	private int idxStA = 0;
	private int idxEdA = 0;
	
	public Alignment(int imaxalen, int imaxrlen, double indel, double miss, double hit, double unobserved_match, double pad_insert){
		maxalen = imaxalen + 1; maxrlen = imaxrlen+1;
		buffA = new byte[maxalen]; R = new byte[maxrlen];
		maxeditlen = maxalen + maxrlen;
		edit = new byte[maxeditlen];
		
		FM1 = new double[maxalen][maxrlen];
		BM1 = new int[maxalen][maxrlen];
		setCosts(indel, miss, hit, unobserved_match, pad_insert);
		
	}
	
	public void setCosts(double indel, double miss, double hit, double unobserved_match, double pad_insert){
		dx = indel; dy = indel;
		sx = pad_insert;
		for(int i=0;i<5;i++){
			for(int j=0;j<5;j++){
				p[i][j] = miss;
			}
			p[i][i] = hit;
		}
		for(int i=0;i<5;i++){
			p[i][4] = 0;
			p[4][i] = unobserved_match;
		}
		p[4][4] = 0;
	}
	
	public void setBuffA(int[] _A, int _sizeA, int _start, int _end){
		_start = _start - extend;
		int st = Math.max(_start, 0);
		int leftPad =  Math.max(0,0-_start);
		int en =  Math.min(_end, _sizeA);
		int rightPad =  Math.max(0, _end-_sizeA);
		lenA = _end-_start;
		if(st<=en){
			int i = 0;
			for(i=0;i<leftPad;)buffA[i++] = 4;
			idxStA = i;
			for(int j=st;j<en;j++){
				buffA[i++] = (byte) _A[j];
			}
			idxEdA = i - 1;
			for(int j=0;j<rightPad;j++){
				buffA[i++] = 4;;
			}
		} else {
			for(int i=0;i<lenA;i++)buffA[i] = 4;
			System.out.println("SetBuffA.Error");
			assert(false);
		}
	}
	
	public void setBuffA(byte[] _A, int _sizeA, int _start, int _end){
		_start = _start - extend;
		int st = Math.max(_start, 0);
		int leftPad =  Math.max(0,0-_start);
		int en =  Math.min(_end, _sizeA);
		int rightPad =  Math.max(0, _end-_sizeA);
		lenA = _end-_start;
		if(st<=en){
			int i = 0;
			for(i=0;i<leftPad;)buffA[i++] = 4;
			idxStA = i;
			for(int j=st;j<en;j++){
				buffA[i++] = (byte) _A[j];
			}
			idxEdA = i - 1;
			for(int j=0;j<rightPad;j++){
				buffA[i++] = 4;;
			}
		} else {
			for(int i=0;i<lenA;i++)buffA[i] = 4;
			System.out.println("SetBuffA.Error");
			assert(false);
		}
	}

	public byte[] getBuffA() {
		return buffA;
	}

	public int getBuffAlen() {
		return lenA;
	}

	public byte[] getR() {
		return R;
	}

	public int getLenR() {
		return lenR;
	}
	
	public void setR(int[] _R, int _lenR, int _sizeR){
		lenR = _sizeR;
		for(int i=0;i<lenR;i++){
			if(i<_lenR){
				R[i] = (byte) _R[i];
			}else{
				R[i] = 4;
			}
		}
		idxStR = 0;
		idxEdR = _lenR>_sizeR?_sizeR - 1:_lenR - 1;
	}
	
	public void setR(byte[] _R, int _lenR, int _sizeR){
		lenR = _sizeR;
		for(int i=0;i<lenR;i++){
			if(i<_lenR){
				R[i] = (byte) _R[i];
			}else{
				R[i] = 4;
			}
		}
		idxStR = 0;
		idxEdR = _lenR>_sizeR?_sizeR - 1:_lenR - 1;
	}
	
	public void setR(int[] _R, int _lenR, int _sizeR, int _st){
		lenR = _sizeR;
		for(int i=0;i<lenR;i++){
			if(i<_lenR){
				R[i] = (byte) _R[_st+i];
			}else{
				R[i] = 4;
			}
		}
		idxStR = 0;
		idxEdR = _lenR-_st>_sizeR?_sizeR - 1:_lenR - 1 - _st;
	}
	
	public void setR(byte[] _R, int _lenR, int _sizeR, int _st){
		lenR = _sizeR;
		for(int i=0;i<lenR;i++){
			if(i<_lenR){
				R[i] = (byte) _R[_st+i];
			}else{
				R[i] = 4;
			}
		}
		idxStR = 0;
		idxEdR = _lenR-_st>_sizeR?_sizeR - 1:_lenR - 1 - _st;
	}
	
	public void bandedAlign(boolean scoreOnly) {
		m_band = Math.abs(lenA-lenR);
		if(m_band<50)m_band=50;
		//initial the second row
		FM1[0][0] = 0;
		for(int i=0;i<=lenA;i++){
			int left_margin = 0>i-m_band?0:i-m_band;
			int right_margin = lenR<i+m_band?lenR:i+m_band;
			for(int j=left_margin;j<=right_margin;j++){
				if(i==0&&j==0)continue;
				vals[0] = -DBL_MAX; vals[1] = -DBL_MAX; vals[2] = -DBL_MAX;
				if(i>0&&j<i+m_band){
					vals[0] = FM1[i-1][j];
					if (j>0 && j<lenR){
						vals[0] = vals[0] + dx;//insertion in the Sb
					}else{
						vals[0] = vals[0] + sx;//insertion outside the Sb
					}
				}
				if(j>left_margin){
					vals[1] =  dy + FM1[i][j-1];
				}
				if(i>0&&j>0){
					vals[2] = FM1[i-1][j-1] + p[buffA[i-1]][R[j-1]];
				}
				//max3
				if (vals[0]>=vals[1] && vals[0]>=vals[2])
				{
					FM1[i][j] = vals[0];
					BM1[i][j] = 0;
				}
				else
				{
					if (vals[1] >= vals[2])
					{
						FM1[i][j] = vals[1];
						BM1[i][j] = 1;
					}
					else
					{
						FM1[i][j] = vals[2];
						BM1[i][j] = 2;
					}
				}
			};
		}
		
		//score = FM1[buffAlen][lenR];
		
		int i = lenA; int j = lenR;
		int id = BM1[i][j];
		ei = lenA + lenR;
		int endZero = ei;
		int firstZero = 0;
		while(true){
			edit[--ei] = (byte) (sj[id] - si[id]);
			i = i + si[id];
			j = j + sj[id];
			id = BM1[i][j];
			if(i==idxEdA){
				if(endZero>ei){
					endZero = ei;
					endA = i;
					endR = j;
				}
			}
			if(j==idxEdR){
				if(endZero>ei){
					endZero = ei;
					endA = i;
					endR = j;
				}
			}
			if(i==idxStA){
				if(firstZero<ei){
					firstZero = ei;
					startA = i;
					startR = j;
				}
			}
			if(j==idxStR){
				if(firstZero<ei){
					firstZero = ei;
					startA = i;
					startR = j;
				}
			}
			if(i==0&&j==0)
				break;
		}
		score = 0;
		int eii = lenA + lenR; i = lenA; j = lenR;
		id = BM1[i][j];
		lenConsensus = endZero - firstZero;
		while(true){
			eii--;
			i = i + si[id]; j = j + sj[id]; id = BM1[i][j];
			if(eii<=endZero&&eii>=firstZero){
				if(edit[eii]==0){
					score += p[buffA[i]][R[j]];
					consensus[lenConsensus] = buffA[i];
				}else{
					score += dx;
					if(edit[eii]==1){
						consensus[lenConsensus] = buffA[i];
					}else{
						consensus[lenConsensus] = R[j];
					}
				}
				lenConsensus--;
			}
			if(i==0&&j==0)
				break;
		}
		lenConsensus = endZero - firstZero + 1;
		if(!scoreOnly){
			editLen = lenA + lenR;
			//for(int k = 0;k <editLen;k++)edit[k] = edit[k+ei];
			System.out.println();
			int _index = 0;
			for(int l=ei;l<editLen;l++){
				switch(edit[l]){
				case -1:System.out.print("-");break;
				case 0:System.out.print(buffA[_index++]);break;
				case 1:System.out.print(buffA[_index++]);break;
				}
			}
			System.out.println();
			_index = 0;
			for(int l=ei;l<editLen;l++){
				switch(edit[l]){
				case -1:System.out.print(R[_index++]);break;
				case 0:System.out.print(R[_index++]);break;
				case 1:System.out.print("-");break;
				}
			}
			System.out.println();
		}
		
		/*if(!scoreOnly){
			//get edit
			outLenEditR = editLen;
			int srcEditR = 0;
			for(srcEditR=0;srcEditR<editLen;srcEditR++){
				if(edit[srcEditR]==1){
					outLenEditR--;
				}else break;
			}
			for(int k=editLen-1;k>0;k--){
				if(edit[k]==1){
					outLenEditR--;
				}else break;
			}
			
			for(int k=0;k<outLenEditR;k++){
				switch(edit[k+srcEditR]){
				case 0:editR[k]=0;break;
				case 1:editR[k]=1;break;
				case -1:editR[k]=2;
				}
			}
			//applyEdit
			int in=0, out=0;
			for(int k=0;k<editLen;k++){
				switch(edit[k]){
				case 0:
					align[out++] = (byte) (in<lenR?R[in++]:nBases);break;
				case -1:
					in++;break;
				case 1:
					if(!(in==0||in==lenR))
						align[out++] = (byte) nBases;
				}
			}
			outStrLen = out;
		}*/
	}
	
	public int getOrder(int[] _order){
		int idxC = 0;
		int idxR = 0;
		for(int i=ei;i<lenA+lenR;i++){
			switch(edit[i]){
			case 0:_order[idxR] = idxC-extend;idxR++;idxC++;break;
			case -1:_order[idxR] = idxC-extend;idxR++;break;
			case 1:idxC++;break;
			}
		}
		return idxR;
	}
	
	public String getAlignment(){
		strTemp.delete(0, strTemp.length());
		int _index = 0;
		for(int l=0;l<editLen;l++){
			switch(edit[l]){
			case -1:strTemp.append(line);break;
			case 0:strTemp.append(buffA[_index++]);break;
			case 1:strTemp.append(buffA[_index++]);break;
			}
		}
		_index = 0;
		strTemp.append(enter);
		for(int l=0;l<editLen;l++){
			switch(edit[l]){
			case -1:strTemp.append(R[_index++]);break;
			case 0:strTemp.append(R[_index++]);break;
			case 1:strTemp.append(line);break;
			}
		}
		strTemp.append(enter);
		return strTemp.toString();
	}

	public double getScore() {
		return score;
	}

	public int getOutStrLen() {
		return outStrLen;
	}

	public byte[] getAlign() {
		return align;
	}

	public int getOutLenEditR() {
		return outLenEditR;
	}
	
	
	public byte[] getEditR() {
		return editR;
	}

	public int getStartA(int _exterIdx) {
		return startA + _exterIdx - extend;
	}

	public int getEndA(int _exterIdx) {
		return endA + _exterIdx + 1 - extend;
	}

	public int getStartR(int _exterIdx) {
		return startR + _exterIdx;
	}

	/**
	 * get excluded
	 * @param _exterIdx
	 * @return
	 */
	public int getEndR(int _exterIdx) {
		return endR + _exterIdx + 1;
	}
	
	public byte[] getConsensus(byte[] _con){
		for(int i=0;i<lenConsensus;i++){
			_con[i] = consensus[i];
		}
		return _con;
	}
	
	public int getLenConsensus(){
		return lenConsensus;
	}
}
