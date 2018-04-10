import java.util.ArrayList;
import java.util.HashSet;


//  SuffixTree.java
//  SuffixTree
//
//  Created by longbaolin on 2018/4/01.
//  Copyright © 2018年 longbaolin. All rights reserved.
//

/**
 *  根据
 *  https://stackoverflow.com/questions/9452701/ukkonens-suffix-tree-algorithm-in-plain-english?answertab=votes#tab-top
 *  的后缀树实现
 *  为了实现上的方便，在这里习惯性的不使用边而是使用节点来保存信息(因为边只是由两个节点来隐式的表示)
 * */

public class SuffixTree {

    //保存用来建树的文本
    private ArrayList<String> texts = new ArrayList<>();
    //相对应的文本所在的位置
    private ArrayList<String> position = new ArrayList<>();
    //相应的文本在代码中所在的位置
    private ArrayList<String> locations = new ArrayList<>();
    //当前用来建树的文本
    private String curText;
    //SuffixTree的根结点
    private Node root = new Node();
    //活动点
    private ActivePoint activePoint = new ActivePoint(root, -1, -1);
    //在当前build步骤还需要插入多少节点
    private int remainder = 0;
    //当前节点数统计
    private int count = 0;

    //相似方法结果
    private HashSet<HashSet<String>> nameSetSet = new HashSet<>();

    //表示新加入文本是否已存在的flag
    private boolean spilt = false;

//    /**
//     * SuffixTree的构造器，传入需要用来建树的文本
//     * */
//    public SuffixTree(String text) {
//        texts.add(text + "$");
//        this.curText = text + "$";
//        this.build();
//    }

    /**
     * SuffixTree的空参数构造器
     * */
    public SuffixTree(){

    }

    public void addText(String text, String location) {
        //重置上下文状态
        remainder = 0;
        activePoint = new ActivePoint(root, -1, -1);
        spilt = false;

        texts.add(text + "$");
        this.locations.add(location);
        this.curText = text + "$";
        this.build();
        //this.merge();
        //this.getEquivalenceClass();
    }

    public void resetRoot() {
        root = new Node();
    }

    /**
     * SuffixTree的节点
     * */
    public class Node {
        //所包含的字符串开始的位置
        public Index left;
        //所包含的字符串结束的位置
        public Index right;
        //所有的子节点
        public ArrayList<Node> subs = new ArrayList<>();
        //SuffixLink
        public Node suffixNode = null;
        //文本列表
        public HashSet<Integer> equi;
        //引用文本
        public int ref = -1;
        //唯一标识符
        public int flag = 0;

        //两个构造方法
        public Node(){
            this.flag = count++;
        };
        public Node(Index left, Index right){
            this.left = left;
            this.right = right;
            this.flag = count++;
            this.equi = new HashSet<>();
            this.equi.add(texts.size() - 1);
            this.ref = texts.size() - 1;
        }


        @Override
        public String toString() {
            if (this.left == null) {
                return "root";
            }
            return texts.get(this.ref).substring(left.cur, right.cur + 1) + flag + "|" + equi;
        }
    }

    /**
     *活动点"active point"，即文中的三元组(active_node,active_edge,active_length)
     * */
    private class ActivePoint {
        //活动节点
        public Node active_node;
        //用来索引active_node下的某一个节点
        public int active_edge;
        public int active_length;

        public ActivePoint(Node active_node, int active_edge, int active_length) {
            this.active_node = active_node;
            this.active_edge = active_edge;
            this.active_length = active_length;
        }
    }

    /**
     * 指向text中某一个位置的指针
     * */
    private class Index {
        public int cur;
        public Index(){}
        public Index(int cur) {
            this.cur = cur;
        }
    }

