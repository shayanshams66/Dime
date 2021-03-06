package sshams2.cct.dime3;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;


/**
 * input <id|id1,score,st_id,ed_id,st_id1,ed_id1,0,id2,score,st_id,ed_id,st_id2,ed_id2,1> 
 * input <id|id1,id2,id3,-> or <-id|id1,id2,->
 * 
 * @author Xuan
 *
 */
public class Task3Red extends Reducer<IntWritable, Text, IntWritable, Text>{
	
	public static String strAligned = "Aligned";
	public static String strUnaligned = "Unaligned";
	
	private String comm = ",";
	private char strNeg = '-';
	private String[] strs;
	private int id2;
	private DoubleIntArray[] arrScores;
	private int size = 0;
	private HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>(100);
	private Text valueOut = new Text();
	private StringBuffer sb = new StringBuffer();
	private Entry<Integer, Integer> entry;
	private int idx = 0;
	private MultipleOutputs<IntWritable, Text> mos;
	private String strIte;
	
	protected void setup(Context context){
		arrScores = new DoubleIntArray[100];
		for(int i=0;i<arrScores.length;i++){
			arrScores[i] = new DoubleIntArray();
		}
		mos = new MultipleOutputs<IntWritable, Text>(context);
	}
	
	public synchronized void reduce(IntWritable key, Iterable<Text> values, Context context) 
			throws IOException, InterruptedException {
		size = 0;
		hm.clear();
		for(Iterator<Text> ite = values.iterator();ite.hasNext();){
			strIte = ite.next().toString();
			if(strIte.charAt(strIte.length()-1)==strNeg){
				sb.delete(0, sb.length());
				sb.append(strIte);
				sb.deleteCharAt(sb.length()-1);
				valueOut.set(sb.toString());
				mos.write(strUnaligned, key, valueOut);
				continue;
			}
			strs = strIte.split(comm);
			for(int i=0;i<strs.length;i+=7){
				id2 = Integer.parseInt(strs[i]);
				if(hm.containsKey(id2)){
					arrScores[hm.get(id2)].setValue(Integer.parseInt(strs[i+1]), 
							Integer.parseInt(strs[i+2]), 
							Integer.parseInt(strs[i+3]), 
							Integer.parseInt(strs[i+4]),
							Integer.parseInt(strs[i+5]),
							Integer.parseInt(strs[i+6]));
				}else{
					if(size>=arrScores.length){
						DoubleIntArray[] arrNew = new DoubleIntArray[arrScores.length*3/2];
						for(int j=0;j<arrScores.length;j++){
							arrNew[j] = arrScores[j];
						}
						for(int j=arrScores.length;j<arrNew.length;j++){
							arrNew[j] = new DoubleIntArray();
						}
						arrScores = arrNew;
					}
					hm.put(id2, size);
					arrScores[size].reset();
					arrScores[size].setValue(Integer.parseInt(strs[i+1]), 
							Integer.parseInt(strs[i+2]), 
							Integer.parseInt(strs[i+3]), 
							Integer.parseInt(strs[i+4]),
							Integer.parseInt(strs[i+5]),
							Integer.parseInt(strs[i+6]));
					size++;
				}
			}
		}
		sb.delete(0, sb.length());
		for(Iterator<Entry<Integer, Integer>> ite = hm.entrySet().iterator();ite.hasNext();){
			entry = ite.next();
			id2 = entry.getKey();
			idx = entry.getValue();
			sb.append(id2);
			sb.append(comm);
			sb.append(arrScores[idx].score);
			sb.append(comm);
			sb.append(arrScores[idx].startR);
			sb.append(comm);
			sb.append(arrScores[idx].endR);
			sb.append(comm);
			sb.append(arrScores[idx].startBuffA);
			sb.append(comm);
			sb.append(arrScores[idx].endBuffA);
			sb.append(comm);
			if(arrScores[idx].rev){
				sb.append(1);
			}else{
				sb.append(0);
			}
			sb.append(comm);
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
			valueOut.set(sb.toString());
			mos.write(strAligned, key, valueOut);
		}
	}
	
	public static int extract(String _target, StringBuffer _sb, int _st, char _separator){
		_sb.delete(0, _sb.length());
		while(_target.charAt(_st)!=_separator){
			_sb.append(_target.charAt(_st));
			_st++;
			if(_st>=_target.length())break;
		}
		return _st;
	}

	protected void cleanup(Context context) throws IOException, InterruptedException {
        mos.close();
    }
}
