import java.util.ArrayList;


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
    private String text;
    //SuffixTree的根结点
    private Node root = new Node();
    //活动点
    private ActivePoint activePoint = new ActivePoint(root, -1, -1);
    //在当前build步骤还需要插入多少节点
    private int remainder = 0;
    //当前节点数统计
    private int count = 0;

    /**
     * SuffixTree的构造器，传入需要用来建树的文本
     * */
    public SuffixTree(String text) {
        this.text = text + "$";
        this.build();
    }

    /**
     * SuffixTree的节点
     * */
    private class Node {
        public Index left;
        public Index right;
        public ArrayList<Node> subs = new ArrayList<>();
        public Node suffixNode = null;
        public int flag = 0;

        //两个构造方法
        public Node(){
            this.flag = count++;
        };
        public Node(Index left, Index right){
            this.left = left;
            this.right = right;
            this.flag = count++;
        }


        @Override
        public String toString() {
            if (this.left == null) {
                return "root";
            }
            return text.substring(left.cur, right.cur + 1) + flag;
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
        while (index.cur < text.length()) {
            System.out.println("Tree Structure before index++"); {
                print();
            }
            //需要插入的下一个字符
            char insert = text.charAt(index.cur);

            //如果一个新添加的字符已经存在，直接往后运行
            System.out.println("在Index相应增加的时候查找待插入字符是否存在");
            if (find(insert)) {
                remainder++;
                index.cur++;
                continue;
            }

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

        char insert = text.charAt(index.cur);
        System.out.println("在递归插入的流程中寻找待插入字符是否存在");
        if (find(insert)) {
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
                //splitNode.right = new Index(splitNode.left.cur + activePoint.active_length);
                splitNode.left = new Index(splitNode.left.cur + activePoint.active_length + 1);

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
            char newIndex = text.charAt(index.cur - remainder + 1);
            for (int i = 0; i < activePoint.active_node.subs.size(); i++) {
                Node cur = activePoint.active_node.subs.get(i);
                if (text.charAt(cur.left.cur) == newIndex) {
                    activePoint.active_edge = i;
                    break;
                }
            }
            dealWithActiveNodeTrans(index);
        } else if (activePoint.active_node.suffixNode == null) {
            System.out.println("活动点: " + text.charAt(activePoint.active_node.left.cur ) + " suffix为空......");
            activePoint.active_node = root;
            activePoint.active_length = index.cur - (index.cur - remainder + 1) - 1;
            char newIndex = text.charAt(index.cur - remainder + 1);
            activePoint.active_edge = -1;
            for (int i = 0; i < activePoint.active_node.subs.size(); i++) {
                Node cur = activePoint.active_node.subs.get(i);
                if (text.charAt(cur.left.cur) == newIndex) {
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
            char newIndex = text.charAt(charIndex);
            //System.out.println("Char to insert: " + newIndex + " And next char to Insert " + text.charAt(i + 1));
            for (int j = 0; j < activePoint.active_node.subs.size(); j++) {
                Node cur = activePoint.active_node.subs.get(j);
                if (text.charAt(cur.left.cur) == newIndex) {
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
                System.out.println("Edg: " + edg + ": " + text.substring(edg.left.cur, edg.right.cur + 1));
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

                char find = text.charAt(charIndex + sub);
                System.out.println("边长度不够，前进一个节点，下一个被查找的字符是：" + find + " remainder是：" + remainder + " index cur是： " + index.cur);
                for (int j = 0; j < activePoint.active_node.subs.size(); j++) {
                    Node cur = activePoint.active_node.subs.get(j);
                    if (text.charAt(cur.left.cur) == find) {
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
            System.out.println("Edg: " + edg + ": " + text.substring(edg.left.cur, edg.right.cur + 1));
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

            char find = text.charAt(index.cur - remainder + 1 + sub);
            System.out.println("边长度不够，前进一个节点，下一个被查找的字符是：" + find + " remainder是：" + remainder + " index cur是： " + index.cur);
            for (int i = 0; i < activePoint.active_node.subs.size(); i++) {
                Node cur = activePoint.active_node.subs.get(i);
                if (text.charAt(cur.left.cur) == find) {
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
        System.out.println("Find character " + c);
        if (activePoint.active_edge == -1) {
            for (int i = 0; i < activePoint.active_node.subs.size(); i++) {
                Node curNode = activePoint.active_node.subs.get(i);
                if (text.charAt(curNode.left.cur) == c) {
                    activePoint.active_edge = i;
                    activePoint.active_length = 0;
                    if (curNode.left.cur + activePoint.active_length == curNode.right.cur) {
                        activePoint.active_node = curNode;
                        activePoint.active_edge = -1;
                        activePoint.active_length -= curNode.right.cur - curNode.left.cur + 1;
                        System.out.println("往前跳一个节点 + " + activePoint.active_length);
                        if (activePoint.active_node.suffixNode != null && activePoint.active_node.suffixNode.left != null) {
                            System.out.println("节点的suffix: " + text.charAt(activePoint.active_node.suffixNode.left.cur));
                        }
                    }
                    return true;
                }
            }
        } else {
            Node curNode = activePoint.active_node.subs.get(activePoint.active_edge);
            if (curNode.left.cur + activePoint.active_length == curNode.right.cur) {
                for (int i = 0; i < curNode.subs.size(); i++) {
                    Node subNode = curNode.subs.get(i);
                    if (text.charAt(subNode.left.cur) == c) {
                        activePoint.active_node = curNode;
                        activePoint.active_edge = i;
                        activePoint.active_length = 0;
                        System.out.println("往前跳一个节点");
                        return true;
                    }
                }
            } else {
                if (text.charAt(curNode.left.cur + activePoint.active_length + 1) == c) {
                    activePoint.active_length++;
                    if (curNode.left.cur + activePoint.active_length == curNode.right.cur) {
                        activePoint.active_node = curNode;
                        activePoint.active_edge = -1;
                        activePoint.active_length -= curNode.right.cur - curNode.left.cur + 1;
                        System.out.println("往前跳一个节点");
                    }
                    return true;
                }
            }
        }
        System.out.println("Character " + c + "  misMatch");
        return false;
    }

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
                for (Node sub : n.subs) {
                    temp.add(sub);
                }
//              System.out.println("text:" + text);
//              System.out.println("n: " + n);
//              System.out.println("left: " + n.left.cur);
//              System.out.println("right: " + n.right.cur);
                //System.out.println("left: " + n.left.cur + " right: " + n.right.cur + " ");
                if (n.left != null && n.right != null) System.out.print(n.toString()  + " " + n.suffixNode + "(" + line + ")    ");
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
            System.out.println("ActiveEdg: " + text.substring(edg.left.cur, edg.right.cur + 1));
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
        SuffixTree suffixTree = new SuffixTree("aaaabbbbaaaabbbb");
        System.out.println();
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("最终结果： ");
        suffixTree.print();
    }
}

