package sshams2.cct.dime110;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;



public class LocalMerge {
	
	private Contig[] arrContig;
	private int[] arrLenContig;
	private int[][] arrRange;
	public static String strStartContig = "1	*******************";
	public static String strBeginContig = "Contig ";
	public static String strEndContig = " ******";
	public static String strBeginSeqContig = "1	consensus";
	public static String strStartSequencePart = "1	DETAILED";
	public static String strReadId = ">";//">r";//need to change for genovo
	public static int readIdSt = 1;//need to change for genovo
	public static int readIdEd = 0;//need to change for genovo
	
	private static int idxReadPrefix = 2;//0;//1;//need to change for cap3
	private static String strReadPostfix = "";//".1";//"";//".1"; for cap3
	
	public static String strComm = ",";
	public static String strTab = "\t";
	
	public static int miniOverlap = 20;
	public static double miniScore = 0.5;
	public static char chrComm = ',';
	public static int SEQLENLIMIT = 500;
	public int[][] matrixClusterRead;
	
	private SortedBestScore sortedBestScore = new SortedBestScore();
	public int[] arrMergeIdx;
	
	public static void main(String[] args){
		LocalMerge obj = new LocalMerge();
		String cap3Results = "G:\\Research\\Results\\CloudAssembly\\Cloud\\SRR072232\\ResultsCap3";
		String hadoopResults = "G:\\Research\\Results\\CloudAssembly\\Cloud\\SRR072232\\MergeInput";
		String resultSeq = "G:\\Research\\Results\\CloudAssembly\\Cloud\\SRR072232\\Cap3Final\\contig02.fna";
		int numContig = 22351;
		
		cap3Results = args[0];
		hadoopResults = args[1];
		resultSeq = args[2];
		numContig = Integer.parseInt(args[3]);
		
		obj.run(cap3Results, hadoopResults, resultSeq, numContig);
	}
	
	public void merge(){
		arrMergeIdx = new int[arrContig.length];
		for(int i=0;i<arrContig.length;i++)arrMergeIdx[i] = i;
		Edge edge;
		sortedBestScore.setSTED(arrContig);
		edge = sortedBestScore.pop();
		int id1 = 0;
		int id2 = 0;
		int st1, ed1, st2, ed2;
		byte[] STED;
		boolean rev1, rev2, revE1, revE2;
		MergeObj merger = new MergeObj();
		while(edge!=null){
			STED = edge.getSTED();
			//make sure the id1 is in left and id2 is in right
			if(STED[0]>=2){
				id1 = edge.ID1(); id2 = edge.ID2();
				st1 = edge.StartId1(); st2 = edge.StartId2();
				ed1 = edge.EndId1(); ed2 = edge.EndId2();
				rev1 = edge.Rev(); rev2 = false; revE1 = rev1; revE2 = rev2;
				rev1 = arrContig[id1].Rev(); rev2 = arrContig[id2].Rev();
			}else{
				id1 = edge.ID2(); id2 = edge.ID1();
				st1 = edge.StartId2(); st2 = edge.StartId1();
				ed1 = edge.EndId2(); ed2 = edge.EndId1();
				rev2 = edge.Rev(); rev1 = false; revE1 = rev1; revE2 = rev2;
				rev1 = arrContig[id1].Rev(); rev2 = arrContig[id2].Rev();
				byte tempSTED = STED[0]; STED[0] = STED[1]; STED[1] = tempSTED;
			}
			//get the correct positions
			while(arrMergeIdx[id1]!=id1){
				//st1 = arrContig[id1].getPos(st1, rev1);
				//ed1 = arrContig[id1].getPos(ed1, rev1);
				//rev1 = arrContig[id1].Rev();
				id1 = arrMergeIdx[id1];
			}
			while(arrMergeIdx[id2]!=id2){
				//st2 = arrContig[id2].getPos(st2, rev2);
				//ed2 = arrContig[id2].getPos(ed2, rev2);
				//rev2 = arrContig[id2].Rev();
				id2 = arrMergeIdx[id2];
			}
			if(id1==id2){
				edge = sortedBestScore.pop();
				continue;
			}
			System.out.println(id1+"\t"+id2);
			if(merger.merge(arrContig[id1], st1, ed1, rev1, revE1, STED[0],
					arrContig[id2], st2, ed2, rev2, revE2, STED[1])){
				sortedBestScore.remove(id1, rev1, STED[0], id2, rev2, STED[1]);
				arrMergeIdx[id2] = id1;
				//System.out.println("merged");
			}else{
				//System.out.println("not merged");
			}
			//System.out.println("tested "+sortedBestScore.count);
			edge = sortedBestScore.pop();
		}
	}
	
