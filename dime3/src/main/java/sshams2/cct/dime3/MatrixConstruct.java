package sshams2.cct.dime3;

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
	public static String strParaNumGroup = "M";
	public static int numM = 5;
	public static String strAligned = "Aligned";
	public static String strUnaligned = "Unaligned";
	
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
		
		Path pthInputRead = new Path(otherArgs[0]);
		Path pthOutputEdges = new Path(otherArgs[1]);
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
			if(otherArgs[i].compareTo(strParaNumGroup)==0){
				numM = Integer.parseInt(otherArgs[i+1]);
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
		FileInputFormat.addInputPath(job, pthInputRead);
		Path pthOutputTask1 = new Path(pthInputRead.toString()+"Task1");
		//Path outputTask1temp = new Path(inputPath.toString()+"temp");
		FileSystem fs = FileSystem.get(getConf());
		if(fs.exists(pthOutputTask1))
		{
			fs.delete(pthOutputTask1, true);
		}
		//FileOutputFormat.setOutputPath(job, outputTask1);
		FileOutputFormat.setOutputPath(job, pthOutputTask1);
		MultipleOutputs.addNamedOutput(job, strLmerSmall, TextOutputFormat.class, IntWritable.class, Text.class);
		MultipleOutputs.addNamedOutput(job, strLmerLong, TextOutputFormat.class, IntWritable.class, Text.class);
		if (!job.waitForCompletion(true))
			return -1;
		FileStatus[] files = fs.listStatus(pthOutputTask1);
		Path pthLongHashTable = new Path(pthInputRead.toString()+"Task1"+strLmerLong);
		if(fs.exists(pthLongHashTable))
		{
			fs.delete(pthLongHashTable, true);
		}
		fs.mkdirs(pthLongHashTable);
		for(FileStatus x : files){
			if(x.getPath().toString().contains(strLmerLong)){
				String xStr = x.getPath().toString();
				xStr = xStr.substring(xStr.lastIndexOf("/"), xStr.length());
				xStr = pthLongHashTable.toString()+xStr;
				fs.rename(x.getPath(), new Path(xStr));
			}
		}
		for(FileStatus x : files){
			if(!x.getPath().toString().contains(strLmerSmall))
				fs.delete(x.getPath(), true);
		}
		
		//Task 2
		job = new Job(conf, "Task 2-spike");
		files = fs.listStatus(pthLongHashTable);
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
		FileInputFormat.addInputPath(job, pthOutputTask1);
		Path pthOutputTask2 = new Path(pthInputRead.toString()+"Task2");
		if(fs.exists(pthOutputTask2))
		{
			fs.delete(pthOutputTask2, true);
		}
		FileOutputFormat.setOutputPath(job, pthOutputTask2);
		if (!job.waitForCompletion(true))
			return -1;
		files = fs.listStatus(pthOutputTask2);
		for(FileStatus x : files){
			if(!x.getPath().toString().contains("part"))
				fs.delete(x.getPath(), true);
		}
		
		//Task 3 with repeat operations
		int idx = 0;
		if(fs.exists(pthOutputEdges))
		{
			files = fs.listStatus(pthOutputEdges);
			for(FileStatus x : files){
				fs.delete(x.getPath(), true);
			}
		}else{
			fs.mkdirs(pthOutputEdges);
		}
		Repeat repeat = new Repeat(fs.listStatus(pthInputRead), numM);
		files = repeat.get();
		while(files!=null){
			job = new Job(conf, "Task 3-alignment");
			for(FileStatus x : files){
				Path pathShare = x.getPath();
				DistributedCache.addCacheFile(pathShare.toUri(), job.getConfiguration());
			}
			job.setJarByClass(MatrixConstruct.class);
			job.setMapperClass(Task3Map.class);
			job.setMapOutputKeyClass(IntWritable.class);
			job.setMapOutputValueClass(Text.class);
			job.setReducerClass(Task3Red.class);
			job.setOutputKeyClass(IntWritable.class);
			job.setOutputValueClass(Text.class);
			FileInputFormat.addInputPath(job, pthOutputTask2);
			Path pthOutputTask3 = new Path(pthInputRead.toString()+"Task3");
			if(fs.exists(pthOutputTask3)){
				fs.delete(pthOutputTask3, true);
			}
			FileOutputFormat.setOutputPath(job, pthOutputTask3);
			MultipleOutputs.addNamedOutput(job, strAligned, TextOutputFormat.class, IntWritable.class, Text.class);
			MultipleOutputs.addNamedOutput(job, strUnaligned, TextOutputFormat.class, IntWritable.class, Text.class);
			if (!job.waitForCompletion(true))
				return -1;
			if(fs.exists(pthOutputTask2)){
				System.out.print("\"Hello line 203\"");
				files = fs.listStatus(pthOutputTask2);
				for(FileStatus x : files){
					fs.delete(x.getPath(), true);
				}
			}
			files = fs.listStatus(pthOutputTask3);
			for(FileStatus x : files){
				if(x.getPath().toString().contains(strAligned)){
					String xStr = x.getPath().toString();
					xStr = xStr.substring(xStr.lastIndexOf("/"), xStr.length());
					xStr = pthOutputEdges.toString()+xStr+"-"+String.format("%06d", idx);
					idx++;
					fs.rename(x.getPath(), new Path(xStr));
				}
				if(x.getPath().toString().contains(strUnaligned)){
					String xStr = x.getPath().toString();
					xStr = xStr.substring(xStr.lastIndexOf("/"), xStr.length());
					xStr = pthOutputTask2.toString()+xStr;
					fs.rename(x.getPath(), new Path(xStr));
				}
			}
			files = repeat.get();
		}
		
		/*Path outputTask2 = new Path(inputPath.toString()+"Task2");
		//Task 3
		
		
		Job*/ //job = new Job(conf, "Task 3-alignment");
		/*FileSystem fs = FileSystem.get(getConf());
		FileStatus[]*/ /*files = fs.listStatus(pthInputRead);
		for(FileStatus x : files){
			Path pathShare = x.getPath();
			DistributedCache.addCacheFile(pathShare.toUri(), job.getConfiguration());
		}
		job.setJarByClass(MatrixConstruct.class);
		job.setMapperClass(Task3Map.class);
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(Text.class);
		job.setReducerClass(Task3Red.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, outputTask2);
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
		}*/
		
		//dump the edges: Task4Map and Task4Reduce, result in multiple files
		conf.setNumMapTasks(numMapTasks);
		conf.setNumReduceTasks(numReduceTasks);
		job = new Job(conf, "Task 4");
		job.setJarByClass(MatrixConstruct.class);
		job.setMapperClass(Task5Map.class);
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(Text.class);
		job.setReducerClass(Task5Red.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, pthOutputEdges);
		Path pthOutputTask4 = new Path("EdgesDump");
		fs = FileSystem.get(getConf());
		if(fs.exists(pthOutputTask4))
		{
			fs.delete(pthOutputTask4, true);
		}
		FileOutputFormat.setOutputPath(job, pthOutputTask4);
		if (!job.waitForCompletion(true))
			return -1;
		files = fs.listStatus(pthOutputTask4);
		for(FileStatus x : files){
			if(!x.getPath().toString().contains("part"))
				fs.delete(x.getPath(), true);
		}
		
		if(fs.exists(pthOutputTask1))
			fs.delete(pthOutputTask1, true);
		if(fs.exists(pthOutputTask2))
			fs.delete(pthOutputTask2, true);
		Path pthOutputTask3 = new Path(pthInputRead.toString()+"Task3");
		if(fs.exists(pthOutputTask3))
			fs.delete(pthOutputTask3, true);
		if(fs.exists(pthLongHashTable))
			fs.delete(pthLongHashTable, true);
		if(fs.exists(pthOutputEdges))
			fs.delete(pthOutputEdges, true);
		return 0;
	}
}
