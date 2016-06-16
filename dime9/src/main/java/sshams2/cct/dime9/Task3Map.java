package sshams2.cct.dime9;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * input <id|id1,id2,id3> or <-id|id1,id2>
 * emit <id|id1,score,st_id,ed_id,st_id1,ed_id1,0,id2,score,st_id,ed_id,st_id2,ed_id2,1> 
 * 0 = id is positive
 * 1 = id is negative sequence of id need to be reversed 
 * ed_id is not included
 * @author Xuan
 *
 */
public class Task3Map extends Mapper<Object,Text,IntWritable,Text>{

	private IntWritable keyOut = new IntWritable();
	private Text valueOut = new Text();
	
	private char comm = ',';
	private String tab = "\t";
	public static String comma = ",";
	
	private StringBuffer sb = new StringBuffer();
	private StringBuffer sbStr = new StringBuffer();
	private int numReads = 0;
	private SequenceCompact[] arrSeq;
	private Path[] cacheFiles;
	
	private int[] R = null;
	private int[] buffA = null;
	private int id1;
	private int id2;
	private int lenR;
	private int lenA;
	private int idxTab;
	private int idxComma;
	private Spikes spikes =new Spikes(2500, 10);
	private int stat = 0; 
	private int kmer = 0;
	private static int MASK = 1073741823;//1048575;
	public static int lenKmer = 15;
	private int x = 0;
	private ArrayList<Integer> arrLoc;
	private Alignment aligner = new Alignment(5000, 5000, -3, -3, 1, -1, 0);
	private int extend = Alignment.extend;
	private int L = 0;
	private DoubleIntArray bestScore = new DoubleIntArray();
	private static final int ThreScore = 0; 
	//the score for match of one bit is 1; 20 means at least 20 bits match
	
	private int LenCutOff = MatrixConstruct.LenCutOff;
	private int[] R_backup = null;
	private int lenR_backup = 0;
	private int[] R_position = null;
	private int[] buffA_backup = null;
	private int lenA_backup = 0;
	private int[] buffA_position = null;
	
