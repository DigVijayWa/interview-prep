package preparation.dsa;

import java.util.Arrays;

public class RemoveDuplicates {
    public int removeDuplicates(int[] nums) {
        int max = nums.length;
        for(int i=1; i<max; i++) {
            if(nums[i] == nums[i-1]) {
                int temp = nums[i];
                int index = i;
                while(i < max && temp == nums[i]) {
                    i++;
                    count++;
                }
                int step = i;
                while(index < max && step < max) {
                    nums[index++] = nums[step++];
                }
                nums[index] = temp;

            }
        }

        System.out.println(Arrays.toString(nums));
        return 0;
    }

    public static void main(String[] args) {
            RemoveDuplicates rd = new RemoveDuplicates();

        // Test 1: No duplicates
        int[] arr1 = {1, 2, 3, 4, 5};
        rd.removeDuplicates(arr1);

        // Test 2: All duplicates
        int[] arr2 = {2, 2, 2, 2, 2};
        rd.removeDuplicates(arr2);

        // Test 3: Some duplicates
        int[] arr3 = {1, 1, 2, 3, 3, 4, 5, 5};
        rd.removeDuplicates(arr3);

        // Test 4: Empty array
        int[] arr4 = {};
        rd.removeDuplicates(arr4);

        // Test 5: Single element
        int[] arr5 = {7};
        rd.removeDuplicates(arr5);

        // Test 6: Duplicates at the end
        int[] arr6 = {1, 2, 3, 4, 4, 4};
        rd.removeDuplicates(arr6);

        // Test 7: Duplicates at the start
        int[] arr7 = {5, 5, 6, 7, 8};
        rd.removeDuplicates(arr7);
    }
}
