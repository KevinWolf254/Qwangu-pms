import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

public class PracticalPriorityQueue {

    public static void main(String[] args) {
        // [[2,3],[2,2],[3,3],[1,3],[5,7],[2,2],[4,6]] = [[1,3],[4,7]] [[1,3],[4,6],[5,7]]
        int[][] case1 = {{2, 3}, {2, 2}, {3, 3}, {1, 3}, {5, 7}, {2, 2}, {4, 6}};
        System.out.println(merge(case1));

        // [[0,30],[5,10],[15,20]] = 2
        int[][] caseOne = {{0, 30}, {5, 10}, {15, 20}};
//        System.out.println(calculateNoOfMeetingRooms(caseOne));
//        System.out.println(merge(caseOne));

        // [[7,10],[2,4]] = 1
        int[][] caseTwo = {{7, 10}, {2, 4}};
//        System.out.println(calculateNoOfMeetingRooms(caseTwo));
//        System.out.println(merge(caseTwo));
    }

    public static int calculateNoOfMeetingRooms(int[][] intervals) {
        Arrays.sort(intervals, Comparator.comparingInt((int[] a) -> a[0])); // TC = O(N LOG N), SC = O(1)
        PriorityQueue<Integer> q = new PriorityQueue<>(intervals.length); // TC = O(LOG N), SC = O(N)
        q.add(intervals[0][1]);
        for(int i = 1; i < intervals.length; i++) {
            int start = intervals[i][0];
            int end = intervals[i][1];
            int firstEndTime = q.peek();
            if(start >= firstEndTime) {
                q.poll();
            }
            q.add(end);
        }

        return q.size();
        // TC = O(N LOG N), SC = O(N)
    }

    public static int[][] merge(int[][] intervals) {
        // sort the array by start time in asc
        Arrays.sort(intervals, (int[] a, int[] b) -> a[0] - b[0]);
        // initialize a priority queue that with priority being end time in asc
        PriorityQueue<Integer[]> q = new PriorityQueue<>(intervals.length, (Integer[] a, Integer[] b) -> a[1] - b[1]);
        // retrieve the first interval add it to priority queue
        Integer[] first = {intervals[0][0], intervals[0][1]};
        q.add(first);

        // iterate through the sorted intervals
        for(int i = 1; i < intervals.length; i++) {
            // get current interval
            Integer start = intervals[i][0];
            Integer end = intervals[i][1];

            // get min interval from queue
            Integer firstStart = q.peek()[0];
            Integer lastEnd = q.peek()[1];

            // check if next start time collides with previous end time
            if(start <= lastEnd) {
                // if collide merge the intervals
                q.poll();
                // compare the end time and take the highest
                Integer[] merged = {firstStart, end > lastEnd ? end : lastEnd};
                q.add(merged);
            } else {
                // if no collision add the interval
                Integer[] next = {start, end};
                q.add(next);
            }

        }
        // initialize result
        int[][] result = new int[q.size()][2];

        // iterate through the queue
        Iterator<Integer[]> i = q.iterator();
        int index = 0;

        while(i.hasNext()) {
            // add all intervals to result
            Integer[] current = q.poll();
            result[index][0] = current[0];
            result[index][1] = current[1];
            System.out.println("{ " +current[0]+ ", " +current[1]+ " }" );
            index++;
        }

        return result;
    }
}
