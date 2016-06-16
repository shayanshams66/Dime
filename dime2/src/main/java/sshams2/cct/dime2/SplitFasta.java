package sshams2.cct.dime2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * commmand
 * SRR024126.fasta(original fasta file) 110388(total reads) 64(designed number of files) SRR024126(designed folder to store the split files)
 * @author Xuan
 *
 */
public class SplitFasta {

	public static String enter = "\n";
	public static String space = " ";
	
	private static String fileName = "";
	private static int numReads = 1000;
	private static int numSplit = 1000;
	private static String folderName = "";
	private static String resultName = "";
	
	public static void main(String[] args){
		if(args.length == 5){
			fileName = args[0];
			numReads = Integer.parseInt(args[1]);
			numSplit = Integer.parseInt(args[2]);
			folderName = args[3];
			resultName = args[4];
		}else{
			System.out.println("java -jar Step02_SplitFastaForHadoop.jar " +
					"FileName " +
					"NumberOfReads " +
					"NumberOfSplits " +
					"TargetFolderPath " +
					"resultNamePrefix");
			return;
		}
		long beginTime = System.currentTimeMillis();
		int balancedSize = numReads/numSplit;
		int reminder = numReads%numSplit;
		if(reminder>=0)balancedSize++;
		int numInFile = 0;
		int indexFile = 0;
		int indexRead = 1;//start from the 1
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			BufferedWriter out = new BufferedWriter(new FileWriter(new File(folderName+resultName+"_"+indexFile)));
			String inputLine = "";
			StringBuffer fragment = new StringBuffer();
			
			while ((inputLine = br.readLine()) != null) {
				if(inputLine.startsWith(">")){
					if(fragment.length() == 0)continue;
					if(numInFile < balancedSize){
						out.write(Integer.toString(indexRead));
						out.write(space);
						out.write(fragment.toString());
						out.write(enter);
						numInFile++;
						indexRead++;
					}else{
						out.flush();
						out.close();
						//new another file;
						numInFile = 0;
						indexFile++;
						out = new BufferedWriter(new FileWriter(new File(folderName + resultName+"_"+indexFile)));
						out.write(Integer.toString(indexRead));
						out.write(space);
						out.write(fragment.toString());
						out.write(enter);
						numInFile++;
						indexRead++;
					}
					if(indexRead%1000==0)System.out.println("Writing: the "+indexRead+" reads");
					fragment.delete(0, fragment.length());
				}else{
					fragment.append(inputLine);
				}
			}
			if(fragment.length()>0){
				out.write(Integer.toString(indexRead));
				out.write(space);
				out.write(fragment.toString());
				indexRead++;
				out.flush();
				out.close();
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("\nTotal Time:"+(System.currentTimeMillis()-beginTime)/1000+"s.");
	}
}
