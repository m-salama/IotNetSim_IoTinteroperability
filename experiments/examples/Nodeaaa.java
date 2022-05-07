package examples;

import java.awt.geom.Point2D;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class Nodeaaa {

    private Point2D myPosition;
    private Map<Point2D, Nodeaaa> neighborLinks;

    public class NearestComparator implements Comparator<Entry> {

        @Override
        public int compare(Entry e1, Entry e2) {
            return Double.compare((Double) e1.getValue(), (Double) e2.getValue());
        }
    }

    public List<Point2D> getSendOrder(Point2D destination) {
        LinkedList<Entry> sendOrderList = new LinkedList<>();
        Entry<Point2D, Double> pointWithDist;

        // calculate distance from destination and wrap it using an Entry
        pointWithDist = new SimpleEntry<>(myPosition, myPosition.distance(destination));
        sendOrderList.add(pointWithDist);
        for (Point2D otherPoint : neighborLinks.keySet()) {
            pointWithDist = new SimpleEntry<>(otherPoint, otherPoint.distance(destination));
            sendOrderList.add(pointWithDist);
        }

        // sort list by distance from destination
        Collections.sort(sendOrderList, new NearestComparator());

        // print all the list (debug)
        System.out.println(Arrays.toString(sendOrderList.toArray()));

        // unwrap and deep copy
        LinkedList<Point2D> copiedList = new LinkedList<>();
        Point2D pointToCopy, copiedPoint;
        for (Entry entry : sendOrderList) {
            pointToCopy = (Point2D) entry.getKey();
            copiedPoint = new Point2D.Double(pointToCopy.getX(), pointToCopy.getY());
            copiedList.add(copiedPoint);
        }

        return copiedList;
    }

    public Nodeaaa(Point2D myPosition, Map<Point2D, Nodeaaa> neighborLinks) {
        this.myPosition = myPosition;
        this.neighborLinks = neighborLinks;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Map<Point2D, Nodeaaa> neighborLinks = new HashMap<>();
        Random rand = new Random();
        double x, y, max = 5;
        for (int i = 0; i < 10; ++i) {
            x = rand.nextDouble() * max;
            y = rand.nextDouble() * max;
            neighborLinks.put(new Point2D.Double(x, y), null);
        }

        Point2D nodePos = new Point2D.Double();
        Nodeaaa myNode = new Nodeaaa(nodePos, neighborLinks);

        Point2D destination = new Point2D.Double();
        myNode.getSendOrder(destination);
    }

}