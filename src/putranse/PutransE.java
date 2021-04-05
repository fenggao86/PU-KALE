package putranse;


import kale.joint.EmbeddingSpace;
import kale.struct.Triple;
import kale.struct.TripleSet;

import java.util.*;

/**
 * @author: ygtao
 * @date: 2020/10/27 20:43
 */
public class PutransE {

    private int numOfEntity;
    private int numOfRelation;

    private TripleSet trainingTriples;  //三元组集合
    private HashSet<Triple> copyOfTriples;
    private int dimension;  //维度
    private int tripleContraint;
    public HashMap<Integer, HashSet<Triple>> relToTripleMap;
    public HashMap<Integer, HashMap<Integer, HashSet<Triple>>> headToTailMap;
    public HashMap<Integer, HashMap<Integer, HashSet<Triple>>> tailToHeadMap;
    public List<Integer> reLStatList;



    //构造函数


    public PutransE() {
    }

    public PutransE(TripleSet trainingTriples, int tripleContraint) {
        this.trainingTriples = trainingTriples;
        this.tripleContraint =  tripleContraint;
        relToTripleMap = new HashMap<>();
        headToTailMap = new HashMap<>();
        tailToHeadMap = new HashMap<>();
        copyOfTriples = new HashSet<>();
        List<Triple> tempList = trainingTriples.pTriple;
        for (Triple triple : tempList) {
            copyOfTriples.add(new Triple(triple.head(), triple.tail(), triple.relation()));
        }
    }

    public List<EmbeddingSpace> buildEmbeddingSpaceDataset() {
        System.out.println("开始构建嵌入空间的数据集");
        long start = System.currentTimeMillis();
        List<EmbeddingSpace> list = new ArrayList<>();
        //获取relation的个数
        int size;
        Random random = new Random();
        int globalTripleCount = 0;
        int index = 1;
        while ((size = relToTripleMap.size()) > 0) { //只要还有triple就要继续生成嵌入空间
            System.out.println("第" + (index++) + "个嵌入空间. size=" + size);
            TripleSet tripleSet = new TripleSet(); //save triple
            HashSet<Triple> tempTriples = new HashSet<>();
            HashSet<Integer> entitySet = new HashSet<>(); //save entity
            HashSet<Integer> tempEntitySet = new HashSet<>();
            HashSet<Integer> relationSet = new HashSet<>();  // save relation
            //随机选择
            int tripleCount = 0;

            while (tripleCount < tripleContraint && (size = relToTripleMap.size()) > 0) {//triple个数不够就要一直添加triple，除非没有多余的triplele
                int relation;
                HashSet<Triple> triples;
                Iterator<Integer> iter = relToTripleMap.keySet().iterator();
                if (iter.hasNext()) {
                    relation = iter.next(); //随机获取一个relation
                    triples = relToTripleMap.get(relation);
                    Iterator<Triple> iterator = triples.iterator();  //这些triple具有相同的relation
                    while (iterator.hasNext() && tripleCount < tripleContraint) {
                        //1. 随机选择一个triple
                        Triple triple = iterator.next();
                        tempTriples.add(triple);
                        iterator.remove(); //从relToTripleMap中删除
                        entitySet.add(triple.head());
                        entitySet.add(triple.tail());
                        tempEntitySet.add(triple.head()); //暂存头尾实体
                        tempEntitySet.add(triple.tail());
                        relationSet.add(triple.relation());
                        tripleCount++; //triple个数+1

                        //System.out.println("从节点出发，采用随机游走策略选择triple.");
                        Iterator<Integer> iterator1 = tempEntitySet.iterator();
                        while (iterator1.hasNext() && tripleCount < tripleContraint) {
                            Integer next = iterator1.next(); //起始节点
                            //获取这个节点出方向的triple，
                            HashMap<Integer, HashSet<Triple>> outTriples = headToTailMap.get(next);
                            //HashMap<Integer, HashSet<Triple>> inTriples = tailToHeadMap.get(next);
                            tripleCount = selectTriplesByRandomWalk(outTriples, tempTriples, relationSet, entitySet, tripleCount,relation);
                            //tripleCount = selectTriplesByRandomWalk(inTriples, tempTriples, relationSet, entitySet, tripleCount, relation);
                        }
                    }
                    if (triples.size() == 0) {
                        relToTripleMap.remove(relation);
                    }
                }

            }
            System.out.println("已经达到所设置的嵌入空间triple限定值或者三元组已经遍历完：" + tripleContraint);
            tripleSet.pTriple = new ArrayList<>(tempTriples);
            tripleSet.setiNumberOfTriples(tripleSet.pTriple.size());
            tripleSet.setiNumberOfEntities(entitySet.size());
            tripleSet.setiNumberOfRelations(relationSet.size());
            EmbeddingSpace e = new EmbeddingSpace(tripleSet, entitySet, relationSet);
            globalTripleCount += tripleCount;
            System.out.println("当前嵌入空间的triple个数：" + tripleSet.pTriple.size());
            System.out.println("当前总得嵌入空间triple个数为：" + globalTripleCount);
            System.out.println("==========================================================");
            list.add(e);
        }
        long end = System.currentTimeMillis();
        System.out.println("嵌入空间构建结束. 花费时间：" + (end - start) + "ms");
        return list;
    }

