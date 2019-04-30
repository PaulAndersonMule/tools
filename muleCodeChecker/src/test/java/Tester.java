
import com.mulesoft.services.mulecodechecker.MuleCodeChecker;

/**
 *
 * @author kun.li
 */
public class Tester {

    public static void main(String[] args) {
        MuleCodeChecker codeChecker = new MuleCodeChecker("/Users/kun.li/localGitRepo/mulesoft-consulting/test",
                "/Users/kun.li/localGitRepo/mulesoft-consulting/tools/better-xml/src/test/resources/testXQueries_tiny.txt");

        codeChecker.runCodeChecker();
    }
}