    /**
     * 构建后缀树
     * */
    private void build(){
        Index index = new Index();
        index.cur = 0;
        while (index.cur < curText.length()) {
            System.out.println("Tree Structure before index++");

            print();

            //需要插入的下一个字符
            char insert = curText.charAt(index.cur);

            //如果一个新添加的字符已经存在，直接往后运行
            System.out.println("在Index相应增加的时候查找待插入字符是否存在");
            if (find(insert)) {
                remainder++;
                index.cur++;
                continue;
            }

            spilt = true;
            //如果这个字符不存在的话👀
            if (remainder == 0) { //只需要插入当前字符
                //当remainder为0的时候肯定是在根节点上
                Node newNode = new Node(new Index(index.cur), index);
                activePoint.active_node.subs.add(newNode);
            } else { //还需要处理之前的步骤留下来的后缀们
                remainder++;
                innerSplit(index, null);
            }
            index.cur++;
        }
        index.cur--;
    }

    /*
    * 处理剩余的插入后缀
    * */
    private void innerSplit(Index index, Node prefixNode) {
        System.out.println();
        System.out.println();
        System.out.println("********Inner Split with index: " + index.cur + "********* prefixNode: " + prefixNode);
        System.out.println("Deal With remainder: " + remainder);
        System.out.println("Active Point before Insertion: ");
        printActivePoint();

        char insert = curText.charAt(index.cur);
        System.out.println("在递归插入的流程中寻找待插入字符是否存在");
        if (find(insert)) {
//            if (remainder == curText.length()) {
//                if (activePoint.active_edge == -1) {
//                    activePoint.active_node.equi.add(texts.size() - 1);
//                } else {
//                    activePoint.active_node.subs.get(activePoint.active_edge).equi.add(texts.size() - 1);
//                }
//            }
            System.out.println("待插入后缀： " +  insert + " 已找到，暂且退出递归");
            return;
        }

        if ( activePoint.active_length == -1) {
            Node insertNode = new Node(new Index(index.cur), index);
            activePoint.active_node.subs.add(insertNode);
            prefixNode = null;
        } else {
            Node splitNode = activePoint.active_node.subs.get(activePoint.active_edge);
            if (activePoint.active_length < splitNode.right.cur) {
                System.out.println("开始split.........");

                //把原来的一个节点分割成两个节点
                //Node newNode = new Node(new Index(splitNode.left.cur + activePoint.active_length + 1), splitNode.right);
                Node newNode = new Node(splitNode.left, new Index(splitNode.left.cur + activePoint.active_length));
                newNode.ref = splitNode.ref;
                //splitNode.right = new Index(splitNode.left.cur + activePoint.active_length);
                splitNode.left = new Index(splitNode.left.cur + activePoint.active_length + 1);
                System.out.println("插入用来分裂的新节点："  + newNode);

                if (prefixNode != null) prefixNode.suffixNode = newNode;

                //newNode.subs = splitNode.subs;
                //newNode.suffixNode = splitNode.suffixNode;
                newNode.subs = new ArrayList<>();
                //splitNode.subs.add(newNode);
                newNode.subs.add(splitNode);
                //splitNode.suffixNode = null;
                activePoint.active_node.subs.remove(splitNode);
                activePoint.active_node.subs.add(newNode);
                //插入需要插入的新节点
                Node insertNode = new Node(new Index(index.cur), index);
                newNode.subs.add(insertNode);

                prefixNode = newNode;
            }
        }
        //减少remainder
        remainder--;

        if (remainder == 0) {
            print();
            System.out.println("********递归结束: " + index.cur + "*********");
            System.out.println("Active Point after Insertion: ");
            printActivePoint();
            System.out.println();
            System.out.println();
            System.out.println();
            return;
        }


        System.out.println("插入完成，检测sufffixNode: " + activePoint.active_node.suffixNode);
        //节点已经插入完毕，根据规则一和规则三对ActiveNode进行处理
        if (activePoint.active_node == root) {
            System.out.println("活动点是root......");
            activePoint.active_length--;
            activePoint.active_edge = -1;
            System.out.println("..........Find Index: " + (index.cur - remainder + 1));
            char newIndex = curText.charAt(index.cur - remainder + 1);
            for (int i = 0; i < activePoint.active_node.subs.size(); i++) {
                Node cur = activePoint.active_node.subs.get(i);
                if (texts.get(cur.ref).charAt(cur.left.cur) == newIndex) {
                    activePoint.active_edge = i;
                    break;
                }
            }
            dealWithActiveNodeTrans(index);
        } else if (activePoint.active_node.suffixNode == null) {
            System.out.println("活动点: " + texts.get(activePoint.active_node.ref).charAt(activePoint.active_node.left.cur ) + " suffix为空......");
            activePoint.active_node = root;
            activePoint.active_length = index.cur - (index.cur - remainder + 1) - 1;
            System.out.println("newIndex's index: " + (index.cur - remainder + 1));
            char newIndex = curText.charAt(index.cur - remainder + 1);
            activePoint.active_edge = -1;
            for (int i = 0; i < activePoint.active_node.subs.size(); i++) {
                Node cur = activePoint.active_node.subs.get(i);
                if (texts.get(cur.ref).charAt(cur.left.cur) == newIndex) {
                    activePoint.active_edge = i;
                    break;
                }
            }
            dealWithActiveNodeTrans(index);
        } else {
            System.out.println("sssssssssss --- follow suffix link");
            activePoint.active_node = activePoint.active_node.suffixNode;
            int preIndex = index.cur - remainder + 1;
            int insetLength = index.cur - preIndex + 1;
            int impPrefix = insetLength - 1 - (activePoint.active_length + 1);
            int charIndex = preIndex + impPrefix;
            char newIndex = curText.charAt(charIndex);
            //System.out.println("Char to insert: " + newIndex + " And next char to Insert " + text.charAt(i + 1));
            for (int j = 0; j < activePoint.active_node.subs.size(); j++) {
                Node cur = activePoint.active_node.subs.get(j);
                if (texts.get(cur.ref).charAt(cur.left.cur) == newIndex) {
                    activePoint.active_edge = j;
                    break;
                }
            }

            //dealWithActiveNodeTrans(index);
            int sub = 0;
            while (activePoint.active_edge >= 0) {
                System.out.println("在处理边长度不够的情况 length = " + activePoint.active_length);

                Node edg = activePoint.active_node.subs.get(activePoint.active_edge);
                int length = activePoint.active_length;
                System.out.println("Edg: " + edg + ": " + texts.get(edg.ref).substring(edg.left.cur, edg.right.cur + 1));
                if (edg.right.cur - edg.left.cur < length) {
                    System.out.println("边长度不够的时候有剩余的往前跳");
                    activePoint.active_node = edg;
                    activePoint.active_length -= edg.right.cur - edg.left.cur + 1;
                    sub += edg.right.cur - edg.left.cur + 1;
                    activePoint.active_edge = -1;
                } else if (edg.right.cur - edg.left.cur == length) {
                    System.out.println("边长度恰好的时候往前跳");
                    activePoint.active_node = edg;
                    activePoint.active_length = -1;
                    activePoint.active_edge = -1;
                    break;
                } else {
                    System.out.println("边长足够，不跳");
                    break;
                }

                char find = curText.charAt(charIndex + sub);
                System.out.println("边长度不够，前进一个节点，下一个被查找的字符是：" + find + " remainder是：" + remainder + " index cur是： " + index.cur);
                for (int j = 0; j < activePoint.active_node.subs.size(); j++) {
                    Node cur = activePoint.active_node.subs.get(j);
                    if (texts.get(cur.ref).charAt(cur.left.cur) == find) {
                        activePoint.active_edge = j;
                        break;
                    }
                }

            }
        }




        System.out.println("Tree Structure after insertion");
        print();
        System.out.println("Active Point after Insertion: ");
        printActivePoint();

        System.out.println("********Done Split with index: " + index.cur + "*********");
        System.out.println();
        System.out.println();
        System.out.println();

        innerSplit(index, prefixNode);

    }