    public int selectTriplesByRandomWalk(HashMap<Integer, HashSet<Triple>> map, HashSet<Triple> selectTriples,
                                         HashSet relationSet, HashSet entitySet, int tripleCount, int iRelation) {
        if (map != null && map.size() != 0) {
            Set<Map.Entry<Integer, HashSet<Triple>>> entries = map.entrySet();
            Iterator<Map.Entry<Integer, HashSet<Triple>>> iterator = entries.iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, HashSet<Triple>> next = iterator.next();
                Integer relation = next.getKey();
                if (relation != iRelation) {
                    //添加relation
                    relationSet.add(relation);
                    HashSet<Triple> triples = relToTripleMap.get(relation);
                    //获取relation对应的所有tripe
                    HashSet<Triple> value = next.getValue();
                    if (value != null) {
                        Iterator<Triple> iterator1 = value.iterator();
                        while (iterator1.hasNext()) {
                            //获取一个triple
                            Triple next1 = iterator1.next();
                            //将triple加入集合
                            selectTriples.add(next1);
                            tripleCount++;
                            entitySet.add(next1.tail()); //添加tail实体
                            if (triples != null) {
                                triples.remove(next1); //remove
                            }
                        }
                        //value遍历完成，删除之
                        iterator.remove();
                    }
                }
            }
        }
        return tripleCount;
    }


    /**
     *
     */
    public List<EmbeddingSpace> buildTripleSetForEachIter() {
        System.out.println("开始构建嵌入空间的数据集");
        long start = System.currentTimeMillis();
        //1. 从全局relation集合中随机选取一个
        initializeOrResetRelStatList(false); //初始化状态
        List<EmbeddingSpace> list = new ArrayList<>(); //保存所有嵌入空间

        TripleSet tripleSet = new TripleSet();
        HashSet<Integer> entitySet = new HashSet<>();
        HashSet<Integer> relSet = new HashSet<>();
        int count = 0;
        int numOfTriple = 0;
        int counting = 1;
        while (hasNextRelation()) {
            int randomRel = nextRelation(); //选择一个relation
            System.out.println("第" + counting++ + "次选择的relation序号：" + randomRel);
            //if (!relSet.contains((Integer)randomRel)) {
                relSet.add(randomRel);
                HashSet<Triple> triples = relToTripleMap.get(randomRel);
                System.out.println("relation-" + randomRel + "对应的triple个数：" + triples.size());
                HashSet<Integer> tempEntitySet = new HashSet<>();
                for (Triple triple : triples) {//遍历每个triple
                    tripleSet.addTriple(triple); //添加triple
                    entitySet.add(triple.head());
                    entitySet.add(triple.tail());
                    tempEntitySet.add(triple.head());
                    tempEntitySet.add(triple.tail());
                    count++;
                }
                if (count >= tripleContraint) {//保存一个嵌入空间
                    System.out.println("已经达到或者超过所设置的嵌入空间triple限定值.");
                    tripleSet.setiNumberOfTriples(tripleSet.pTriple.size());
                    tripleSet.setiNumberOfEntities(entitySet.size());
                    tripleSet.setiNumberOfRelations(relSet.size());
                    EmbeddingSpace e = new EmbeddingSpace(tripleSet, entitySet, relSet);
                    numOfTriple += count;
                    System.out.println("当前嵌入空间的triple个数：" + tripleSet.pTriple.size());
                    System.out.println("当前总得嵌入空间triple个数为：" + numOfTriple);
                    System.out.println("==========================================================");
                    list.add(e);
                    tripleSet = new TripleSet(); //重置
                    entitySet = new HashSet<>();
                    relSet = new HashSet<>();
                    count = 0;
                } else {//从顶点，随意游走选择triple
                    System.out.println("进入随机游走策略，选择triple.");
                    Iterator<Integer> iterator = tempEntitySet.iterator();
                    while (iterator.hasNext()) {
                        Integer entityId = iterator.next();
                        System.out.println("随机游走：选择的entityId=" + entityId);
                        HashMap<Integer, HashSet<Triple>> relToTailMap = headToTailMap.get(entityId);
                        System.out.println("entityId= " + entityId + "出方向的relation个数为：" + relToTailMap.size());
                        count += addMapInfoToSet(relToTailMap, tripleSet, entitySet, relSet, tripleContraint);
                        HashMap<Integer, HashSet<Triple>> relToHeadMap = tailToHeadMap.get(entityId);
                        System.out.println("entityId= " + entityId + "入方向的relation个数为：" + relToHeadMap.size());
                        count += addMapInfoToSet(relToHeadMap, tripleSet, entitySet, relSet, tripleContraint);
                        System.out.println("随机游走：count=" + count);
                        if (count >= tripleContraint || !hasNextRelation()) { // triple个数达到限制，或者达到最后一个relation
                            tripleSet.setiNumberOfTriples(tripleSet.pTriple.size());
                            tripleSet.setiNumberOfEntities(entitySet.size());
                            tripleSet.setiNumberOfRelations(relSet.size());
                            EmbeddingSpace e = new EmbeddingSpace(tripleSet, entitySet, relSet);
                            numOfTriple += count;
                            System.out.println("当前嵌入空间的triple个数：" + tripleSet.pTriple.size());
                            System.out.println("当前总得嵌入空间triple个数为：" + numOfTriple);
                            System.out.println("==========================================================");
                            list.add(e);
                            tripleSet = new TripleSet(); //重置
                            entitySet = new HashSet<>();
                            relSet = new HashSet<>();
                            count = 0;
                        }
                    }
                }
    //        }
        }
        System.out.println("嵌入空间数据集构建结束。嵌入空间个数为：" + list.size());
        initializeOrResetRelStatList(true); //重置状态
        long end = System.currentTimeMillis();
        System.out.println("构建嵌入空间的时间为：" + (end - start) + "ms");
        return list;
    }

    //返回添加的triple个数
    private int addMapInfoToSet(HashMap<Integer, HashSet<Triple>> map, TripleSet tripleSet,
                                HashSet<Integer> entitySet, HashSet<Integer> relSet, int tripleContraint) {
        int count = 0;
        if (map != null) {
            int num = tripleSet.pTriple.size();
            Set<Map.Entry<Integer, HashSet<Triple>>> entries = map.entrySet();
            Iterator<Map.Entry<Integer, HashSet<Triple>>> iterator = entries.iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, HashSet<Triple>> next = iterator.next();
                relSet.add(next.getKey()); // add relation
                HashSet<Triple> value = next.getValue();
                Iterator<Triple> iterator1 = value.iterator();
                while (iterator1.hasNext() && num < tripleContraint) {
                    Triple t = iterator1.next();
                    tripleSet.addTriple(t); // add triple
                    entitySet.add(t.head()); // add entity
                    entitySet.add(t.tail());
                    num++;
                    count++;
                }
            }
        }
        return count;
    }

    public void initailize() {
        ArrayList<Triple> triples = trainingTriples.pTriple;
        int count = 0;
        //初始化：relToTripleMap，headToTailMap，tailToHeadMap
        if (triples != null) {
            //遍历TripleSet中的每个triple

            for (Triple triple : trainingTriples.pTriple) {
                int head = triple.head();
                int relation = triple.relation();
                int tail = triple.tail();
                count++;
                //初始化relToTripleMap
                HashSet set = relToTripleMap.get(relation);
                if (set == null) {
                    //先创建一个HashSet
                    set = new HashSet();
                    relToTripleMap.put(relation, set);
                }
                set.add(triple);

                //初始化headToTailMap和tailToHeadMap
                HashMap<Integer, HashSet<Triple>> tailMap = headToTailMap.get(head);
                HashMap<Integer, HashSet<Triple>> headMap = tailToHeadMap.get(tail);

                if (tailMap == null) {
                    tailMap = new HashMap();
                    HashSet<Triple> tailSet = new HashSet<>();
                    tailSet.add(triple);
                    tailMap.put(relation, tailSet);
                    headToTailMap.put(head, tailMap);
                } else {
                    HashSet<Triple> tailSet = tailMap.get(relation);
                    if (tailSet == null) {
                        tailSet = new HashSet<>();
                    }
                    tailSet.add(triple);
                }

                if (headMap == null) {
                    headMap = new HashMap<>();
                    HashSet<Triple> headSet = new HashSet<>();
                    headSet.add(triple);
                    headMap.put(relation, headSet);
                    tailToHeadMap.put(tail, headMap);
                } else {
                    HashSet<Triple> headSet = headMap.get(relation);
                    if (headSet == null) {
                        headSet = new HashSet<>();
                    }
                    headSet.add(triple);
                }
            }
            //
            numOfRelation = relToTripleMap.size();
        }
        System.out.println("初始化时的triple的个数为：" + count);
    }

    private void initializeOrResetRelStatList(boolean reset) {
        if (reLStatList == null) {
            reLStatList = new ArrayList<>();
        }
        if (reset) {//重置状态
            reLStatList.clear();
        }
        for (int i = 0; i < numOfRelation; i++) {
            reLStatList.add(i);
        }
    }

    public boolean hasNextRelation() {
        return reLStatList.size() > 0;
    }

    public int nextRelation() {
        int size = reLStatList.size();
        Random random = new Random();
        int index = random.nextInt(size);
        int res = reLStatList.get(index);
        reLStatList.remove(index);
        return res;
    }

    /**
     * 测试嵌入空间triple的重复率
     * @param embeddingSpaces
     */
    public void testTripeCoverage(List<EmbeddingSpace> embeddingSpaces) {
        int origin_size = copyOfTriples.size();
        int single_size = 0;
        int repeat_size = 0;
        HashMap<Triple, Boolean> map = new HashMap<>();

        for (EmbeddingSpace space : embeddingSpaces) {
            List<Triple> triples = space.getTripleSet().pTriple;
            for (Triple triple : triples) {
                if (copyOfTriples.contains(triple)) {
                    single_size++;
                    copyOfTriples.remove(triple);
                }else {
                    repeat_size++;
                }
                map.put(triple, true);
            }
        }
        System.out.println("训练集triple原始个数："+ origin_size);
        System.out.println("single_size = " + single_size + ", repeat_size = " + repeat_size + ",重复率：" + (repeat_size * 1.0 / origin_size));
        System.out.println("剩余未分配的triple个数：" + copyOfTriples.size());
    }
}
