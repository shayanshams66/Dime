package sshams2.cct.dime7;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class HadoopProcess extends Configured implements Tool{

	public static String strParaDir = "paraDir";
	
	public static void main(String[] args) throws Exception {
		long begin = System.currentTimeMillis();
		int result = ToolRunner.run(new Configuration(), new HadoopProcess(), args);
		System.out.println((System.currentTimeMillis()-begin)/1000+"s");
		System.exit(result);
	}

	public int run(String[] args) throws Exception {
		Configuration confs = this.getConf();
		JobConf conf = new JobConf(confs);
		//int numMapTasks = 4;
		//int numReduceTasks = 4;
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		
		Path pathInput = new Path(otherArgs[0]);// all the commands
		Path pathOutput = new Path(otherArgs[1]);// all the stand output
		
		for(int i=2;i<otherArgs.length;i+=2){
			if(otherArgs[i].compareTo(strParaDir)==0){
				conf.set(strParaDir, otherArgs[i+1]);
				continue;
			}
		}
		
		Job job = new Job(conf, "Task 1-progress");
		FileSystem fs = FileSystem.get(getConf());
		
		job.setJarByClass(HadoopProcess.class);
		job.setMapperClass(TaskMap.class);
		job.setNumReduceTasks(0);
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, pathInput);
		if(fs.exists(pathOutput))
		{
			fs.delete(pathOutput, true);
		}
		FileOutputFormat.setOutputPath(job, pathOutput);
		if (!job.waitForCompletion(true))
			return -1;
		FileStatus[] files = fs.listStatus(pathOutput);
		for(FileStatus x : files){
			if(!x.getPath().toString().contains("part"))
				fs.delete(x.getPath(), true);
		}
		
		return 0;
	}
	
}