    /**
     * 处理活动点转移的时候活动边长度不够的问题
     * */
    private void dealWithActiveNodeTrans(Index index) {
        //处理新边长度不够的情况
        int sub = 0;
        while (activePoint.active_edge >= 0 && activePoint.active_length >= 0) {
            System.out.println("在处理边长度不够的情况 length = " + activePoint.active_length);

            Node edg = activePoint.active_node.subs.get(activePoint.active_edge);
            int length = activePoint.active_length;
            System.out.println("Edg: " + edg + ": " + texts.get(edg.ref).substring(edg.left.cur, edg.right.cur + 1));
            if (edg.right.cur - edg.left.cur < length) {
                System.out.println("边长度不够的时候有剩余的往前跳");
                activePoint.active_node = edg;
                activePoint.active_length -= edg.right.cur - edg.left.cur + 1;
                sub += edg.right.cur - edg.left.cur + 1;
                activePoint.active_edge = -1;
            } else if (edg.right.cur - edg.left.cur == length) {
                System.out.println("边长度恰好的时候往前跳");
                activePoint.active_node = edg;
                activePoint.active_length = -1;
                activePoint.active_edge = -1;
                break;
            } else {
                System.out.println("边长足够，不跳");
                break;
            }

            char find = curText.charAt(index.cur - remainder + 1 + sub);
            System.out.println("边长度不够，前进一个节点，下一个被查找的字符是：" + find + " remainder是：" + remainder + " index cur是： " + index.cur);
            for (int i = 0; i < activePoint.active_node.subs.size(); i++) {
                Node cur = activePoint.active_node.subs.get(i);
                if (texts.get(cur.ref).charAt(cur.left.cur) == find) {
                    activePoint.active_edge = i;
                    break;
                }
            }

        }
    }

