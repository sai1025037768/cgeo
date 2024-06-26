package cgeo.geocaching.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.offset;

public class CommonUtilsTest {

    private String testField = "";

    @Test
    public void nullComparator() {
        final List<String> someList = new ArrayList<>(Arrays.asList("one", null, "two", "three"));
        someList.sort(CommonUtils.getNullHandlingComparator((s1, s2) -> -s1.compareTo(s2), true)); //sort backwards with null as first element
        assertThat(someList).as("List: " + someList).containsExactly(null, "two", "three", "one");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void groupList() {
        //Original Index of test data:          0       1      2        3        4         5       6        7
        final List<String> data = Arrays.asList("blue", "red", "green", "x-red", "yellow", "gray", "brown", "x-pink");
        //for test, group list after first letter with standard group order. "x" is not part of a group
        final List<List<Object>> groupedList = new ArrayList<>();
        CommonUtils.groupList(data, (s) -> s.startsWith("x-") ? null : s.substring(0, 1), null, null,
                (group, firstIdx, elements) -> groupedList.add(Arrays.asList(group, true, firstIdx, elements)),
                (item, originalIdx, group, groupIndex) -> groupedList.add(Arrays.asList(item, false, originalIdx, group, groupIndex)));

        assertThat(groupedList).containsExactly(
                //non-grouped items come first. They appear in original order
                Arrays.asList("x-red", false, 3, null, -1),
                Arrays.asList("x-pink", false, 7, null, -1),
                //groups. Groups are sorted alphabetically, items inside group are sorted in original order
                Arrays.asList("b", true, 3, Arrays.asList("blue", "brown")),
                Arrays.asList("blue", false, 0, "b", 2),
                Arrays.asList("brown", false, 6, "b", 2),
                Arrays.asList("g", true, 6, Arrays.asList("green", "gray")),
                Arrays.asList("green", false, 2, "g", 5),
                Arrays.asList("gray", false, 5, "g", 5),
                Arrays.asList("r", true, 9, Collections.singletonList("red")),
                Arrays.asList("red", false, 1, "r", 8),
                Arrays.asList("y", true, 11, Collections.singletonList("yellow")),
                Arrays.asList("yellow", false, 4, "y", 10)
                );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void groupListMinCountPerGroup() {
        //Original Index of test data:          0        1        2        3
        final List<String> data = Arrays.asList("brown", "green", "x-red", "blue");
        //for test, group list after first letter with standard group order. "x" is not part of a group
        final List<List<Object>> groupedList = new ArrayList<>();
        CommonUtils.groupList(data, (s) -> s.startsWith("x-") ? null : s.substring(0, 1), null,
                (g, cnt) -> cnt.size() >= 2,
                (group, firstIdx, elements) -> groupedList.add(Arrays.asList(group, true, firstIdx, elements)),
                (item, originalIdx, group, groupIndex) -> groupedList.add(Arrays.asList(item, false, originalIdx, group, groupIndex)));

        //expect that the one grtop with 2 items is created, but the others are not
        assertThat(groupedList).containsExactly(
                //grouping shall be done only for 'b'. Order inside 'b' shall be as in original list. Ungrouped items shall go on top
                Arrays.asList("x-red", false, 2, null, -1),
                Arrays.asList("b", true, 2, Arrays.asList("brown", "blue")),
                Arrays.asList("brown", false, 0, "b", 1),
                Arrays.asList("blue", false, 3, "b", 1),
                Arrays.asList("green", false, 1, "g", -1)
            );
    }

    @Test
    public void testModulo() {
        assertThat(18 % 8.7).isEqualTo(0.6, offset(0.00001));
        assertThat(-18 % 8.7).isEqualTo(-0.6, offset(0.00001));
    }

    @Test
    public void testListSortingComparator() {
        final List<String> sorterList = Arrays.asList("apple", "bee", "peach");
        final List<String> toSort = new ArrayList<>(Arrays.asList("milk", "peach", "corn", "apple", "bean", "bee"));
        Collections.sort(toSort, CommonUtils.getListSortingComparator(null, true, sorterList));
        assertThat(toSort).as("Actual: " + toSort).containsExactly("apple", "bee", "peach",  "milk", "corn",  "bean");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getReferencedClasses() {
        //A Lambda with back reference should contain the class
        Runnable r = () -> testField = testField + "*";
        final Set<Class<? extends CommonUtilsTest>> set = CommonUtils.getReferencedClasses(r, CommonUtilsTest.class);
        assertThat(set).hasSize(1);
        assertThat(CommonUtils.first(set)).isEqualTo(CommonUtilsTest.class);

        //A Lambda without back reference should NOT contain the class
        r = CommonUtilsTest::staticMethod;
        assertThat(CommonUtils.getReferencedClasses(r, CommonUtilsTest.class)).isEmpty();

        //Searching for a super class
        final Set<Class<? extends Number>> set2 = CommonUtils.getReferencedClasses(new FindReferencesTestClass(), Number.class);
        assertThat(set2).containsExactlyInAnyOrder(Integer.class, Float.class, Number.class); // and does NOT contain Object.class although it is a field

    }

    private static void staticMethod() {
        //do nothing
    }

    @SuppressWarnings("unused") //fields are necessary for unit test, which is based on reflection
    private static class FindReferencesTestClass {

        private Integer int1;
        private Float float1;

        private Float float2;

        private Number num;

        private Object obj1;
    }

}
