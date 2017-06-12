import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.*;


public class Main {
    public static void main(String[] args) throws Exception {
        String[] fileName = new String[5];

        Table table = new Table();

        for (int i = 0; i < 5; i++) {
            fileName[i] = "HKIB-20000_00" + (i + 1) + ".txt";
        }

        String[] writeText = new String[8];
        HashMap<String, Integer> map = new HashMap<String, Integer>();

        map.put("건강과 의학", 0);
        map.put("경제", 1);
        map.put("과학", 2);
        map.put("교육", 3);
        map.put("문화와 종교", 4);
        map.put("사회", 5);
        map.put("산업", 6);
        map.put("여가생활", 7);


        int[] countC = new int[8];
        for(int i = 0; i < 8; i++){
            countC[i]  = 0;
        }

        int count = 0;

        for (int T = 0; T < 4; T++) {

            Scanner sc = new Scanner(new FileInputStream(fileName[T]));
            //  Scanner sc = new Scanner(new FileInputStream("sample_input.txt"));

            String temp;
            String line = "";
            int cat = -1;

            while (sc.hasNext()) {

                temp = sc.nextLine();

                if (temp.contains("<KW>")) {
                    while (!(temp = sc.nextLine()).contains("@")) {
                        if (!sc.hasNext()) {

                            // document[cat].setText(line);
                            List<String> result = table.getSplit(line);
                            for (int i = 0; i < result.size(); i++) {
                                table.addCount(cat, result.get(i));
                            }
                            line = "";
                            count++;
                            temp = "";
                            break;
                        }
                    }

                }
                if (temp.contains("<html>")) {
                    while (!(temp = sc.nextLine()).contains("</html>")) {
                    }
                    temp = sc.nextLine();
                }
                if (temp.contains("<a>")) {
                    while (!(temp = sc.nextLine()).contains("</a>")) {
                    }
                    temp = sc.nextLine();
                }
                if (temp.contains("<!-search")) {
                    int index = temp.indexOf(">");
                    temp = temp.substring(index + 1, temp.length());
                }

                if (temp.contains("@")) {
                    //document[cat].setText(line);
                    if(cat != -1) {

                        List<String> result = table.getSplit(line);

                        for (int i = 0; i < result.size(); i++) {
                            table.addCount(cat, result.get(i));
                        }

                        //   System.out.println(count);
                        count++;
                        line = "";
                    }
                    while (!(temp = sc.nextLine()).contains("TEXT")) {
                        if (temp.contains("CAT'03")) {
                            String[] split = temp.split("/");
                            cat = map.get(split[1]);
                            countC[cat] ++;

                        }
                        continue;
                    }
                } else {
                    int tempIndex = 0;

                    if (temp.length() != 0) {
                        for (tempIndex = 0; tempIndex < temp.length(); tempIndex++) {
                            if (temp.charAt(tempIndex) != ' ') {
                                break;
                            }
                        }
                    }

                    for (int i = tempIndex; i < temp.toCharArray().length; i++) {
                        char c = temp.charAt(i);
                        if (c == '\n')
                            line += "";
                        if (c == '-' || c == ',' || c == '/' || c == '.' || c == '<' || c == '>' || c == '『' || c == '』' ||
                                c == '"' || c == '"' || c == '[' || c == ']' || c == '{' || c == '}') {
                            line += " ";
                        } else {
                            line += temp.charAt(i);
                        }
                    }
                }
            }
            sc.close();
        }

        for(int i = 0 ; i < 8; i++){
            System.out.println(i +":"+countC[i]);
        }

        FileWriter fw = null;
        int index = table.getIndex();
        int[][] t = table.getTable();

        HashMap<Integer, String> m = table.getItoSmap();





        ChiSquare chi = new ChiSquare(index, countC);
        chi.setTable(t);
        PriorityQueue<Feature> result = chi.getChiResult();
        Feature[] features = new Feature[index];

        HashMap<String, Integer> resultMap = new HashMap<>();
        int newIndex = 0;

        while (!result.isEmpty()) {

            Feature f = result.remove();
            f.setNewIndex(newIndex);
            features[newIndex] = f;

            resultMap.put(m.get(f.getIndex()), newIndex);
            newIndex++;

        }

        String trainFile = "train.txt";
        String testFile = "test.txt";

        MakeTrain extract = new MakeTrain(features, resultMap);

        fw = new FileWriter(trainFile, true);

        for(int T = 0; T < 5; T++){
            int catNum = -1;
            Scanner sc = new Scanner(new FileInputStream(fileName[T]));
            if(T == 4){
                fw.close();
                fw = null;
                fw = new FileWriter(testFile, true);
            }
            String line = "";
            String temp = "";

            while (sc.hasNext()) {

                temp = sc.nextLine();
                //System.out.println("1");

                if (temp.contains("<KW>")) {
                    while (!(temp = sc.nextLine()).contains("@")) {
                        if (!sc.hasNext()) {

                            // document[cat].setText(line);
                            PriorityQueue<Feature> FeatureQueue = extract.getTrainOfDocument(line);

                            fw.write((catNum+1)+" ");
                            fw.flush();

                            Feature[] fList = new Feature[50000];
                            int i = 0;

                            while(!FeatureQueue.isEmpty()) {
                                fList[i] =FeatureQueue.remove();
                                i++;
                            }
                            
                            int tF;
                            for(int j = 0; j < i; j++){
                                for(int l = 0; l < i; l++){

                                    if(fList[j].getNewIndex() < fList[l].getNewIndex()){
                                        tF = fList[l].getNewIndex();
                                        fList[l].setNewIndex(fList[j].getNewIndex());
                                        fList[j].setNewIndex(tF);
                                    }
                                }
                            }

                            for(int j = 0; j < i; j++) {
                                fw.write((fList[j].getNewIndex()+1) +":"+fList[j].getWeight()+" ");
                                fw.flush();
                            }
                            fw.write("\n");
                            fw.flush();
                            line = "";
                            count++;
                            temp = "";
                            break;
                        }
                    }

                }
                if (temp.contains("<html>")) {
                    while (!(temp = sc.nextLine()).contains("</html>")) {
                    }
                    temp = sc.nextLine();
                }
                if (temp.contains("<a>")) {
                    while (!(temp = sc.nextLine()).contains("</a>")) {
                    }
                    temp = sc.nextLine();
                }
                if (temp.contains("<!-search")) {
                    int sub = temp.indexOf(">");
                    temp = temp.substring(sub + 1, temp.length());
                }

                if (temp.contains("@")) {
                    //document[cat].setText(line);
                    PriorityQueue<Feature> FeatureQueue = extract.getTrainOfDocument(line);

                    if(catNum != -1) {

                        fw.write((catNum+1) + " ");
                        fw.flush();


                        Feature[] fList = new Feature[50000];
                        int i = 0;
                        while (!FeatureQueue.isEmpty()) {
                            fList[i] = FeatureQueue.remove();
                            i++;
                        }
                        int tF;
                        for (int j = 0; j < i; j++) {
                            for (int l = 0; l < i; l++) {

                                if (fList[j].getNewIndex() < fList[l].getNewIndex()) {
                                    tF = fList[l].getNewIndex();
                                    fList[l].setNewIndex(fList[j].getNewIndex());
                                    fList[j].setNewIndex(tF);
                                }
                            }
                        }

                        for (int j = 0; j < i; j++) {
                            fw.write((fList[j].getNewIndex()+1) + ":" + fList[j].getWeight() + " ");
                            fw.flush();
                        }
                        fw.write("\n");
                        fw.flush();

                        count++;
                        line = "";
                    }
                    while (!(temp = sc.nextLine()).contains("TEXT")) {
                        if (temp.contains("CAT'03")) {
                            String[] split = temp.split("/");
                            catNum = map.get(split[1]);
                        }
                        continue;
                    }
                } else {
                    int tempIndex = 0;

                    if (temp.length() != 0) {
                        for (tempIndex = 0; tempIndex < temp.length(); tempIndex++) {
                            if (temp.charAt(tempIndex) != ' ') {
                                break;
                            }
                        }
                    }

                    for (int i = tempIndex; i < temp.toCharArray().length; i++) {
                        char c = temp.charAt(i);
                        if (c == '\n')
                            line += "";
                        if (c == '-' || c == ',' || c == '/' || c == '.' || c == '<' || c == '>' || c == '『' || c == '』' ||
                                c == '"' || c == '"' || c == '[' || c == ']' || c == '{' || c == '}') {
                            line += " ";
                        } else {
                            line += temp.charAt(i);
                        }
                    }
                }
            }
            sc.close();

        }
        fw.close();
    }

}

