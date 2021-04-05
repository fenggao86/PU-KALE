package kale.struct;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

import basic.util.StringSplitter;

/**
 * 三元组集合
 */
public class TripleSet {
	private int iNumberOfEntities; //实体个数
	private int iNumberOfRelations; //关系个数
	private int iNumberOfTriples; //三元组个数
	public ArrayList<Triple> pTriple = null; //三元组集合
	public HashMap<String, Boolean> pTripleStr = null;
	public HashMap<String, HashMap<String, Set<Triple>>> classifiedTriples;   //<relationn, <head, List<Triple>>>
	private ArrayList<Triple> pTripleTemp;
	public TripleSet() {
		pTripleStr = new HashMap<String, Boolean>();
	}
	
	
	
	public TripleSet(int iEntities, int iRelations) throws Exception {
		iNumberOfEntities = iEntities;
		iNumberOfRelations = iRelations;
	}
	
	public int entities() {
		return iNumberOfEntities;
	}
	
	public int relations() {
		return iNumberOfRelations;
	}
	
	public int triples() {
		return iNumberOfTriples;
	}
	
	public HashMap<String, Boolean> tripleSet() {
		return pTripleStr;
	}

	public void setiNumberOfEntities(int iNumberOfEntities) {
		this.iNumberOfEntities = iNumberOfEntities;
	}

	public void setiNumberOfRelations(int iNumberOfRelations) {
		this.iNumberOfRelations = iNumberOfRelations;
	}

	public void setiNumberOfTriples(int iNumberOfTriples) {
		this.iNumberOfTriples = iNumberOfTriples;
	}

	public Triple get(int iID) throws Exception {
		if (iID < 0 || iID >= iNumberOfTriples) {
			throw new Exception("getTriple error in TripleSet: ID out of range");
		}
		return pTriple.get(iID);
	}

