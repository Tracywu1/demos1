package Test;
import java.util.Scanner;
public class test1 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("请您输入机票的原价：");
        double ori = sc.nextDouble();
        System.out.println("请您输入仓位类型: ");
        String type = sc.next();
        System.out.println("请您输入机票的月份（1-12）：");
        int mon = sc.nextInt();
        double price=fin(ori,type,mon);
        System.out.println("机票的最终价格为："+price);
    }
    public static double fin(double a, String b, int c) {
        if (c <= 10 && c >= 5) {
            switch (b) {
                case "头等舱":
                    return a * 0.9;
                case "经济舱":
                    return a * 0.85;
                default:
                    System.out.println("您输入的仓位类型有误！");
                    return -1.0;
            }
        } else if (c == 11 || c == 12 || ( c >=1 && c<= 4)) {
            switch (b) {
                case "头等舱":
                    return a * 0.7;
                case "经济舱":
                    return a * 0.65;
                default:
                    System.out.println("您输入的仓位类型有误！");
                    return -1.0;
            }
        } else {
            System.out.println("您输入的月份信息有误！");
            return -1.0;
        }
    }
}



