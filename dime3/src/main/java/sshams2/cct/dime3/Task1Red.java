package sshams2.cct.dime3;
import java.io.IOException;
import java.util.Iterator;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;

/**
 * | = tab or to separate key and value
 * input <kmer|id count,-id count,id,-id>
 * emit <kmer|id count,-id count,id,-id>
 * if the count is larger than C_max, it goes to LmerLong
 * else it goes to LmerSmall
 * @author Xuan
 *
 */
public class Task1Red extends Reducer<IntWritable, Text, IntWritable, Text>{

	private static String strLmerSmall = "LmerSmall";
	private static String strLmerLong = "LmerLong";
	
	private Text valueOut = new Text();
	private String space = " ";
	private String comm = ",";
	private StringBuffer sb = new StringBuffer();
	private String[] strs;
	private String[] arrStrs;
	private int id = 0;
	private short count = 0;
	private SortArrayPair hm =new SortArrayPair(20000);
	
	private MultipleOutputs<IntWritable, Text> mos;
	private int Cmax = 500;
	
	public void setup(Context context) {
		Cmax = Integer.parseInt(context.getConfiguration().get(MatrixConstruct.strParaCmax,"500"));
        mos = new MultipleOutputs<IntWritable, Text>(context);
    }
	
	public void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
		
		hm.clear();
		for(Iterator<Text> ite = values.iterator();ite.hasNext();){
			strs = ite.next().toString().split(comm);
			for(int i=0;i<strs.length;i++){
				if(strs[i].contains(space)){
					arrStrs = strs[i].split(space);
					id = Integer.parseInt(arrStrs[0]);
					count = Short.parseShort(arrStrs[1]);
				}else{
					id = Integer.parseInt(strs[i]);
					count = 1;
				}
				hm.add(id, count);
			}
		}
		//output the text
		sb.delete(0, sb.length());
		for(int i = 0;i<hm.getSize();i++){
			id = hm.getId(i);
			count = hm.getVal(i);
			if(count == 1){
				sb.append(id);
				sb.append(comm);
			}else{
				sb.append(id);
				sb.append(space);
				sb.append(count);
				sb.append(comm);
			}
		}
		sb.deleteCharAt(sb.length()-1);
		valueOut.set(sb.toString());
		if(hm.getSize()>Cmax){
			mos.write(strLmerLong, key, valueOut);
		}else{
			mos.write(strLmerSmall, key, valueOut);
		}
		//context.write(key, valueOut);
	}
	
    protected void cleanup(Context context) throws IOException, InterruptedException {
        mos.close();
    }
	
}
