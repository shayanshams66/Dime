package sshams2.cct.dime7;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class TaskMap extends Mapper<Object,Text,IntWritable,Text>{

	private IntWritable keyOut = new IntWritable();
	private Text valueOut = new Text();
	private static String strHomeDir = "";
	
	protected void setup(Context context){
		strHomeDir = context.getConfiguration().get(HadoopProcess.strParaDir,"/home/xguo9/HadoopResult");
	}
	
	public synchronized void map(Object key, Text value, Context context) 
			throws IOException,InterruptedException
	{
		String str = value.toString();
		String[] arrStr = str.split(" ");
		
		List<String> command = new ArrayList<String>();
		for(int i=0;i<arrStr.length;i++){
			command.add(arrStr[i]);
		}
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.directory(new File(strHomeDir));
		Process p = builder.start();
		BufferedReader bri = new BufferedReader
		        (new InputStreamReader(p.getInputStream()));
		String line;
		while ((line = bri.readLine()) != null) {
			keyOut.set(1);
			valueOut.set(line);
			context.write(keyOut, valueOut);
		}
		p.waitFor();
	}
	
}
