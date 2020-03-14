package com.ebookfrenzy.expressioneditor.recognizer;

public class PointCloudRecognizer {
    /**
     * Main function of the $P recognizer.
     * Classifies a candidate gesture against a set of training samples.
     * Returns the class of the closest neighbor in the training set.
     *
     * @param candidate
     * @param trainingSet
     * @return
     */
    public static String Classify(Gesture candidate, Gesture[] trainingSet)
    {
        float minDistance = Float.MAX_VALUE;
        String gestureClass = "";
        for (Gesture template : trainingSet)
        {
            float dist = GreedyCloudMatch(candidate.Points, template.Points);
            if (dist < minDistance)
            {
                minDistance = dist;
                gestureClass = template.Name;
            }
        }
        return gestureClass;
    }

    /**
     * Implements greedy search for a minimum-distance matching between two point clouds
     *
     * @param points1
     * @param points2
     * @return
     */
    private static float GreedyCloudMatch(Point[] points1, Point[] points2)
    {
        int n = points1.length; // the two clouds should have the same number of points by now
        float eps = 0.5f;       // controls the number of greedy search trials (eps is in [0..1])
        int step = (int)Math.floor(Math.pow(n, 1.0f - eps));
        float minDistance = Float.MAX_VALUE;
        for (int i = 0; i < n; i += step)
        {
            float dist1 = CloudDistance(points1, points2, i);   // match points1 --> points2 starting with index point i
            float dist2 = CloudDistance(points2, points1, i);   // match points2 --> points1 starting with index point i
            minDistance = Math.min(minDistance, Math.min(dist1, dist2));
        }
        return minDistance;
    }

    /**
     * Computes the distance between two point clouds by performing a minimum-distance greedy matching
     * starting with point startIndex
     *
     * @param points1
     * @param points2
     * @param startIndex
     * @return
     */
    private static float CloudDistance(Point[] points1, Point[] points2, int startIndex)
    {
        int n = points1.length;       // the two clouds should have the same number of points by now
        boolean[] matched = new boolean[n]; // matched[i] signals whether point i from the 2nd cloud has been already matched
        //java.util.Arrays.fill(matched, 0, n, false);   // no points are matched at the beginning

        float sum = 0;  // computes the sum of distances between matched points (i.e., the distance between the two clouds)
        int i = startIndex;
        do
        {
            int index = -1;
            float minDistance = Float.MAX_VALUE;
            for(int j = 0; j < n; j++)
                if (!matched[j])
                {
                    float dist = Geometry.EuclideanDistance(points1[i], points2[j]);
                    if (dist < minDistance)
                    {
                        minDistance = dist;
                        index = j;
                    }
                }
            matched[index] = true; // point index from the 2nd cloud is matched to point i from the 1st cloud
            float weight = 1.0f - ((i - startIndex + n) % n) / (1.0f * n);
            sum += weight * minDistance; // weight each distance with a confidence coefficient that decreases from 1 to 0
            i = (i + 1) % n;
        } while (i != startIndex);
        return sum;
    }
}
