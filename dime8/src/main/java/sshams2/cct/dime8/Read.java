package sshams2.cct.dime8;

public class Read {

	private Sequence seq;
	private int id;
	private int idContig;
	private int st = -1;
	private int ed = -1;//included
	private int sizeSeq = 0;
	
	public Read(int _id, int _idContig){
		id = _id;
		idContig = _idContig;
	}
	
	public Read(int[] _arrSeq, int _sizeSeq, int _id, int _idContig, int _st){
		seq = new Sequence(_arrSeq, _sizeSeq);
		id = _id;
		idContig = _idContig;
		st = _st;
		ed = _st + _sizeSeq - 1;
	}
	
	/**
	 * 
	 * @param _line
	 * @param _st
	 * @param _ed exclude
	 * @param _posST
	 * @param _posED include
	 */
	public void addSeq(String _line, int _st, int _ed, int _posST, int _posED){
		if(st==-1)st = _posST;
		if(ed<_posED) ed = _posED;
		seq.add(_line, _st, _ed);
		sizeSeq = seq.size();
	}
	
	public void addSizeSeq(int _len){
		sizeSeq += _len;
	}
	
	/**
	 * get the letter between st and ed on the contig
	 * @param _idx
	 * @return
	 */
	public int get(int _idx) {
		if(_idx>=st&&_idx<=ed){
			return seq.get(_idx-st);
		}
		return -1;
	}
	
	/**
	 * get the letter on the sequence with index with empty
	 * @param _idx
	 * @return
	 */
	public int getWithEmpty(int _idx){
		return seq.get(_idx);
	}
	
	public int getId(){
		return id;
	}
	
	public int getContigId(){
		return idContig;
	}
	
	public int getSt(){
		return st;
	}
	
	public int getEd(){
		return ed;
	}
	
	/**
	 * get the sequence into _arr (byte) no empty
	 * @param _st
	 * @param _ed included
	 * @param _arr
	 */
	public int getSeq(int _st, int _ed, byte[] _arr){
		return seq.getSeqNoEmpty(_arr, _st, _ed+1);
	}
	
	/**
	 * get the sequence into _arr (int)
	 * @param _st
	 * @param _ed
	 * @param _arr
	 * @return
	 */
	public int getSeq(int _st, int _ed, int[] _arr){
		return seq.getSeqNoEmpty(_arr, _st, _ed);
	}
	
	/**
	 * set the size of sequence (only for the reading)
	 */
	public void initialSizeSeq(){
		seq = new Sequence(sizeSeq);
		sizeSeq = 0;
	}
	
	/**
	 * set the read sequence to _seq
	 * @param _seq
	 */
	public void setSeq(Sequence _seq){
		seq = _seq;
		this.sizeSeq = seq.size();
	}
	
	public void reverse(int[] _arr, int _lenContig){
		seq.reverse(_arr);
		st = _lenContig - 1 - st;
		ed = _lenContig - 1 - ed;
		int temp = st;
		st = ed;
		ed = temp;
	}
	
	public int size(){
		return seq.size();
	}


	public void setST(int _st) {
		st = _st;
	}

	/**
	 * _ed should be included
	 * @param _ed
	 */
	public void setED(int _ed) {
		ed = _ed;
	}

	public void setId(int _id) {
		this.id = _id;
	}

}
