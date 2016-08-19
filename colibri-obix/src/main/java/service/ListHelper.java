package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This Class is used to perform actions on lists.
 */
public class ListHelper {

    /**
     * Checks if two unordered lists are equal.
     *
     * @param firstList     First list to check.
     * @param secondList    Second list to check.
     * @return              True, if the two unordered lists are equal, otherwise false.
     */
    public static boolean listsUnorderedEqual(List<String> firstList, List<String> secondList){
        if (firstList == null && secondList == null){
            return true;
        }

        if((firstList == null && secondList != null)
                || firstList != null && secondList == null
                || firstList.size() != secondList.size()){
            return false;
        }

        firstList = new ArrayList<String>(firstList);
        secondList = new ArrayList<String>(secondList);

        Collections.sort(firstList);
        Collections.sort(secondList);
        return firstList.equals(secondList);
    }

    /**
     * Checks if a list of objects has duplicates.
     *
     * @param list  The list to check.
     * @return      True, if the list has duplicates, otherwise false.
     */
    public static boolean hasDuplicates(List<Object> list) {
        List<Object> tempList = Collections.synchronizedList(new ArrayList<>());
        for (Object o : list) {
            if(!tempList.contains(o)) {
                tempList.add(o);
            } else {
                return true;
            }
        }
        return false;
    }
}
