package kale.joint;

import kale.struct.Matrix;
import kale.struct.Triple;
import kale.struct.TripleSet;

import java.awt.peer.TrayIconPeer;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * @author: ygtao
 * @date: 2020/10/31 20:06
 */
public class EmbeddingSpace {

    private TripleSet tripleSet;
    private HashSet<Integer> entitySet;
    private HashSet<Integer> relationSet;
    private HashMap<Integer, List<Integer>> entityMatrix;
    private HashMap<Integer, List<Integer>> relationMatrix;
//    public Matrix EntityMatrix; 嵌入空间的矩阵用Matrix不合适，用map保存
//    public Matrix RelationMatrix;

    public HashMap<Integer, ArrayList<String>> LstTrainingTriples = null;  //map<relation, list[head_tail]>
    public HashMap<Integer, HashMap<Integer,HashMap<Integer,Boolean>>> LstInferredTriples = null;   //map<relation, map<subject, map<object, true>>>
    public HashMap<String, Boolean> TrainingTriples_list= null;  //map<line, true>

    public EmbeddingSpace() {
    }

    public EmbeddingSpace(TripleSet tripleSet, HashSet<Integer> entitySet, HashSet<Integer> relationSet) {
        this.tripleSet = tripleSet;
        this.entitySet = entitySet;
        this.relationSet = relationSet;
    }

    public void initialize() {
        System.out.println("根据当前嵌入空间的triple，初始化一些基础属性元素...");
        LstTrainingTriples = new HashMap<Integer, ArrayList<String>>();
        TrainingTriples_list = new HashMap<String,Boolean>();
        LstInferredTriples = new HashMap<Integer, HashMap<Integer,HashMap<Integer,Boolean>>>();
        ArrayList<Triple> triples = tripleSet.pTriple;
        for (Triple triple : triples) {
            int head = triple.head();
            int iRelationID = triple.relation();
            int tail = triple.tail();
            String strValue = head + "_" + tail;
            String line = head + "\t" + iRelationID + "\t" + tail;
            TrainingTriples_list.put(line,true);  //map<str_triple, true>
            if (!LstTrainingTriples.containsKey(iRelationID)) {
                ArrayList<String> tmpLst = new ArrayList<String>();
                tmpLst.add(strValue);//  head_tail添加到list
                LstTrainingTriples.put(iRelationID, tmpLst);  //以relation为key，head_tail构成的list为value
            } else {
                LstTrainingTriples.get(iRelationID).add(strValue);
            }

            if (!LstInferredTriples.containsKey(iRelationID)) {
                HashMap<Integer,HashMap<Integer,Boolean>> tmpMap = new HashMap<Integer,HashMap<Integer,Boolean>>();
                if(!tmpMap.containsKey(head)){
                    HashMap<Integer,Boolean> tmpMap_in = new HashMap<Integer,Boolean>();
                    tmpMap_in.put(tail,true);
                    tmpMap.put(head, tmpMap_in);
                }
                else{
                    tmpMap.get(head).put(tail,true);
                }
                LstInferredTriples.put(iRelationID, tmpMap);  //map<relation, map<subject, map<object, true>>>
            } else {
                HashMap<Integer,HashMap<Integer,Boolean>> tmpMap = LstInferredTriples.get(iRelationID);
                if(!tmpMap.containsKey(head)){
                    HashMap<Integer,Boolean> tmpMap_in = new HashMap<Integer,Boolean>();
                    tmpMap_in.put(tail,true);
                    tmpMap.put(head, tmpMap_in);
                }
                else{
                    tmpMap.get(head).put(tail,true);
                }
            }
        }
        System.out.println("Success!");
    }

    public TripleSet getTripleSet() {
        return tripleSet;
    }

    public void setTripleSet(TripleSet tripleSet) {
        this.tripleSet = tripleSet;
    }

    public HashSet<Integer> getEntitySet() {
        return entitySet;
    }

    public void setEntitySet(HashSet<Integer> entitySet) {
        this.entitySet = entitySet;
    }

    public HashSet<Integer> getRelationSet() {
        return relationSet;
    }

    public void setRelationSet(HashSet<Integer> relationSet) {
        this.relationSet = relationSet;
    }

    public HashMap<Integer, List<Integer>> getEntityMatrix() {
        return entityMatrix;
    }

    public void setEntityMatrix(HashMap<Integer, List<Integer>> entityMatrix) {
        this.entityMatrix = entityMatrix;
    }

    public HashMap<Integer, List<Integer>> getRelationMatrix() {
        return relationMatrix;
    }

    public void setRelationMatrix(HashMap<Integer, List<Integer>> relationMatrix) {
        this.relationMatrix = relationMatrix;
    }
}
