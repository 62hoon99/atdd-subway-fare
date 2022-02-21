package nextstep.subway.domain;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.List;
import java.util.stream.Collectors;

public class SubwayMap {
    private List<Line> lines;

    public SubwayMap(List<Line> lines) {
        this.lines = lines;
    }

    public Path findPath(Station source, Station target, PathType pathType, String time) {
        List<GraphPath<Station, SectionEdge>> paths = findAllKShortestPaths(source, target, pathType);

//        FindShortest findShortest = new FindShortest();
        ShortestPaths shortestPaths = findShortest(paths, time);

        int fareDistance = shortestPaths.getShortestDistance();

        List<Section> sections;

        if (pathType.equals(PathType.DISTANCE)) {
            sections = shortestPaths.getShortestDistanceSections();
            return Path.of(new Sections(sections), fareDistance);
        }

        sections = shortestPaths.getShortestDurationSections();
        return Path.of(new Sections(sections), fareDistance);
    }

    protected List<GraphPath<Station, SectionEdge>> findAllKShortestPaths(Station source, Station target, PathType pathType) {
        return new KShortestPaths(createGraph(pathType), 100)
            .getPaths(source, target);
    }

    public ShortestPaths findShortest(List<GraphPath<Station, SectionEdge>> paths, String time) {
        for (GraphPath<Station, SectionEdge> path : paths) {
            List<Section> sectionList = makeSectionListFromGraphPath(path);

        }

        return new ShortestPaths();
    }

    //////////////////////////////////////////////////////////////////////////////////////

//    public Path findPath(Station source, Station target, PathType pathType, String time) {
//        GraphPath<Station, SectionEdge> result = findMinDistancePath(source, target);
//        int fareDistance = result.getEdgeList().stream()
//            .mapToInt(value -> value.getSection().getDistance())
//            .sum();
//
//        if (pathType.equals(PathType.DURATION)) {
//            result = findMinDurationPath(source, target);
//        }
//
//        List<Section> sections = makeSectionListFromGraphPath(result);
//
//        return Path.of(new Sections(sections), fareDistance);
//    }

    private GraphPath<Station, SectionEdge> findMinDistancePath(Station source, Station target) {
        DijkstraShortestPath<Station, SectionEdge> dijkstraShortestPath
            = new DijkstraShortestPath<>(createGraph(PathType.DISTANCE));
        return dijkstraShortestPath.getPath(source, target);
    }

    private GraphPath<Station, SectionEdge> findMinDurationPath(Station source, Station target) {
        DijkstraShortestPath<Station, SectionEdge> dijkstraShortestPath
            = new DijkstraShortestPath<>(createGraph(PathType.DURATION));
        return dijkstraShortestPath.getPath(source, target);
    }

    private List<Section> makeSectionListFromGraphPath(GraphPath<Station, SectionEdge> graphPath) {
        return graphPath.getEdgeList().stream()
            .map(SectionEdge::getSection)
            .collect(Collectors.toList());
    }

    private SimpleDirectedWeightedGraph<Station, SectionEdge> createGraph(PathType pathType) {
        SimpleDirectedWeightedGraph<Station, SectionEdge> graph
            = new SimpleDirectedWeightedGraph<>(SectionEdge.class);

        addVertex(graph);
        addWeights(graph, pathType);

        return graph;
    }

    private void addWeights(SimpleDirectedWeightedGraph<Station, SectionEdge> graph, PathType pathType) {
        if (pathType.equals(PathType.DISTANCE)) {
            addEdgeDistanceWeight(graph);
            addOppositeEdgeDistanceWeight(graph);
            return;
        }

        addEdgeDurationWeight(graph);
        addOppositeEdgeDurationWeight(graph);
    }


    private void addVertex(SimpleDirectedWeightedGraph<Station, SectionEdge> graph) {
        // 지하철 역(정점)을 등록
        lines.stream()
                .flatMap(it -> it.getStations().stream())
                .distinct()
                .collect(Collectors.toList())
                .forEach(graph::addVertex);
    }

    private void addEdgeDistanceWeight(SimpleDirectedWeightedGraph<Station, SectionEdge> graph) {
        // 지하철 역의 연결 정보(간선)을 등록
        lines.stream()
                .flatMap(it -> it.getSections().stream())
                .forEach(it -> {
                    SectionEdge sectionEdge = SectionEdge.of(it);
                    graph.addEdge(it.getUpStation(), it.getDownStation(), sectionEdge);
                    graph.setEdgeWeight(sectionEdge, it.getDistance());
                });
    }

    private void addOppositeEdgeDistanceWeight(SimpleDirectedWeightedGraph<Station, SectionEdge> graph) {
        // 지하철 역의 연결 정보(간선)을 등록
        lines.stream()
                .flatMap(it -> it.getSections().stream())
                .map(it -> new Section(
                    it.getLine(),
                    it.getDownStation(),
                    it.getUpStation(),
                    it.getDistance(),
                    it.getDuration()
                ))
                .forEach(it -> {
                    SectionEdge sectionEdge = SectionEdge.of(it);
                    graph.addEdge(it.getUpStation(), it.getDownStation(), sectionEdge);
                    graph.setEdgeWeight(sectionEdge, it.getDistance());
                });
    }

    private void addEdgeDurationWeight(SimpleDirectedWeightedGraph<Station, SectionEdge> graph) {
        // 지하철 역의 연결 정보(간선)을 등록
        lines.stream()
                .flatMap(it -> it.getSections().stream())
                .forEach(it -> {
                    SectionEdge sectionEdge = SectionEdge.of(it);
                    graph.addEdge(it.getUpStation(), it.getDownStation(), sectionEdge);
                    graph.setEdgeWeight(sectionEdge, it.getDuration());
                });
    }

    private void addOppositeEdgeDurationWeight(SimpleDirectedWeightedGraph<Station, SectionEdge> graph) {
        // 지하철 역의 연결 정보(간선)을 등록
        lines.stream()
                .flatMap(it -> it.getSections().stream())
                .map(it -> new Section(
                    it.getLine(),
                    it.getDownStation(),
                    it.getUpStation(),
                    it.getDistance(),
                    it.getDuration()
                ))
                .forEach(it -> {
                    SectionEdge sectionEdge = SectionEdge.of(it);
                    graph.addEdge(it.getUpStation(), it.getDownStation(), sectionEdge);
                    graph.setEdgeWeight(sectionEdge, it.getDuration());
                });
    }
}
