package sshams2.cct.dime9;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class Test {
	
	private static Spikes spikes =new Spikes(2500, 10);
	private static int stat = 0; 
	private static int kmer = 0;
	private static int MASK = 1048575;//1073741823;//1048575;
	public static int lenKmer = 10;
	private static int x = 0;
	private static ArrayList<Integer> arrLoc;
	private static Alignment aligner = new Alignment(5000, 5000, -4, -4, 1, -1, 0);
	private static int extend = Alignment.extend;
	private static int L = 0;
	
	private static int id1 = 1;
	private static int id2 = 0;
	private static int lenR;
	private static int lenA;
	
	private static int LenCutOff = MatrixConstruct.LenCutOff;
	
	private static DoubleIntArray bestScore = new DoubleIntArray();

	public static void main(String[] args){
		String strA = "0,64,592,NNNNNCCCTATCCCCCTGTGTGCCTTGCCTATCCCCCTGTTGCGTGTCTCAGCTCAATAACATAAAAACTATTGCTCTATCCGCAGTGCGTTGGATAGTTTTCCAATCTGGTTGCAAGATATGTAAATCACTAAAGC-AATCATTCCAGTAATCCACCATATCTCTTTTAA-GTTCGATTTCTACACGCCATAAATGTTCAGACATAACTTCAGCATCTGCATTATCTTTACGTTCTTGCTTTTTATTATAAATTCTAATAAATCTATTACTATCTCTCACGCCAAAATATTTTGTTTCTGGCTTACCATTACGACCATAAAAAATAGTTTTCTTAACTGCTTTATCAGACATTGCATAGTAGTCACTCAAATCATCTTCAAAATCAAAGGCTAAATCTAATCTTGTAAAACCGTCATCTTCCATGTAGCTTATTATATATTTGTTTTAACCAAATCATTTCATCTCGTGTAAGTTTGTTTGGATTAAA-TTCAATACGCATATTACGTCTATCCCAACTATCTGCTTTCACTTTGTCATATTCAAT-CTGAGACACGCAACAGGGGATAGGCAAGGCACACAGGGGATAGGNNN";
		String strR = "1,281,593,CTAGTGGAAGATGGTACGGTTTTACAAGATTAGTATTTAGTCCGTTTGTATTTTTAGGAAAGTATAGTATTTGAGTGACTACTATGACAATGTCTGAATAAAGCTAGTTAAGAAAACTTATTTTTTAGTGGTACGTAAGTAGGTAAGCCAAGAAAACAAATTAGTTTGGCGTGAGAGATAAGTAATAGTATTTTATTAGTAATTTATAAATAAAAAGACAAAGAAACGTAAAGAATAATGCAGATGCTAGTAAGTTTATGTCTAGAACTATTTATGGCGTGTAGAAATCGAACTTAAAAGAGATATGGTGGATTACTGGAATGATTGCTTTAGTGATTTACATATCTTGCAACCAGATTGGAAAACTATCCAACGCACTGCGGATAGAGCAATAGTTTTTATGTTATTGAGTGATGAAGAAGAATGGGGAAAGCTTCACAGAAATTCTAGAACAAAATATAAGAATTTGATAAAAGAAATTTCGCCAGTCGATTTAACGGACTTAATGAAATCGACTTTAAAAGCGAAACGAAAAACAATTGACAAAAACAAATCGTATTTTTGGCAACATGAATTTAAATTTTGGAAATAGTGTACATATTAATATTACTGAACAAAAATGATATATTTAAACTATTCTAATTTAGGTAGGATTTTTTTATGTAAGTGTCTATTTAAAAATTTGGGGAATTATATGAGTGAAGAATAATTTACCCCTATAAACTTAGTCACCTCAAGTAAAGAGGTAAAAATTGTTTAGTTTAATATTAAAAAAATTTAAAGGTTTAGTTTTATTAGCGTTTTATTTTGGTCTTTGTATTCTTTCATTTTTTTAGTTGTTATTAAAATGTAAATGGTTTTAAATGTTTCTTTACCTGTATATCTGTAGACACGCAACAGGGGATAGG";
		StringBuffer sbTemp = new StringBuffer();
		int idxComma = strA.indexOf(",");
		int id = Integer.parseInt(strA.substring(0, idxComma));
		sbTemp.delete(0, sbTemp.length());
		idxComma++;
		for(;idxComma<strA.length();idxComma++){
			if(strA.charAt(idxComma)!=',')sbTemp.append(strA.charAt(idxComma));
			else break;
		}
		int stIdx = Integer.parseInt(sbTemp.toString());
		sbTemp.delete(0, sbTemp.length());
		idxComma++;
		for(;idxComma<strA.length();idxComma++){
			if(strA.charAt(idxComma)!=',')sbTemp.append(strA.charAt(idxComma));
			else break;
		}
		int edIdx = Integer.parseInt(sbTemp.toString());
		SequenceCompact[] arrSeq = new SequenceCompact[2];
		SequenceCompact seq = new SequenceCompact(id, strA, stIdx, edIdx, idxComma + 1);
		arrSeq[id] = seq;
		idxComma = strR.indexOf(",");
		id = Integer.parseInt(strR.substring(0, idxComma));
		sbTemp.delete(0, sbTemp.length());
		idxComma++;
		for(;idxComma<strR.length();idxComma++){
			if(strR.charAt(idxComma)!=',')sbTemp.append(strR.charAt(idxComma));
			else break;
		}
		stIdx = Integer.parseInt(sbTemp.toString());
		sbTemp.delete(0, sbTemp.length());
		idxComma++;
		for(;idxComma<strR.length();idxComma++){
			if(strR.charAt(idxComma)!=',')sbTemp.append(strR.charAt(idxComma));
			else break;
		}
		edIdx = Integer.parseInt(sbTemp.toString());
		seq = new SequenceCompact(id, strR, stIdx, edIdx, idxComma + 1);
		arrSeq[id] = seq;
		
		int[] R = new int[3000];;
		int[] buffA = new int[3000];
		int[] R_backup = new int[3000];
		int lenR_backup = 0;
		int[] R_position = new int[3000];
		int[] buffA_backup = new int[3000];
		int lenA_backup = 0;
		int[] buffA_position = new int[3000];
		
		lenR = arrSeq[id2].toIntArrayRev(R);
		lenR_backup = change(R, lenR, R_backup, R_position);
		initSpike(R_backup, lenR_backup, spikes);
		lenA = arrSeq[id1].toIntArray(buffA);
		lenA_backup = change(buffA, lenA, buffA_backup, buffA_position);
		int stR, edR;
		stR = arrSeq[id2].size() - arrSeq[id2].getED() - 1;
		edR = arrSeq[id2].size() - arrSeq[id2].getST() - 1;
		verifyKmer(R, lenR, buffA, lenA);
		alignment(spikes, R_backup, lenR_backup, 
				change(R_position, lenR_backup, stR), change(R_position, lenR_backup, edR),
				buffA_backup, lenA_backup,
				change(buffA_position, lenA_backup, arrSeq[id1].getST()), change(buffA_position, lenA_backup, arrSeq[id1].getED()));
		System.out.println(bestScore.score);
		System.out.println(bestScore.startR);
		System.out.println(bestScore.endR);
		System.out.println(bestScore.startBuffA);
		System.out.println(bestScore.endBuffA);
		System.out.println(bestScore.score);
		System.out.println(R_position[bestScore.startR]);
		System.out.println(R_position[bestScore.endR-1]+1);
		System.out.println(buffA_position[bestScore.startBuffA]);
		System.out.println(buffA_position[bestScore.endBuffA-1]+1);
	}
	
	private static void initSpike(int[] _R, int _sizeR, Spikes _sp){
		_sp.clear();
		stat = 0; 
		kmer = 0;
		x = 0;
		if(_sizeR>2*LenCutOff){
			for(int i=0;i<LenCutOff;i++){
				x = _R[i];
				stat = stat << 2; 
				kmer = kmer << 2; 
				stat += x==4?0:3; 
				kmer += x; 
				stat &= MASK; 
				kmer &= MASK;
				if(stat==MASK){
					_sp.add(kmer, (short) (i - lenKmer + 1));
				}
			}
			for(int i=_sizeR - LenCutOff;i<_sizeR;i++){
				x = _R[i];
				stat = stat << 2; 
				kmer = kmer << 2; 
				stat += x==4?0:3; 
				kmer += x; 
				stat &= MASK; 
				kmer &= MASK;
				if(stat==MASK){
					_sp.add(kmer, (short) (i - lenKmer + 1));
				}
			}
		}else{
			for(int i=0;i<_sizeR;i++){
				x = _R[i];
				stat = stat << 2; 
				kmer = kmer << 2; 
				stat += x==4?0:3; 
				kmer += x; 
				stat &= MASK; 
				kmer &= MASK;
				if(stat==MASK){
					_sp.add(kmer, (short) (i - lenKmer + 1));
				}
			}
		}
	}
	
	private static void alignment(Spikes _sp, int[] _R, int _sizeR, int _stR, int _edR,
			int[] _buffA, int _sizeA, int _stA, int _edA){
		_sp.clearSpikes();
		stat = 0; 
		kmer = 0;
		x = 0;
		for(int i=0;i<_sizeA;i++){
			x = _buffA[i];
			stat = stat << 2; 
			kmer = kmer << 2; 
			stat += x==4?0:3; 
			kmer += x; 
			stat &= MASK; 
			kmer &= MASK;
			if(stat==MASK){
				_sp.addSpikes(kmer, (short) (i - lenKmer + 1));
			}
		}
		arrLoc = _sp.pruneSpikes();
		int pos;
		if(arrLoc.size()!=0){
			bestScore.reset();
			for(int i=0;i<arrLoc.size();i++){
				pos = arrLoc.get(i);
				if(pos>=0&&(_sizeA-pos<LenCutOff)){
					aligner.setR(_R, _sizeR, _sizeA - pos);
					L = _sizeA - pos + 2*extend;
					aligner.setBuffA(_buffA, _sizeA, pos, pos+L);
					aligner.bandedAlign(false);
					bestScore.setValue((int)(aligner.getScore()), 
							aligner.getStartR(0), 
							aligner.getEndR(0),
							aligner.getStartA(pos), 
							aligner.getEndA(pos),0 ,0);
					bestScore.print();
					//remove the end of R
					if(_stR!=0&&pos+_stR<_sizeA){
						aligner.setR(_R, _sizeR, _sizeA - pos - _stR, _stR);
						L = _sizeA - pos - _stR + 2*extend;
						aligner.setBuffA(_buffA, _sizeA, pos + _stR, pos + _stR +L);
						aligner.bandedAlign(false);
						bestScore.setValue((int)(aligner.getScore()), 
								aligner.getStartR(_stR), 
								aligner.getEndR(_stR),
								aligner.getStartA(pos + _stR), 
								aligner.getEndA(pos + _stR), 1, 0);
						bestScore.print();
					}
					//remove the end of A
					if(_edA+1!=_sizeA&&_edA>pos){
						aligner.setR(_R, _sizeR, _edA + 1 - pos);
						L = _edA + 1 - pos + 2*extend;
						aligner.setBuffA(_buffA, _edA + 1, pos, pos+L);
						aligner.bandedAlign(false);
						bestScore.setValue((int)(aligner.getScore()), 
								aligner.getStartR(0), 
								aligner.getEndR(0),
								aligner.getStartA(pos), 
								aligner.getEndA(pos), 0, 2);
						bestScore.print();
					}
					//remove both ends
					if(_stR!=0&&_edA+1!=_sizeA&&pos+_stR<_edA){
						aligner.setR(_R, _sizeR, _edA + 1 - pos - _stR, _stR);
						L = _edA + 1 - pos - _stR + 2*extend;
						aligner.setBuffA(_buffA, _edA + 1, pos + _stR, pos + _stR + L);
						aligner.bandedAlign(false);
						bestScore.setValue((int)(aligner.getScore()), 
								aligner.getStartR(_stR), 
								aligner.getEndR(_stR),
								aligner.getStartA(pos + _stR), 
								aligner.getEndA(pos + _stR), 1, 2);
						bestScore.print();
					}
				}else if(pos<0&&(_sizeR+pos<LenCutOff)){
					aligner.setR(_buffA, _sizeA, _sizeR + pos);
					L = _sizeR + pos + 2*extend;
					aligner.setBuffA(_R, _sizeR, -pos, -pos+L);
					aligner.bandedAlign(false);
					bestScore.setValue((int)(aligner.getScore()), 
							aligner.getStartA(-pos), 
							aligner.getEndA(-pos),
							aligner.getStartR(0), 
							aligner.getEndR(0), 0, 0);
					bestScore.print();
					//remove end of A 
					if(_stA!=0&&-pos+_stA<_sizeR){
						aligner.setR(_buffA, _sizeA, _sizeR + pos - _stA, _stA);
						L = _sizeR + pos - _stA + 2*extend;
						aligner.setBuffA(_R, _sizeR, -pos + _stA, -pos+_stA+L);
						aligner.bandedAlign(false);
						bestScore.setValue((int)(aligner.getScore()), 
								aligner.getStartA(-pos+_stA), 
								aligner.getEndA(-pos+_stA),
								aligner.getStartR(_stA), 
								aligner.getEndR(_stA), 0, 1);
						bestScore.print();
					}
					//remove end of R
					if(_edR+1!=_sizeR&&_edR>-pos){
						aligner.setR(_buffA, _sizeA, _edR + 1 + pos);
						L = _edR + pos + 2*extend;
						aligner.setBuffA(_R, _edR + 1, -pos, -pos+L);
						aligner.bandedAlign(false);
						bestScore.setValue((int)(aligner.getScore()), 
								aligner.getStartA(-pos), 
								aligner.getEndA(-pos),
								aligner.getStartR(0), 
								aligner.getEndR(0), 2, 0);
						bestScore.print();
					}
					//remove both ends
					if(_stA!=0&&_edR+1!=_sizeR&&-pos+_stA<_edR){
						aligner.setR(_buffA, _sizeA, _edR + 1 + pos, _stA);
						L = _edR + 1 + pos - _stA + 2*extend;
						aligner.setBuffA(_R, _edR, _stA-pos, _stA-pos+L);
						aligner.bandedAlign(false);
						bestScore.setValue((int)(aligner.getScore()), 
								aligner.getStartA(_stA-pos), 
								aligner.getEndA(_stA-pos),
								aligner.getStartR(_stA), 
								aligner.getEndR(_stA), 2, 1);
						bestScore.print();
					}
				}
			}
		}
	}
	
	private static int change(int[] _R, int _lenR, int[] _R_backup, int[] _R_position){
		int temp = 0;
		for(int i=0;i<_lenR;i++){
			if(_R[i]!=5){
				_R_position[temp] = i;
				_R_backup[temp] = _R[i];
				temp++;
			}
		}
		return temp;
	}
	
	private static int change(int[] _R_position, int _lenR, int _st){
		int temp = 0;
		for(int i=0;i<_lenR;i++){
			if(_R_position[i]>=_st){
				temp = i;
				break;
			}
		}
		return temp;
	}
	
	/*private static int revStat = 0;
	private static int revKmer = 0; 
	private static int y = 0;*/
	
	public static void verifyKmer(int[] _R, int _sizeR, int[] _A, int _sizeA){
		HashMap<Integer, Integer> hmR = new HashMap<Integer, Integer>();
		stat = 0;
		kmer = 0;
		for(int i=0;i<_sizeR;i++){
			if(_R[i]!=5){
				x = _R[i];
				stat = stat << 2; 
				kmer = kmer << 2; 
				stat += x==4?0:3; 
				kmer += x; 
				stat &= MASK; 
				kmer &= MASK; 
				if(stat==MASK){
					if(hmR.containsKey(kmer)){
						int count = hmR.get(kmer);
						hmR.put(kmer, count+1);
					}else{
						hmR.put(kmer, 1);
					}
				}
			}
		}
		HashMap<Integer, Integer> hmA = new HashMap<Integer, Integer>();
		for(int i=0;i<_sizeA;i++){
			if(_A[i]!=5){
				x = _A[i];
				stat = stat << 2; 
				kmer = kmer << 2; 
				stat += x==4?0:3; 
				kmer += x; 
				stat &= MASK; 
				kmer &= MASK; 
				if(stat==MASK){
					if(hmA.containsKey(kmer)){
						int count = hmA.get(kmer);
						hmA.put(kmer, count+1);
					}else{
						hmA.put(kmer, 1);
					}
				}
			}
		}
		Entry<Integer, Integer> entry;
		for(Iterator<Entry<Integer, Integer>> ite = hmR.entrySet().iterator();ite.hasNext();){
			entry = ite.next();
			if(hmA.containsKey(entry.getKey())){
				if(hmA.get(entry.getKey())<entry.getValue()){
					System.out.println(entry.getKey()+"\t"+hmA.get(entry.getKey()));
				}else{
					System.out.println(entry.getKey()+"\t"+entry.getValue());
				}
			}
		}
	}
}
