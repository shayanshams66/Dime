package sshams2.cct.dime9;

public class DoubleIntArray {

	public int score = 0;
	public int startBuffA = 0;
	public int endBuffA = 0;
	public int startR = 0;
	public int endR = 0;
	public boolean rev;
	
	//0 keep; 1 remove begin; 2 remove end; 3 remove both;
	public int endStatusA; 
	public int endStatusR;
	
	public void setValue(int _s, int _sR, int _eR, int _sA, int _eA){
		if(_s<score){
			return;
		}
		score = _s;
		startBuffA = _sA;
		endBuffA = _eA;
		startR = _sR;
		endR = _eR;
	}
	
	public void setValue(int _s, int _sR, int _eR, int _sA, int _eA, 
			int _endR, int _endA){
		if(_s<score){
			return;
		}
		score = _s;
		startBuffA = _sA;
		endBuffA = _eA;
		startR = _sR;
		endR = _eR;
		endStatusA = _endA;
		endStatusR = _endR;
	}
	
	public void setValue(int _score, int _sR, int _eR, int _stA, int _edA, int _r){
		if(_score<score){
			return;
		}
		score = _score;
		startBuffA = _stA;
		endBuffA = _edA;
		startR = _sR;
		endR = _eR;
		if(_r==1){
			rev = true;
		}else{
			rev = false;
		}
	}
	
	public void setValue(int _score, int _sR, int _eR, int _stA, int _edA, int _r, 
			int _endR, int _endA){
		if(_score<score){
			return;
		}
		score = _score;
		startBuffA = _stA;
		endBuffA = _edA;
		startR = _sR;
		endR = _eR;
		if(_r==1){
			rev = true;
		}else{
			rev = false;
		}
		endStatusA = _endA;
		endStatusR = _endR;
	}
	
	public void reset(){
		score = -Integer.MAX_VALUE;
		startBuffA = 0;
		endBuffA = 0;
		startR = 0;
		endR = 0;
		rev = false;
		endStatusA = 0;
		endStatusR = 0;
	}
	
	public void print(){
		System.out.println(score+"\t"+startR+"\t"
				+endR+"\t"+startBuffA+"\t"+endBuffA+"\t"
				+endStatusR+"\t"+endStatusA);
	}

}