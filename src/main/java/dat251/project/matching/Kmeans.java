package dat251.project.matching;

import dat251.project.entities.Course;
import dat251.project.entities.Group;
import dat251.project.entities.User;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.*;

public class Kmeans {

    public static class Centroid { //Todo: placeholder
        public Map<String, Double> coords;

        public Centroid(Map<String, Double> coords) {
            this.coords = coords;
        }
    }


    private static Random rand = new Random(5);//TODO: remove seed after testing
    private static int maxIterations = 1000;
    private static List<String> abilities;
    private static Course course;
    private static Map<Centroid, ArrayList<User>> clusters;

    public Kmeans(Course course) {
        this.abilities = course.getAbilities();
        this.course = course;
    }

    /**
     * @param user The user to be placed in a group
     * @return The group with the closest matching for this user
     * @throws NullPointerException if list of groups in course is empty
     */
    public static Group findClosestGroup(User user) throws NullPointerException {
        if (course.getRelatedGroups().isEmpty()) {
            throw new NullPointerException("No groups registered for this course");
        }
        Map<String, Double> abMap = new HashMap<>();
        for (String ab : abilities) {
            abMap.put(ab, 0.0);
        }
        Group closestGroup = null;
        double minDist = Double.MAX_VALUE;
        for (Group group : course.getRelatedGroups()) {
            Centroid centroid = new Centroid(abMap);
            ArrayList<User> users = new ArrayList<>(group.getMembers());
            centroid = calcNewCentroid(centroid, users);
            Double dist = distance(centroid.coords, user.getAbilities(course));
            if (dist < minDist) {
                closestGroup = group;
                minDist = dist;
            }
        }
        return closestGroup;
    }


    public static Map<Centroid, ArrayList<User>> runKmeans(ArrayList<User> users, int k) {
        int clusterSize = (users.size() % k == 0) ? (users.size()/k) : ((users.size()/k)+1);
        System.out.println("clusterSize: " +clusterSize);
        Map<Centroid, Integer> sizes = new HashMap<>();
        clusters = new HashMap<>();
        Map<Centroid, ArrayList<User>> lastClusters = new HashMap<>();
        ArrayList<Centroid> centroids = new ArrayList<>();

        //create k random centroids
        for (int i = 0; i < k; i++) {
            Map<String, Double> randomMap = new HashMap<>();
            for (String ability : abilities) {
                randomMap.put(ability, (double) (rand.nextInt(10)));
            }
            Centroid randC = new Centroid(randomMap);
            centroids.add(randC);
            clusters.put(randC, new ArrayList<>());
            sizes.put(randC, 0);
        }


        for (int i = 0; i < maxIterations; i++) {
            for (User u : users) {
                ArrayList<Centroid> removedCentroids = new ArrayList<>();
                //Assign each user to a cluster
                while(true) {
                    Centroid closest = closestCentroid(centroids, u);
                    if (sizes.get(closest) < clusterSize) { //still room in cluster
                        setToCluster(u, closest);
                        sizes.put(closest, sizes.get(closest) + 1);
                        centroids.addAll(removedCentroids);
                        break;
                    }
                    else { //cluster full
                        removedCentroids.add(closest);
                        centroids.remove(closest);
                    }
                }

            }
            boolean done = clusters.equals(lastClusters); //Todo: Equals never true for some reason
            lastClusters = clusters;
            if (i == maxIterations - 1 || done) {
                break;
            }

            //update centroids
            ArrayList<Centroid> newCentroids = new ArrayList<>();
            Map<Centroid, ArrayList<User>> newClusters = new HashMap<>();
            sizes.clear();
            for (Centroid c : centroids) {
                Centroid newC = calcNewCentroid(c, clusters.get(c));
                newCentroids.add(newC);
                newClusters.put(newC, new ArrayList<>());
                sizes.put(newC, 0);
            }
            centroids = newCentroids;
            clusters = newClusters;
        }
        return lastClusters;
    }


    //euclidean distance between two attribute lists.
    private static double distance(Map<String, Double> a1, Map<String, Double> a2) {
        double sum = 0;
        for (String key : a1.keySet()) {
            sum += Math.pow(a1.get(key) - a2.get(key), 2); //This assumes that every user will have the same attributes in their list
        }
        return Math.sqrt(sum);
    }

    //finds nearest centroid
    private static Centroid closestCentroid(ArrayList<Centroid> centroids, User user) {
        double minDist = Double.MAX_VALUE;
        Centroid closest = null;
        for (Centroid centroid : centroids) {
            double dist = distance(centroid.coords, user.getAbilities(course));
            if (dist < minDist) {
                minDist = dist;
                closest = centroid;
            }
        }
        return closest;
    }

    private static void setToCluster(User user, Centroid centroid) {
        if (clusters.get(centroid) == null) {
            clusters.put(centroid, new ArrayList<>());
        }
        clusters.get(centroid).add(user);
    }


    private static Centroid calcNewCentroid(Centroid centroid, ArrayList<User> users) {
        if (users.isEmpty()) {
            return centroid;
        }
        Map<String, Double> average = centroid.coords;
        for (String ab : abilities) {
            average.put(ab, 0.0); // set all centroid abilities to 0.0
        }
        for (User u : users) {
            for (String ab : abilities) {
                double current = average.get(ab);
                centroid.coords.put(ab, current + u.getAbilities(course).get(ab)); //increment average values by user values
            }
        }
        for (String ab : abilities) {
            double current = average.get(ab);
            average.put(ab, current / users.size()); // find average by dividing each attribute sum by total size of cluster
        }
        return new Centroid(average);
    }
}
