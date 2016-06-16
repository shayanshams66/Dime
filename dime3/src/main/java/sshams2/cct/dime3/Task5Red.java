package sshams2.cct.dime3;
/**
 * input <id|id1,score,st1,ed1,st,ed,rev>
 * input <id|id2,score,st2,ed2,st,ed,rev>
 * input <id|id1,score,st,ed,st1,ed1,rev,id2,score,st,ed,st2,ed2,rev>
 * emit <id|sorted edge>
 */
import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class Task5Red extends Reducer<IntWritable, Text, IntWritable, Text>{

	private Text valueOut = new Text();
	
	private Edge edge = new Edge();
	private StringBuffer sb = new StringBuffer();
	private static char comm = ',';
	private String str;
	private int id;
	private int st;
	private SortedEdges arr = new SortedEdges(500);
	
	public void reduce(IntWritable key, Iterable<Text> values, Context context) 
			throws IOException, InterruptedException {
		id = key.get();
		arr.clear();
		for(Iterator<Text> ite = values.iterator();ite.hasNext();){
			str = ite.next().toString();
			st = 0;
			while(st<str.length()){
				st = edge.set(id, str, st, comm, sb);
				arr.add(edge.getId(id), edge);
			}
		}
		sb.delete(0, sb.length());
		for(int i=0;i<arr.Size();i++){
			sb.append(arr.get(i).getId(id));
			sb.append(comm);
			sb.append(arr.get(i).Score());
			sb.append(comm);
			sb.append(arr.get(i).StartId1());
			sb.append(comm);
			sb.append(arr.get(i).EndId1());
			sb.append(comm);
			sb.append(arr.get(i).StartId2());
			sb.append(comm);
			sb.append(arr.get(i).EndId2());
			sb.append(comm);
			if(arr.get(i).Rev()){
				sb.append(1);
			}else{
				sb.append(0);
			}
			sb.append(comm);
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
			valueOut.set(sb.toString());
			context.write(key, valueOut);
		}
	}

}