    /**
     * 检测当前需要插入的字符是否已经被隐式包含了
     * */
    private boolean find(char c) {
        System.out.println("检测当前需要插入的字符是否已经被隐式包含了 Find character " + c);
        if (activePoint.active_edge == -1) {
            for (int i = 0; i < activePoint.active_node.subs.size(); i++) {
                Node curNode = activePoint.active_node.subs.get(i);
                String textToCompare = texts.get(curNode.ref);
                System.out.println("TextToCompare: " + textToCompare + "Index: " + curNode.left.cur);
                if (textToCompare.charAt(curNode.left.cur) == c) {
                    activePoint.active_edge = i;
                    activePoint.active_length = 0;
                    if (c == '$' && !spilt) {
                        int number = texts.size() - 1;
                        activePoint.active_node.subs.get(activePoint.active_edge).equi.add(number);
                    }
                    if (curNode.left.cur + activePoint.active_length == curNode.right.cur) {
                        activePoint.active_node = curNode;
                        activePoint.active_edge = -1;
                        activePoint.active_length -= curNode.right.cur - curNode.left.cur + 1;
                        System.out.println("往前跳一个节点 + " + activePoint.active_length);
                        if (activePoint.active_node.suffixNode != null && activePoint.active_node.suffixNode.left != null) {
                            System.out.println("节点的suffix: " + texts.get(activePoint.active_node.suffixNode.ref).charAt(activePoint.active_node.suffixNode.left.cur));
                        }
                    }
//                    if (c == '$') {
//                        activePoint.active_node.positions.add(texts.size() - 1);
//                    }
                    return true;
                }
            }
        } else {
            Node curNode = activePoint.active_node.subs.get(activePoint.active_edge);
            if (curNode.left.cur + activePoint.active_length == curNode.right.cur) {
                for (int i = 0; i < curNode.subs.size(); i++) {
                    Node subNode = curNode.subs.get(i);
                    if (texts.get(subNode.ref).charAt(subNode.left.cur) == c) {
                        activePoint.active_node = curNode;
                        activePoint.active_edge = i;
                        activePoint.active_length = 0;
                        System.out.println("往前跳一个节点");
                        if (c == '$' && !spilt) {
                            int number = texts.size() - 1;
                            activePoint.active_node.subs.get(activePoint.active_edge).equi.add(number);
                        }
                        return true;
                    }
                }
            } else {
                if (texts.get(curNode.ref).charAt(curNode.left.cur + activePoint.active_length + 1) == c) {
                    activePoint.active_length++;
                    if (curNode.left.cur + activePoint.active_length == curNode.right.cur) {
                        activePoint.active_node = curNode;
                        activePoint.active_edge = -1;
                        activePoint.active_length -= curNode.right.cur - curNode.left.cur + 1;
                        System.out.println("往前跳一个节点");
                    }
                    if (c == '$' && !spilt) {
                        int number = texts.size() - 1;
                        activePoint.active_node.equi.add(number);
                    }
                    return true;
                }
            }
        }
        System.out.println("Character " + c + "  misMatch");
        return false;
    }

