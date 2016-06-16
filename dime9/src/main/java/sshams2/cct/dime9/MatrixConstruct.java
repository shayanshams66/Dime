package sshams2.cct.dime9;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class MatrixConstruct extends Configured implements Tool{

	public static String strLmerSmall = "LmerSmall";
	public static String strLmerLong = "LmerLong";
	public static String strParaLmer = "Lmer";
	public static String strParaCmax = "Cmax";
	public static String strParaThre2 = "thre2";
	public static String strParaNumReads = "numReads";
	public static String strParaNumMaps = "nMap";
	public static String strParaNumReduce = "nRed";
	public static String strParaPrefix = "Prefix";
	
	public static final int LenCutOff = 1000;
	
	public static void main(String[] args) throws Exception {
		long begin = System.currentTimeMillis();
		int result = ToolRunner.run(new Configuration(), new MatrixConstruct(), args);
		System.out.println((System.currentTimeMillis()-begin)/1000+"s");
		System.exit(result);
	}

	public int run(String[] args) throws Exception {
		
		Configuration confs = this.getConf();
		JobConf conf = new JobConf(confs);
		int numMapTasks = 4;
		int numReduceTasks = 4;
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		
		Path inputPath = new Path(otherArgs[0]);
		Path outputPath = new Path(otherArgs[1]);
		for(int i=2;i<otherArgs.length;i+=2){
			if(otherArgs[i].compareTo(strParaLmer)==0){
				conf.set(strParaLmer, otherArgs[i+1]);
				continue;
			}
			if(otherArgs[i].compareTo(strParaNumReads)==0){
				conf.set(strParaNumReads, otherArgs[i+1]);
				continue;
			}
			if(otherArgs[i].compareTo(strParaCmax)==0){
				conf.set(strParaCmax, otherArgs[i+1]);
				continue;
			}
			if(otherArgs[i].compareTo(strParaThre2)==0){
				conf.set(strParaThre2, otherArgs[i+1]);
				continue;
			}
			if(otherArgs[i].compareTo(strParaNumMaps)==0){
				numMapTasks = Integer.parseInt(otherArgs[i+1]);
				continue;
			}
			if(otherArgs[i].compareTo(strParaNumReduce)==0){
				numReduceTasks = Integer.parseInt(otherArgs[i+1]);
				continue;
			}
			if(otherArgs[i].compareTo(strParaPrefix)==0){
				conf.set(strParaPrefix, otherArgs[i+1]);
				continue;
			}
		}
		conf.setNumMapTasks(numMapTasks);
		conf.setNumReduceTasks(numReduceTasks);
		
		//Task 1
		Job job = new Job(conf, "Task 1-build hash table");
		job.setJarByClass(MatrixConstruct.class);
		job.setMapperClass(Task1Map.class);
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(Text.class);
		//job.setCombinerClass(Task1Com.class);
		//job.setNumReduceTasks(0);
		job.setReducerClass(Task1Red.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, inputPath);
		Path outputTask1 = new Path(inputPath.toString()+"Task1");
		//Path outputTask1temp = new Path(inputPath.toString()+"temp");
		FileSystem fs = FileSystem.get(getConf());
		if(fs.exists(outputTask1))
		{
			fs.delete(outputTask1, true);
		}
		//FileOutputFormat.setOutputPath(job, outputTask1);
		FileOutputFormat.setOutputPath(job, outputTask1);
		MultipleOutputs.addNamedOutput(job, strLmerSmall, TextOutputFormat.class, IntWritable.class, Text.class);
		MultipleOutputs.addNamedOutput(job, strLmerLong, TextOutputFormat.class, IntWritable.class, Text.class);
		if (!job.waitForCompletion(true))
			return -1;
		FileStatus[] files = fs.listStatus(outputTask1);
		Path pathLongHashTable = new Path(inputPath.toString()+"Task1"+strLmerLong);
		if(fs.exists(pathLongHashTable))
		{
			fs.delete(pathLongHashTable, true);
		}
		fs.mkdirs(pathLongHashTable);
		for(FileStatus x : files){
			if(x.getPath().toString().contains(strLmerLong)){
				String xStr = x.getPath().toString();
				xStr = xStr.substring(xStr.lastIndexOf("/"), xStr.length());
				xStr = pathLongHashTable.toString()+xStr;
				fs.rename(x.getPath(), new Path(xStr));
			}
		}
		for(FileStatus x : files){
			if(!x.getPath().toString().contains(strLmerSmall))
				fs.delete(x.getPath(), true);
		}
		
		//Task 2
		job = new Job(conf, "Task 2-spike");
		files = fs.listStatus(pathLongHashTable);
		for(FileStatus x : files){
			Path pathShare = x.getPath();
			DistributedCache.addCacheFile(pathShare.toUri(), job.getConfiguration());
		}
		job.setJarByClass(MatrixConstruct.class);
		job.setMapperClass(Task2Map.class);
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(Text.class);
		//job.setCombinerClass(Task2Com.class);
		job.setReducerClass(Task2Red.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, outputTask1);
		Path outputTask2 = new Path(inputPath.toString()+"Task2");
		if(fs.exists(outputTask2))
		{
			fs.delete(outputTask2, true);
		}
		FileOutputFormat.setOutputPath(job, outputTask2);
		if (!job.waitForCompletion(true))
			return -1;
		files = fs.listStatus(outputTask2);
		for(FileStatus x : files){
			if(!x.getPath().toString().contains("part"))
				fs.delete(x.getPath(), true);
		}
		/*Path outputTask2 = new Path(inputPath.toString()+"Task2");
		//Task 3
		
		Job*/ job = new Job(conf, "Task 3-alignment");
		/*FileSystem fs = FileSystem.get(getConf());
		FileStatus[]*/ files = fs.listStatus(inputPath);
		for(FileStatus x : files){
			Path pathShare = x.getPath();
			DistributedCache.addCacheFile(pathShare.toUri(), job.getConfiguration());
			//System.out.println("True");
		}
		job.setJarByClass(MatrixConstruct.class);
		job.setMapperClass(Task3Map.class);
		//job.setNumReduceTasks(0);
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(Text.class);
		job.setReducerClass(Task3Red.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, outputTask2);
		//FileInputFormat.addInputPath(job, inputPath);
		if(fs.exists(outputPath))
		{
			fs.delete(outputPath, true);
		}
		FileOutputFormat.setOutputPath(job, outputPath);
		if (!job.waitForCompletion(true))
			return -1;
		files = fs.listStatus(outputPath);
		for(FileStatus x : files){
			if(!x.getPath().toString().contains("part"))
				fs.delete(x.getPath(), true);
		}
		return 0;
	}
	
}
