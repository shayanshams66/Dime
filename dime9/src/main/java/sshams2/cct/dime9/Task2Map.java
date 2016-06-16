package sshams2.cct.dime9;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
/**
 * | = tab or separator between key and value
 * input <kmer|id count,-id count,id,-id>
 * emit <id|id1 count,id2 count,id3,id4 count> or <-id|id1 count,id2 count,id3,id4 count>
 * @author Xuan
 */
public class Task2Map extends Mapper<Object,Text,IntWritable,Text>{

	public IntWritable keyOut = new IntWritable();
	public Text valueOut = new Text();
	
	private String space = " ";
	private String comm = ",";
	private String tab = "\t";
	private StringBuffer sb = new StringBuffer();
	
	private SortArrayPair hmp = new SortArrayPair(500);
	private SortArrayPair hmn =new SortArrayPair(500);
	private String[] strs;
	private String[] arrStrs;
	private int id = 0;
	private short count = 0;
	private int id1 = 0;
	private int id2 = 0;
	
	public synchronized void map(Object key, Text value, Context context) 
			throws IOException,InterruptedException
	{
		hmp.clear();
		hmn.clear();
		strs = value.toString().split(tab)[1].split(comm);
		for(int i=0;i<strs.length;i++){
			if(strs[i].contains(space)){
				arrStrs = strs[i].split(space);
				if(arrStrs.length>2){System.out.println("T2M: two more element for (id count)");System.exit(1);}
				id = Integer.parseInt(arrStrs[0]);
				count = Short.parseShort(arrStrs[1]);
			}else{
				id = Integer.parseInt(strs[i]);
				count = 1;
			}
			if(id>0){
				hmp.add(id, count);
			}else{
				hmn.add(id, count);
			}
			
		}
		//output counts of Kmer between two reads
		for(int i=0;i<hmp.getSize();i++){
			id1 = hmp.getId(i);
			sb.delete(0, sb.length());
			for(int j=i+1;j<hmp.getSize();j++){
				count = hmp.getVal(i)<hmp.getVal(j)?hmp.getVal(i):hmp.getVal(j);
				if(count==1){
					sb.append(hmp.getId(j));
					sb.append(comm);
				}else{
					sb.append(hmp.getId(j));
					sb.append(space);
					sb.append(count);
					sb.append(comm);
				}
				
			}
			if(sb.length()>0){
				sb.deleteCharAt(sb.length()-1);
				keyOut.set(id1);
				valueOut.set(sb.toString());
				context.write(keyOut, valueOut);
			}
		}
		for(int i=0;i<hmn.getSize();i++){
			id1 = hmn.getId(i);
			sb.delete(0, sb.length());
			for(int j=0;j<hmp.getSize();j++){
				id2 = hmp.getId(j);
				if((-id1)<id2){
					count = hmn.getVal(i)<hmp.getVal(j)?hmn.getVal(i):hmp.getVal(j);
					if(count==1){
						sb.append(id2);
						sb.append(comm);
					}else{
						sb.append(id2);
						sb.append(space);
						sb.append(count);
						sb.append(comm);
					}
				}
			}
			if(sb.length()>0){
				sb.deleteCharAt(sb.length()-1);
				keyOut.set(id1);
				valueOut.set(sb.toString());
				context.write(keyOut, valueOut);
			}
		}
	}

}
