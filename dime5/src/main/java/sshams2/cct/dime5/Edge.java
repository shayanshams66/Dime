package sshams2.cct.dime5;

public class Edge {
	
	private int id2;
	private int id1;
	private short[] arr;
	//score st1 ed1 st2 ed2
	private boolean rev;
	
	private static String Zero = "0";
	
	public Edge(){
		arr = new short[5];
	}
	
	public Edge(int _id1,
			String _id2,
			String _score,
			String _st1,
			String _ed1,
			String _st2,
			String _ed2,
			String _rev){
		id1 = _id1;
		id2 = Integer.parseInt(_id2);
		arr = new short[5];
		arr[0] = Short.parseShort(_score);
		arr[1] = Short.parseShort(_st1);
		arr[2] = Short.parseShort(_ed1);
		arr[3] = Short.parseShort(_st2);
		arr[4] = Short.parseShort(_ed2);
		if(_rev.compareTo(Zero)==0){
			rev = false;
		}else{
			rev = true;
		}
	}
	
	public boolean compareTo(Edge _edge){
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
	
	public short EndId1(){
		return arr[2];
	}
	
	public short EndId2(){
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
			arr[i] = Short.parseShort(_sb.toString());
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
		return _st;
	}
	
	
	public short Score(){
		return arr[0];
	}
	
	public short StartId1(){
		return arr[1];
	}
	
	public short StartId2(){
		return arr[3];
	}

}
