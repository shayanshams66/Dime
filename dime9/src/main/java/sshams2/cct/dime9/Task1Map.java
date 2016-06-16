package sshams2.cct.dime9;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * | = tab or the separator between key and value 
 * input <id,st,ed,SEQ> 1,27,113,TACCAAATCATTAGATACGCTTAAAGCTTCTGAGTGTCATGTATGAATTAAACTAAATGTAGGTGGATGGTTTCATAAAGAGAAGCAAGAAACAGGCAATATTGTAATTACTACTGAGACACGCAACAGGGGATAGGCAAGGCACACAGGGGATAGGN
 * emit the <kmer|id>
 * emit the <kmer|-id>
 * @author Xuan
 *
 */
public class Task1Map extends Mapper<Object,Text,IntWritable,Text>{
	private static int MASK = 1073741823;//16777215;//1048575;
	public static int lenKmer = 15;//12;//10;
	
	private IntWritable keyOut = new IntWritable();
	private Text valueOut = new Text();
	private StringBuffer sb = new StringBuffer();
	public String space = " ";
	private String dash = "-";
	public static String comma = ",";
	
	private int stat = 0; 
	private int kmer = 0;
	private int revStat = 0;
	private int revKmer = 0; 
	private int x = 0;
	private int y = 0;
	
	private int LenCutOff = MatrixConstruct.LenCutOff;
	private char empty = '-';
	
	public void setup(Context context) {
		lenKmer = Integer.parseInt(context.getConfiguration().get(MatrixConstruct.strParaLmer,"15"));
		MASK = 1;
		MASK = MASK<<(2*lenKmer);
		MASK = MASK - 1;
	}
	
	public synchronized void map(Object key, Text value, Context context) throws IOException,InterruptedException
	{
		String str[] = value.toString().split(comma);
		String id = str[0];
		int len = str[3].length();
		stat = 0; kmer = 0; revStat = 0; revKmer = 0;
		x = 0; y = 0;
		int upper = 0;
		if(len >= 2*LenCutOff){
			upper = LenCutOff;
		}else{
			upper = len;
		}
		for(int i=0;i<upper;i++){
			if(str[3].charAt(i)!=empty){
				x = this.toHash(str[3].charAt(i));
				stat = stat << 2; 
				kmer = kmer << 2; 
				stat += x==4?0:3; 
				kmer += x; 
				stat &= MASK; 
				kmer &= MASK; 
				if(stat==MASK){
					keyOut.set(kmer);
					sb.delete(0, sb.length());
					sb.append(id);
					valueOut.set(sb.toString());
					context.write(keyOut, valueOut);
				}
			}
			if(str[3].charAt(len - i - 1)!=empty){
				y = this.toHash(str[3].charAt(len - i - 1));
				y = y==4?4:3-y;
				revStat = revStat << 2;
				revKmer = revKmer << 2;
				revStat += y==4?0:3;
				revKmer += y;
				revStat &= MASK;
				revKmer &= MASK;
				if(revStat==MASK){
					keyOut.set(revKmer);
					sb.delete(0, sb.length());
					sb.append(dash); sb.append(id);
					valueOut.set(sb.toString());
					context.write(keyOut, valueOut);
				}
			}
		}
	}
	
	/**
	 * convert from ACGT to 0123 and N is changed to 4
	 * @param _a
	 * @return
	 */
	private int toHash(char _a){
		switch (_a){
		case 'A':
		case 'a':return 0;
		case 'C':
		case 'c':return 1;
		case 'G':
		case 'g':return 2;
		case 'T':
		case 't':return 3;
		case 'N':
		case 'n':return 4;
		case 'R':return 4;
		case 'Y':return 4;
		case 'W':return 4;
		case '-':{System.out.println("Error in inputSequence: "+_a);return 4;}
		default:
			System.out.println("Error in inputSequence: "+_a);
			//System.exit(1);
			return 4;
		}		
	}
}
