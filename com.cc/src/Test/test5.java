package Test;
/*在唱歌比赛中，有6名评委给选手打分，分数范围是[0 - 100]之间的整数。
选手的最后得分为：去掉最高分、最低分后的4个评委的平均分，请完成上述过程并计算出选手的得分。*/
/*1.把6个评委的分数录入到程序中去 ----> 使用数组——int[] scores=new int [6];
 2.遍历数组中每个数据，进行累加求和，并找出最高分、最低分。
 3.按照分数的计算规则算出平均分。*/
import java.util.Scanner;
public class test5 {
    public static void main(String[] args) {
        int[] scores=new int [6];
        Scanner sc=new Scanner(System.in);
        for(int i=0;i< scores.length;i++)
        {
            System.out.println("请输入第" + (i + 1) + "个评委的打分分数：");
            scores[i]= sc.nextInt();
        }
        double avg=(sum(scores)-max(scores)-min(scores))/4.0;
        System.out.println("选手的最后得分为：" + avg);
    }
    public static int sum(int[]a){
        int ret=0;
        for (int i = 0; i < 6; i++) {
             ret+=a[i];
        }
        return ret;
    }
    public static int max(int[]a){
        int ret=a[0];
        for (int i = 1; i < 6; i++) {
            if(ret<a[i])
                ret=a[i];
        }
        return ret;
    }
    public static int min(int[]a){
        int ret=a[0];
        for (int i = 1; i < 6; i++) {
            if(ret>a[i])
                ret=a[i];
        }
        return ret;
    }
}
