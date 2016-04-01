package algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

import graph.Edge;
import graph.Graph;
import graph.Vertex;

public class PlyGraphGenerator{
		
//	Map<Integer, Double> circlesRadiiMap;
		
	public PlyGraphGenerator(){
		
//		this.circlesRadiiMap = new HashMap<Integer, Double>();		
		
	}


	public Set<Vertex> computePlyCircles(Graph graph, Apfloat radiusRatio){
				
		this.computeEdgesDistances(graph);
				
		for(Vertex currVertex : graph.getVertices()){
						
			Apfloat maxRadiusLength = new Apfloat("0");
			
			Set<Edge> adjEdges = graph.getIncidentEdges(currVertex);
			
			for(Edge currEdge : adjEdges){
				
				Apfloat dist = currEdge.getLenth();
				
				Apfloat currRadius = dist.multiply(radiusRatio);

				maxRadiusLength = (maxRadiusLength.compareTo(currRadius)== 1) ? maxRadiusLength : currRadius;
				// ApfloatMath.max(maxRadiusLength, currRadius);
			}
			
			currVertex.circleRadius = maxRadiusLength;
//			this.circlesRadiiMap.put(currVertex.identifier, maxRadiusLength);
			
			
		}
		
		return graph.getVertices();
		
	}


	public Graph generatePlyIntersectionGraph(Graph graph, Apfloat radiusRatio){
		
		computePlyCircles(graph, radiusRatio);

		Map<Integer, Vertex> verticesMap = graph.getVerticesMap();
		Map<Integer, Edge> edgesMap = new HashMap<Integer, Edge>();

			
		ArrayList<Vertex> orderedVerticesList = new ArrayList<Vertex>(verticesMap.values());
		
		int edgeId = 0;
		
		for(int i=0; i<orderedVerticesList.size(); i++){
			
			Vertex firstVertex = orderedVerticesList.get(i);
			
			for(int j=i+1; j<orderedVerticesList.size(); j++){
				
				Vertex secondVertex = orderedVerticesList.get(j);
				
				if(doCirclesIntesect(firstVertex, secondVertex)){
										
					Edge currIntersectionEdge = new Edge(edgeId, firstVertex.identifier, secondVertex.identifier);
					edgesMap.put(edgeId, currIntersectionEdge);
					edgeId++;
						
				}
		
			}
			
		}
		
		Graph plyGraph = new Graph(verticesMap, edgesMap);
		
		return plyGraph;
	}
	
	private boolean doCirclesIntesect(Vertex v1, Vertex v2){
		
		Apfloat distance = distance(v1, v2);
		
		Apfloat v1CircleRadius = v1.circleRadius;
		Apfloat v2CircleRadius = v2.circleRadius;
		
		Apfloat radiiSum = ApfloatMath.sum(v1CircleRadius, v2CircleRadius);
		
		int compare = (radiiSum.compareTo(distance));
		
		return (compare > 0);
		
	}
	
	
	//ComputeDistances
	
	private void computeEdgesDistances(Graph graph){
		
		Set<Edge> edges = graph.getEdges();
		Map<Integer, Vertex> verticesMap = graph.getVerticesMap();
		
	
		for(Edge edge : edges){
			
			Vertex source = verticesMap.get(edge.getSourceIdentifier());
			Vertex target = verticesMap.get(edge.getTargetIdentifier());
			
			Apfloat currDist = this.distance(source, target);
			edge.setLength(currDist);
			
		}
		
		
		
	}
	
	private Apfloat distance(Vertex firstVertex, Vertex secondVertex){
		
		Apfloat x1 = firstVertex.x;
		Apfloat y1 = firstVertex.y;
		Apfloat x2 = secondVertex.x;
		Apfloat y2 = secondVertex.y;
			
		Apfloat xDist = ApfloatMath.pow(ApfloatMath.abs(x1.subtract(x2)), 2);
		Apfloat yDist = ApfloatMath.pow(ApfloatMath.abs(y1.subtract(y2)), 2);

		
		
		Apfloat  dist = ApfloatMath.sqrt(xDist.add(yDist));
		
		return dist;
	}

}
