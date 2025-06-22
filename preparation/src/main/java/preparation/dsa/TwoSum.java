package preparation.dsa;

import java.util.Map;
import java.util.HashMap;

public class TwoSum {
    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> map = new HashMap<>();
        for(int i=0; i<nums.length; i++) {
            if(map.containsKey(target - nums[i])) {
                return new int[] {map.get(target-nums[i]), i};
            }
            map.put(nums[i], i);
        }
        return new int[0];
    }
    public static void main(String[] args) {
        TwoSum ts = new TwoSum();

        // Test 1: Normal case
        int[] result1 = ts.twoSum(new int[]{2, 7, 11, 15}, 9);
        if (result1.length == 2) {
            System.out.println("Test 1 Passed: [" + result1[0] + ", " + result1[1] + "]");
        } else {
            System.out.println("Test 1 Failed");
        }

        // Test 2: No solution
        int[] result2 = ts.twoSum(new int[]{1, 2, 3}, 7);
        if (result2.length == 0) {
            System.out.println("Test 2 Passed: No solution");
        } else {
            System.out.println("Test 2 Failed");
        }

        // Test 3: Negative numbers
        int[] result3 = ts.twoSum(new int[]{-3, 4, 3, 90}, 0);
        if (result3.length == 2) {
            System.out.println("Test 3 Passed: [" + result3[0] + ", " + result3[1] + "]");
        } else {
            System.out.println("Test 3 Failed");
        }

        // Test 4: Duplicate numbers
        int[] result4 = ts.twoSum(new int[]{3, 3}, 6);
        if (result4.length == 2) {
            System.out.println("Test 4 Passed: [" + result4[0] + ", " + result4[1] + "]");
        } else {
            System.out.println("Test 4 Failed");
        }

        // Test 5: Single element
        int[] result5 = ts.twoSum(new int[]{5}, 5);
        if (result5.length == 0) {
            System.out.println("Test 5 Passed: No solution");
        } else {
            System.out.println("Test 5 Failed");
        }
    }
}

