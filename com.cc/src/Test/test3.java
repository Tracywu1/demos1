package Test;
//定义方法实现随机产生一个指定位数的验证码，每位可能是数字、大写、小写字母
/*定义一个方法，生成验证码返回：返回值类型是String，需要形参接收位数。
在方法内部使用for循环依次生成每位随机字符，并连接起来。
把连接好的随机字符作为一组验证码返回。*/
import java.util.Random;
import java.util.Scanner;
public class test3 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n=sc.nextInt();
        System.out.println(creatCode(n));
    }
    public static String creatCode(int n) {
        String code = "";
        Random r = new Random();
        for (int i = 0; i < n; i++) {
            int type = r.nextInt(3);
            switch (type) {
                case 0:
                    char ch = (char) (r.nextInt(26) + 65);
                    code += ch;
                    break;
                case 1:
                    char ch1 = (char) (r.nextInt(26) + 97);
                    code += ch1;
                    break;
                case 2:
                    int ch2 = r.nextInt(10);
                    code += ch2;
                    break;
            }
        }
        return code;
    }
}
