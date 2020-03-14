package com.ebookfrenzy.expressioneditor.recognizer;

public class Gesture {
    public Point[] Points = null;            // gesture points (normalized)
    public String Name = "";                 // gesture class

    private static final int SAMPLING_RESOLUTION = 32;                             // default number of points on the gesture path
    private static final int MAX_INT_COORDINATES = 1024;                           // $Q only: each point has two additional x and y integer coordinates in the interval [0..MAX_INT_COORDINATES-1] used to operate the LUT table efficiently (O(1))
    public static int LUT_SIZE = 64;                                        // $Q only: the default size of the lookup table is 64 x 64
    public static int LUT_SCALE_FACTOR = MAX_INT_COORDINATES / LUT_SIZE;    // $Q only: scale factor to convert between integer x and y coordinates and the size of the LUT

    public int[][] LUT = null;               // lookup table

    /**
     * Constructs a gesture from an array of points
     *
     * @param points
     * @param gestureName
     */
    public Gesture(Point[] points, String gestureName)
    {
        this(points, gestureName, true);
    }

    /**
     * Constructs a gesture from an array of points
     *
     * @param points
     */
    public Gesture(Point[] points)
    {
        this(points, "", true);
    }


    /**
     * Constructs a gesture from an array of points
     *
     * @param points
     * @param gestureName
     * @param constructLUT
     */
    public Gesture(Point[] points, String gestureName, boolean constructLUT)
    {
        this.Name = gestureName;

        // normalizes the array of points with respect to scale, origin, and number of points
        this.Points = Scale(points);
        this.Points = TranslateTo(Points, Centroid(Points));
        this.Points = Resample(Points, SAMPLING_RESOLUTION);

        if (constructLUT)
        {
            // constructs a lookup table for fast lower bounding (used by $Q)
            this.TransformCoordinatesToIntegers();
            this.ConstructLUT();
        }
    }

    /**
     * Constructs a gesture from an array of points
     *
     * @param points
     * @param constructLUT
     */
    public Gesture(Point[] points, boolean constructLUT)
    {
        this(points, "", constructLUT);
    }

    //region gesture pre-processing steps: scale normalization, translation to origin, and resampling

    /**
     * Performs scale normalization with shape preservation into [0..1]x[0..1]
     *
     * @param points
     * @return
     */
    private Point[] Scale(Point[] points)
    {
        float minx = Float.MAX_VALUE, miny = Float.MAX_VALUE, maxx = -Float.MAX_VALUE, maxy = -Float.MAX_VALUE;
        for (int i = 0; i < points.length; i++)
        {
            if (minx > points[i].X) minx = points[i].X;
            if (miny > points[i].Y) miny = points[i].Y;
            if (maxx < points[i].X) maxx = points[i].X;
            if (maxy < points[i].Y) maxy = points[i].Y;
        }

        Point[] newPoints = new Point[points.length];
        float scale = Math.max(maxx - minx, maxy - miny);
        for (int i = 0; i < points.length; i++)
            newPoints[i] = new Point((points[i].X - minx) / scale, (points[i].Y - miny) / scale, points[i].StrokeID);
        return newPoints;
    }

    /**
     * Translates the array of points by p
     *
     * @param points
     * @param p
     * @return
     */
    private Point[] TranslateTo(Point[] points, Point p)
    {
        Point[] newPoints = new Point[points.length];
        for (int i = 0; i < points.length; i++)
            newPoints[i] = new Point(points[i].X - p.X, points[i].Y - p.Y, points[i].StrokeID);
        return newPoints;
    }

    /**
     * Computes the centroid for an array of points
     *
     * @param points
     * @return
     */
    private Point Centroid(Point[] points)
    {
        float cx = 0, cy = 0;
        for (int i = 0; i < points.length; i++)
        {
            cx += points[i].X;
            cy += points[i].Y;
        }
        return new Point(cx / points.length, cy / points.length, 0);
    }

    /**
     * Resamples the array of points into n equally-distanced points
     *
     * @param points
     * @param n
     * @return
     */
    public Point[] Resample(Point[] points, int n)
    {
        Point[] newPoints = new Point[n];
        newPoints[0] = new Point(points[0].X, points[0].Y, points[0].StrokeID);
        int numPoints = 1;

        float I = PathLength(points) / (n - 1); // computes interval length
        float D = 0;
        for (int i = 1; i < points.length; i++)
        {
            if (points[i].StrokeID == points[i - 1].StrokeID)
            {
                float d = Geometry.EuclideanDistance(points[i - 1], points[i]);
                if (D + d >= I)
                {
                    Point firstPoint = points[i - 1];
                    while (D + d >= I)
                    {
                        // add interpolated point
                        float t = Math.min(Math.max((I - D) / d, 0.0f), 1.0f);
                        if (Float.isNaN(t)) t = 0.5f;
                        newPoints[numPoints++] = new Point(
                                (1.0f - t) * firstPoint.X + t * points[i].X,
                                (1.0f - t) * firstPoint.Y + t * points[i].Y,
                                points[i].StrokeID
                        );

                        // update partial length
                        d = D + d - I;
                        D = 0;
                        firstPoint = newPoints[numPoints - 1];
                    }
                    D = d;
                }
                else D += d;
            }
        }

        if (numPoints == n - 1) // sometimes we fall a rounding-error short of adding the last point, so add it if so
            newPoints[numPoints++] = new Point(points[points.length - 1].X, points[points.length - 1].Y, points[points.length - 1].StrokeID);
        return newPoints;
    }

    /**
     * Computes the path length for an array of points
     *
     * @param points
     * @return
     */
    private float PathLength(Point[] points)
    {
        float length = 0;
        for (int i = 1; i < points.length; i++)
            if (points[i].StrokeID == points[i - 1].StrokeID)
                length += Geometry.EuclideanDistance(points[i - 1], points[i]);
        return length;
    }

    /**
     * Scales point coordinates to the integer domain [0..MAXINT-1] x [0..MAXINT-1]
     */
    private void TransformCoordinatesToIntegers()
    {
        for (int i = 0; i < Points.length; i++)
        {
            Points[i].intX = (int)((Points[i].X + 1.0f) / 2.0f * (MAX_INT_COORDINATES - 1));
            Points[i].intY = (int)((Points[i].Y + 1.0f) / 2.0f * (MAX_INT_COORDINATES - 1));
        }
    }

    /**
     * Constructs a Lookup Table that maps grip points to the closest point from the gesture path
     */
    private void ConstructLUT()
    {
        this.LUT = new int[LUT_SIZE][];
        for (int i = 0; i < LUT_SIZE; i++)
            LUT[i] = new int[LUT_SIZE];

        for (int i = 0; i < LUT_SIZE; i++)
            for (int j = 0; j < LUT_SIZE; j++)
            {
                int minDistance = Integer.MAX_VALUE;
                int indexMin = -1;
                for (int t = 0; t < Points.length; t++)
                {
                    int row = Points[t].intY / LUT_SCALE_FACTOR;
                    int col = Points[t].intX / LUT_SCALE_FACTOR;
                    int dist = (row - i) * (row - i) + (col - j) * (col - j);
                    if (dist < minDistance)
                    {
                        minDistance = dist;
                        indexMin = t;
                    }
                }
                LUT[i][j] = indexMin;
            }
    }
}
