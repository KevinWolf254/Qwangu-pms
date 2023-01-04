public class KadaneAlgorithm {

    public static void main(String[] args) {
        int[] array = {-2, 2, 5, -11};

        System.out.println(findMaxContiguousSubArray(array));
    }

    public static int findMaxContiguousSubArray(int[] array) {

        int size = array.length;
        int maxSoFar = Integer.MIN_VALUE;
        int maxEndingHere = 0;

        for (int i = 0; i < size; i++) {
            maxEndingHere = maxEndingHere + array[i];
            if (maxSoFar < maxEndingHere)
                maxSoFar = maxEndingHere;
            if (maxEndingHere < 0)
                maxEndingHere = 0;
        }
        return maxSoFar;
    }
}
