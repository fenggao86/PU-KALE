package kale.joint;
import java.util.*;

import kale.struct.Matrix;
import kale.struct.MatrixMap;
import kale.struct.Rule;
import kale.struct.Triple;

/**
 * 随机梯度下降更新
 */
public class StochasticUpdate {
	public ArrayList<Triple> lstPosTriples;
	public ArrayList<Triple> lstHeadNegTriples;
	public ArrayList<Triple> lstTailNegTriples;
	public ArrayList<Rule> lstRules;
//	public ArrayList<Rule> lstFstRelNegRules;
	public ArrayList<Rule> lstSndRelNegRules;

	public MatrixMap MatrixE;
	public MatrixMap MatrixR;
	public MatrixMap MatrixEGradient;
	public MatrixMap MatrixRGradient;
	public double dGammaE;
	public double dGammaR;
	public double dDelta;
	public double m_Weight;
	
	public StochasticUpdate(
			ArrayList<Triple> inLstPosTriples,
			ArrayList<Triple> inLstHeadNegTriples,
			ArrayList<Triple> inLstTailNegTriples,
			ArrayList<Rule> inlstRules,
			ArrayList<Rule> inlstSndRelNegRules,
			MatrixMap inMatrixE,
			MatrixMap inMatrixR,
			MatrixMap inMatrixEGradient,
			MatrixMap inMatrixRGradient,
			double inGammaE,
			double inGammaR,
			double inDelta,
			double in_m_Weight) {
		lstPosTriples = inLstPosTriples;
		lstHeadNegTriples = inLstHeadNegTriples;
		lstTailNegTriples = inLstTailNegTriples;
		lstRules = inlstRules;
		lstSndRelNegRules = inlstSndRelNegRules;
		MatrixE = inMatrixE;
		MatrixR = inMatrixR;
		MatrixEGradient = inMatrixEGradient;
		MatrixRGradient = inMatrixRGradient;
		m_Weight = in_m_Weight;  //weight
		dGammaE = inGammaE; //learning rate
		dGammaR = inGammaR; //learning rate
		dDelta = inDelta; //margin
	}
	
	public void stochasticIteration() throws Exception {
		MatrixEGradient.setToValue(0.0);
		MatrixRGradient.setToValue(0.0);


		for (int iID = 0; iID < lstPosTriples.size(); iID++) { //对每个triple
			Triple PosTriple = lstPosTriples.get(iID);
			Triple HeadNegTriple = lstHeadNegTriples.get(iID);
			Triple TailNegTriple = lstTailNegTriples.get(iID);
			
			TripleGradient headGradient = new TripleGradient(
					PosTriple,
					HeadNegTriple,
					MatrixE,
					MatrixR,
					MatrixEGradient,
					MatrixRGradient,
					dDelta);
			headGradient.calculateGradient();

			TripleGradient tailGradient = new TripleGradient(
					PosTriple,
					TailNegTriple,
					MatrixE,
					MatrixR,
					MatrixEGradient,
					MatrixRGradient,
					dDelta);
			tailGradient.calculateGradient();
		}

		for (int iID = 0; iID < lstRules.size(); iID++) {
			Rule rule = lstRules.get(iID);
			Rule sndRelNegrule = lstSndRelNegRules.get(iID);
			
			RuleGradient tailruleGradient = new RuleGradient(
					rule,
					sndRelNegrule,
					MatrixE,
					MatrixR,
					MatrixEGradient,
					MatrixRGradient,
					dDelta);
			tailruleGradient.calculateGradient(m_Weight);	
		}
		
		MatrixEGradient.rescaleByRow();
		MatrixRGradient.rescaleByRow();
		
		/*for (int i = 0; i < MatrixE.rows(); i++) {
			for (int j = 0; j < MatrixE.columns(); j++) {
				double dValue = MatrixEGradient.get(i, j);
				MatrixEGradient.accumulatedByGrad(i, j);
				double dLrate = Math.sqrt(MatrixEGradient.getSum(i, j)) + 1e-8;
				MatrixE.add(i, j, -1.0 * dGammaE * dValue / dLrate);
			}
		}*/
		Set<Map.Entry<Integer, List<Double>>> entries = MatrixE.pData.entrySet();
		Iterator<Map.Entry<Integer, List<Double>>> iterator = entries.iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, List<Double>> next = iterator.next();
			int key = next.getKey();
			List<Double> columnList = next.getValue();
			List<Double> gradientCol = MatrixEGradient.pData.get(key);
			for (int j = 0; j < columnList.size(); j++) {
				double dValue = gradientCol.get(j);
				MatrixEGradient.accumulatedByGrad(key, j);
				double dLrate = Math.sqrt(MatrixEGradient.getSum(key, j)) + 1e-8;
				MatrixE.add(key, j, -1.0 * dGammaE * dValue / dLrate);
			}
		}


		/*for (int i = 0; i < MatrixR.rows(); i++) {
			for (int j = 0; j < MatrixR.columns(); j++) {
				double dValue = MatrixRGradient.get(i, j);
				MatrixRGradient.accumulatedByGrad(i, j);
				double dLrate = Math.sqrt(MatrixRGradient.getSum(i, j)) + 1e-8;
				MatrixR.add(i, j, -1.0 * dGammaR * dValue / dLrate);
			}
		}*/

		Set<Map.Entry<Integer, List<Double>>> entries1 = MatrixR.pData.entrySet();
		Iterator<Map.Entry<Integer, List<Double>>> iterator1 = entries1.iterator();
		while (iterator1.hasNext()) {
			Map.Entry<Integer, List<Double>> next = iterator1.next();
			int key = next.getKey();
			List<Double> columnList = next.getValue();
			List<Double> gradientCol = MatrixRGradient.pData.get(key);
			for (int j = 0; j < columnList.size(); j++) {
				double dValue = gradientCol.get(j);
				MatrixRGradient.accumulatedByGrad(key, j);
				double k = MatrixRGradient.getSum(key, j);
				double dLrate = Math.sqrt(k) + 1e-8;
				MatrixR.add(key, j, -1.0 * dGammaR * dValue / dLrate);
			}
		}
		MatrixE.normalizeByRow();
		MatrixR.normalizeByRow();
	}
}
