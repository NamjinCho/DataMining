import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class MakeTrain {

    private Feature[] features;

    private HashMap<String, Integer> resultMap;
    private PriorityQueue<Feature> featureList ;

    MakeTrain(Feature[] features, HashMap<String, Integer> map){
        featureList = new PriorityQueue<>();
        this.features = features;
        this.resultMap = map;
    }

    public PriorityQueue<Feature> getTrainOfDocument(String document){

        featureList.clear();
        String[] split = document.split(" ");

        for(int i = 0; i < split.length; i++){

            if(resultMap.get(split[i]) != null){

                int index = resultMap.get(split[i]);
           //     System.out.println(index +":"+features[index]);

                if(!featureList.contains(features[index])){
                    featureList.add(features[index]);
                }

            }

        }

        return featureList;

    }

}
