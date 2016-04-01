package linesweep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.math.DoubleRange;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

public class LineSweepAlgorithm {

	Set<Circle> circles;
	Set<Circle> activeCircles;

	Map<Double, Set<Event>> events;

	public int startOnCircles(Set<Circle> circles){

		activeCircles = new HashSet<Circle>();

		//Compute Events
		EventsMng em = new EventsMng();
		Map<Apfloat, Set<Event>> eventsMap = em.computeStartingEndingAndIntersectingEvents(circles);

		//Ordered key list
		ArrayList<Apfloat> eventsX = new ArrayList<Apfloat>(eventsMap.keySet());
		Collections.sort(eventsX);

		int maxPly = 0;
		
		System.out.println("Events: " + eventsX.size());
		
		int i = 0;

		for(Apfloat x : eventsX){
			
			System.out.print(i+") ");
			i++;

			Set<Event> events = eventsMap.get(x);

			for (Event e : events){
				//Setup circles
				prepareForEvent(e);	
			}

			if(activeCircles.size()>0){
			//Compute all Intersections
			ArrayList<DoubleRange> intervals = computeIntersections(x);

			//Point of maximum overlap
			int currPly = pointOfMaximumOverlap(intervals);


			//Check Ply
			if(currPly>maxPly) maxPly = currPly;
			}
			else{
				System.out.println("no active circles");
			}


		}

		
		System.out.println("Max Ply: " + maxPly);
		
		return maxPly;

	}


	private void prepareForEvent(Event e){

		switch(e.type){
		case OPENING:{
			if(activeCircles.contains(e.circle))
				throw new IllegalArgumentException("Trying to activating a circle twice");
			activeCircles.add(e.circle);
		}
		break;
		case CLOSING:{
			if(!activeCircles.contains(e.circle))
				throw new IllegalArgumentException("Trying to close a non active circle");
			activeCircles.remove(e.circle);
		}
		break;
		case INTERSECTION:{
			Circle c1 = null;
			Circle c2 = null;
			if(!activeCircles.contains(c1) &&
					!activeCircles.contains(c2))
				throw new IllegalArgumentException("Intersection between non active circles");

		}
		break;
		default:
			break;
		}

	}

	private ArrayList<DoubleRange> computeIntersections(Apfloat xLine){

		ArrayList<DoubleRange> rangeSet = new ArrayList<DoubleRange>();

		for(Circle circle : this.activeCircles){

			Apfloat xCenter = circle.getX();
			Apfloat yCenter = circle.getY();
			Apfloat radius = circle.radius;
		
			//System.out.println("xLine: "+xLine+"\ncenter: (" +xCenter + ", "+ yCenter +") radius: "+ radius );
			


			//Apfloat a = new Apfloat("1", Apfloat.INFINITE);
			Apfloat b = yCenter.multiply(new Apfloat("2", 10000)).negate();
			Apfloat c = ApfloatMath.sum(ApfloatMath.pow(yCenter, new Apfloat("2", 10000)),
					 ApfloatMath.pow(ApfloatMath.abs(xLine.subtract(xCenter)), new Apfloat("2", 10000)),
					 ApfloatMath.pow(radius, new Apfloat("2", 10000)).negate());


			Apfloat sqrt = ApfloatMath.sqrt(
							ApfloatMath.sum(
									ApfloatMath.pow(b, 2),
										c.multiply(new Apfloat("4", 10000)).negate()));
			
			Apfloat y1 = b.negate().subtract(sqrt).divide(new Apfloat("2", 10000));
			Apfloat y2 = b.negate().add(sqrt).divide(new Apfloat("2", 10000));

			DoubleRange currRange = new DoubleRange(y1, y2);
			
			//System.out.println(" ["+y1+" , "+y2+"]");

			rangeSet.add(currRange);

		}

		return rangeSet;

	}

	private int pointOfMaximumOverlap(ArrayList<DoubleRange> intervals){

		Map<Double, ArrayList<DoubleRange>> openingRangesMap = new HashMap<Double, ArrayList<DoubleRange>>();
		Map<Double, ArrayList<DoubleRange>> closingRangesMap = new HashMap<Double, ArrayList<DoubleRange>>();
		Map<Double, ArrayList<DoubleRange>> degenerateRangesMap = new HashMap<Double, ArrayList<DoubleRange>>();

		for (DoubleRange r : intervals){

			if(r.getMaximumDouble() == r.getMinimumDouble()){
				double key = r.getMinimumDouble();
				ArrayList<DoubleRange> dR = degenerateRangesMap.get(key);
				if(dR == null) dR = new ArrayList<DoubleRange>();
				dR.add(r);
				degenerateRangesMap.put(key, dR);

			}else{

				double minKey = r.getMinimumDouble();
				ArrayList<DoubleRange> oR = openingRangesMap.get(minKey);
				if(oR == null) oR = new ArrayList<DoubleRange>();
				oR.add(r);
				openingRangesMap.put(minKey, oR);

				double maxKey = r.getMaximumDouble();
				ArrayList<DoubleRange> cR = closingRangesMap.get(maxKey);
				if(cR == null) cR = new ArrayList<DoubleRange>();
				cR.add(r);
				closingRangesMap.put(maxKey, cR);

			}


		}
		
		ArrayList<Double> allKeys = new ArrayList<Double>();
		allKeys.addAll(openingRangesMap.keySet());
		allKeys.addAll(closingRangesMap.keySet());
		allKeys.addAll(degenerateRangesMap.keySet());
		
		Collections.sort(allKeys);
		
		int p = -1;
		int tempP = 0;
		
		Set<DoubleRange> openRanges = new HashSet<DoubleRange>();
		
		for(Double k : allKeys){
			
			ArrayList<DoubleRange> cR = closingRangesMap.get(k);
			if(cR != null){
				if(!openRanges.containsAll(cR)) throw new IllegalArgumentException("Closing not open Range");
				openRanges.removeAll(cR);
				tempP -= cR.size();
			}

			ArrayList<DoubleRange> oR = openingRangesMap.get(k);
			if(oR != null){
				openRanges.addAll(oR);
				tempP +=oR.size();
				
			}
			
			ArrayList<DoubleRange> dR = degenerateRangesMap.get(k);
			if(dR != null){
				tempP += dR.size();
				if(tempP > p) p = tempP;
				tempP -= dR.size();
			}
			
			if(tempP > p) p = tempP;
			
		}


		return p;
	}

}
