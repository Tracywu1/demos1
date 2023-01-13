package Test;
import java.util.Scanner;
public class test6_1 {
    public static void main(String[] args) {
        Scanner sc=new Scanner(System.in);
        System.out.println("请输入密码的位数：");
        int n= sc.nextInt();
        int[] code=new int [n];
        System.out.println("请输入密码：");
        for(int i=0;i<n;i++){
            code[i]=sc.nextInt();
        }
        for (int i = 0; i < n; i++) {
            code[i]=(code[i]+5)%10;
        }
        System.out.println("加密后的结果为：");
        tran(code);
    }
    public static void tran(int[]arr){
        if(arr.length%2==0) {
            for(int i=0,j=arr.length-1;i<arr.length/2&&j>=arr.length/2;i++,j--){
                int temp=arr[i];
                arr[i]=arr[j];
                arr[j]=temp;
            }
            print(arr);
        }
        else{
            for(int i=0,j=arr.length-1;i<(arr.length-1)/2&&j>(arr.length-1)/2;i++,j--){
                int temp=arr[i];
                arr[i]=arr[j];
                arr[j]=temp;
            }
            print(arr);
        }
    }
    public static void print(int[]arr){
        for (int i = 0; i <arr.length ; i++) {
            System.out.print(arr[i]+" ");
        }
    }
}
