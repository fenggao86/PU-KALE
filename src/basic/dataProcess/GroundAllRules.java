package basic.dataProcess;

import java.io.*;
import java.util.*;

import basic.util.StringSplitter;
import com.sun.org.apache.xpath.internal.operations.Bool;
import kale.joint.EmbeddingSpace;
import kale.struct.TripleSet;

public class GroundAllRules {
	private String relationPath;
	private String rulePath;
	public HashMap<String, Integer> MapRelationID = null;
	public HashMap<Integer, ArrayList<Integer>> LstRuleTypeI = null;
	public HashMap<Integer, ArrayList<Integer>> LstRuleTypeII = null;
	public HashMap<String, ArrayList<Integer>> LstRuleTypeIII = null;
	public HashMap<Integer, ArrayList<String>> LstTrainingTriples = null;
	public HashMap<Integer, HashMap<Integer,HashMap<Integer,Boolean>>> LstInferredTriples = null;
	public HashMap<String, Boolean> TrainingTriples_list= null;
	private EmbeddingSpace space;

	/**
	 *
	 * @param p1 relation.txt的路径
	 * @param p2 规则文件的路径
	 */
	public GroundAllRules(String p1, String p2) {
		this.relationPath = p1;
		this.rulePath = p2;
	}
	/**
	 *
	 * @param fnRelationIDMap relation.txt文件路径
	 * @param fnRules  规则文件路径
	 * @param fnTrainingTriples //训练集
	 * @param fnOutput //groundings.txt输出路径
	 * @throws Exception
	 */
	public void GroundRuleGeneration(
			String fnRelationIDMap,
			String fnRules,
			String fnTrainingTriples,
			String fnOutput) throws Exception {
		/**
		 * readData做的事情：
		 * 1. 读取relation.txt的内容到内存，即：MapRelationID
		 * 2. 读取规则文件（三种类型规则）到内存中，即：LstRuleTypeI， LstRuleTypeII， LstRuleTypeIII = map<rel1_rel2, list<rel3>>
		 * 3. 读取训练集，初始化：LstTrainingTriples = map<line, true>, TrainingTriples_list = map<relation, list[head_tail]>
		 * 4. 读取训练集，初始化：LstInferredTriples = map<relation, map<subject, map<object, true>>>
		 */
		//readData(fnRelationIDMap, fnRules, fnTrainingTriples);

		/**
		 * 根据规则生程推理triple，输出到文件grounding.txt中
		 */
		//groundRule(fnOutput);
	}

	public void initialize() throws Exception{
		MapRelationID = readRelationMappingFile(relationPath); //relation.txt
		readRuleFromFile(rulePath); //三种类型规则
	}

	/**
	 * 读取relation.txt，返回Map<String, Integer>
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public HashMap<String, Integer> readRelationMappingFile(String path) throws Exception {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(path), "UTF-8")); //从文件读取relation到id的映射到内存中
		String line = "";
		while ((line = reader.readLine()) != null) {
//			System.out.println(line);
			String[] tokens = line.split("\t");
			int iRelationID = Integer.parseInt(tokens[0]);
			String strRelation = tokens[1];
//			System.out.println(iRelationID+strRelation);
			map.put(strRelation, iRelationID);
		}
		reader.close();
		return map;
	}

	public void readRuleFromFile(String ruleFilePath) throws Exception{
		System.out.println("Start to load rules......");

		LstRuleTypeI = new HashMap<Integer, ArrayList<Integer>>();
		LstRuleTypeII = new HashMap<Integer, ArrayList<Integer>>();
		LstRuleTypeIII = new HashMap<String, ArrayList<Integer>>();
		String line = "";
		if (!ruleFilePath.equals("")) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(ruleFilePath), "UTF-8"));
			int count =0;
			while ((line = reader.readLine()) != null) {
				if (line.split("&&").length == 1 && line.endsWith("(x,y)")){
					String[] tokens = StringSplitter.RemoveEmptyEntries(StringSplitter
							.split("=> ", line));
					int iFstRelation = MapRelationID.get(tokens[0]);
					int iSndRelation = MapRelationID.get(tokens[1]);
					if(!LstRuleTypeI.containsKey(iFstRelation)){
						ArrayList<Integer> lstSnd=new ArrayList<Integer>();
						lstSnd.add(iSndRelation);
						LstRuleTypeI.put(iFstRelation, lstSnd);
					}
					else{
						LstRuleTypeI.get(iFstRelation).add(iSndRelation);
					}
				}
				else if(line.split("&&").length == 1 && line.endsWith("(y,x)")){
					String[] tokens = StringSplitter.RemoveEmptyEntries(StringSplitter
							.split("=> ", line));
					int iFstRelation = MapRelationID.get(tokens[0]);
					int iSndRelation = MapRelationID.get(tokens[1]);
					if(!LstRuleTypeII.containsKey(iFstRelation)){
						ArrayList<Integer> lstSnd=new ArrayList<Integer>();
						lstSnd.add(iSndRelation);
						LstRuleTypeII.put(iFstRelation, lstSnd);
					}
					else{
						LstRuleTypeII.get(iFstRelation).add(iSndRelation);
					}
				}
				else{
					String[] tokens = StringSplitter.RemoveEmptyEntries(StringSplitter
							.split("=>& ", line));
					int iFstRelation = MapRelationID.get(tokens[0]); //第一个
					int iSndRelation = MapRelationID.get(tokens[1]); //第二个
					int iTrdRelation = MapRelationID.get(tokens[2]); //第三个
					String  sFstCompon = iFstRelation+"&&"+iSndRelation; // 第一个 && 第二个 作为map的key
					if(!LstRuleTypeIII.containsKey(sFstCompon)){
						ArrayList<Integer> lstSnd=new ArrayList<Integer>();
						lstSnd.add(iTrdRelation); //第三个 放到list中
						LstRuleTypeIII.put(sFstCompon, lstSnd);   //map<rel1_rel2, list<rel3>>
					}
					else{
						LstRuleTypeIII.get(sFstCompon).add(iTrdRelation);
					}
				}


			}
			reader.close();
		}
	}

	public Set<String> getInferredTriplesAccordingRule(EmbeddingSpace space) throws Exception{
		space.initialize();
		LstTrainingTriples = space.LstTrainingTriples;
		LstInferredTriples = space.LstInferredTriples;
		TrainingTriples_list = space.TrainingTriples_list;
		return groundRule(space);
	}

	
	public Set<String> groundRule(EmbeddingSpace space) throws Exception {
		System.out.println("Start to propositionalize rules......");
		/*BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fnOutput), "UTF-8"));*/
		
