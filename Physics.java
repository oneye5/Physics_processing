import java.util.ArrayList;
import java.util.Collections;
import processing.core.PApplet;
 class Physics
{
    static ArrayList<Object> PHYSICS_OBJECTS = new ArrayList<>();
    static int TESTS_PER_HITBOX = 3;
    static float WORLD_GRAVITY = -0.05f;

    static void CreateObject(Vector3 pos,float radius)
    {
        Circle c = new Circle(new Vector2(0.0f,0.0f),radius);
        Object o = new Object(Object.Type.DynamicAngular,pos,new Square[]{},new Circle[]{c});
        PHYSICS_OBJECTS.add(o);
    }
    static Hit physicsTick(float delta)
    {
        tickVelocity();
       return checkHits();
    }
    static void tickVelocity()
    {
        for(var obj : PHYSICS_OBJECTS)
        {
            if(obj.TYPE == Object.Type.Static)
                continue;


            obj.VELOCITY.y +=  obj.GRAVITY_MULTIPLIER * Physics.WORLD_GRAVITY;

            obj.POS.x += obj.VELOCITY.x;
            obj.POS.y += obj.VELOCITY.y;
            System.out.println("velocity " + obj.VELOCITY.y);
        }
    }
    static Hit checkHits()
    {
        for (int i = 0; i < PHYSICS_OBJECTS.size(); i++)
        {
            var obj = PHYSICS_OBJECTS.get(i);
            var hit = checkHitAgainstAll(obj.HIT_BOX_CIRCLE[0]);
            return hit;
        }
        return null;
    }
    static Hit checkHitAgainstAll(Circle c1)
    {
        for(int i = 0; i < PHYSICS_OBJECTS.size(); i++)
        {
            if(i == c1.parentIndex)
                continue;

            //circle circle intersect
            Circle c2 = PHYSICS_OBJECTS.get(i).HIT_BOX_CIRCLE[0];

            Vector2 p1 = c1.ORIGIN.add(PHYSICS_OBJECTS.get(c1.parentIndex).POS);
            Vector2 p2 = c2.ORIGIN.add(PHYSICS_OBJECTS.get(c2.parentIndex).POS);

            var relativePos = p1.subtract(p2);
            float dist = relativePos.mag();
            if(dist > (c1.RADIUS + c2.RADIUS)/2.0f)
                continue;

            var hit = new Hit();
            Vector2 dir = relativePos.normalize();
            Vector2 hitPos = dir.multiply(dist - c1.RADIUS/2.0f).add(p2);

            hit.hitPosition = hitPos;



            //handle collision
            var obj1 = PHYSICS_OBJECTS.get(c1.parentIndex);
            var obj2 = PHYSICS_OBJECTS.get(c2.parentIndex);

            if(obj1.TYPE != Object.Type.Static && obj2.TYPE != Object.Type.Static)
            {
               var mag1 = obj1.VELOCITY.mag();
                var mag2 = obj2.VELOCITY.mag();

                obj1.VELOCITY = dir.multiply(-mag1);
                obj2.VELOCITY = dir.multiply(-mag2);
            }
            return hit;
        }
        return null;
    }
}
class Hit
{
    Vector2 hitPosition;
    float dist;
}
class Object
{
    Type TYPE;
    Vector2 SCALE = new Vector2(1.0f,1.0f);
    Vector3 POS;
    Vector2 VELOCITY;
    float ANGULAR_VEL;

    float MASS;
    float FRICTION;
    float ELASTICITY;
    float ROTATION;
    float GRAVITY_MULTIPLIER =1.0f;

    Square[] HIT_BOX_SQUARE;
    Circle[] HIT_BOX_CIRCLE;

    enum Type
    {
        Static,
        Dynamic,
        DynamicAngular;
    }


