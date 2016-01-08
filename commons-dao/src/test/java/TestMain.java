import com.fansz.pub.utils.BeanTools;

/**
 * Created by allan on 15/12/31.
 */
public class TestMain {
    public static void main(String[] args){
        TestBean bean=new TestBean();
        BeanTools.setProperty(bean, "testSubBean.id", "123");
        System.out.println(bean.getTestSubBean().getId());
    }
}
