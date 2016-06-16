package sshams2.cct.dime5;

public class Score {

	private static double score;
	private static double len1;
	private static double len2;
	
	public static byte FULLGRADE = 100;
	
	public static byte LogScore(Edge _edge){
		len1 = _edge.EndId1() - _edge.StartId1();
		len2 = _edge.EndId2() - _edge.StartId2();
		score = _edge.Score();
		score = score/(len1>len2?len1:len2);
		//if(score<0.5)return 0;
		//score = Math.ceil(10*(2 - Math.log10((1 - score)*100)));
		score = Math.ceil(Math.pow(100, score));
		return (byte)score;
	}
	
}