    /**
     * 对等价类进行去重和合并
     * */
    private void merge() {
        ArrayList<HashSet<String>> temp = new ArrayList<>();
        for (HashSet<String> set: nameSetSet) {
            temp.add(set);
        }
        for (HashSet<String> set: temp) {
            nameSetSet.remove(set);
        }

        boolean[] marked = new boolean[temp.size()];

        HashSet<HashSet<String>> set = new HashSet<>();
        for (int i = 0; i < temp.size(); i++) {
            if (marked[i]) {
                continue;
            }
            marked[i] = true;
            HashSet<String> cur = temp.get(i);
            for (int j = i + 1; j < temp.size(); j++) {
                if (marked[j]) {
                    continue;
                }
                boolean needMerge = false;
                for (String s : temp.get(j)) {
                    if (cur.contains(s)) {
                        needMerge = true;
                    }
                }
                if (!needMerge) {
                    continue;
                }
                marked[j] = true;
                for (String s: temp.get(j)) {
                    cur.add(s);
                }
            }
            set.add(cur);
        }
        nameSetSet = set;
    }

    /**
     * 输出等价类的最终结果
     * */
    public void printBidirectional() {
        System.out.println("双向比较之后的结果");
        merge();
        for (HashSet<String> set : nameSetSet) {
            if (set.size() > 1) {
                String m = "";
                for (String s: set) {
                    m += s;
                }
                System.out.println(m);
            }
        }
    }

    /**
     * 输出等到的等价类
     * */
    public void printEquivalenceClass() {
        System.out.println("\n\n\n\n\n");
        System.out.println("以下是找到的等价类");

        ArrayList<Node> list = new ArrayList<>(root.subs);
        ArrayList<Node> temp = new ArrayList<>();
        int index = 0;

        while (true) {
            for (Node n: list) {
                for (Node sub : n.subs) {
                    temp.add(sub);
                }
                if ( index > 0 && n.equi.size() > 1) {
                    //说明是叶子节点，输出等价类
                    HashSet<String> nameSet = new HashSet<>();
//                    String ec = "";
//                    for (int p: n.equi) {
//                        ec += locations.get(p);
//                    }
//                    System.out.println(ec);

                    for (int p : n.equi) {
                        nameSet.add(locations.get(p));
                    }
                    if (nameSet.size() <= 1) {
                        continue;
                    }
                    String ec = "";
                    for (String s : nameSet) {
                        ec += s;
                    }
                    System.out.println(ec);
                }
            }
            if (temp.size() == 0) break;
            list = temp;
            temp = new ArrayList<>();
            index++;
        }
    }

    /**
     * 获取等价类
     * */
    public void getEquivalenceClass(){
        //ArrayList<String> ret = new ArrayList<>();
        ArrayList<Node> list = new ArrayList<>(root.subs);
        ArrayList<Node> temp = new ArrayList<>();
        int index = 0;

        while (true) {
            for (Node n : list) {
                for (Node sub : n.subs) {
                    temp.add(sub);
                }
                if (index > 0 && n.equi.size() > 1) {
                    //说明是叶子节点，输出等价类
                    HashSet<String> nameSet = new HashSet<>();

                    for (int p : n.equi) {
                        nameSet.add(locations.get(p));
                    }
                    if (nameSet.size() <= 1) {
                        continue;
                    }
                    nameSetSet.add(nameSet);
                }
            }
            if (temp.size() == 0) break;
            list = temp;
            temp = new ArrayList<>();
            index++;
        }

    }

