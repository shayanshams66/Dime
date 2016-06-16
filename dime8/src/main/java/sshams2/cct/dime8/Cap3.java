package sshams2.cct.dime8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Cap3 {

	public static String strStartContig = "1	*******************";
	public static String strBeginContig = "Contig ";
	public static int countBeginContig = 7;
	public static String strEndContig = " ******";
	public static String strBeginSeqContig = "1	consensus";
	public static String strStartSequencePart = "1	DETAILED";
	
	private static int idxReadPrefix = 2;//1;
	private static String strReadPostfix = "";//".1";
	
	public static void main(String[] args){
		String strSrsFolder = "G:\\Research\\Results\\CloudAssembly\\Cloud\\SRR072232\\ResultsCap3";
		String strDstFolder = "G:\\Research\\Results\\CloudAssembly\\Cloud\\SRR072232\\ResultsCap3Hadoop\\";
		
		strSrsFolder = args[0];
		strDstFolder = args[1];
		
		Cap3 data = new Cap3();
		int numClus = data.run(strSrsFolder, strDstFolder);
		System.out.println(numClus);
	}
	
	public int run(String _folder, String _folderResult){
		File dir = new File(_folder);
		File file[] = dir.listFiles();
		int idClus = 0;
		for (int i = 0; i < file.length; i++) {
			//System.out.println("Reading file "+ file[i].getName());
			if (!file[i].isDirectory()){
				Cluster cluster = readResults(file[i], 
						strStartContig, 
						strBeginContig, 
						strEndContig, 
						strBeginSeqContig, 
						strStartSequencePart);
				idClus = writeCluster(_folderResult+file[i].getName(),
						cluster, idClus);
			}
		}
		return idClus;
	}
	
	
	public Cluster readResults(File _fileName,
			String _strStartContig,//*******************
			String _strBeginContig,//Contig 
			String _strEndContig,// ******
			String _strBeginSeqContig, //consensus
			String _strStartSequencePart){//c
		Cluster cluster = new Cluster();
		try {
			BufferedReader br = new BufferedReader(new FileReader(_fileName));
			String line = null;
			int idClus = 0;
			int idRead = 0;
			int numReads = 0;
			while((line = br.readLine())!=null){
				if(line.startsWith(_strStartSequencePart)){
					break;
				}
				if(line.startsWith(_strStartContig)){
					if(idClus>0){
						cluster.getContig().setSizeArrRead(numReads);
					}
					numReads = 0;
					idClus++;
					if(idClus==Integer.parseInt(line.substring(line.indexOf(_strBeginContig)+countBeginContig,
							line.indexOf(_strEndContig)))){
						Contig contig = new Contig(idClus);
						cluster.addContig(contig);
					}else{
						System.out.println("error");
					}
				}else{
					numReads++;
				}
			}
			cluster.getContig().setSizeArrRead(numReads);
			//read size of sequence
			while((line = br.readLine())!=null){
				if(line.startsWith(_strStartContig)){
					idClus = Integer.parseInt(line.substring(line.indexOf(_strBeginContig)+countBeginContig,
							line.indexOf(_strEndContig)));
				}else{
					if(line.startsWith("   "))continue;
					if(line.startsWith(_strBeginSeqContig)){
						cluster.getContig(idClus).addContigLen(lastACGT(line) - firstACGT(line));
						continue;
					}
					if(line.contains("+ ")){
						idRead = Integer.parseInt(line.substring(idxReadPrefix, 
								line.indexOf(strReadPostfix+"+ ")));
						cluster.getContig(idClus).addReadLen(idRead, lastACGT(line) - firstACGT(line));
						continue;
					}
					if(line.contains("- ")){
						idRead = Integer.parseInt(line.substring(idxReadPrefix, 
								line.indexOf(strReadPostfix+"- ")));
						cluster.getContig(idClus).addReadLen(idRead, lastACGT(line) - firstACGT(line));
					}
				}
			}
			br.close();
			//read sequence
			for(int i=0;i<cluster.size();i++){
				cluster.get(i).setSequence();
			}
			br = new BufferedReader(new FileReader(_fileName));
			while((line = br.readLine())!=null){
				if(line.startsWith(_strStartSequencePart))break;
			}
			while((line = br.readLine())!=null){
				if(line.startsWith(_strStartContig)){
					idClus = Integer.parseInt(line.substring(line.indexOf(_strBeginContig)+countBeginContig,
							line.indexOf(_strEndContig)));
				}else{
					if(line.startsWith("   "))continue;
					if(line.startsWith(_strBeginSeqContig)){
						cluster.getContig(idClus).addSeq(line, firstACGT(line), lastACGT(line));
						continue;
					}
					if(line.contains("+ ")){
						idRead = Integer.parseInt(line.substring(idxReadPrefix, 
								line.indexOf(strReadPostfix+"+ ")));
						cluster.getContig(idClus).addReadSeq(idRead, line, firstACGT(line),
								lastACGT(line), cluster.getContig(idClus).getPos());
						continue;
					}
					if(line.contains("- ")){
						idRead = Integer.parseInt(line.substring(idxReadPrefix, 
								line.indexOf(strReadPostfix+"- ")));
						cluster.getContig(idClus).addReadSeq(idRead, line, firstACGT(line),
								lastACGT(line), cluster.getContig(idClus).getPos());
					}
				}
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return cluster;
	}
	
	
	public int writeCluster(String _fileName, Cluster _cluster, int _idClus){
		
		try {
			FileWriter fw = new FileWriter(_fileName);
			StringBuffer sb = new StringBuffer();
			for(int i=0;i<_cluster.size();i++){
				fw.write(_cluster.get(i).toStringHadoop(sb, _idClus));
				fw.write("\n");
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return _idClus + _cluster.size();
		
	}
	
	public static int firstACGT(String _line){
		for(int i=22;i<_line.length();i++){
			switch(_line.charAt(i)){
			case 'A': return i;
			case 'C': return i;
			case 'G': return i;
			case 'T': return i;
			case 'N': return i;
			case '-': return i;
			default: continue;
			}
		}
		return -1;
	}
	
	public static int lastACGT(String _line){
		for(int i=_line.length()-1;i>=0;i--){
			switch(_line.charAt(i)){
			case 'A': return i + 1;
			case 'C': return i + 1;
			case 'G': return i + 1;
			case 'T': return i + 1;
			case 'N': return i + 1;
			case '-': return i + 1;
			default: continue;
			}
		}
		return -1;
	}
	
}