	protected void setup(Context context){
		lenKmer = Integer.parseInt(context.getConfiguration().get(MatrixConstruct.strParaLmer,"15"));
		MASK = 1;
		MASK = MASK<<(2*lenKmer);
		MASK = MASK - 1;
		numReads = Integer.parseInt(context.getConfiguration().get(MatrixConstruct.strParaNumReads,"0"));
		if(numReads==0){
			System.out.println("error get the parameter in task 3 Map");
			System.exit(1);
		}
		if(arrSeq ==null||arrSeq.length!=numReads+1){
			arrSeq = new SequenceCompact[numReads+1];
		}
		String prefix = context.getConfiguration().get(MatrixConstruct.strParaPrefix,"SRR");
		StringBuffer sbTemp = new StringBuffer();
		int maxLen = 0;
		try {
			cacheFiles = DistributedCache.getLocalCacheFiles(context.getConfiguration());
			for (Path pathFile : cacheFiles) {
				if(pathFile.toString().contains(prefix)){
					BufferedReader br = new BufferedReader(new FileReader(pathFile.toString()));
					String inputLine = null;
					while((inputLine = br.readLine())!=null){
						idxComma = inputLine.indexOf(comma);
						//strs = inputLine.split(space);
						int id = Integer.parseInt(inputLine.substring(0, idxComma));
						sbTemp.delete(0, sbTemp.length());
						idxComma++;
						for(;idxComma<inputLine.length();idxComma++){
							if(inputLine.charAt(idxComma)!=',')sbTemp.append(inputLine.charAt(idxComma));
							else break;
						}
						int stIdx = Integer.parseInt(sbTemp.toString());
						sbTemp.delete(0, sbTemp.length());
						idxComma++;
						for(;idxComma<inputLine.length();idxComma++){
							if(inputLine.charAt(idxComma)!=',')sbTemp.append(inputLine.charAt(idxComma));
							else break;
						}
						int edIdx = Integer.parseInt(sbTemp.toString());
						if(arrSeq[id]!=null&&arrSeq[id].size()==(inputLine.length()-idxComma-1))continue;
						SequenceCompact seq = new SequenceCompact(id, inputLine, stIdx, edIdx, idxComma + 1);
						if(seq.size()>maxLen)maxLen = seq.size();
						arrSeq[seq.getId()] = seq;
					}
					br.close();
				}
			}
			R = new int[maxLen];
			buffA = new int[maxLen];
			R_backup = new int[maxLen];
			R_position = new int[maxLen];
			buffA_backup = new int[maxLen];
			buffA_position = new int[maxLen];
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void map(Object key, Text value, Context context) 
			throws IOException,InterruptedException
	{
		//boolean flagtest = true;
		//if(flagtest)return;
		String strVal = value.toString();
		idxTab = strVal.indexOf(tab);
		id1 = Integer.parseInt(strVal.substring(0, idxTab));
		int stR, edR;
		if(id1>0){
			lenR = arrSeq[id1].toIntArray(R);
			stR = arrSeq[id1].getST();
			edR = arrSeq[id1].getED();
		}else{
			lenR = arrSeq[-id1].toIntArrayRev(R);
			stR = lenR - arrSeq[-id1].getED() - 1;
			edR = lenR - arrSeq[-id1].getST() - 1;
		}
		lenR_backup = change(R, lenR, R_backup, R_position);
		this.initSpike(R_backup, lenR_backup, spikes);
		sb.delete(0, sb.length());
		for(int i=idxTab+1;i<strVal.length();i++){
			sbStr.delete(0, sbStr.length());
			while(strVal.charAt(i)!=comm){
				sbStr.append(strVal.charAt(i));
				i++;
				if(i>=strVal.length())break;
			}
			id2 = Integer.parseInt(sbStr.toString());
			lenA = arrSeq[id2].toIntArray(buffA);
			lenA_backup = change(buffA, lenA, buffA_backup, buffA_position);
			bestScore.reset();
			this.alignment(spikes, R_backup, lenR_backup, 
					change(R_position, lenR_backup, stR), change(R_position, lenR_backup, edR),
					buffA_backup, lenA_backup, 
					change(buffA_position, lenA_backup, arrSeq[id2].getST()), change(buffA_position, lenA_backup, arrSeq[id2].getED()));
			if(bestScore.score<ThreScore){
				continue;
			}
			sb.append(id2); 
			sb.append(comm); 
			sb.append(bestScore.score);
			sb.append(comm);
			sb.append(bestScore.startR);
			sb.append(comm);
			sb.append(bestScore.endR);
			sb.append(comm);
			sb.append(bestScore.startBuffA);
			sb.append(comm);
			sb.append(bestScore.endBuffA);
			sb.append(comm);
			if(id1>0){
				sb.append(0);//not reverse
				sb.append(comm);
			}else{
				sb.append(1);//reverse
				sb.append(comm);
			}
			sb.append(bestScore.endStatusR);
			sb.append(comm);
			sb.append(bestScore.endStatusA);
			sb.append(comm);
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
			keyOut.set(Math.abs(id1));
			valueOut.set(sb.toString());
			context.write(keyOut, valueOut);
		}
	}
	
	private void initSpike(int[] _R, int _sizeR, Spikes _sp){
		_sp.clear();
		stat = 0; 
		kmer = 0;
		x = 0;
		if(_sizeR>2*LenCutOff){
			stat = 0; 
			kmer = 0;
			x = 0;
			for(int i=0;i<LenCutOff;i++){
				x = _R[i];
				stat = stat << 2; 
				kmer = kmer << 2; 
				stat += x==4?0:3; 
				kmer += x; 
				stat &= MASK; 
				kmer &= MASK;
				if(stat==MASK){
					_sp.add(kmer, (i - lenKmer + 1));
				}
			}
			stat = 0; 
			kmer = 0;
			x = 0;
			for(int i=_sizeR - LenCutOff;i<_sizeR;i++){
				x = _R[i];
				stat = stat << 2; 
				kmer = kmer << 2; 
				stat += x==4?0:3; 
				kmer += x; 
				stat &= MASK; 
				kmer &= MASK;
				if(stat==MASK){
					_sp.add(kmer, (i - lenKmer + 1));
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
					_sp.add(kmer, (i - lenKmer + 1));
				}
			}
		}
	}
	
	private void alignment(Spikes _sp, int[] _R, int _sizeR, int _stR, int _edR,
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
				_sp.addSpikes(kmer, (i - lenKmer + 1));
			}
		}
		arrLoc = _sp.pruneSpikes();
		int pos;
		if(arrLoc.size()!=0){
			bestScore.reset();
			for(int i=0;i<arrLoc.size();i++){
				pos = arrLoc.get(i);
				if(pos>=0&&(_sizeA-pos<LenCutOff)){
					this.aligner.setR(_R, _sizeR, _sizeA - pos);
					L = _sizeA - pos + 2*extend;
					this.aligner.setBuffA(_buffA, _sizeA, pos, pos + L);
					this.aligner.bandedAlign(true);
					bestScore.setValue((int)(aligner.getScore()), 
							aligner.getStartR(0), 
							aligner.getEndR(0),
							aligner.getStartA(pos), 
							aligner.getEndA(pos), 0, 0);
					//remove the end of R
					if(_stR!=0&&pos+_stR<_sizeA){
						this.aligner.setR(_R, _sizeR, _sizeA - pos - _stR, _stR);
						L = _sizeA - pos - _stR + 2*extend;
						this.aligner.setBuffA(_buffA, _sizeA, pos + _stR, pos + _stR +L);
						this.aligner.bandedAlign(true);
						bestScore.setValue((int)(aligner.getScore()), 
								aligner.getStartR(_stR), 
								aligner.getEndR(_stR),
								aligner.getStartA(pos + _stR), 
								aligner.getEndA(pos + _stR), 1, 0);
					}
					//remove the end of A
					if(_edA+1!=_sizeA&&_edA>pos){
						this.aligner.setR(_R, _sizeR, _edA + 1 - pos);
						L = _edA + 1 - pos + 2*extend;
						this.aligner.setBuffA(_buffA, _edA + 1, pos, pos+L);
						this.aligner.bandedAlign(true);
						bestScore.setValue((int)(aligner.getScore()), 
								aligner.getStartR(0), 
								aligner.getEndR(0),
								aligner.getStartA(pos), 
								aligner.getEndA(pos), 0, 2);
					}
					//remove both ends
					if(_stR!=0&&_edA+1!=_sizeA&&pos+_stR<_edA){
						this.aligner.setR(_R, _sizeR, _edA + 1 - pos - _stR, _stR);
						L = _edA + 1 - pos - _stR + 2*extend;
						this.aligner.setBuffA(_buffA, _edA + 1, pos + _stR, pos + _stR + L);
						this.aligner.bandedAlign(true);
						bestScore.setValue((int)(aligner.getScore()), 
								aligner.getStartR(_stR), 
								aligner.getEndR(_stR),
								aligner.getStartA(pos + _stR), 
								aligner.getEndA(pos + _stR), 1, 2);
					}
				}else if(pos<0&&(_sizeR+pos<LenCutOff)){
					this.aligner.setR(_buffA, _sizeA, _sizeR + pos);
					L = _sizeR + pos + 2*extend;
					if(L<0){
						System.out.println("L: "+L+"; _sizeR: "+_sizeR+"; pos: "+pos);
						System.exit(1);
					}
					this.aligner.setBuffA(_R, _sizeR, -pos, -pos+L);
					this.aligner.bandedAlign(true);
					bestScore.setValue((int)(aligner.getScore()), 
							aligner.getStartA(-pos), 
							aligner.getEndA(-pos),
							aligner.getStartR(0), 
							aligner.getEndR(0), 0, 0);
					//remove end of A 
					if(_stA!=0&&-pos+_stA<_sizeR){
						this.aligner.setR(_buffA, _sizeA, _sizeR + pos - _stA, _stA);
						L = _sizeR + pos - _stA + 2*extend;
						this.aligner.setBuffA(_R, _sizeR, -pos + _stA, -pos+_stA+L);
						this.aligner.bandedAlign(true);
						bestScore.setValue((int)(aligner.getScore()), 
								aligner.getStartA(-pos+_stA), 
								aligner.getEndA(-pos+_stA),
								aligner.getStartR(_stA), 
								aligner.getEndR(_stA), 0, 1);
					}
					//remove end of R
					if(_edR+1!=_sizeR&&_edR>-pos){
						this.aligner.setR(_buffA, _sizeA, _edR + 1 + pos);
						L = _edR + pos + 2*extend;
						this.aligner.setBuffA(_R, _edR + 1, -pos, -pos+L);
						this.aligner.bandedAlign(true);
						bestScore.setValue((int)(aligner.getScore()), 
								aligner.getStartA(-pos), 
								aligner.getEndA(-pos),
								aligner.getStartR(0), 
								aligner.getEndR(0), 2, 0);
					}
					//remove both ends
					if(_stA!=0&&_edR+1!=_sizeR&&-pos+_stA<_edR){
						this.aligner.setR(_buffA, _sizeA, _edR + 1 + pos, _stA);
						L = _edR + 1 + pos - _stA + 2*extend;
						this.aligner.setBuffA(_R, _edR, _stA-pos, _stA-pos+L);
						this.aligner.bandedAlign(true);
						bestScore.setValue((int)(aligner.getScore()), 
								aligner.getStartA(_stA-pos), 
								aligner.getEndA(_stA-pos),
								aligner.getStartR(_stA), 
								aligner.getEndR(_stA), 2, 1);
					}
				}
			}
		}
	}
	
	private int change(int[] _R, int _lenR, int[] _R_backup, int[] _R_position){
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
	
	private int change(int[] _R_position, int _lenR, int _st){
		int temp = 0;
		for(int i=0;i<_lenR;i++){
			if(_R_position[i]>=_st){
				temp = i;
				break;
			}
		}
		return temp;
	}
}
