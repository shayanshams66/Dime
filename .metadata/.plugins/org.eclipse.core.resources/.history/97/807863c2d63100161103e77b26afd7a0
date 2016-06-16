package sshams2.cct.dime110;


public class AlignRead {

	public byte[] seq;
	public int[] order;
	public int sizeSeq;
	public int sizeOrder;
	public Read read;
	public int stRead;//at the read sequence position [0...]
	public int edRead;//included
	public int idxST;//inner start position (included)
	public int idxED;//inner end position (excluded)
	public boolean leftConnect = false;
	public boolean rightConnect = false;
	//public int stContig;
	private static int[] hm = new int[5000];
	private static int stHm = 1000;//0 = 1000
	
	public AlignRead(int _size){
		seq = new byte[_size];
		order = new int[_size];
		sizeSeq = 0;
		read = null;
		stRead = -1;
		edRead = -1;
	}
	
	public void align(Alignment _aligner, byte[] consensus, int _sizeCon){
		_aligner.setBuffA(consensus, _sizeCon, 0, _sizeCon+2*Alignment.extend);
		_aligner.setR(seq, sizeSeq, sizeSeq);
		_aligner.bandedAlign(true);
		sizeOrder = _aligner.getOrder(order);
	}
	
	/**
	 * count the correspondent index of read
	 * @param _hm
	 */
	public void count(int[] _hm){
		for(int i=0;i<hm.length;i++)hm[i] = 0;
		for(int i=0;i<sizeOrder;i++){
			hm[stHm+order[i]]++;
		}
		for(int i=0;i<hm.length;i++){
			if(hm[i]>0){
				if(hm[i]>_hm[i]){
					_hm[i] = hm[i];
				}
			}
		}
	}
	
	/**
	 * complete the alignment on the read
	 * @param _hm
	 */
	public void apply(int[] _hm){
		for(int i=0;i<hm.length;i++)hm[i] = 0;
		int idxOrder = 0;
		int idx = 0;
		int i = 0;
		idxST = 0;
		idxED = 0;
		if(leftConnect){
			for(i=0;i<order[0]+stHm;i++){
				if(_hm[i]>0){
					idxED += _hm[i];
					for(int j=0;j<_hm[i];j++){
						hm[idx] = 5;
						idx++;
					}
				}
			}
			idxST = 0;
		}else{
			for(i=0;i<order[0]+stHm;i++){
				if(_hm[i]>0){
					idxST += _hm[i];
					idxED += _hm[i];
				}
			}
		}
		order[sizeOrder] = -1;
		if(sizeOrder == 0){sizeSeq = idx;return;}
		for(i=order[0]+stHm;i<order[sizeOrder-1]+stHm;i++){
			if(_hm[i]>0){
				for(int j=0;j<_hm[i];j++){
					if(idxOrder>=sizeOrder)break;
					if(order[idxOrder]==i-stHm){
						hm[idx] = seq[idxOrder];
						idxOrder++;
						idx++;
						idxED++;
					}else{
						hm[idx] = 5;
						idx++;
						idxED++;
					}
				}
			}
		}
		if(rightConnect){
			for(i=order[sizeOrder-1]+stHm;i<_hm.length;i++){
				idxED+=_hm[i];
				if(_hm[i]>0){
					for(int j=0;j<_hm[i];j++){
						if(order[idxOrder]==i-stHm){
							hm[idx] = seq[idxOrder];
							idxOrder++;
							idx++;
						}else{
							hm[idx] = 5;
							idx++;
						}
					}
				}
			}
		}else{
			for(i=order[sizeOrder-1]+stHm;i<=order[sizeOrder-1]+stHm;i++){
				if(_hm[i]>0){
					for(int j=0;j<_hm[i];j++){
						if(idxOrder>=sizeOrder)break;
						if(order[idxOrder]==i-stHm){
							hm[idx] = seq[idxOrder];
							idxOrder++;
							idx++;
							idxED++;
						}else{
							hm[idx] = 5;
							idx++;
							idxED++;
						}
					}
				}
			}
		}
		for(i=0;i<idx;i++){
			seq[i] = (byte) hm[i];
		}
		sizeSeq = idx;
	}
	
	/**
	 * apply the aligned read to the left contig
	 * @param _st
	 */
	public void connectToReadLeft(int _st){
		int lenNew = stRead + sizeSeq + (read.size() - edRead - 1);
		Sequence seqNew = new Sequence(lenNew);
		//read.getSeq(0, read.size(), order);
		for(int i=0;i<stRead;i++)
			seqNew.add(read.getWithEmpty(i));
		for(int i=0;i<sizeSeq;i++)
			seqNew.add(seq[i]);
		for(int i=edRead+1;i<read.size();i++)
			seqNew.add(read.getWithEmpty(i));
		read.setSeq(seqNew);
		if(!leftConnect)
			read.setST(_st+idxST);
		read.setED(_st+idxED-1);//idxED is excluded
	}
	
	/**
	 * apply the aligned read to the right contig
	 * @param _st
	 */
	public void connectToReadRight(int _st){
		if(read.getId()==1817637)
			System.out.println("check");
		int lenNew = stRead + sizeSeq + (read.size() - edRead - 1);
		Sequence seqNew = new Sequence(lenNew);
		//read.getSeq(0, read.size(), order);
		for(int i=0;i<stRead;i++)
			seqNew.add(read.getWithEmpty(i));
		for(int i=0;i<sizeSeq;i++)
			seqNew.add(seq[i]);
		for(int i=edRead+1;i<read.size();i++)
			seqNew.add(read.getWithEmpty(i));
		read.setSeq(seqNew);
		read.setST(_st+idxST);
		if(!rightConnect)
			read.setED(_st+idxED-1);
	}
	
	/**
	 * return the letter on _idx - idxST
	 * @param _idx
	 * @return -1 if read does not contain _idx
	 */
	public int get(int _idx){
		if(_idx<idxST||_idx>=idxED)return -1;
		return seq[_idx - idxST];
	}
	
	/**
	 * get the sequence with no empty '-'
	 * @param _read
	 * @param _st
	 * @param _ed
	 */
	public void set(Read _read, int _st, int _ed){
		stRead = Math.max(0, _st - _read.getSt());
		leftConnect = _read.getSt()<=_st?true:false;
		//stContig = Math.max(0, _read.getSt() - _st);
		edRead = _read.getEd()<_ed?_read.getEd()-_read.getSt():_ed - _read.getSt();
		rightConnect = _read.getEd()>=_ed?true:false;
		sizeSeq = _read.getSeq(stRead, edRead, seq);
		read = _read;
	}
	
	/**
	 * set the sequence size to _size
	 * @param _size
	 */
	public void resize(int _size){
		seq = new byte[_size];
		order = new int[_size];
		sizeSeq = 0;
		read = null;
		stRead = -1;
		edRead = -1;
	}
	
}
