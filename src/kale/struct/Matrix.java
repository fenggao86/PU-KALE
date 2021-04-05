package kale.struct;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Random;

import basic.util.StringSplitter;

//矩阵
public class Matrix {
	private double[][] pData = null;
	private double[][] pSumData = null;
	private int iNumberOfRows; //行数
	private int iNumberOfColumns; //列数
	
	public Matrix() {
	}
	
	public Matrix(int iRows, int iColumns) {
		pData = new double[iRows][];
		pSumData = new double[iRows][];
		for (int i = 0; i < iRows; i++) {
			pData[i] = new double[iColumns];
			pSumData[i] = new double[iColumns];
			for (int j = 0; j < iColumns; j++) {
				pData[i][j] = 0.0;
				pSumData[i][j] = 0.0;
			}
		}
		iNumberOfRows = iRows;
		iNumberOfColumns =  iColumns;
	}
	
	public int rows() {
		return iNumberOfRows;
	}
	
	public int columns() {
		return iNumberOfColumns;
	}
	
	public double get(int i, int j) throws Exception {
		if (i < 0 || i >= iNumberOfRows) {
			throw new Exception("get error in DenseMatrix: RowID out of range");
		}
		if (j < 0 || j >= iNumberOfColumns) {
			throw new Exception("get error in DenseMatrix: ColumnID out of range");
		}
		return pData[i][j];
	}
	
	public void set(int i, int j, double dValue) throws Exception {
		if (i < 0 || i >= iNumberOfRows) {
			throw new Exception("set error in DenseMatrix: RowID out of range");
		}
		if (j < 0 || j >= iNumberOfColumns) {
			throw new Exception("set error in DenseMatrix: ColumnID out of range");
		}
		pData[i][j] = dValue;
	}
	
	public void setToValue(double dValue) {
		for (int i = 0; i < iNumberOfRows; i++) {
			for (int j = 0; j < iNumberOfColumns; j++) {
				pData[i][j] = dValue;
			}
		}
	}
	
	public void setToRandom() {
		Random rd = new Random(123);
		for (int i = 0; i < iNumberOfRows; i++) {
			for (int j = 0; j < iNumberOfColumns; j++) {
				double dValue = rd.nextDouble();
				pData[i][j] = 2.0 * dValue - 1.0;
			}
		}
	}
	
	public double getSum(int i, int j) throws Exception {
		if (i < 0 || i >= iNumberOfRows) {
			throw new Exception("get error in DenseMatrix: RowID out of range");
		}
		if (j < 0 || j >= iNumberOfColumns) {
			throw new Exception("get error in DenseMatrix: ColumnID out of range");
		}
		return pSumData[i][j];
	}
	
	public void add(int i, int j, double dValue) throws Exception {
		if (i < 0 || i >= iNumberOfRows) {
			throw new Exception("add error in DenseMatrix: RowID out of range");
		}
		if (j < 0 || j >= iNumberOfColumns) {
			throw new Exception("add error in DenseMatrix: ColumnID out of range");
		}
		pData[i][j] += dValue;
	}

	//标准化
	public void normalize() {
		double dNorm = 0.0;
		for (int i = 0; i < iNumberOfRows; i++) {
			for (int j = 0; j < iNumberOfColumns; j++) {
				dNorm += pData[i][j] * pData[i][j];
			}
		}
		dNorm = Math.sqrt(dNorm);
		if (dNorm != 0.0) {
			for (int i = 0; i < iNumberOfRows; i++) {
				for (int j = 0; j < iNumberOfColumns; j++) {
					pData[i][j] /= dNorm;
				}
			}
		}
	}
	
	public void normalizeByRow() { //归一化
		for (int i = 0; i < iNumberOfRows; i++) {
			double dNorm = 0.0;
			for (int j = 0; j < iNumberOfColumns; j++) {
				dNorm += pData[i][j] * pData[i][j];
			}
			dNorm = Math.sqrt(dNorm);
			if (dNorm != 0.0) {
				for (int j = 0; j < iNumberOfColumns; j++) {
					pData[i][j] /= dNorm;
				}
			}
		}
	}
	
