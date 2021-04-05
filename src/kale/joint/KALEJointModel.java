package kale.joint;


import java.io.*;
import java.util.*;

import basic.dataProcess.GroundAllRules;
import kale.struct.*;
import kale.util.BaseUtils;
import kale.util.MetricMonitor;
import kale.util.NegativeRuleGeneration;
import kale.util.NegativeTripleGeneration;
import putranse.PutransE;
import sun.java2d.pipe.SpanClipRenderer;
import sun.java2d.pipe.ValidatePipe;

public class KALEJointModel {
	public TripleSet m_TrainingTriples;
	public TripleSet m_ValidateTriples;
	public TripleSet m_TestingTriples;
	public TripleSet m_Triples;
	public RuleSet m_TrainingRules;
	
	//public Matrix m_Entity_Factor_MatrixE;
	public MatrixMap m_Entity_Factor_MatrixE;
	//public Matrix m_Relation_Factor_MatrixR;
	public MatrixMap m_Relation_Factor_MatrixR;
	//public Matrix m_MatrixEGradient;
	public MatrixMap m_MatrixEGradient;
	//public Matrix m_MatrixRGradient;

	public MatrixMap m_MatrixRGradient;

	public int m_NumRelation;
	public int m_NumEntity;
	public String m_MatrixE_prefix = "";
	public String m_MatrixR_prefix = "";
	
	public int m_NumFactor = 20;
	public int m_NumMiniBatch = 100;
	public double[] margin = {0.1,0.2,0.3,0.4};
	public double m_Delta = 0.1;  //margin
	public double learning_rate[] = {0.1, 0.01, 0.001};
	public double m_GammaE = 0.01; //learning rate
	public double m_GammaR = 0.01; //learning rate
	public int m_NumIteration = 1000;
	public int m_OutputIterSkip = 50;
	public double weight[] = {0.1, 0.01};
	public double m_Weight = 0.01;  //weight
	private String baseLogDir;
	private String relationTxtPath;
	private String rulePath;
	private int allEmbeddingspaceNum;
	
	java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("#.######");
	
	public KALEJointModel() {
	}

	/**
	 *
	 * @param strNumRelation 关系个数
	 * @param strNumEntity 实体个数
	 * @param fnTrainingTriples 训练集
	 * @param fnValidateTriples 验证集
	 * @param fnTestingTriples 测试集
	 * @param fnTrainingRules 规则
	 * @throws Exception
	 */
	public void Initialization(String strNumRelation, String strNumEntity,
			String fnTrainingTriples, String fnValidateTriples, String fnTestingTriples,
			String fnTrainingRules, String relationTxtPath, String rulePath) throws Exception {
		m_NumRelation = Integer.parseInt(strNumRelation); //关系个数
		m_NumEntity = Integer.parseInt(strNumEntity); //实体个数
		this.relationTxtPath = relationTxtPath;
		this.rulePath = rulePath;

		m_MatrixE_prefix = "MatrixE-k" + m_NumFactor  //20
				+ "-d" + decimalFormat.format(m_Delta) //0.1
				+ "-ge" + decimalFormat.format(m_GammaE) //0.01
				+ "-gr" + decimalFormat.format(m_GammaR) //0.01
				+ "-w" +  decimalFormat.format(m_Weight); //0.01    矩阵E前缀
		m_MatrixR_prefix = "MatrixR-k" + m_NumFactor
				+ "-d" + decimalFormat.format(m_Delta)
				+ "-ge" + decimalFormat.format(m_GammaE)
				+ "-gr" + decimalFormat.format(m_GammaR)
				+ "-w" +  decimalFormat.format(m_Weight); //矩阵R前缀
		
		System.out.println("\nLoading training and validate triples"); //加载训练和验证集
		m_TrainingTriples = new TripleSet(m_NumEntity, m_NumRelation);
		m_ValidateTriples = new TripleSet(m_NumEntity, m_NumRelation);
		m_Triples = new TripleSet();
		m_TrainingTriples.load(fnTrainingTriples); //读取训练集三元组到内存
		//m_ValidateTriples.classifyTriples(fnTestingTriples);
		m_ValidateTriples.load(fnValidateTriples); //从验证集读取1000个三元组到内存
		m_Triples.loadStr(fnTrainingTriples);
		m_Triples.loadStr(fnValidateTriples);
		m_Triples.loadStr(fnTestingTriples);
		System.out.println("Success.");
		
		/*System.out.println("\nLoading grounding rules");
		*//*m_TrainingRules = new RuleSet(m_NumEntity, m_NumRelation);
		m_TrainingRules.load(fnTrainingRules); //读取grounding.txt中的规则，加载到内存中
		System.out.println("Success.");		*//*
*/
		/*//随机初始化矩阵R 和 矩阵E
		System.out.println("\nRandomly initializing matrix E and matrix R");
		m_Entity_Factor_MatrixE = new Matrix(m_NumEntity, m_NumFactor);
		m_Entity_Factor_MatrixE.setToRandom(); //随机初始化矩阵E
		m_Entity_Factor_MatrixE.normalizeByRow(); //归一化
		m_Relation_Factor_MatrixR = new Matrix(m_NumRelation, m_NumFactor);
		m_Relation_Factor_MatrixR.setToRandom();
		m_Relation_Factor_MatrixR.normalizeByRow();
		System.out.println("Success.");

		//初始化矩阵E和矩阵R的梯度
		System.out.println("\nInitializing gradients of matrix E and matrix R");
		m_MatrixEGradient = new Matrix(m_NumEntity, m_NumFactor);
		m_MatrixRGradient = new Matrix(m_NumRelation, m_NumFactor);
		System.out.println("Success.");*/

		baseLogDir = BaseUtils.initBaseLogPath();
		System.out.println("日志输出基础路径：" + baseLogDir);
	}

