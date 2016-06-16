package sshams2.cct.dime3;
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
 * emit <id|id1,id2,id3,-> or <-id|id1,id2,->
 * 0 = id is positive
 * 1 = id is negative sequence of id need to be reversed
 * - = id, or id1, or id2, ..., are not in the Dictionary
 * ed_id is not included
 * @author Xuan
 *
 */
public class Task3Map extends Mapper<Object,Text,IntWritable,Text>{

	private IntWritable keyOut = new IntWritable();
	private Text valueOut = new Text();
	
	private char comm = ',';
	private String tab = "\t";
	private static String space = " ";
	private static String strNeg = "-";
	
	private StringBuffer sb = new StringBuffer();
	private StringBuffer sbStr = new StringBuffer();
	private int numReads = 0;
	private SequenceCompact[] arrSeq;
	private Path[] cacheFiles;
	
	private int[] R = new int[5000];
	private int[] buffA = new int[5000];
	private int id1;
	private int id2;
	private int len1;
	private int len2;
	private int idxTab;
	private int idxSpace;
	private Spikes spikes =new Spikes(2500, 10);
	private int stat = 0; 
	private int kmer = 0;
	private static int MASK = 1073741823;//1048575;
	public static int lenKmer = 15;
	private int x = 0;
	private ArrayList<Integer> arrLoc;
	private Alignment aligner = new Alignment(5000, 5000, -4, -4, 1, -1, 0);
	private static int extend = 5;
	private int L = 0;
	private DoubleIntArray bestScore = new DoubleIntArray();
	private static final int ThreScore = 0; 
	//the score for match of one bit is 1; 20 means at least 20 bits match
	
	private int idM = 0;//used in the repeat operation
	private int[] arrIdM = new int[100];
	private int sizeArrIdM = 0;
	
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
		for(int i=0;i<arrSeq.length;i++){
			if(arrSeq[i]!=null)arrSeq[i]=null;
		}
		String prefix = context.getConfiguration().get(MatrixConstruct.strParaPrefix,"SRR");
		try {
			cacheFiles = DistributedCache.getLocalCacheFiles(context.getConfiguration());
			for (Path pathFile : cacheFiles) {
				if(pathFile.toString().contains(prefix)){
					BufferedReader br = new BufferedReader(new FileReader(pathFile.toString()));
					String inputLine = null;
					while((inputLine = br.readLine())!=null){
						idxSpace = inputLine.indexOf(space);
						//strs = inputLine.split(space);
						int id = Integer.parseInt(inputLine.substring(0, idxSpace));
						if(arrSeq[id]!=null&&arrSeq[id].getSize()==(inputLine.length()-idxSpace-1))continue;
						SequenceCompact seq = new SequenceCompact(id, inputLine, idxSpace + 1);
						arrSeq[seq.getId()] = seq;
					}
					br.close();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized void map(Object key, Text value, Context context) 
			throws IOException,InterruptedException
	{
		//boolean flagtest = true;
		//if(flagtest)return;
		sizeArrIdM = 0;
		String strVal = value.toString();
		idxTab = strVal.indexOf(tab);
		id1 = Integer.parseInt(strVal.substring(0, idxTab));
		idM = Math.abs(id1);
		if(arrSeq[idM]==null){
			keyOut.set(id1);
			valueOut.set(strVal.substring(idxTab+1)+strNeg);
			context.write(keyOut, valueOut);
			return;
		}
		if(id1>0){
			len1 = arrSeq[id1].toIntArray(R);
		}else{
			len1 = arrSeq[-id1].toIntArrayRev(R);
		}
		this.initSpike(R, len1, spikes);
		sb.delete(0, sb.length());
		for(int i=idxTab+1;i<strVal.length();i++){
			sbStr.delete(0, sbStr.length());
			while(strVal.charAt(i)!=comm){
				sbStr.append(strVal.charAt(i));
				i++;
				if(i>=strVal.length())break;
			}
			id2 = Integer.parseInt(sbStr.toString());
			if(arrSeq[id2]==null){
				if(sizeArrIdM>=arrIdM.length){
					int[] arrIdMNew = new int[arrIdM.length*3/2];
					for(int x=0;x<arrIdM.length;x++)arrIdMNew[x] = arrIdM[x];
					arrIdM = arrIdMNew;
				}
				arrIdM[sizeArrIdM] = id2;
				sizeArrIdM++;
				continue;
			}
			len2 = arrSeq[id2].toIntArray(buffA);
			bestScore.reset();
			this.alignment(spikes, R, len1, buffA, len2);
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
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
			keyOut.set(Math.abs(id1));
			valueOut.set(sb.toString());
			context.write(keyOut, valueOut);
		}
		if(sizeArrIdM>0){
			keyOut.set(id1);
			sb.delete(0, sb.length());
			for(int i=0;i<sizeArrIdM;i++){
				sb.append(Integer.toString(arrIdM[i]));
				sb.append(comm);
			}
			sb.append(strNeg);
			valueOut.set(sb.toString());
			context.write(keyOut, valueOut);
			sizeArrIdM = 0;
		}
	}
	
	private void initSpike(int[] _R, int _sizeR, Spikes _sp){
		_sp.clear();
		stat = 0; 
		kmer = 0;
		x = 0;
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
	
	private void alignment(Spikes _sp, int[] _R, int _sizeR, int[] _buffA, int _sizeA){
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
		if(arrLoc.size()!=0){
			bestScore.reset();
			this.aligner.setR(_R, _sizeR);
			for(int i=0;i<arrLoc.size();i++){
				L = _sizeR + 2*extend;
				this.aligner.setBuffA(_buffA, _sizeA, arrLoc.get(i), arrLoc.get(i)+L);
				this.aligner.bandedAlign(true);
				bestScore.setValue((int)(aligner.getScore()), 
						aligner.getStartR(), 
						aligner.getEndR(),
						aligner.getStartBuffA(), 
						aligner.getEndBuffA());
			}
		}
	}
}