	//将文件中的数据集三元组加载到内存中的
	public void load(String fnInput) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(fnInput), "UTF-8"));
		pTriple = new ArrayList<Triple>();
		
		String line = "";
		while ((line = reader.readLine()) != null) {
			String[] tokens = StringSplitter.RemoveEmptyEntries(StringSplitter
					.split("\t ", line));
			if (tokens.length != 3) {
				throw new Exception("load error in TripleSet: data format incorrect");
			}
			int iHead = Integer.parseInt(tokens[0]);
			int iTail = Integer.parseInt(tokens[2]);
			int iRelation = Integer.parseInt(tokens[1]);
			if (iHead < 0 || iHead >= iNumberOfEntities) {
				throw new Exception("load error in TripleSet: head entity ID out of range");
			}
			if (iTail < 0 || iTail >= iNumberOfEntities) {
				throw new Exception("load error in TripleSet: tail entity ID out of range");
			}
			if (iRelation < 0 || iRelation >= iNumberOfRelations) {
				throw new Exception("load error in TripleSet: relation ID out of range");
			}
			pTriple.add(new Triple(iHead, iTail, iRelation));
		}
		iNumberOfTriples = pTriple.size();
		pTripleTemp = pTriple;
		reader.close();
	}

	public void getTripleInSelectedEntityAndRel(Set<Integer> entitySet, Set<Integer> relationSet) {
		ArrayList<Triple> list = new ArrayList<>();
		for (Triple triple : pTripleTemp) {
			int head = triple.head();
			int relation = triple.relation();
			int tail = triple.tail();
			if (entitySet.contains(head) && entitySet.contains(tail) && relationSet.contains(relation)) {
				list.add(triple);
			}
		}
		pTriple = list;
		iNumberOfTriples = list.size();
	}
	
	public void loadStr(String fnInput) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(fnInput), "UTF-8"));
		
		String line = "";
		while ((line = reader.readLine()) != null) {
			pTripleStr.put(line.trim(), true);
		}
		reader.close();
	}

	//从文件中读取1000个三元组到内存中
	public void subload(String fnInput) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(fnInput), "UTF-8"));
		pTriple = new ArrayList<Triple>();
		
		String line = "";
		int count=0;
		while ((line = reader.readLine()) != null) {
			count++;
			String[] tokens = StringSplitter.RemoveEmptyEntries(StringSplitter
					.split("\t ", line));
			if (tokens.length != 3) {
				throw new Exception("load error in TripleSet: data format incorrect");
			}
			int iHead = Integer.parseInt(tokens[0]);
			int iTail = Integer.parseInt(tokens[2]);
			int iRelation = Integer.parseInt(tokens[1]);
			if (iHead < 0 || iHead >= iNumberOfEntities) {
				throw new Exception("load error in TripleSet: head entity ID out of range");
			}
			if (iTail < 0 || iTail >= iNumberOfEntities) {
				throw new Exception("load error in TripleSet: tail entity ID out of range");
			}
			if (iRelation < 0 || iRelation >= iNumberOfRelations) {
				throw new Exception("load error in TripleSet: relation ID out of range");
			}
			pTriple.add(new Triple(iHead, iTail, iRelation));
			if(count==1000){
				break;
			}
		}
		
		iNumberOfTriples = pTriple.size();
		reader.close();
	}

	public void loadSelectedTripleForValidOrTest(int tripleNum, Set<Integer> entitySet, Set<Integer> relationSet) {
		ArrayList<Triple> list = new ArrayList<>();

		int count = 0;
		boolean flag = false;
		for (Integer relation : relationSet) {
			HashMap<String, Set<Triple>> map = classifiedTriples.get(relation.toString());
			Set<Map.Entry<String, Set<Triple>>> entries = map.entrySet();
			Iterator<Map.Entry<String, Set<Triple>>> iterator = entries.iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Set<Triple>> next = iterator.next();
				String key = next.getKey();
				if (entitySet.contains(key)) {
					Set<Triple> value = next.getValue();
					Iterator<Triple> itr = value.iterator();
					while (itr.hasNext()) {
						Triple triple = itr.next();
						if (entitySet.contains(triple.tail())) {
							list.add(triple);
							if (count++ == tripleNum) {
								pTriple = list;
								iNumberOfTriples = pTriple.size();
								flag = true;
								break;
							}
						}
					}
				}
				if (flag) {
					break;
				}
			}
			if (flag) {
				break;
			}
		}
	}

	public void classifyTriples(String fnInput) throws Exception{
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(fnInput), "UTF-8"));
		pTriple = new ArrayList<Triple>();

		String line = "";
		int count=0;
		HashMap<String, HashMap<String, Set<Triple>>> map = new HashMap<>();
		while ((line = reader.readLine()) != null) {
			count++;
			String[] tokens = StringSplitter.RemoveEmptyEntries(StringSplitter
					.split("\t ", line));
			if (tokens.length != 3) {
				throw new Exception("load error in TripleSet: data format incorrect");
			}
			int iHead = Integer.parseInt(tokens[0]);
			int iTail = Integer.parseInt(tokens[2]);
			int iRelation = Integer.parseInt(tokens[1]);
			Triple triple = new Triple(iHead, iTail, iRelation);
			HashMap<String, Set<Triple>> tripleMap = map.get(tokens[1]);
			if (tripleMap == null) {
				tripleMap = new HashMap<>();
				Set<Triple> set = new HashSet<>();
				set.add(triple);
				tripleMap.put(tokens[0], set);
				map.put(tokens[1], tripleMap);
			} else {
				Set<Triple> set = tripleMap.get(tokens[0]);
				if (set == null) {
					set = new HashSet<>();
					set.add(triple);
					tripleMap.put(tokens[0], set);
				} else {
					set.add(triple);
				}
			}
		}
		classifiedTriples = map;
	}
	
	//把三元组打乱
	public void randomShuffle() {
		TreeMap<Double, Triple> tmpMap = new TreeMap<Double, Triple>();
		for (int iID = 0; iID < iNumberOfTriples; iID++) {
			int i = pTriple.get(iID).head();
			int j = pTriple.get(iID).tail();
			int k = pTriple.get(iID).relation();
			tmpMap.put(Math.random(), new Triple(i, j, k));
		}
		
		pTriple = new ArrayList<Triple>();
		Iterator<Double> iterValues = tmpMap.keySet().iterator();
		while (iterValues.hasNext()) {
			double dRand = iterValues.next();
			Triple trip = tmpMap.get(dRand);
			pTriple.add(new Triple(trip.head(), trip.tail(), trip.relation()));
		}
		iNumberOfTriples = pTriple.size();
		tmpMap.clear();
	}

	public void addTriple(Triple triple) {
		if (pTriple == null) {
			pTriple = new ArrayList<>();
		}
		pTriple.add(triple);
	}

}
