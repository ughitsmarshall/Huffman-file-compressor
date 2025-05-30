/*
 * @author Marshall Carey-Matthews
 * Created 5 May 2024
 * Huffman file compressor
 */

import java.io.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class Compressor implements  Huffman {
    private String filepath;
    private String compressedPath;
    private String decompressedPath;
    private Map<Character, String> codeMap;

    public Compressor(String filepath, String compressedPath, String decompressedPath){
        this.filepath = filepath;
        this.compressedPath = compressedPath;
        this.decompressedPath = decompressedPath;
        codeMap = new HashMap<>();

    }

    public Map<Character, Long> countFrequencies(String pathName) throws IOException{
        BufferedReader in = new BufferedReader(new FileReader(filepath));
        HashMap<Character, Long> frequencyMap = new HashMap<>();
        try {
            int readInt = in.read();
            //while the file is not done basically
            while (readInt != -1){
                char read = (char) readInt;
                if (frequencyMap.containsKey(read)){
                    frequencyMap.put(read, frequencyMap.get(read)+1);
                } else {
                    frequencyMap.put(read, 1L);
                }
                readInt = in.read();
            }
        } catch (Exception e){
            System.out.println("Invalid on Count. " + e.getMessage());
        } finally {
            in.close();
        }
        System.out.println(frequencyMap);
        return frequencyMap;
    };

    public BinaryTree<CodeTreeElement> makeCodeTree(Map<Character, Long> frequencies){
        //we need this comparator to sort by frequency
        class TreeComparator implements Comparator<BinaryTree<CodeTreeElement>> {
            @Override
            public int compare(BinaryTree<CodeTreeElement> e1, BinaryTree<CodeTreeElement> e2) {
                if (e1.getData().getFrequency() > e2.getData().getFrequency()){
                    return 1;
                } else if (e1.getData().getFrequency() < e2.getData().getFrequency()){
                    return -1;
                } else {
                    return 0;
                }
            }
        }
        TreeComparator treeComparator = new TreeComparator();
        PriorityQueue<BinaryTree<CodeTreeElement>> pq = new PriorityQueue<>(treeComparator);
        //add to priority queue
        for (Character key : frequencies.keySet()){
            CodeTreeElement initial = new CodeTreeElement(frequencies.get(key), key);
            BinaryTree<CodeTreeElement> initialTree = new BinaryTree<>(initial);
            pq.add(initialTree);
        }

        //make trees
        while (pq.size() > 1){
            BinaryTree<CodeTreeElement> e1 = pq.remove();
            BinaryTree<CodeTreeElement> e2 = pq.remove();
            BinaryTree<CodeTreeElement> root = new BinaryTree<>(new CodeTreeElement(e1.getData().getFrequency() + e2.getData().getFrequency(), null));
            root.setLeft(e1);
            root.setRight(e2);
            pq.add(root);
        }

        return pq.peek();
    };

    public Map<Character, String> computeCodes(BinaryTree<CodeTreeElement> codeTree){
        if (codeTree == null){ //edge case for empty file because null node can't operate on BT methods
            return codeMap;
        }
        if (codeTree.isLeaf()){ //if the head is a leaf, it can't do the path thing
            codeMap.put(codeTree.getData().getChar(), "1");
            return codeMap;
        } else {

            String path = "";
            System.out.println(codeTree);
            if (codeTree != null) {
                computeCodesHelper(path, codeTree);
            }
            System.out.println(codeMap);
            return codeMap;
        }
    };

    private void computeCodesHelper(String path, BinaryTree<CodeTreeElement> treeNode){
        if (treeNode.isLeaf()){ //only leaves contain character data, so this is when we map
            CodeTreeElement codedChar = treeNode.getData();
            codeMap.put(codedChar.getChar(), path);
        } else {
            if (treeNode.hasLeft()) { //recursively move down
                String newPath = path + "0";
                computeCodesHelper(newPath, treeNode.getLeft());
            }
            if (treeNode.hasRight()) { //recursively move down
                String newPath = path + "1";
                computeCodesHelper(newPath, treeNode.getRight());
            }
        }
    }


    public void compressFile(Map<Character, String> codeMap, String pathName, String compressedPathName) throws IOException{
        BufferedReader in = new BufferedReader(new FileReader(filepath));
        BufferedBitWriter out = new BufferedBitWriter(compressedPath);
        try {
            int readInt = in.read();
            //while the file is not done basically
            while (readInt != -1){
                char read = (char) readInt;
                String code = codeMap.get(read);
                for (int i = 0; i < code.length(); i++){
                    char character = code.charAt(i);
                    out.writeBit(character != '0');
                }
                readInt = in.read();
            }
        } catch (Exception e){
            System.out.println("Invalid on Compress. " + e.getMessage());
        } finally {
            in.close();
            out.close();
        }
    }

    public void decompressFile(String compressedPathName, String decompressedPathName, BinaryTree<CodeTreeElement> codeTree) throws IOException{
        BufferedBitReader in = new BufferedBitReader(compressedPath);
        BufferedWriter out = new BufferedWriter(new FileWriter(decompressedPath));
        try{
            BinaryTree<CodeTreeElement> returner = codeTree; //gets you back to top after you write
            BinaryTree<CodeTreeElement> treeReader = codeTree;
            while (in.hasNext()){
                boolean bit = in.readBit();
                if (bit){
                    treeReader = treeReader.getRight();
                } else {
                    treeReader = treeReader.getLeft();
                }
                //in single character cases you have to do this
                if (treeReader == null){
                    CodeTreeElement node = returner.getData(); //sneaky sneaky!
                    char toWrite = node.getChar();
                    out.write(toWrite);
                    treeReader = returner;
                }
                //in other cases you can do it regularly like this
                else if (treeReader.isLeaf()){
                    CodeTreeElement node = treeReader.getData();
                    char toWrite = node.getChar();
                    out.write(toWrite);
                    treeReader = returner;
                }
            }
        } catch (Exception e){
            System.out.println("Invalid on Decompress. " + e.getMessage());
        } finally {
            in.close();
            out.close();
        }
    };

    public static void main(String[] args) throws IOException {
        String filename = "EmptyTest.txt";

        int period;
        for (period = 0; period < filename.length(); period++){
            if (filename.charAt(period) == '.'){
                break; //this is how you make the filename dynamic
            }
        }

        String filename_compressed = filename.substring(0, period) + "_compressed" + filename.substring(period);
        String filename_decompressed = filename.substring(0, period) + "_decompressed" + filename.substring(period);


        Compressor test = new Compressor("/Users/marshallcarey-matthews/IdeaProjects/cs10/ps3/src/"+filename, "/Users/marshallcarey-matthews/IdeaProjects/cs10/ps3/src/"+filename_compressed, "/Users/marshallcarey-matthews/IdeaProjects/cs10/ps3/src/"+filename_decompressed);
        Map<Character, Long> coolMap = test.countFrequencies(test.filepath);

        BinaryTree<CodeTreeElement> coolTree = test.makeCodeTree(coolMap);
        Map<Character, String> coolCodeMap = test.computeCodes(coolTree);
        test.compressFile(coolCodeMap, test.filepath, test.compressedPath);
        test.decompressFile(test.compressedPath, test.decompressedPath, coolTree);
    }
}