	/**
	 * for the cap3
	 * @param _cap3Results
	 * @param _hadoopResults
	 * @param _resultSeq
	 * @param _resultReadId
	 * @param _num
	 */
	public void run(String _cap3Results, String _hadoopResults, String _resultSeq, int _num){
		readContig(_cap3Results, _num);
		readBestScore(_hadoopResults);
		merge();
		this.writeResult(_resultSeq);
	}
	
	/**
	 * for the genovo
	 * @param _clusteringResult
	 * @param _cap3Results
	 * @param _hadoopResults
	 * @param _resultSeq
	 * @param _resultReadId
	 * @param _num
	 */
	public void run(String _clusteringResult, String _cap3Results, String _hadoopResults, String _resultSeq, int _num){
		this.readReadFile(_clusteringResult);
		
		readBestScore(_hadoopResults);
		merge();
		this.writeResult(_resultSeq);
	}
	
	/**
	 * just get the id
	 * @param _cap3Results
	 * @param _resultSeq
	 * @param _resultReadId
	 * @param _num
	 */
	public void run(String _cap3Results,  String _resultSeq, int _num){
		readContig(_cap3Results, _num);
		this.writeResult(_resultSeq);
	}
	
	public void runGenovoReadId(String _clusteringResult, String _cap3Results, String _resultSeq, String _resultReadId, int _num){
		//this.readReadFile(_clusteringResult);
		matrixClusterRead = new int[1][220288];
		for(int i=0;i<matrixClusterRead[0].length;i++){
			matrixClusterRead[0][i] = i+1;
		}
		
		//readBestScore(_hadoopResults);
		//merge();
		this.writeResult(_resultSeq);
	}
	