    public Object(Type t,Vector3 pos, Square[] hit_boxes, Circle[] hit_circle)
    {
        TYPE = t;
        POS = pos;
        HIT_BOX_SQUARE = hit_boxes;
        HIT_BOX_CIRCLE = hit_circle;
        VELOCITY = new Vector2(0.0f,0.0f);
        //calculate mass from volume
        //transport center to centre of mass by shifting hit-boxes
    }


}
class Square
{
    Vector2 ORIGIN;
    Vector2 BRCORNER;
    void Square(Vector2 origin,Vector2 bottomRight)
    {
        ORIGIN = origin;
        BRCORNER = bottomRight;
    }
}
class Circle
{
    Vector2 ORIGIN;
    float RADIUS;
    int parentIndex;
    public Circle(Vector2 origin,float radius)
    {
        ORIGIN = origin;
        RADIUS = radius;
        parentIndex = Physics.PHYSICS_OBJECTS.size();
    }
}

class Vector2 {
    public float x;
    public float y;

    public Vector2(Float X, Float Y) {
        x = X;
        y = Y;
    }

    public Vector2(String X, String Y) {
        x = Float.valueOf(X);
        y = Float.valueOf(Y);
    }

    public Vector2 add(Vector2 v) {
        Vector2 out = new Vector2(0.0f, 0.0f);
        out.x = x + v.x;
        out.y = y + v.y;
        return out;
    }
    public Vector2 add(Vector3 v) {
        Vector2 out = new Vector2(0.0f, 0.0f);
        out.x = x + v.x;
        out.y = y + v.y;
        return out;
    }
    public Vector2 subtract(Vector2 v) {
        Vector2 out = new Vector2(0.0f, 0.0f);
        out.x = x - v.x;
        out.y = y - v.y;
        return out;
    }

    public Vector2 multiply(Vector2 v) {
        Vector2 out = new Vector2(0.0f, 0.0f);
        out.x = x * v.x;
        out.y = y * v.y;
        return out;
    }

    public Vector2 multiply(Float n) {
        Vector2 out = new Vector2(0.0f, 0.0f);
        out.x = x * n;
        out.y = y * n;
        return out;
    }

    public Vector2 divide(Float n) {
        Vector2 out = new Vector2(0.0f, 0.0f);
        out.x = x / n;
        out.y = y / n;
        return out;
    }

    public Vector2 divide(Vector2 n) {
        Vector2 out = new Vector2(0.0f, 0.0f);
        out.x = x / n.x;
        out.y = y / n.y;
        return out;
    }


    static Vector2 clipRot(Vector2 v) {
        Vector2 out = new Vector2(0.0f, 0.0f);
        if (v.x > 90.0f)
            out.x = 90.0f;
        if (v.x < -90.0f)
            out.x = -90.0f;

        if (v.y > 180.0f)
            out.y = v.y - 360.0f;
        if (v.y < -180.0f)
            out.y = v.y + 360.0f;

        return out;
    }

    static Vector2 clipRot(float x, float y) {
        Vector2 out = new Vector2(0.0f, 0.0f);
        if (x > 90.0f)
            out.x = 90.0f;
        if (x < -90.0f)
            out.x = -90.0f;

        if (y > 180.0f)
            out.y = y - 360.0f;
        if (y < -180.0f)
            out.y = y + 360.0f;

        return out;
    }
     float dist(Vector2 p2)
    {
        Vector2 p1 = this;
        var v = p1.subtract(p2);
        return v.mag();
    }
    float mag()
    {
        return (float)Math.sqrt((this.x*this.x) + (this.y * this.y));
    }
    Vector2 normalize()
    {
      float mag = this.mag();
      return this.divide(mag);
    }
}


class Vector3
{
    float x;
    float y;
    float z;

    public static Vector3 subtract(Vector3 x,Vector3 v)
    {
        Vector3 out = new Vector3(0.0f,0.0f,0.0f);
        out.x = x.x - v.x;
        out.y = x.y - v.y;
        out.z = x.z - v.z;
        return out;
    }
    public static Vector3 add(Vector3 x,Vector3 v)
    {
        Vector3 out = new Vector3(0.0f,0.0f,0.0f);
        out.x = x.x + v.x;
        out.y = x.y + v.y;
        out.z = x.z + v.z;
        return out;
    }