		HashMap<String, Boolean> tmpLst = new HashMap<String, Boolean>();
		HashSet<Integer> relationSet = space.getRelationSet();
		HashSet<String> result = new HashSet<>();
		
		int count=0;
		
		Iterator<Integer> lstRelations = LstRuleTypeI.keySet().iterator();
		while (lstRelations.hasNext()) {
			int iFstRelation = lstRelations.next(); //第一个relation
			ArrayList<Integer> lstSndRelation = LstRuleTypeI.get(iFstRelation); //第二个relation集合

			List<String> list = LstTrainingTriples.get(iFstRelation);
			if (list != null) {
				int iSize = list.size();
				for (int iIndex = 0; iIndex < iSize; iIndex++) {
					String strValue = list.get(iIndex);
//				System.out.println(strValue);
					int iSubjectID = Integer.parseInt(strValue.split("_")[0]);
					int iObjectID = Integer.parseInt(strValue.split("_")[1]);
					for(int iSndRelation: lstSndRelation){
						//把新增的relation添加到space的relationset中
						relationSet.add(iSndRelation);
						String strKey = "(" + iSubjectID + "\t" + iFstRelation + "\t" + iObjectID + ")\t"
								+ "(" + iSubjectID + "\t" + iSndRelation + "\t" + iObjectID + ")";
						String strCons = iSubjectID + "\t" + iSndRelation + "\t" + iObjectID;
						if (!tmpLst.containsKey(strKey)) {
							//writer.write(strKey + "\n");
							tmpLst.put(strKey, true);
							result.add(strKey);
						}
					}
				}
				for(int iSndRelation: lstSndRelation){
					ArrayList<String> list2 = LstTrainingTriples.get(iSndRelation);
					if (list2 != null) {
						iSize = list2.size();
						for (int iIndex = 0; iIndex < iSize; iIndex++) {
							String strValue = LstTrainingTriples.get(iSndRelation).get(iIndex);
							int iSubjectID = Integer.parseInt(strValue.split("_")[0]);
							int iObjectID = Integer.parseInt(strValue.split("_")[1]);
							String strKey = "(" + iSubjectID + "\t" + iFstRelation + "\t" + iObjectID + ")\t"
									+ "(" + iSubjectID + "\t" + iSndRelation + "\t" + iObjectID + ")";
							String strAnte = iSubjectID + "\t" + iFstRelation + "\t" + iObjectID;
							if (!tmpLst.containsKey(strKey)&&!TrainingTriples_list.containsKey(strAnte)) {
								//writer.write(strKey + "\n");
								tmpLst.put(strKey, true);
								result.add(strKey);
							}
						}
					}
				}
			}
			//writer.flush();
		}
		