	public void rescaleByRow() {
		for (int i = 0; i < iNumberOfRows; i++) {
			double dNorm = 0.0;
			for (int j = 0; j < iNumberOfColumns; j++) {
				dNorm += pData[i][j] * pData[i][j];
			}
			dNorm = Math.sqrt(dNorm);
			if (dNorm != 0.0) {
				for (int j = 0; j < iNumberOfColumns; j++) {
					pData[i][j] *= Math.min(1.0, 1.0/dNorm);
				}
			}
		}
	}
	
	public void normalizeByColumn() {
		for (int j = 0; j < iNumberOfColumns; j++) {
			double dNorm = 0.0;
			for (int i = 0; i < iNumberOfRows; i++) {
				dNorm += pData[i][j] * pData[i][j];
			}
			dNorm = Math.sqrt(dNorm);
			if (dNorm != 0.0) {
				for (int i = 0; i < iNumberOfRows; i++) {
					pData[i][j] /= dNorm;
				}
			}
		}
	}
	
	public void accumulatedByGrad(int i, int j) throws Exception {
		if (i < 0 || i >= iNumberOfRows) {
			throw new Exception("add error in DenseMatrix: RowID out of range");
		}
		if (j < 0 || j >= iNumberOfColumns) {
			throw new Exception("add error in DenseMatrix: ColumnID out of range");
		}
		pSumData[i][j] += pData[i][j] * pData[i][j];
	}	
	
	public boolean load(String fnInput) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(fnInput), "UTF-8"));
		
		String line = "";
		line = reader.readLine();
		String[] first_line = StringSplitter.RemoveEmptyEntries(StringSplitter
				.split(":; ", line));
		//初始化的行和列与读取文件的行和列不一致，抛出异常
		if (iNumberOfRows != Integer.parseInt(first_line[1]) || 
				iNumberOfColumns != Integer.parseInt(first_line[3])) {
			throw new Exception("load error in DenseMatrix: row/column number incorrect");
		}
		
		int iRowID = 0;
		while ((line = reader.readLine()) != null) { //读取每一行
			String[] tokens = StringSplitter.RemoveEmptyEntries(StringSplitter
					.split("\t ", line));
			//验证行
			if (iRowID < 0 || iRowID >= iNumberOfRows) {
				throw new Exception("load error in DenseMatrix: RowID out of range");
			}
			//验证列
			if (tokens.length != iNumberOfColumns) {
				throw new Exception("load error in DenseMatrix: ColumnID out of range");
			}

			for (int iColumnID = 0; iColumnID < tokens.length; iColumnID++) {
				pData[iRowID][iColumnID] = Double.parseDouble(tokens[iColumnID]);
			}
			iRowID++;
		}
		
		reader.close();
		return true;
	}
	
	public void output(String fnOutput) throws Exception {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fnOutput), "UTF-8"));
		
		writer.write("iNumberOfRows: " + iNumberOfRows + "; iNumberOfColumns: " + iNumberOfColumns + "\n");
		for (int i = 0; i < iNumberOfRows; i++) {
			writer.write((pData[i][0] + " ").trim());
			for (int j = 1; j < iNumberOfColumns; j++) {
				writer.write("\t" + (pData[i][j] + " ").trim());
			}
			writer.write("\n");
		}
		
		writer.close();
	}
	
	public void releaseMemory() {
		for (int i = 0; i < iNumberOfRows; i++) {
			pData[i] = null;
		}
		pData = null;
		iNumberOfRows = 0;
		iNumberOfColumns = 0;
	}
	
	public void resetToZero() {
		for (int i = 0; i < iNumberOfRows; i++) {
			for (int j = 0; j < iNumberOfColumns; j++) {
				pSumData[i][j] = 0.0;
			}
		}
	}
}
