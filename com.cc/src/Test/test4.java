package Test;
/*需要动态初始化一个数组，长度与原数组一样。
 遍历原数组的每个元素，依次赋值给新数组。
 输出两个数组的内容。*/
public class test4 {
    public static void main(String[] args) {
        int[]arr1={1,2,3,4,5};
        int[]arr2=new int[arr1.length];
        for(int i=0;i<arr1.length;i++)
        {
            arr2[i]=arr1[i];
        }
        for(int i=0;i<arr1.length;i++){
            System.out.print(arr1[i]+" ");
        }
        System.out.println("");
        for(int i=0;i<arr2.length;i++){
            System.out.print(arr2[i]+" ");
        }
    }
}