    public Vector3(Float X, Float Y,Float Z)
    {
        x = X;
        y = Y;
        z = Z;
    }
    public Vector3(String X,String Y,String Z)
    {
        x = Float.valueOf(X);
        y = Float.valueOf(Y);
        z = Float.valueOf(Z);
    }
    public static String toStr(Vector3 in)
    {
        String out = "Vector3(x: ";
        out += in.x;
        out+= " y: ";
        out+= in.y;
        out += " z: ";
        out += in.z + " )";
        return out;
    }
    public Vector3 add(Vector3 v)
    {
        Vector3 out = new Vector3(0.0f,0.0f,0.0f);
        out.x = x + v.x;
        out.y = y + v.y;
        out.z = z + v.z;
        return out;
    }
    public Vector3 subtract(Vector3 v)
    {
        Vector3 out = new Vector3(0.0f,0.0f,0.0f);
        out.x = x - v.x;
        out.y = y - v.y;
        out.z = z - v.z;
        return out;
    }
    public Vector3 multiply(Vector3 v)
    {
        Vector3 out = new Vector3(0.0f,0.0f,0.0f);
        out.x = x * v.x;
        out.y = y * v.y;
        out.z = z * v.z;
        return out;
    }
    public Vector3 multiply(Float n)
    {
        Vector3 out = new Vector3(0.0f,0.0f,0.0f);
        out.x = x * n;
        out.y = y * n;
        out.z = z * n;
        return out;
    }
    public Vector3 divide(Float n)
    {
        Vector3 out = new Vector3(0.0f,0.0f,0.0f);
        out.x = x / n;
        out.y = y / n;
        out.z = z / n;
        return out;
    }
    public Vector3 divide(Vector3 n)
    {
        Vector3 out = new Vector3(0.0f,0.0f,0.0f);
        out.x = x / n.x;
        out.y = y / n.y;
        out.z = z / n.z;
        return out;
    }

    static Vector3 normalize(Vector3 x)
    {
        float mag = (float)Math.sqrt((x.x * x.x)+(x.y * x.y) +( x.z * x.z));
        return x.divide(mag);
    }
}




/*

import java.awt.geom.Point2D;

public class CircleIntersection {

    public static Point2D[] findIntersectionPoints(double x1, double y1, double r1, double x2, double y2, double r2) {
        double d = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));

        // Check for no intersection or one intersection (tangent)
        if (d > r1 + r2 || d < Math.abs(r1 - r2)) {
            return new Point2D[0]; // No intersection
        }

        // Calculate the angle between the line connecting the centers and the x-axis
        double theta = Math.atan2(y2 - y1, x2 - x1);

        // Calculate the coordinates of the intersection points
        double intersectionX1 = x1 + r1 * Math.cos(theta);
        double intersectionY1 = y1 + r1 * Math.sin(theta);
        double intersectionX2 = x1 - r1 * Math.cos(theta);
        double intersectionY2 = y1 - r1 * Math.sin(theta);

        return new Point2D[] { new Point2D.Double(intersectionX1, intersectionY1),
                               new Point2D.Double(intersectionX2, intersectionY2) };
    }

    public static void main(String[] args) {
        // Circle 1: Center (2, 3), Radius 4
        double x1 = 2;
        double y1 = 3;
        double r1 = 4;

        // Circle 2: Center (6, 7), Radius 3
        double x2 = 6;
        double y2 = 7;
        double r2 = 3;

        Point2D[] intersections = findIntersectionPoints(x1, y1, r1, x2, y2, r2);

        if (intersections.length == 0) {
            System.out.println("The circles do not intersect.");
        } else if (intersections.length == 1) {
            System.out.println("The circles are tangent at point (" + intersections[0].getX() + ", " + intersections[0].getY() + ").");
        } else {
            System.out.println("The circles intersect at two points:");
            System.out.println("Point 1: (" + intersections[0].getX() + ", " + intersections[0].getY() + ")");
            System.out.println("Point 2: (" + intersections[1].getX() + ", " + intersections[1].getY() + ")");
        }
    }
}


 */