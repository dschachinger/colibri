package Utils;

import java.util.*;

/**
 * This class is a List implementation which sorts the elements by its date interval.
 *
 * @param <T>
 */
public class SortedDateIntervalList<T> extends LinkedList<Pair<Pair<Date, Date>, T>> {
    /**
     * Comparator used to sort the list.
     */
    private IntervalComparator comparator = null;
    /**
     * Construct a new instance with the list elements sorted in their
     * {@link java.lang.Comparable} natural ordering.
     */
    public SortedDateIntervalList() {
        comparator = new IntervalComparator();
        comparator.setMode(0);
    }

    /**
     * Add a new entry to the list. The insertion point is calculated using the
     * comparator.
     *
     * @param paramT
     */
    @Override
    public boolean add(Pair<Pair<Date, Date>, T> paramT) {
        comparator.setMode(0);
        int insertionPoint = Collections.binarySearch(this, paramT, comparator);
        super.add((insertionPoint > -1) ? insertionPoint : (-insertionPoint) - 1, paramT);
        return true;
    }

    /**
     * Check, if this list contains the given Element. This is faster than the
     * {@link #contains(Object)} method, since it is based on binary search.
     *
     * @return <code>true</code>, if the element is contained in this list;
     * <code>false</code>, otherwise.
     */
    public List<Pair<Pair<Date, Date>, T>> getAllFittingIntervals(Pair<Date, Date> interval) {

        int lowerIndex;
        if(interval.getFst() != null) {
            comparator.setMode(0);
            lowerIndex = Collections.binarySearch(this, new Pair<Pair<Date, Date>, T>(interval, null), comparator);
            if (lowerIndex < 0) {
                lowerIndex = Math.abs(lowerIndex) - 1;
            }
        } else{
            lowerIndex = 0;
        }

        int upperIndex;
        if(interval.getSnd() != null) {
            comparator.setMode(1);
            upperIndex = Collections.binarySearch(this, new Pair<Pair<Date, Date>, T>(interval, null), comparator);
            if (upperIndex < 0) {
                upperIndex = Math.abs(upperIndex) - 1;
            } else {
                upperIndex = upperIndex + 1;
            }
        } else {
            upperIndex = this.size();
        }

        if(lowerIndex == -1)
            lowerIndex = 0;
        if(upperIndex > this.size())
            upperIndex = this.size();
        return this.subList(lowerIndex,upperIndex);
    }


    /**
     * It is only allowed that a search interval overlaps with other intervals.
     * All the inserted intervals should not overlap.
     */
    private class IntervalComparator implements Comparator<Pair<Pair<Date, Date>, T>> {
        int mode = 0;   //-1...lower bound mode, 0...normal mode, 1...upper bound mode

        public void setMode(int mode) {
            this.mode = mode;
        }

        @Override
        public int compare(Pair<Pair<Date, Date>, T> o1, Pair<Pair<Date, Date>, T> o2) {
            Pair<Pair<Date,Date>,T> con_o1 = ((Pair<Pair<Date,Date>,T>)o1);
            Pair<Pair<Date,Date>,T> con_o2 = ((Pair<Pair<Date,Date>,T>)o2);

            Date startDate1 = con_o1.getFst().getFst();
            Date startDate2 = con_o2.getFst().getFst();

            Date endDate1 = con_o1.getFst().getSnd();
            Date endDate2 = con_o2.getFst().getSnd();

            if(mode == 0) {
                if(startDate1.equals(startDate2)){
                    return 0;
                }

                return startDate1.before(startDate2) ? -1 : 1;
            } else {
                if(endDate1.equals(endDate2)){
                    return 0;
                }
                return endDate1.before(endDate2) ? -1 : 1;

            }
        }
    };
}
