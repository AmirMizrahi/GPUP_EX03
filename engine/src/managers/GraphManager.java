package managers;

import graph.Graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GraphManager {

    private final Map<String, Graph> graphNameToGraph;
    private Set<String> graphNameEntered; //Will use it to to synchronize part when uploading xml file

    public GraphManager() {
        graphNameToGraph = new HashMap<>();
    }

    public synchronized void addGraph(String graphName, Graph graph) {
        graphNameToGraph.put(graphName, graph);
    }

//    public synchronized void removeUser(String username) {
  //      graphNameToGraph.remove(username);
  //  }

    public synchronized Map<String,Graph> getGraphs() {
        return Collections.unmodifiableMap(graphNameToGraph);
    }

    public boolean isGraphExists(String graphName) {
        return graphNameToGraph.containsKey(graphName);
    }

}
