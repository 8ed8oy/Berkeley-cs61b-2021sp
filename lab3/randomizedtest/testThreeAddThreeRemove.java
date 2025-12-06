package randomizedtest;

public class testThreeAddThreeRemove {
    public static void main(String[] args) {
        BuggyAList<Integer> buggyAList = new BuggyAList<>();
        AListNoResizing<Integer> aListNoResizing = new AListNoResizing<>();
        for (int i = 0; i < 3; i++) {
            buggyAList.addLast(i + 3);
            aListNoResizing.addLast(i + 3);
        }
        for (int i = 0; i < 3; i++) {
            int buggyValue = buggyAList.removeLast();
            int aListValue = aListNoResizing.removeLast();
            if (buggyValue != aListValue) {
                System.out.println("Error found: BuggyAList returned " + buggyValue + ",but AListNoResizing returned " + aListValue);
                return;
            }
        }
        System.out.println("Test passed");
    }
}
