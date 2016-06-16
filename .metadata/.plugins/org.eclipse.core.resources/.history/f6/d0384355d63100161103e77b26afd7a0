package sshams2.cct.dime110;



public class Edge implements Comparable<Edge>{
	
	private int id2;
	private int id1;
	private int[] arr;
	//0 score; 1 start position; 2 end position excluded
	private byte[] STED = new byte[2];//0 first begin; 1 second begin; 2 second end; 3 last end
	//score st1 ed1 st2 ed2
	private boolean rev;
	private int endStatus1;
	private int endStatus2;
	private double sc;
	
	private static String Zero = "0";
	
	public Edge(){
		arr = new int[5];
	}
	
	public Edge(int _id1,
			String _id2,
			String _score,
			String _st1,
			String _ed1,
			String _st2,
			String _ed2,
			String _rev,
			String _endS1,
			String _endS2){
		id1 = _id1;
		id2 = Integer.parseInt(_id2);
		arr = new int[5];
		arr[0] = Integer.parseInt(_score);
		arr[1] = Integer.parseInt(_st1);
		arr[2] = Integer.parseInt(_ed1);
		arr[3] = Integer.parseInt(_st2);
		arr[4] = Integer.parseInt(_ed2);
		if(_rev.compareTo(Zero)==0){
			rev = false;
		}else{
			rev = true;
		}
		endStatus1 = Integer.parseInt(_endS1);
		endStatus2 = Integer.parseInt(_endS2);
		sc = LogScore(this);
	}
	
	public boolean compare(Edge _edge){
		if(id1!=_edge.id1){
			return false;
		}
		if(id2!=_edge.id2){
			return false;
		}
		if(rev!=_edge.rev){
			return false;
		}
		for(int i=0;i<5;i++){
			if(arr[i]!=_edge.arr[i]){
				return false;
			}
		}
		return true;
	}
	
	public int EndId1(){
		return arr[2];
	}
	
	public int EndId2(){
		return arr[4];
	}
	
	/**
	 * return the opposite id
	 * @param _id
	 * @return
	 */
	public int getId(int _id){
		if(id1==_id){
			return id2;
		}
		return id1;
	}
	
	public byte[] getSTED(){
		return STED;
	}
	
	public int ID1(){
		return id1;
	}
	
	public int ID2(){
		return id2;
	}
	
	public boolean Rev(){
		return rev;
	}
	
	/**
	 * deeply copy
	 * @param _edge
	 */
	public void set(Edge _edge){
		id1 = _edge.ID1();
		id2 = _edge.ID2();
		arr[0] = _edge.Score();
		arr[1] = _edge.StartId1();
		arr[3] = _edge.StartId2();
		arr[2] = _edge.EndId1();
		arr[4] = _edge.EndId2();
		rev = _edge.Rev();
		endStatus1 = _edge.endStatus1;
		endStatus2 = _edge.endStatus2;
		sc = _edge.sc;
	}
	
	public int set(int _id, String _str, int _st, char _sep, StringBuffer _sb){
		_sb.delete(0, _sb.length());
		while(_str.charAt(_st)!=_sep){
			_sb.append(_str.charAt(_st));
			_st++;
		}
		id1 = _id;
		id2 = Integer.parseInt(_sb.toString());
		if(id1>id2){
			id1 = id2;
			id2 = _id;
		}
		_st++;
		for(int i=0;i<5;i++){
			_sb.delete(0, _sb.length());
			while(_str.charAt(_st)!=_sep){
				_sb.append(_str.charAt(_st));
				_st++;
			}
			arr[i] = Integer.parseInt(_sb.toString());
			_st++;
		}
		_sb.delete(0, _sb.length());
		while(_st<_str.length()&&_str.charAt(_st)!=_sep){
			_sb.append(_str.charAt(_st));
			_st++;
		}
		if(_sb.toString().compareTo(Zero)==0){
			rev = false;
		}else{
			rev = true;
		}
		_st++;
		_sb.delete(0, _sb.length());
		while(_st<_str.length()&&_str.charAt(_st)!=_sep){
			_sb.append(_str.charAt(_st));
			_st++;
		}
		endStatus1 = Integer.parseInt(_sb.toString());
		_sb.delete(0, _sb.length());
		_st++;
		while(_st<_str.length()&&_str.charAt(_st)!=_sep){
			_sb.append(_str.charAt(_st));
			_st++;
		}
		endStatus2 = Integer.parseInt(_sb.toString());
		_st++;
		sc = LogScore(this);
		return _st;
	}
	
	//0 first begin; 1 second begin; 2 second end; 3 last end
	public void setSTED(Contig _c1, Contig _c2){
		if(this.endStatus1 == 0 && this.endStatus2 == 0){
			if(arr[1]<arr[3]){
				STED[0] = 0;
				STED[1] = 3;
			}else{
				STED[0] = 3;
				STED[1] = 0;
			}
		}else{
			if(this.endStatus1 == 1){
				STED[0] = 1;
				if(this.endStatus2 == 0){
					STED[1] = 3;
				}else{
					if(this.endStatus2!=2)
						System.out.println("error STED.");
					STED[1] = 2;
				}
			}else if(this.endStatus1 == 2){
				STED[0] = 2;
				if(this.endStatus2==0){
					STED[1] = 0;
				}else{
					if(this.endStatus2!=1)
						System.out.println("error STED.");
					STED[1] = 1;
				}
			}else{
				if(this.endStatus2 == 1){
					STED[0] = 3;
					STED[1] = 1;
				}else{
					if(this.endStatus2!=2)
						System.out.println("error STED.");
					STED[0] = 0;
					STED[1] = 2;
				}
			}
		}
	}
	
	
	public int Score(){
		return arr[0];
	}
	
	public int StartId1(){
		return arr[1];
	}
	
	public int StartId2(){
		return arr[3];
	}
	
	public int endStatus1(){
		return endStatus1;
	}
	
	public int endStatus2(){
		return endStatus2;
	}

	private static double len1;
	private static double len2;
	private static double s;
	public static double LogScore(Edge _edge){
		len1 = _edge.EndId1() - _edge.StartId1();
		len2 = _edge.EndId2() - _edge.StartId2();
		s = _edge.Score();
		s = s/(len1>len2?len1:len2);
		//if(score<0.5)return 0;
		//score = Math.ceil(10*(2 - Math.log10((1 - score)*100)));
		//s = Math.ceil(Math.pow(100, s));
		return s;
	}

	public int compareTo(Edge o) {
		if(sc>o.sc){
			return -1;
		}else if(sc == o.sc){
			if(this.arr[0]>o.arr[0]){
				return -1;
			}else if(this.arr[0]<o.arr[0]){
				return 1;
			}
			return 0;
		}
		return 1;
	}
	
	public double sc(){
		return sc;
	}
	
	/**
	 * 
	 * @param _id
	 * @param _rev
	 * @param _STED 0 begin; 1 second begin; 2 last second end; 3 last;
	 * @return
	 */
	public boolean testIdEnd(int _id, boolean _rev, byte _STED){
		if(id1 == _id){
			if((STED[0]<2&&_STED<2)||(STED[0]>1&&_STED>1)){
				return true;
			}
		}else if(id2 == _id){
			if((STED[1]<2&&_STED<2)||(STED[1]>1&&_STED>1)){
				return true;
			}
		}
		return false;
	}
}
