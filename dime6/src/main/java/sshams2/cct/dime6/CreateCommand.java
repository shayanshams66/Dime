package sshams2.cct.dime6;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CreateCommand {

	public static void main(String[] args){
		String strNameProgram = args[0];
		String strSrsFolder = args[1];
		String strDstFolder = args[2];
		int numMapper = Integer.parseInt(args[3]);
		int numIterator = 0;
		if(args.length>4){
			numIterator = Integer.parseInt(args[4]);
		}
		ArrayList<String> arrNames = new ArrayList<String>();
		File folder = new File(strSrsFolder);
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			 if(listOfFiles[i].getName().contains("part")){
				 arrNames.add(listOfFiles[i].getName());
			 }
		}
		int numComsPerSplit = (int) Math.floor(((double)arrNames.size())/((double)numMapper));
		int numRemaider = arrNames.size()%numMapper;
		System.out.println(numComsPerSplit);
		int idx = 0;
		for(int i=0;i<arrNames.size();){
			try {
				FileWriter fw = new FileWriter(strDstFolder+"/Split"+idx+".txt");
				for(int j=0;j<numComsPerSplit&&i<arrNames.size();j++){
					fw.write("./"+strNameProgram+" "+arrNames.get(i));
					if(strNameProgram.compareTo("cap3")==0){
						fw.write(" >"+arrNames.get(i)+".out");
					}
					i++;
					if(numIterator>0){
						fw.write(" "+numIterator);
					}
					fw.write("\n");
				}
				if(i<arrNames.size()&&numRemaider>0){
					fw.write("./"+strNameProgram+" "+arrNames.get(i));
					if(strNameProgram.compareTo("cap3")==0){
						fw.write(" >"+arrNames.get(i)+".out");
					}
					if(numIterator>0){
						fw.write(" "+numIterator);
					}
					i++;
					fw.write("\n");
					numRemaider--;
				}
				fw.flush();
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			idx++;
		}
	}
	
}
