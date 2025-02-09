import org.junit.jupiter.api.Test;
import org.talend.maplang.el.parser.model.ELNode;
import org.talend.maplang.el.parser.model.ELNodeType;
import org.talend.tqldsel.DSELConverter;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DSELVisitorNotTest {

    @Test
    public void testParseSingleNot() {
        final String query = "not (field1=123 and field2<124)";
        ELNode actual = DSELConverter.convert(query);

        List<ELNode> lst = new ArrayList<ELNode>();
        lst.add(buildNode(ELNodeType.EQUAL, "==", "field1", "123"));
        lst.add(buildNode(ELNodeType.LOWER_THAN, "<", "field2", "124"));
        ELNode andNode = new ELNode(ELNodeType.AND, "&&");
        andNode.addChildren(lst);
        ELNode notNode = new ELNode(ELNodeType.NOT, "!");
        notNode.addChild(andNode);

        assertEquals(DSELConverter.wrapNode(notNode), actual);
    }

    private ELNode buildNode(ELNodeType type, String image, String name, String value) {
        ELNode current = new ELNode(type, image);
        current.addChild(new ELNode(ELNodeType.HPATH, name));
        current.addChild(new ELNode(ELNodeType.INTEGER_LITERAL, value));
        return current;
    }
}
