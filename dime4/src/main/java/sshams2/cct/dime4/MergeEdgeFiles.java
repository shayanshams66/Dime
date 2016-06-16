package sshams2.cct.dime4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MergeEdgeFiles {

	public static void main(String[] args){
		boolean bl = false;
		if(bl){
			runVerify();
			return;
		}
		String folder = "G:\\Research\\Results\\CloudAssembly\\Cloud\\HCH\\EdgesDump";
		String result = "G:\\Research\\Results\\CloudAssembly\\Cloud\\HCH\\EdgesDumpSorted\\";
		int num = 30;//number of reads
		if(args.length==3){
			folder = args[0];
			result = args[1];
			num = Integer.parseInt(args[2]);
		}else{
			System.out.println(folder);
			System.out.println(result);
			System.out.println("number of reads: ");
			return;
		}
		runMerge(folder, result, num);
		
	}
	
	public static void runVerify(){
		String folder = "G:\\Research\\Results\\CloudAssembly\\Cloud\\SRR001069\\01_02_EdgesDump";
		String result = "G:\\Research\\Results\\CloudAssembly\\Cloud\\SRR001069\\01_03_EdgesDumpSorted";
		int numReads = 50211;
		boolean[] arrOri = new boolean[numReads+1];
		boolean[] arrMerge = new boolean[numReads+1];
		int id = 0;
		File dir = new File(folder);
		File file[] = dir.listFiles();
		
		try {
			for(int i=0;i<file.length;i++){
				BufferedReader br = new BufferedReader(new FileReader(file[i]));
				String inputLine = null;
				while((inputLine = br.readLine())!=null){
					id = Integer.parseInt(inputLine.substring(0, inputLine.indexOf("\t")));
					if(arrOri[id]){
						System.out.println("Original duplicated: "+id);
					}else{
						arrOri[id] = true;
					}
					if(id%10000==0)System.out.println(id);
				}
				br.close();
			}
			System.out.println("---------------------------------------------------");
			dir = new File(result);
			file = dir.listFiles();
			for(int i=0;i<file.length;i++){
				BufferedReader br = new BufferedReader(new FileReader(file[i]));
				String inputLine = null;
				while((inputLine = br.readLine())!=null){
					id = Integer.parseInt(inputLine.substring(0, inputLine.indexOf("\t")));
					if(arrMerge[id]){
						System.out.println("Merge duplicated: "+id);
					}else{
						arrMerge[id] = true;
					}
					if(id%10000==0)System.out.println(id);
				}
				br.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("---------------------------------------------------");
		for(int i=1;i<numReads;i++){
			if(arrOri[i]!=arrMerge[i]){
				System.out.println("Not equal: "+i);
			}
			if(i%10000==0)System.out.println(i);
		}
	}
	
	
	public static void runMerge(String _folder, String _result, int _num){
		String folder = _folder;
		String result = _result;
		File dir = new File(folder);
		File file[] = dir.listFiles();
		int num = file.length;
		int numReads = _num;
		int avg = (int) Math.ceil(((double)numReads)/((double)num));
		int idx = 0;
		int iFile = 0;
		BufferedReader[] arrBr = new BufferedReader[num];
		String[] strs = new String[num];
		int id = 0;
		try {
			for(int i=0;i<num;i++){
				arrBr[i] = new BufferedReader(new FileReader(file[i]));
				//System.out.println(file[i].getName());
			}
			FileWriter fw = new FileWriter(result+"part-r-000"+String.format("%02d", iFile));
			String inputLine = null;
			for(int i = 1;i<=numReads;i++){
				idx++;
				if(strs[i%num]!=null){
					id = Integer.parseInt(strs[i%num].substring(0, strs[i%num].indexOf("\t")));
					if(i==id){
						fw.write(strs[i%num]);
						fw.write("\n");
						strs[i%num] = null;
					}
				}else{
					while((inputLine = arrBr[i%num].readLine())!=null){
						id = Integer.parseInt(inputLine.substring(0, inputLine.indexOf("\t")));
						if(i<id){
							strs[i%num] = inputLine;
							break;
						}
						if(i>id){
							System.out.println("error");
						}
						fw.write(inputLine);
						fw.write("\n");
						break;
					}
				}
				if(i%10000==0)System.out.println(i);
				if(idx>=avg){
					fw.flush();
					fw.close();
					iFile++;
					fw = new FileWriter(result+"part-r-000"+String.format("%02d", iFile));
					idx = 0;
				}
				fw.flush();
			}
			fw.close();
			for(int i=0;i<num;i++){
				arrBr[i].close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
