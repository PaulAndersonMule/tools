
import com.mulesoft.services.mulecodechecker.MuleCodeChecker;

/**
 *
 * @author kun.li
 */
public class Tester {

    public static void main(String[] args) {
        MuleCodeChecker.runCodeChecker(args[0], args[1], args[2]);
    }
}
