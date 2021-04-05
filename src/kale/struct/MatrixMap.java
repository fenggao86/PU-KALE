package kale.struct;

import basic.util.StringSplitter;

import java.io.*;
import java.util.*;

/**
 * @author: ygtao
 * @date: 2020/11/16 21:57
 */
public class MatrixMap {

    public HashMap<Integer, List<Double>> pData;
    public HashMap<Integer, List<Double>> pSumData;
    private int iNumberOfRows; //行
    private int iNumberOfColumns; //列
    private HashSet<Integer> rowSet;

    public MatrixMap(HashSet<Integer> entitySet, int column) {
        iNumberOfRows = entitySet.size();
        iNumberOfColumns = column;
        pData = new HashMap<>(iNumberOfRows);
        rowSet = new HashSet<>();
        for (Integer e : entitySet) {
            rowSet.add(e);
        }
        initPSumData();
    }

    public int rows() {
        return iNumberOfRows;
    }

    public int columns() {
        return iNumberOfColumns;
    }

    public double get(int i, int j) throws Exception {
        if (!rowSet.contains(i)) {
            throw new Exception("当前嵌入空间不存在该rowId : " + i);
        }
        if (j < 0 || j >= iNumberOfColumns) {
            throw new Exception("get error in DenseMatrix: ColumnID out of range");
        }
        return pData.get(i).get(j);
    }

    public void set(int i, int j, double dValue) throws Exception {
        if (!rowSet.contains(i)) {
            throw new Exception("当前嵌入空间不存在该rowId : " + i);
        }
        if (j < 0 || j >= iNumberOfColumns) {
            throw new Exception("set error in DenseMatrix: ColumnID out of range");
        }
        List<Double> column = pData.get(i);
        if (column == null) {
            column = new ArrayList<>();
            for (int k = 0; k < iNumberOfColumns; k++) {
                column.add(0.0);
            }
            pData.put(i, column);
        }
        column.set(j, dValue);
    }

    public void setToValue(double dValue) {
        for (int i : rowSet) {
            List<Double> column = pData.get(i);
            if (column == null) {
                column = new ArrayList<>(iNumberOfColumns);
                for (int k = 0; k < iNumberOfColumns; k++) {
                    column.add(0.0);
                }
                pData.put(i, column);
            }
            for (int j = 0; j < iNumberOfColumns; j++) {
                column.set(j, dValue);
            }
        }
    }

    public void initPSumData() {
        pSumData = new HashMap<>();
        for (int i : rowSet) {
            List<Double> column = new ArrayList<>(iNumberOfColumns);
            for (int j = 0; j < iNumberOfColumns; j++) {
                column.add(0.0);
            }
            pSumData.put(i, column);
        }
    }

    public void setToRandom() {
        Random rd = new Random(123);
        for (int i : rowSet) {
            List<Double> column = pData.get(i);
            if (column == null) {
                column = new ArrayList<>(iNumberOfColumns);
                for (int k = 0; k < iNumberOfColumns; k++) {
                    column.add(0.0);
                }
                pData.put(i, column);
            }
            for (int j = 0; j < iNumberOfColumns; j++) {
                double dValue = rd.nextDouble();
                column.set(j, 2.0 * dValue - 1.0);
            }
        }
    }

    public double getSum(int i, int j) throws Exception {
        if (!rowSet.contains(i)) {
            throw new Exception("当前嵌入空间不存在该rowId : " + i);
        }
        if (j < 0 || j >= iNumberOfColumns) {
            throw new Exception("get error in DenseMatrix: ColumnID out of range");
        }
        return pSumData.get(i).get(j);
    }

    public void add(int i, int j, double dValue) throws Exception {
        if (!rowSet.contains(i)) {
            throw new Exception("当前嵌入空间不存在该rowId : " + i);
        }
        if (j < 0 || j >= iNumberOfColumns) {
            throw new Exception("add error in DenseMatrix: ColumnID out of range");
        }
        List<Double> column = pData.get(i);
        column.set(j, column.get(j) + dValue);
    }

    //标准化
    public void normalize() {
        double dNorm = 0.0;
        for (int i : rowSet) {
            List<Double> column = pData.get(i);
            for (int j = 0; j < iNumberOfColumns; j++) {
                dNorm += column.get(j) * column.get(j);
            }
        }
        dNorm = Math.sqrt(dNorm);
        if (dNorm != 0.0) {
            for (int i : rowSet) {
                List<Double> column = pData.get(i);
                for (int j = 0; j < iNumberOfColumns; j++) {
                    column.set(j, column.get(j)/dNorm);
                }
            }
        }
    }

    public void normalizeByRow() { //归一化
        for (int i : rowSet) {
            double dNorm = 0.0;
            List<Double> column = pData.get(i);
            for (int j = 0; j < iNumberOfColumns; j++) {
                dNorm += column.get(j) * column.get(j);
            }
            dNorm = Math.sqrt(dNorm);
            if (dNorm != 0.0) {
                for (int j = 0; j < iNumberOfColumns; j++) {
                    column.set(j, column.get(j) / dNorm);
                }
            }
        }
    }

    public void rescaleByRow() {
        for (int i : rowSet) {
            List<Double> column = pData.get(i);
            double dNorm = 0.0;
            for (int j = 0; j < iNumberOfColumns; j++) {
                dNorm += column.get(j) * column.get(j);
            }
            dNorm = Math.sqrt(dNorm);
            if (dNorm != 0.0) {
                for (int j = 0; j < iNumberOfColumns; j++) {
                    column.set(j, column.get(j) * Math.min(1.0, 1.0/dNorm));
                }
            }
        }
    }

    public void accumulatedByGrad(int i, int j) throws Exception {
        if (!rowSet.contains(i)) {
            throw new Exception("当前嵌入空间不存在该rowId : " + i);
        }
        if (j < 0 || j >= iNumberOfColumns) {
            throw new Exception("add error in DenseMatrix: ColumnID out of range");
        }
        Double value = pData.get(i).get(j);
        List<Double> sumData = pSumData.get(i);
        sumData.set(j, value * value + sumData.get(j));

    }

    /*public boolean load(String fnInput) throws Exception {
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
    }*/

    public void output(String fnOutput) throws Exception {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fnOutput), "UTF-8"));

        writer.write("iNumberOfRows: " + iNumberOfRows + "; iNumberOfColumns: " + iNumberOfColumns + "\n");
        /*for (int i = 0; i < iNumberOfRows; i++) {
            writer.write((pData[i][0] + " ").trim());
            for (int j = 1; j < iNumberOfColumns; j++) {
                writer.write("\t" + (pData[i][j] + " ").trim());
            }
            writer.write("\n");
        }*/
        Set<Map.Entry<Integer, List<Double>>> entries = pData.entrySet();
        Iterator<Map.Entry<Integer, List<Double>>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, List<Double>> next = iterator.next();
            Integer key = next.getKey();
            writer.write(key + "\t");
            List<Double> columnList = next.getValue();
            for (double column : columnList) {
                writer.write("\t" + column);
            }
            writer.write("\n");
        }

        writer.close();
    }
}