    /**
     * 等价类的划分：把有关系的字符串们划分到一处
     * */


    /**
     * 格式化打印出整个后缀树
     * 层次遍历，按照层次打印
     */
    public void print() {
        System.out.println("---------Tree Structure---------");
        ArrayList<Node> list = new ArrayList<>(root.subs);
        ArrayList<Node> temp = new ArrayList<>();
        int line = 1;
        while (true) {
            for (Node n : list) {
                if (n.left != null && n.right != null) System.out.print(n.toString()  + " " + n.suffixNode + "(" + line + ")    ");
                for (Node sub : n.subs) {
                    temp.add(sub);
                    System.out.print("|-" + sub);
                }
                System.out.print("    ");
//              System.out.println("text:" + text);
//              System.out.println("n: " + n);
//              System.out.println("left: " + n.left.cur);
//              System.out.println("right: " + n.right.cur);
                //System.out.println("left: " + n.left.cur + " right: " + n.right.cur + " ");

            }
            System.out.println();
            if (temp.size() == 0) break;
            list = temp;
            temp = new ArrayList<>();
            line++;
        }

    }

    /*
    * 打印当前的ActivePoint
    * **/
    public void printActivePoint() {
        System.out.println("@@@@@@@@@ Current activePoint structure @@@@@@@@@");
        System.out.println("ActiveNode: " + activePoint.active_node.toString() + "  ");
        System.out.println("ActiveLength: " + activePoint.active_length );
        if (activePoint.active_edge == -1) {
            System.out.println("ActiveEdg: " + activePoint.active_edge);
        } else {
            Node edg = activePoint.active_node.subs.get(activePoint.active_edge);
            System.out.println("ActiveEdg: " + texts.get(edg.ref).substring(edg.left.cur, edg.right.cur + 1));
        }
        if (activePoint.active_node.suffixNode != null) {
            System.out.println("Has Suffix:  " + activePoint.active_node.suffixNode);
        }
        System.out.println("@@@@@@@@@ end activePoint structure @@@@@@@@@");

    }

    //测试方法
    // aasasasaa pass
    // abbbaabbb pass
    // abcabxabcd pass
    // aaabaaabaaab pass
    // aaaaa pass
    // abababab pass
    // aaaabbbbaaaabbbbbbbb pass
    public static void main(String[] args){

        /**
         * 注意一点，有的后缀没有被加进去
         * */
        String[] tests = {    "CompoundStmtDeclStmtDeclStmtReturnStmtIntegerLiteralIntegerLiteralBinaryOperatorImplicitCastExprImplicitCastExprImplicitCastExprDeclRefExprDeclRefExprDeclRefExpr", "CompoundStmtDeclStmtDeclStmtReturnStmtIntegerLiteralIntegerLiteralBinaryOperatorImplicitCastExprImplicitCastExprImplicitCastExprDeclRefExprDeclRefExprDeclRefExpr",     "aaa",     "aaa",     "aaabaaa",  "aasasasaa",   "aaaabbbbaaaabbbbbbbb"};
        String[] locations = {"method1",   "method2", "method3", "method4", "method5",  "method6",     "method7"};
        SuffixTree suffixTree = new SuffixTree();
        for (int i = 0; i < tests.length; i++) {
            System.out.println("Add text : " + tests[i]);
            suffixTree.addText(tests[i], locations[i]);
            System.out.println();
            System.out.println("Added text "+  tests[i] + "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ remainder: " + suffixTree.remainder);
        }

        suffixTree.getEquivalenceClass();

        suffixTree.resetRoot();
        for (int i = tests.length - 1; i >= 0; i--) {
            System.out.println("Add text : " + tests[i]);
            suffixTree.addText(tests[i], locations[i]);
            System.out.println();
            System.out.println("Added text "+  tests[i] + "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ remainder: " + suffixTree.remainder);
        }

        suffixTree.getEquivalenceClass();

        suffixTree.merge();

        suffixTree.printBidirectional();
    }
}

