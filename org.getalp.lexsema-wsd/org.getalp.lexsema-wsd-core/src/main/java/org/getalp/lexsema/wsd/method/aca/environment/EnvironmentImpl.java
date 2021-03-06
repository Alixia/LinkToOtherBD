package org.getalp.lexsema.wsd.method.aca.environment;


import cern.jet.random.engine.MersenneTwister;
import org.getalp.lexsema.similarity.signatures.SemanticSignature;
import org.getalp.lexsema.similarity.signatures.symbols.SemanticSymbol;
import org.getalp.lexsema.util.dataitems.Pair;
import org.getalp.lexsema.util.dataitems.PairImpl;
import org.getalp.lexsema.wsd.method.aca.agents.Ant;
import org.getalp.lexsema.wsd.method.aca.environment.graph.Node;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.*;

public class EnvironmentImpl implements Environment {
    private final INDArray adjacency;
    private final List<Node> nodes;
    private final Map<Integer, Node> nestIndex;
    private final List<Node> words;
    private final List<Ant> ants;
    private final Set<Pair<Integer, Integer>> bridgeIndex;
    private final Map<Integer, List<Node>> wordSenseIndex;
    private final double initialPheromone;

    /**
     * Used to check if the value of the double is infinitesimally close to zero
     */
    public static final double ZERO_EPSILON = 0.00000001d;

    public EnvironmentImpl(List<Node> nodes, Map<Integer, Node> nestIndex, List<Node> words, Map<Integer, List<Node>> wordSenseIndex, INDArray adjacency, double initialPheromone) {
        this.adjacency = adjacency;
        this.nodes = Collections.unmodifiableList(nodes);
        this.nestIndex = Collections.unmodifiableMap(nestIndex);
        this.words = Collections.unmodifiableList(words);
        this.wordSenseIndex = Collections.unmodifiableMap(wordSenseIndex);
        ants = new LinkedList<>();
        bridgeIndex = new HashSet<>();
        this.initialPheromone = initialPheromone;
    }

    @Override
    public INDArray getOutgoingVector(int position) {
        return adjacency.getRow(position);
    }

    @Override
    public List<Integer> getOutgoingNodes(int position) {
        INDArray outgoingVector = getOutgoingVector(position);
        List<Integer> neighbouringNodes = new ArrayList<>();

        for (int i = 0; i < outgoingVector.columns(); i++) {
            double value = outgoingVector.getDouble(i);
            if (value > -1) {
                neighbouringNodes.add(i);
            }
        }
        return neighbouringNodes;
    }


    @Override
    public boolean isNest(int position) {
        return nestIndex.containsKey(position);
    }

    @Override
    public boolean areSiblings(int position1, int position2) {
        List<Integer> neighbours1 = getOutgoingNodes(position1);
        List<Integer> neighbours2 = getOutgoingNodes(position2);
        int sizeBefore = neighbours1.size();
        neighbours1.removeAll(neighbours2);
        int sizeAfter = neighbours1.size();
        return sizeAfter != sizeBefore;
    }

    @Override
    public boolean isFriendNest(int position, int antHome) {
        return isNest(position) && !areSiblings(position, antHome);
    }

    @Override
    public synchronized double getPheromone(int startPosition, int targetPosition) {
        return adjacency.getDouble(startPosition, targetPosition);
    }

    @Override
    public synchronized void setPheromone(int startPosition, int targetPosition, double pheromone) {
        adjacency.put(startPosition, targetPosition, pheromone);
        //adjacency.put(targetPosition, startPosition, pheromone);
    }

    @Override
    public synchronized double getEnergy(int position) {
        final Node node = nodes.get(position);
        return node.getEnergy();
    }

    @Override
    public synchronized void setEnergy(int position, double energy) {
        final Node node = nodes.get(position);
        node.setEnergy(energy);
    }

    @Override
    public List<Node> nodes() {
        return Collections.unmodifiableList(nodes);
    }

    @Override
    public Collection<Ant> ants() {
        return Collections.unmodifiableList(ants);
    }

    @Override
    public List<Node> words() {
        return Collections.unmodifiableList(words);
    }

    @Override
    public synchronized void addAnt(Ant ant) {
        ants.add(ant);
    }

    @Override
    public SemanticSignature getNodeSignature(int position) {
        final Node node = nodes.get(position);
        return node.getSemanticSignature();
    }

    @Override
    public synchronized void removeDeadAnts() {
        ants.removeIf(a -> Math.abs(0 - a.getLife()) < ZERO_EPSILON);
    }

    @Override
    public void depositSignature(List<SemanticSymbol> semanticSymbols, int position, MersenneTwister mersenneTwister) {
        final Node node = nodes.get(position);
        node.depositSignature(semanticSymbols, mersenneTwister);
    }

    @Override
    public synchronized void createBridge(int start, int end) {
        if (isNest(start) && isNest(end) && isFriendNest(start, end)) {
            bridgeIndex.add(new PairImpl<>(start, end));
            setPheromone(start, end, initialPheromone);
        }
    }

    @Override
    public synchronized void cleanupBridges() {
        bridgeIndex.removeIf(pair -> getPheromone(pair.first(), pair.second()) < 0);
    }

    @Override
    public synchronized boolean isBridge(int start, int end) {
        return bridgeIndex.contains(new PairImpl<>(start, end));
    }

    @Override
    public Node getNode(int position) {
        return nodes.get(position);
    }

    @Override
    public List<Node> getNestsForNode(int position) {
        if (wordSenseIndex.containsKey(position)) {
            return Collections.unmodifiableList(wordSenseIndex.get(position));
        }
        return Collections.emptyList();
    }

    @Override
    public int numberOfBridges() {
        return bridgeIndex.size();
    }
}