	public void puTransE_Learn() throws Exception {
        PutransE putransE = new PutransE(m_TrainingTriples, 2000);
        putransE.initailize(); //初始化
       // List<EmbeddingSpace> embeddingSpaces = putransE.buildTripleSetForEachIter(); //获取所有的嵌入空间
		List<EmbeddingSpace> embeddingSpaces = putransE.buildEmbeddingSpaceDataset();
		allEmbeddingspaceNum = embeddingSpaces.size();
        int count = 1;
		long start = System.currentTimeMillis();
		GroundAllRules groundAllRules = new GroundAllRules(relationTxtPath, rulePath);
		groundAllRules.initialize(); //读取relation.txt和规则文件到内存
		BufferedWriter writerGlobal = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream( baseLogDir + File.separator + "ahelp.log", true), "UTF-8"));
		for (EmbeddingSpace space : embeddingSpaces) {
			space.initialize();
			System.out.println("根据规则获取推理的triple...");
			Set<String> inferedTriples = groundAllRules.getInferredTriplesAccordingRule(space); //其实就是类似groundings.txt中的内容
			RuleSet ruleSet = new RuleSet();
			ruleSet.loadTriples(inferedTriples);
			m_TrainingRules = ruleSet;

			System.out.println("\nRandomly initializing matrix E and matrix R of Embedding " + count);
			//m_Entity_Factor_MatrixE = new Matrix(m_NumEntity, m_NumFactor); //这里的矩阵很多是多余的，可以优化
			m_Entity_Factor_MatrixE = new MatrixMap(space.getEntitySet(), m_NumFactor);
			m_Entity_Factor_MatrixE.setToRandom(); //随机初始化矩阵E
			m_Entity_Factor_MatrixE.normalizeByRow(); //归一化
			m_Relation_Factor_MatrixR = new MatrixMap(space.getRelationSet(), m_NumFactor);
			m_Relation_Factor_MatrixR.setToRandom();
			m_Relation_Factor_MatrixR.normalizeByRow();
			System.out.println("Success.");

			//初始化矩阵E和矩阵R的梯度
			System.out.println("\nInitializing gradients of matrix E and matrix R of Embedding " + count);
			m_MatrixEGradient = new MatrixMap(space.getEntitySet(), m_NumFactor);
			m_MatrixRGradient = new MatrixMap(space.getRelationSet(), m_NumFactor);
			System.out.println("Success.");

			//m_ValidateTriples.loadSelectedTripleForValidOrTest(200, space.getEntitySet(), space.getRelationSet());
			m_ValidateTriples.getTripleInSelectedEntityAndRel(space.getEntitySet(), space.getRelationSet());

			TransE_Learn(count++, space.getTripleSet(), space, writerGlobal);
        }
		writerGlobal.close();
		long end = System.currentTimeMillis();
		System.out.println("全部训练时间：" + (end- start) / (1000 * 3600 * 1.0) + "s");
	}
	
	public void TransE_Learn(int embeddingNum, TripleSet trainingTriples, EmbeddingSpace space, BufferedWriter writerGlobal) throws Exception {
		HashMap<Integer, ArrayList<Triple>> lstPosTriples = new HashMap<Integer, ArrayList<Triple>>(); //正确的三元组
		HashMap<Integer, ArrayList<Triple>> lstHeadNegTriples = new HashMap<Integer, ArrayList<Triple>>(); //头实体错误的三元组
		HashMap<Integer, ArrayList<Triple>> lstTailNegTriples = new HashMap<Integer, ArrayList<Triple>>(); //尾实体错误的三元组
		HashMap<Integer, ArrayList<Rule>> lstRules = new HashMap<Integer, ArrayList<Rule>>(); //规则
		HashMap<Integer, ArrayList<Rule>> lstSndRelNegRules = new HashMap<Integer, ArrayList<Rule>>(); //第二个关系时错误的规则

		String outDir = baseLogDir + File.separator + "embedding-" + embeddingNum;
		File file = new File(outDir);
		if (!file.exists()) {
			file.mkdirs(); //创建日志，矩阵文件输出目录
		}

		String PATHLOG = "result-k" + m_NumFactor 
				+ "-d" + decimalFormat.format(m_Delta)
				+ "-ge" + decimalFormat.format(m_GammaE) 
				+ "-gr" + decimalFormat.format(m_GammaR)
				+ "-w" +  decimalFormat.format(m_Weight) + "-Embedding_ "+ embeddingNum + ".log"; //日志文件名字
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream( outDir+ File.separator + PATHLOG), "UTF-8"));
		
		int iIter = 0; //迭代次数
		writer.write("=============================================================================================");
		writer.write("第" + embeddingNum + "/ " + allEmbeddingspaceNum + "个嵌入空间，参数：margin=" + m_Delta + ", learning_rate=" + m_GammaR + ",weight=" + m_Weight);
		writer.write("=============================================================================================");
		writer.write("Embedding-" + embeddingNum + "/" + allEmbeddingspaceNum + " Complete iteration #" + iIter + ":\n");
		System.out.println("第" + embeddingNum + "/" + allEmbeddingspaceNum + "个嵌入空间，参数：margin=" + m_Delta + ", learning_rate=" + m_GammaR + ",weight=" + m_Weight);
		System.out.println("当前嵌入空间的triple个数为：" + trainingTriples.pTriple.size());
		System.out.println("Complete iteration #" + iIter + ":");
		MetricMonitor first_metrics = new MetricMonitor(
				m_ValidateTriples, //验证集三元组
				m_Triples.tripleSet(), //训练集、验证集、测试集组成的Map<String, boolean>，其中，key是一个triple字符串
				m_Entity_Factor_MatrixE,
				m_Relation_Factor_MatrixR,
				space);
		writerGlobal.write("===================当前嵌入空间：" + embeddingNum);
		int validCount1 = first_metrics.calculateMetrics(); //计算MRR和hit
		double dCurrentHits = first_metrics.dHits; //当前的
		double dCurrentMRR = first_metrics.dMRR;
		writerGlobal.write("初始---Current MRR：" + dCurrentMRR + "\tCurrent Hits@10："+ dCurrentHits + "\t验证集三元组个数" + validCount1 + "\n");
		writer.write("Embedding-" + embeddingNum + "/" + allEmbeddingspaceNum + "------Current MRR:"+ dCurrentMRR + "\tCurrent Hits@10:" + dCurrentHits + "\n");
		System.out.print("\n");
		double dBestHits = first_metrics.dHits;  //最好的
		double dBestMRR = first_metrics.dMRR;
		int iBestIter = 0;
		
		
		long startTime = System.currentTimeMillis();
		while (iIter < m_NumIteration) { // < 1000
			long s1 = System.currentTimeMillis();
			//打乱triple
			trainingTriples.randomShuffle();

			long e1 = System.currentTimeMillis();
			long t1 = (e1 - s1);
			ArrayList<Triple> tmpPosLst = new ArrayList<>(); //正确的
			ArrayList<Triple> tmpHeadNegLst = new ArrayList<>(); //头实体错误的
			ArrayList<Triple> tmpTailNegLst = new ArrayList<>(); //尾实体错误的
			//遍历所有triple，生成错误三元组，加入到Map中c
			for (int iIndex = 0; iIndex < trainingTriples.triples(); iIndex++) {
				Triple PosTriple = trainingTriples.get(iIndex); //获取一个triple
				NegativeTripleGeneration negTripGen = new NegativeTripleGeneration(
						PosTriple,space);
				Triple headNegTriple = negTripGen.generateHeadNegTriple(); //生成一个替换掉了head的triple
				Triple tailNegTriple = negTripGen.generateTailNegTriple(); //生成一个替换掉了tail的triple

				tmpPosLst.add(PosTriple);
				tmpHeadNegLst.add(headNegTriple);
				tmpTailNegLst.add(tailNegTriple);

				//int iID = iIndex % m_NumMiniBatch; //这里iID的范围是0 - 99
				//将posTriple、headNegTriple、tailNegTriple加入到Map<id, List>中
//				if (!lstPosTriples.containsKey(iID)) { //不包含这个id
//					ArrayList<Triple> tmpPosLst = new ArrayList<Triple>(); //正确的
//					ArrayList<Triple> tmpHeadNegLst = new ArrayList<Triple>(); //头实体错误的
//					ArrayList<Triple> tmpTailNegLst = new ArrayList<Triple>(); //尾实体错误的
//					tmpPosLst.add(PosTriple);
//					tmpHeadNegLst.add(headNegTriple);
//					tmpTailNegLst.add(tailNegTriple);
//					lstPosTriples.put(iID, tmpPosLst);
//					lstHeadNegTriples.put(iID, tmpHeadNegLst);
//					lstTailNegTriples.put(iID, tmpTailNegLst);
//				} else { //已经存在这个id
//					lstPosTriples.get(iID).add(PosTriple);
//					lstHeadNegTriples.get(iID).add(headNegTriple);
//					lstTailNegTriples.get(iID).add(tailNegTriple);
//				}
			}
			long e2 = System.currentTimeMillis();
			long t2 = (e2 - e1);

			m_TrainingRules.randomShuffle();
			ArrayList<Rule> tmpLst = new ArrayList<>();
			ArrayList<Rule> tmpsndRelNegLst = new ArrayList<>();
			for (int iIndex = 0; iIndex < m_TrainingRules.rules(); iIndex++) {
				Rule rule = m_TrainingRules.get(iIndex);
				
				NegativeRuleGeneration negRuleGen = new NegativeRuleGeneration(
						rule,  space);
				Rule sndRelNegrule = negRuleGen.generateSndNegRule();	//生成否定规则

				tmpLst.add(rule);
				tmpsndRelNegLst.add(sndRelNegrule);
//				int iID = iIndex % m_NumMiniBatch; //iID在0 - 99之间
//				if (!lstRules.containsKey(iID)) { //map中不包含iID
//					ArrayList<Rule> tmpLst = new ArrayList<Rule>();
//					ArrayList<Rule> tmpsndRelNegLst = new ArrayList<Rule>();
//					tmpLst.add(rule);
//					tmpsndRelNegLst.add(sndRelNegrule);
//					lstRules.put(iID, tmpLst);
//					lstSndRelNegRules.put(iID, tmpsndRelNegLst);
//
//				} else { //map中包含iID
//					lstRules.get(iID).add(rule);
//					lstSndRelNegRules.get(iID).add(sndRelNegrule);
//				}
			}

			long e3 = System.currentTimeMillis();
			long t3 = e3 - e2;

			//double m_BatchSize= m_TrainingTriples.triples()/(double)m_NumMiniBatch;
//			for (int iID = 0; iID < m_NumMiniBatch; iID++) { //对每一个triple（包括positive和negative，使用梯度下降进行调整更新）
//				ArrayList<Triple> triples1 = lstPosTriples.get(iID);
//				ArrayList<Triple> triples2 = lstHeadNegTriples.get(iID);
//				ArrayList<Triple> triples3 = lstTailNegTriples.get(iID);
//				ArrayList<Rule> rules1 = lstRules.get(iID);
//				ArrayList<Rule> rules2 = lstSndRelNegRules.get(iID);
				ArrayList<Triple> triples1 = tmpPosLst;
				ArrayList<Triple> triples2 = tmpHeadNegLst;
				ArrayList<Triple> triples3 = tmpTailNegLst;
				ArrayList<Rule> rules1 = tmpLst;
				ArrayList<Rule> rules2 = tmpsndRelNegLst;
//				if (triples1 != null && triples2 != null && triples3 != null && rules1 != null && rules2 != null) {
					StochasticUpdate stochasticUpdate = new StochasticUpdate(
							triples1,
							triples2,
							triples3,
							rules1,
							rules2,
							m_Entity_Factor_MatrixE,
							m_Relation_Factor_MatrixR,
							m_MatrixEGradient,
							m_MatrixRGradient,
//	###					learning rate
							m_GammaE,
							m_GammaR,
//	###					margin
							m_Delta,
//	###					weight
							m_Weight);
					stochasticUpdate.stochasticIteration();
//				}
//			}
			long e4 = System.currentTimeMillis();
			long t4 = e4 - e3;

			lstPosTriples = new HashMap<Integer, ArrayList<Triple>>();
			lstHeadNegTriples = new HashMap<Integer, ArrayList<Triple>>();
			lstTailNegTriples = new HashMap<Integer, ArrayList<Triple>>();

			lstRules = new HashMap<Integer, ArrayList<Rule>>();
			lstSndRelNegRules = new HashMap<Integer, ArrayList<Rule>>();
			
			iIter++;
			System.out.println("Embedding-" + embeddingNum + "/" + allEmbeddingspaceNum + " Complete iteration #" + iIter + ":" + ",shuffle time :" + t1+"ms,生成否定triple：" +t2+"ms,生成否定规则：" + t3+"ms,梯度下降训练时间：" + t4 + "ms");
			
			if (iIter % m_OutputIterSkip == 0) { //每50次
				writer.write("Complete iteration #" + iIter + ":\n");
				System.out.println("Complete iteration #" + iIter + ":");
				long s2 = System.currentTimeMillis();
				MetricMonitor metric = new MetricMonitor(
						m_ValidateTriples,
						m_Triples.tripleSet(),
						m_Entity_Factor_MatrixE,
						m_Relation_Factor_MatrixR,
						space);
				int validCount2 = metric.calculateMetrics();
				long s3 = System.currentTimeMillis();
				dCurrentHits = metric.dHits;
				dCurrentMRR = metric.dMRR;
				writer.write("Embedding-" + embeddingNum + "/" +allEmbeddingspaceNum + "------Current MRR:"+ dCurrentMRR + "\tCurrent Hits@10:" + dCurrentHits + "\n");
				writerGlobal.write("---Current MRR：" + dCurrentMRR + "\tCurrent Hits@10："+ dCurrentHits + "\t验证集三元组个数" + validCount2 + "\n");
				if (dCurrentMRR > dBestMRR) {
					m_Relation_Factor_MatrixR.output(outDir + File.separator + m_MatrixR_prefix + ".best");
					m_Entity_Factor_MatrixE.output(outDir + File.separator + m_MatrixE_prefix + ".best");
					dBestHits = dCurrentHits;
					dBestMRR = dCurrentMRR;
					iBestIter = iIter;
				}
				writer.write("Embedding-" + embeddingNum + "/" + allEmbeddingspaceNum + "------Best iteration #" + iBestIter + "\t" + dBestMRR + "\t" + dBestHits+"\n");
				writer.flush();
				System.out.println("Embedding-" + embeddingNum + "/" + allEmbeddingspaceNum + "------\tBest iteration #" + iBestIter + "\tBest MRR:" + dBestMRR + "Best \tHits@10:" + dBestHits + "\t time：" + (s3-s2) + "ms");
				writer.flush();
			}
		}
		//space.setEntityMatrix(m_Entity_Factor_MatrixE);  //保存实体和关系矩阵
		//space.setRelationMatrix(m_Relation_Factor_MatrixR);
		long endTime = System.currentTimeMillis();
		System.out.println("Embedding-" + embeddingNum + "/" + allEmbeddingspaceNum + " All running time:" + (endTime-startTime) / (1000 * 60)*1.0 +"min");
		writer.close();
	}
}
