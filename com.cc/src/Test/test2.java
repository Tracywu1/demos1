package Test;
import static java.lang.Math.sqrt;
//判断101-200之间有多少个素数，并输出所有的素数
public class test2 {
    public static void main(String[] args) {
        int count =0;
      for(int i=101;i<=200;i+=2){
          int flag=1;
          for(int j=2;j<=sqrt(i);j++){
              if(i%j==0)
              {
                  flag=0;
              }
          }
          if(flag==1)
          {
              count++;
              System.out.print(i+" ");
          }
        }
        System.out.println("");
        System.out.println("101-200之间一共有" + count + "个素数");
    }
}