	public void readBestScore(String _folder){
		File dir = new File(_folder);
		File file[] = dir.listFiles();
		int idxComm = 0;
		int id1 = 0;
		int st = 0;
		Edge edge = new Edge();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < file.length; i++) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(file[i]));
				String line = null;
				while((line=br.readLine())!=null){
					idxComm = line.indexOf(strTab);
					id1 = Integer.parseInt(line.substring(0, idxComm));
					st = idxComm + 1;
					while(st<line.length()){
						st = edge.set(id1, line, st, chrComm, sb);
						/*if(inCluster(id1, id2, arrRange)){
							continue;
						}*/
						if(edge.EndId1()-edge.StartId1()<=miniOverlap||edge.EndId2()-edge.StartId2()<=miniOverlap){
							continue;
						}
						if(edge.sc()<miniScore){
							continue;
						}
						if(!qualifyEdge(edge, arrLenContig)){
							continue;
						}
						Edge edgeNew = new Edge();
						edgeNew.set(edge);
						sortedBestScore.add(edgeNew);
					}
				}
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sortedBestScore.sort();
	}
	
	private boolean qualifyEdge(Edge _edge, int[] _arrLen){
		if(_edge.StartId1()>_edge.StartId2()){
			if(_arrLen[_edge.ID2()] - _edge.EndId2()<_arrLen[_edge.ID1()]-_edge.EndId1()){
				return false;
			}
		}else{
			if(_arrLen[_edge.ID2()] - _edge.EndId2()>_arrLen[_edge.ID1()]-_edge.EndId1()){
				return false;
			}
		}
		return true;
	}
	
	public void readContig(String _folder, int _num){
		arrContig = new Contig[_num+1];
		arrLenContig = new int[_num+1];
		File dir = new File(_folder);
		File file[] = dir.listFiles();
		int idClus = 1;
		arrRange = new int[file.length][2];
		int TotalRead = 0;
		for (int i = 0; i < file.length; i++) {
			System.out.println("Reading file "+ file[i].getName());
			if (!file[i].isDirectory()){
				Cluster cluster = readResults(file[i], 
						strStartContig, 
						strBeginContig, 
						strEndContig, 
						strBeginSeqContig, 
						strStartSequencePart);
				for(int j=0;j<cluster.size();j++){
					arrContig[j+idClus] = cluster.get(j);
					arrContig[j+idClus].secondInfo();
					arrLenContig[j+idClus] = cluster.get(j).getSizeNoEmpty();
				}
				arrRange[i][0] = idClus;
				idClus += cluster.size();
				arrRange[i][1] = idClus;
			}
		}
		for(int i=1;i<arrContig.length;i++){
			TotalRead += arrContig[i].getSizeArrRead();
		}
		System.out.println("Count of Read: "+TotalRead);
	}
	
	private void readReadFile(String _strFolder){
		File dir = new File(_strFolder);
		File file[] = dir.listFiles();
		int j = 0;
		String line;
		int numR = 0;
		int idReadTemp = 0;
		this.matrixClusterRead = new int[file.length][];
		for (int i = 0; i < file.length; i++) {
			for (j = 0; j < file.length; j++) {
				if(file[j].getName().startsWith("part_"+i))
					break;
			}
			numR = 0;
			try {
				BufferedReader br = new BufferedReader(new FileReader(file[j]));
				while((line=br.readLine())!=null){
					if(line.startsWith(strReadId)){
						numR++;
					}
				}
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			matrixClusterRead[i] = new int[numR];
			//get the read id
			numR = 0;
			try {
				BufferedReader br = new BufferedReader(new FileReader(file[j]));
				while((line=br.readLine())!=null){
					if(line.startsWith(strReadId)){
						idReadTemp = Integer.parseInt(line.substring(readIdSt, line.length() - readIdEd));
						matrixClusterRead[i][numR] = idReadTemp;
						numR++;
					}
				}
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private Cluster readResults(File _fileName,
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
					if(idClus==Integer.parseInt(line.substring(line.indexOf(_strBeginContig)+7,
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
					idClus = Integer.parseInt(line.substring(line.indexOf(_strBeginContig)+7,
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
					idClus = Integer.parseInt(line.substring(line.indexOf(_strBeginContig)+7,
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
						//if(idRead==314054)
							//System.out.println("check");
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
	
	public void writeResult(String _str1){
		int id = 1;
		int LEN = 60;
		int countLen = 0;
		int x = 0;
		try {
			FileWriter fw1 = new FileWriter(_str1);//sequence file
			//FileWriter fw2 = new FileWriter(_str2);//read ids sets
			for(int i=1;i<arrContig.length;i++){
				if(arrMergeIdx==null||arrMergeIdx[i]==i){
					if(arrContig[i].getSizeArrRead()==0)continue;
					if(arrContig[i].getSizeNoEmpty()<SEQLENLIMIT)continue;
					fw1.write(">"+id+"\t"+i+"\n");
					countLen = 0;
					for(int j=0;j<arrContig[i].size();j++){
						x = arrContig[i].getSeq(j);
						if(x<5){
							fw1.write(getACGT(x));
							countLen++;
							if(countLen>=LEN){
								fw1.write("\n");
								countLen = 0;
							}
						}
					}
					fw1.write("\n");
					//write the read id
					arrContig[i].secondInfo();
					//fw2.write(Integer.toString(id)+",");
					//fw2.write(Integer.toString(arrContig[i].getFirstRead().getId())+",");
					//fw2.write(Integer.toString(arrContig[i].getLastRead().getId())+",");
					for(int j=0;j<arrContig[i].getSizeArrRead();j++){
						countLen = arrContig[i].getReadId(j);
						if(countLen!=-1){
							//fw2.write(Integer.toString(countLen)+",");
						}
					}
					//fw2.write("\n");
					id++;
				}
			}
			//fw2.flush();
			//fw2.close();
			fw1.flush();
			fw1.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static char getACGT(int _x){
		switch(_x){
		case 0:return 'A';
		case 1:return 'C';
		case 2:return 'G';
		case 3:return 'T';
		case 4:return 'N';
		default:
			return 'N';
		}
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
	
	/**
	 * excluded
	 * @param _line
	 * @return
	 */
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
