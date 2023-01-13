package Test;
import java.util.Random;
import java.util.Scanner;
public class test7 {
    public static void main(String[] args) {
        //随机生成一组中奖号码
        int[] lucknumbers = new int[7];
        Random r = new Random();
        for (int i = 0; i < 6; i++) {
            int data = r.nextInt(33) + 1;
            while (true) {
                boolean flag = true;
                for (int j = 0; j < i; j++) {
                    if (lucknumbers[j] == data) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    lucknumbers[i] = data;
                    break;
                }
            }
        }
        lucknumbers[6] = r.nextInt(16) + 1;
        //用户输入一组双色球号码
        int[] usernumbers = new int[7];
        Scanner sc = new Scanner(System.in);
        for (int j = 0; j < 6; j++) {
            System.out.println("请输入第" + (j + 1) + "个红色球号码（1-33，不重复）：");
            usernumbers[j] = sc.nextInt();
            if (usernumbers[j] > 33 || usernumbers[j] < 1) {
                System.out.println("您输入的号码有误！");
                return;
            }
        }
        System.out.println("请输入蓝色球的号码（1-16）：");
        usernumbers[6] = sc.nextInt();
        if (usernumbers[6] >= 16 || usernumbers[6] <= 1) {
            System.out.println("您输入的号码有误！");
            return;
        }
        //判断中奖情况
        System.out.println("中奖号码是：");
        for (int i = 0; i < 7; i++) {
            System.out.print(lucknumbers[i] + " ");
        }
        System.out.println("");
        System.out.println("您的投注号码是：");
        for (int i = 0; i < 7; i++) {
            System.out.print(usernumbers[i] + " ");
        }
        System.out.println("");
        int count = 0;
        for (int i = 0; i < 6; i++) {
            for (int k = 0; k < 6; k++) {
                if (usernumbers[i] == lucknumbers[k])
                    count++;
            }
        }
        if (usernumbers[6] == lucknumbers[6]) {
            System.out.println("您命中了蓝球号码");
            switch (count) {
                case 0:
                    System.out.println("您命中了" + count + "个红球号码");
                    System.out.println("恭喜您！中了5元!");
                    break;
                case 1:
                    System.out.println("您命中了" + count + "个红球号码");
                    System.out.println("恭喜您！中了5元!");
                    break;
                case 2:
                    System.out.println("您命中了" + count + "个红球号码");
                    System.out.println("恭喜您！中了5元!");
                    break;
                case 3:
                    System.out.println("您命中了" + count + "个红球号码");
                    System.out.println("恭喜您！中了10元!");
                    break;
                case 4:
                    System.out.println("您命中了" + count + "个红球号码");
                    System.out.println("恭喜您！中了200元!");
                    break;
                case 5:
                    System.out.println("您命中了" + count + "个红球号码");
                    System.out.println("恭喜您！中了3000元");
                    break;
                case 6:
                    System.out.println("您命中了" + count + "个红球号码");
                    System.out.println("恭喜您！全部号码都中了！您将最高获得1000万元！");
                    break;
            }
        } else {
            System.out.println("您未命中了蓝球号码");
            switch (count) {
                case 0:
                    System.out.println("您命中了" + count + "个红球号码");
                    System.out.println("很遗憾，您未中奖");
                    break;
                case 1:
                    System.out.println("您命中了" + count + "个红球号码");
                    System.out.println("很遗憾，您未中奖");
                    break;
                case 2:
                    System.out.println("您命中了" + count + "个红球号码");
                    System.out.println("很遗憾，您未中奖");
                    break;
                case 3:
                    System.out.println("您命中了" + count + "个红球号码");
                    System.out.println("很遗憾，您未中奖");
                    break;
                case 4:
                    System.out.println("您命中了" + count + "个红球号码");
                    System.out.println("恭喜您！中了10元!");
                    break;
                case 5:
                    System.out.println("您命中了" + count + "个红球号码");
                    System.out.println("恭喜您！中了200元");
                    break;
                case 6:
                    System.out.println("您命中了" + count + "个红球号码");
                    System.out.println("恭喜您！您将最高获得500万元！");
                    break;
            }

        }
    }
}