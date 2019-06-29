import org.academiadecodigo.simplegraphics.graphics.Rectangle;

import java.util.Scanner;

/**
 * @author albertoreis
 */
public class Gui {
    public static void main(String[] args) {
        Scanner
                scanner=new Scanner(System.in);

        System.out.println("typechit");


        String primeiro="ola olaaaa ";
        Scanner reader=new Scanner(primeiro);
        System.out.println(reader.next()+" "+ reader.next());
        reader.close();


    }
}
