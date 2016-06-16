package sshams2.cct.dime3;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * | = tab or separator between key and value
 * input <id|id1 count,id2 count,id3,id4 count> or <-id|id1 count,id2 count,id3,id4 count>
 * emit <id|id1,id2,id3> or <-id|id1,id2,id3>
 * @author Xuan
 */
public class Task2Red extends Reducer<IntWritable, Text, IntWritable, Text>{

	public Text valueOut = new Text();
	private StringBuffer sb = new StringBuffer();
	private String comm = ",";
	private String space = " ";
	private String tab = "\t";
	private String[] strs;
	private String[] arrStrs;
	private int id;
	private short count;
	private short count1;
	private short count2;
	private SortArrayPair hm =new SortArrayPair(2000);
	
	private static int threshold = 10;
	
	private Path[] cacheFiles;
	private ArrayList<SortArrayPair> arrCacheKmer = new ArrayList<SortArrayPair>(2000);
	private ArrayList<Integer> arrIndexContainID = new ArrayList<Integer>();
	private ArrayList<Short> arrCountOfID = new ArrayList<Short>();
	
	protected void setup(Context context){
	     try {
			cacheFiles = DistributedCache.getLocalCacheFiles(context.getConfiguration());
			for (Path pathFile : cacheFiles) {  
				BufferedReader br = new BufferedReader(new FileReader(pathFile.toString()));
				String inputLine = null;
				while((inputLine = br.readLine())!=null){
					SortArrayPair cacheHm =new SortArrayPair(1000);
					arrCacheKmer.add(cacheHm);
					strs = inputLine.split(tab);
					arrStrs = strs[1].split(comm);
					for(int i=0;i<arrStrs.length;i++){
						if(arrStrs[i].contains(space)){
							strs = arrStrs[i].split(space);
							id = Integer.parseInt(strs[0]);
							count = Short.parseShort(strs[1]);
						}else{
							id = Integer.parseInt(arrStrs[i]);
							count = 1;
						}
						cacheHm.add(id, count);
					}
				}
				br.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized void reduce(IntWritable key, Iterable<Text> values, Context context) 
			throws IOException, InterruptedException {
		hm.clear();
		for(Iterator<Text> ite = values.iterator();ite.hasNext();){
			strs = ite.next().toString().split(comm);
			for(int i=0;i<strs.length;i++){
				if(strs[i].contains(space)){
					arrStrs = strs[i].split(space);
					if(arrStrs.length!=2){System.out.println("T2C: two more element for (id count)");System.exit(1);}
					id = Integer.parseInt(arrStrs[0]);
					count = Short.parseShort(arrStrs[1]);
				}else{
					id = Integer.parseInt(strs[i]);
					count = 1;
				}
				hm.add(id, count);
			}
		}
		if(hm.getSize()==0)return;
		//output key value
		arrIndexContainID.clear();
		this.arrCountOfID.clear();
		id = key.get();
		for(int i=0;i<this.arrCacheKmer.size();i++){
			count = arrCacheKmer.get(i).inArray(id);
			if(count>0){
				arrIndexContainID.add(i);
				arrCountOfID.add(count);
			}
		}
		sb.delete(0, sb.length());
		for(int i=0;i<hm.getSize();i++){
			if(hm.getVal(i)>threshold){
				sb.append(hm.getId(i));
				sb.append(comm);
			}else{
				count = 0;
				id = hm.getId(i);
				for(int j=0;j<arrIndexContainID.size();j++){
					count1 = arrCountOfID.get(j);
					count2 = arrCacheKmer.get(arrIndexContainID.get(j)).inArray(id);
					count += count1<count2?count1:count2;
				}
				if(hm.getVal(i)+count>threshold){
					sb.append(id);
					sb.append(comm);
				}
			}
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
			valueOut.set(sb.toString());
			context.write(key, valueOut);
		}
	}
}
