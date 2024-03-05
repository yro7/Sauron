package fr.yronusa.ultimatetracker.Config;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.SequenceNode;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

public class ConfigLoader {

    public static void test(){
        /**
        try {
            // Specify the path to your YAML file
            FileInputStream input = new FileInputStream("path/to/your/file.yaml");

            // Parse the YAML document
            Yaml yaml = new Yaml();
            Node rootNode = yaml.compose(input);

            // Specify the key of the section you want to extract
            String sectionKey = "yourSection";

            // Extract the specified section
            if (rootNode instanceof SequenceNode) {
                for (Node node : ((SequenceNode) rootNode).getValue()) {
                    if (node instanceof NodeTuple) {
                        NodeTuple tuple = (NodeTuple) node;
                        Node keyNode = tuple.getKeyNode();
                        Node valueNode = tuple.getValueNode();

                        if (keyNode.getTag().equals(Tag.STR) && sectionKey.equals(keyNode.toString())) {
                            // Process the section found
                            Map<String, Object> sectionMap = yaml.load(valueNode.toString());
                            System.out.println("Section: " + sectionMap);
                            break;
                        }
                    }
                }
            } else {
                System.out.println("Root node is not a sequence. Unable to extract sections.");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }**/
    }
}