		lstRelations = LstRuleTypeII.keySet().iterator();
		while (lstRelations.hasNext()) {
			int iFstRelation = lstRelations.next();
			ArrayList<Integer> lstSndRelation = LstRuleTypeII.get(iFstRelation);
//			Integer iSndRelation = LstRuleTypeII.get(iFstRelation);
			List<String> list = LstTrainingTriples.get(iFstRelation);
			if (list != null) {
				int iSize = list.size();
				for (int iIndex = 0; iIndex < iSize; iIndex++) {
					String strValue = LstTrainingTriples.get(iFstRelation).get(iIndex);
					int iSubjectID = Integer.parseInt(strValue.split("_")[0]);
					int iObjectID = Integer.parseInt(strValue.split("_")[1]);
					for(int iSndRelation: lstSndRelation){
						relationSet.add(iSndRelation);
						String strKey = "(" + iSubjectID + "\t" + iFstRelation + "\t" + iObjectID + ")\t"
								+ "(" + iObjectID + "\t" + iSndRelation + "\t" + iSubjectID + ")";
						String strCons = iObjectID + "\t" + iSndRelation + "\t" + iSubjectID;
						if (!tmpLst.containsKey(strKey)) {
							//writer.write(strKey + "\n");
							tmpLst.put(strKey, true);
							result.add(strKey);
						}

					}
				}
				for(int iSndRelation: lstSndRelation){
					ArrayList<String> list2 = LstTrainingTriples.get(iSndRelation);
					if (list2 != null) {
						iSize = list2.size();
						for (int iIndex = 0; iIndex < iSize; iIndex++) {
							String strValue = LstTrainingTriples.get(iSndRelation).get(iIndex);
							int iSubjectID = Integer.parseInt(strValue.split("_")[1]);
							int iObjectID = Integer.parseInt(strValue.split("_")[0]);
							String strKey = "(" + iSubjectID + "\t" + iFstRelation + "\t" + iObjectID + ")\t"
									+ "(" + iObjectID + "\t" + iSndRelation + "\t" + iSubjectID + ")";
							String strAnte = iSubjectID + "\t" + iFstRelation + "\t" + iObjectID;
							if (!tmpLst.containsKey(strKey)&&!TrainingTriples_list.containsKey(strAnte)) {
								//writer.write( strKey + "\n");
								tmpLst.put(strKey, true);
								result.add(strKey);
							}
						}
					}
				}
			}
		//writer.flush();
		}
		tmpLst.clear();
		
