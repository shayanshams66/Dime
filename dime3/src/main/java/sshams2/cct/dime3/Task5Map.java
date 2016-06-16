package sshams2.cct.dime3;

/**
 * input <id|id1,score,st,ed,st1,ed1,rev,id2,score,st,ed,st2,ed2,rev>
 * emit <id1|id,score,st,ed,st1,ed1,rev>
 * emit <id2|id,score,st,ed,st2,ed2,rev>
 * emit <id|id1,score,st,ed,st1,ed1,rev,id2,score,st,ed,st2,ed2,rev>
 * note that id < id1,id2,id3
 */
import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class Task5Map extends Mapper<Object,Text,IntWritable,Text>{
	
	private IntWritable keyOut = new IntWritable();
	private Text valueOut = new Text();
	
	private Edge[] arr = new Edge[100];
	private int size = 0;
	private Edge edge = new Edge();
	private StringBuffer sb = new StringBuffer();
	private static String tab = "\t";
	private static char comm = ',';
	private int idxTab = 0;
	private String str;
	private int id;
	private int st;
	
	public void map(Object key, Text value, Context context) throws IOException,InterruptedException
	{
		for(int i=0;i<arr.length;i++){
			if(arr[i]==null)arr[i] = new Edge();
		}
		str = value.toString();
		idxTab = str.indexOf(tab);
		id = Integer.parseInt(str.substring(0, idxTab));
		st = idxTab + 1;
		size = 0;
		while(st<str.length()){
			st = edge.set(id, str, st, comm, sb);
			if(size>=arr.length){
				Edge[] arrNew = new Edge[arr.length*3/2];
				for(int i=0;i<arr.length;i++)arrNew[i] = arr[i];
				for(int i=arr.length;i<arrNew.length;i++)arrNew[i] = new Edge();
				arr = arrNew;
			}
			arr[size].set(edge);
			size++;
		}
		//output
		for(int i=0;i<size;i++){
			keyOut.set(arr[i].getId(id));
			sb.delete(0, sb.length());
			sb.append(id);
			sb.append(comm);
			sb.append(arr[i].Score());
			sb.append(comm);
			sb.append(arr[i].StartId1());
			sb.append(comm);
			sb.append(arr[i].EndId1());
			sb.append(comm);
			sb.append(arr[i].StartId2());
			sb.append(comm);
			sb.append(arr[i].EndId2());
			sb.append(comm);
			if(arr[i].Rev()){
				sb.append(1);
			}else{
				sb.append(0);
			}
			valueOut.set(sb.toString());
			context.write(keyOut, valueOut);
		}
		keyOut.set(id);
		valueOut.set(str.substring(idxTab+1, str.length()));
		context.write(keyOut, valueOut);
	}

}
