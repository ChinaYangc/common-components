import com.fansz.pub.utils.CollectionTools;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by allan on 16/1/13.
 */
public class TestMain {
    public static void main(String[] args) {
        Set<String> a = new HashSet<>();
        a.add("1");
        a.add("2");

        Set<String> b = new HashSet<>();
        b.add("1");
        b.add("3");
        Collection c=CollectionTools.intersection(a,b);
        for(Object d:c){
            System.out.println(d);
        }
    }
}