		Iterator<String> lstFstCompon = LstRuleTypeIII.keySet().iterator();
		while (lstFstCompon.hasNext()) {
			String sFstCompon = lstFstCompon.next();
			ArrayList<Integer> lstTrdRelation = LstRuleTypeIII.get(sFstCompon); //第三个relation组成的list
			if (lstTrdRelation != null) {
				int iFstRelation = Integer.parseInt(sFstCompon.split("&&")[0]); //第一个relation
				int iSndRelation = Integer.parseInt(sFstCompon.split("&&")[1]); //第二个relation
				HashMap<Integer,HashMap<Integer,Boolean>> mapFstRel = LstInferredTriples.get(iFstRelation); // map<subject, map<object, boolean>>, 即：rel1对应的头尾实体集合
				HashMap<Integer,HashMap<Integer,Boolean>> mapSndRel = LstInferredTriples.get(iSndRelation); // rel2对应的头尾实体集合
				Iterator<Integer> lstSubjectID = mapFstRel.keySet().iterator();
				while (lstSubjectID.hasNext()) {
					int iSubjectID = lstSubjectID.next();  //读取一个subject
					ArrayList<Integer> lstMedianID = new ArrayList<Integer>(mapFstRel.get(iSubjectID).keySet()); //subject对应的object集合
					int iFstSize = lstMedianID.size(); //subject对应的所有object的size
					for (int iFstIndex = 0; iFstIndex < iFstSize; iFstIndex++) { //遍历
						int iMedianID = lstMedianID.get(iFstIndex); // 读取一个object
						if(mapSndRel.containsKey(iMedianID)){ //如果rel2对应的subject中包含rel1对应的这个object
							ArrayList<Integer> lstObjectID = new ArrayList<Integer>(mapSndRel.get(iMedianID).keySet()); //获取rel2中头实体为object对应的所有尾实体集合List<object2>
							int iSize2 = lstObjectID.size();
							for (int iSndIndex = 0; iSndIndex < iSize2; iSndIndex++){ //遍历List<object2>
								int iObjectID = lstObjectID.get(iSndIndex); //获取rel2对应的一个object2
								for(int iTrdRelation: lstTrdRelation){ //遍历第三个relation(即：rel3）的集合
									String infer=iSubjectID + "\t" + iTrdRelation + "\t" + iObjectID;  //推理产生的新triple
									String strKey = "(" + iSubjectID + "\t" + iFstRelation + "\t" + iMedianID + ")\t"
											+"(" + iMedianID + "\t" + iSndRelation + "\t" + iObjectID + ")\t"
											+ "(" + iSubjectID + "\t" + iTrdRelation + "\t" + iObjectID + ")";
									relationSet.add(iFstRelation);
									relationSet.add(iSndRelation);
									relationSet.add(iTrdRelation);
									if (!tmpLst.containsKey(strKey)) {
										//writer.write(strKey + "\n");
										tmpLst.put(strKey, true);
										result.add(strKey);
									}
								}
							}
						}
					}
				}

				for(int iTrdRelation: lstTrdRelation){ //遍历第3个relation
					HashMap<Integer,HashMap<Integer,Boolean>> mapTrdRel = LstInferredTriples.get(iTrdRelation); //获取relation对应的头尾实体集合 map<subject, map<object, true>>
					lstSubjectID = mapTrdRel.keySet().iterator();
					while (lstSubjectID.hasNext()) { //遍历subject
						int iSubjectID = lstSubjectID.next(); //获取一个subject
						ArrayList<Integer> lstObjectID = new ArrayList<Integer>(mapTrdRel.get(iSubjectID).keySet()); //获取subjecct对应的所有object
						int iTrdSize = lstObjectID.size(); //subject对应的object的个数
						for (int iTrdIndex = 0; iTrdIndex < iTrdSize; iTrdIndex++) { //遍历object
							int iObjectID = lstObjectID.get(iTrdIndex); // 获取一个object
							if(mapFstRel.containsKey(iSubjectID)){ //如果rel1的头实体包含当前的这个subject
								ArrayList<Integer> lstMedianID = new ArrayList<Integer>(mapFstRel.get(iSubjectID).keySet()); //获取rel1中这个头实体对应的尾实体集合List<object1>
								int iFstSize = lstMedianID.size(); //尾实体个数
								for (int iFstIndex = 0; iFstIndex < iFstSize; iFstIndex++) { //遍历
									int iMedianID = lstMedianID.get(iFstIndex); //获取一个object1
									String infer=iMedianID + "\t" + iSndRelation + "\t" + iObjectID;
									String strKey = "(" + iSubjectID + "\t" + iFstRelation + "\t" + iMedianID + ")\t"
											+"(" + iMedianID + "\t" + iSndRelation + "\t" + iObjectID + ")\t"
											+ "(" + iSubjectID + "\t" + iTrdRelation + "\t" + iObjectID + ")";
									if (!tmpLst.containsKey(strKey)&&!TrainingTriples_list.containsKey(infer)) {
										//writer.write(strKey + "\n");
										tmpLst.put(strKey, true);
										result.add(strKey);
									}
								}
							}


							Iterator<Integer> iterMedianID = mapSndRel.keySet().iterator(); //rel2 对应的头实体集合
							while (iterMedianID.hasNext()) { //遍历
								int iMedianID = iterMedianID.next(); //获取rel2的一个头实体
								if(mapSndRel.get(iMedianID).containsKey(iObjectID)){ //如果它的尾实体的集合包含rel3的object
									String infer=iSubjectID + "\t" + iFstRelation + "\t" + iMedianID;
									String strKey = "(" + iSubjectID + "\t" + iFstRelation + "\t" + iMedianID + ")\t"
											+"(" + iMedianID + "\t" + iSndRelation + "\t" + iObjectID + ")\t"
											+ "(" + iSubjectID + "\t" + iTrdRelation + "\t" + iObjectID + ")";
									if (!tmpLst.containsKey(strKey)&&!TrainingTriples_list.containsKey(infer)) {
										//writer.write( strKey + "\n");
										tmpLst.put(strKey, true);
										result.add(strKey);
									}
								}
							}
						}
					}
				}
			}

			//writer.flush();
		}
		//writer.close();
		System.out.println("Success!");
		return result;
	}
	
	public static void main(String[] args) throws Exception {
		int iEntities = 14951;
		// Input file:
		/*String fnRelationIDMap = "datasets\\wn18\\relationid.txt";
		String fnRules = "datasets\\wn18\\wn18_rule";
		String fnTrainingTriples = "datasets\\wn18\\train.txt";
		//Output file:
        String fnOutput = "datasets\\wn18\\groundings.txt";*/

		//Linux os
		/*String fnRelationIDMap = "datasets/wn18/relationid.txt";
		String fnRules = "datasets/wn18/wn18_rule";
		String fnTrainingTriples = "datasets/wn18/train.txt";
		//Output file:
		String fnOutput = "datasets/wn18/groundings.txt";
        long startTime = System.currentTimeMillis();
        GroundAllRules generator = new GroundAllRules();
        generator.GroundRuleGeneration(fnRelationIDMap,
        		fnRules, fnTrainingTriples, fnOutput);
        long endTime = System.currentTimeMillis();
		System.out.println("All running time:" + (endTime-startTime)+"ms");*/
	}
}
