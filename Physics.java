import java.util.ArrayList;
import java.util.Arrays;


class Physics
{
    ArrayList<Object> PHYSICS_OBJECTS = new ArrayList<>();
    int TESTS_PER_HITBOX = 3; //placeholder
    float WORLD_GRAVITY = -0.0f;

    Object CreateObject(Vector3 pos,float radius)
    {
        Circle c = new Circle(new Vector2(0.0f,0.0f),radius,PHYSICS_OBJECTS.size());
        Object o = new Object(Type.DynamicAngular,pos,new ArrayList<Square>(),new ArrayList<Circle>(Arrays.asList(c)));

        //calculate volume to get mass
        float volume = radius*2.0f*3.1415f;
        o.MASS = volume;

        PHYSICS_OBJECTS.add(o);
        return PHYSICS_OBJECTS.get(PHYSICS_OBJECTS.size()-1);
    }
    Object CreateObject(Vector3 pos,Vector2 widthHeight)
    {
        Square c = new Square(new Vector2(0.0f,0.0f),widthHeight,PHYSICS_OBJECTS.size());
        Object o = new Object(Type.DynamicAngular,pos,new ArrayList<Square>(Arrays.asList(c)),new ArrayList<Circle>());

        float volume = widthHeight.x * widthHeight.y;
        o.MASS = volume;

        PHYSICS_OBJECTS.add(o);
        return PHYSICS_OBJECTS.get(PHYSICS_OBJECTS.size()-1);
    }
    void physicsTick(float delta)
    {
        tickVelocity();
        resolveIntersections();
    }
    void tickVelocity()
    {
        for(var obj : PHYSICS_OBJECTS) //changes the position by the velocity
        {
            if(obj.TYPE == Type.Static)
                continue;

            obj.VELOCITY.y +=  obj.GRAVITY_MULTIPLIER * WORLD_GRAVITY;

            obj.POS.x += obj.VELOCITY.x;
            obj.POS.y += obj.VELOCITY.y;
        }
    }

    void resolveIntersections()
    {
        for (var obj1p : PHYSICS_OBJECTS)
        {
            obj1p.touchingObject  = false;
            obj1p.otherObjIndex = -1;
            if(obj1p.TYPE == Type.Inactive)
                continue;

            for (var obj2p : PHYSICS_OBJECTS)
            {
                //region setup
                if(obj2p.TYPE == Type.Inactive)
                    continue;

                if (obj1p == obj2p)
                    continue;
                var obj1 = obj1p;
                var obj2 = obj2p;

                if (obj1.HIT_BOX_SQUARE.size() == 0 && obj2.HIT_BOX_SQUARE.size() != 0)
                {
                    obj2 = obj1p;
                    obj1 = obj2p;
                }
                //endregion
                //region circle circle test
                if (obj1.HIT_BOX_CIRCLE.size() != 0 && obj2.HIT_BOX_CIRCLE.size() != 0) //if both objects have circle hitboxes
                {
                    //circle circle intersect
                    Circle c1 = obj1.HIT_BOX_CIRCLE.get(0);
                    Circle c2 = obj2.HIT_BOX_CIRCLE.get(0);

                    Vector2 p1 = c1.ORIGIN.add(PHYSICS_OBJECTS.get(c1.parentIndex).POS);
                    Vector2 p2 = c2.ORIGIN.add(PHYSICS_OBJECTS.get(c2.parentIndex).POS);

                    var relativePos = p1.subtract(p2);
                    float dist = relativePos.mag();

                    if (dist > (c1.RADIUS + c2.RADIUS) / 2.0f)
                        continue; //no hit

                    Vector2 dir = relativePos.normalize();
                    Vector2 hitPos = dir.multiply(dist - c1.RADIUS / 2.0f).add(p2); //may use later

                    //handle collision
                    if (obj1.TYPE != Type.Static && obj2.TYPE != Type.Static)
                    {
                        var mag1 = obj1.VELOCITY.mag();
                        var mag2 = obj2.VELOCITY.mag();

                        obj1.VELOCITY = dir.multiply(mag1);
                        obj2.VELOCITY = dir.multiply(-mag2);
                    }
                    if (obj1.TYPE != Type.Static && obj2.TYPE ==Type.Static)
                    {
                        var mag1 = obj1.VELOCITY.mag();
                        obj1.VELOCITY = dir.multiply(mag1);
                    }
                    if (obj1.TYPE == Type.Static && obj2.TYPE != Type.Static)
                    {
                        var mag1 = obj2.VELOCITY.mag();
                        obj2.VELOCITY = dir.multiply(-mag1);
                    }
                    obj1.otherObjIndex = c2.parentIndex; obj1.touchingObject = true;
                    obj2.otherObjIndex = c1.parentIndex; obj2.touchingObject = true;
                }
                //endregion
                //region circle square
                if (obj1.HIT_BOX_SQUARE.size() != 0 && obj2.HIT_BOX_CIRCLE.size() != 0)
                {
                    Square c1 = obj1.HIT_BOX_SQUARE.get(0); //collider 1, square
                    var c2 = obj2.HIT_BOX_CIRCLE.get(0); //collider 2, circle

                    Vector2 p2 = c2.ORIGIN.add(PHYSICS_OBJECTS.get(c2.parentIndex).POS);
                    Vector2 p1 = c1.ORIGIN.add(PHYSICS_OBJECTS.get(c1.parentIndex).POS);

                    Vector2 relativePos = new Vector2(Math.abs(p1.x - p2.x), Math.abs(p1.y - p2.y));
                    Vector2 overlap = new Vector2(relativePos.x - (c2.RADIUS / 2.0f + c1.widthHeight.x), relativePos.y - (c2.RADIUS / 2.0f + c1.widthHeight.y));

                    if (overlap.x > 0.0f || overlap.y > 0.0f)
                        continue;

                    //make each object act on one another

                    Vector2 dir =new Vector2(0.0f,0.0f);
                    Vector2 centreDir = p1.subtract(p2).normalize();
                    //region calculate normal of square
                    //the likely side the hit is on is the smaller of the x and y overlap, this will give me the axis i need to worry about, and i can get which side by checking the dir
                    if(overlap.x > overlap.y)//worry about x axis
                    {
                        if(centreDir.x > 0)//right side
                        {
                            dir = new Vector2(1.0f,0.0f);
                        }
                        else //left side
                        {
                            dir = new Vector2(-1.0f,0.0f);
                        }
                    }
                    else  //worry about y axis
                    {
                        if(centreDir.y > 0)//top side
                        {
                            dir = new Vector2(0.0f,1.0f);
                        }
                        else //bot side
                        {
                            dir = new Vector2(0.0f,-1.0f);
                        }
                    }

                    //final step of determining the normal of the square, this checks if any of the corners lie inside the circle and gives a diagonal normal if so.
                    if(p2.x > p1.x + c1.widthHeight.x)//left side
                    {
                        if(p2.y > p1.y + c1.widthHeight.y)          //bottom left
                            dir = new Vector2(-1.0f,-1.0f);
                        if(p2.y < p1.y - c1.widthHeight.y)          //top left
                            dir = new Vector2(-1.0f,1.0f);
                    }
                    if(p2.x < p1.x - c1.widthHeight.x)              //right
                    {
                        if(p2.y > p1.y + c1.widthHeight.y)          //bottom right
                            dir = new Vector2(1.0f,-1.0f);
                        if(p2.y < p1.y - c1.widthHeight.y)          //top right
                            dir = new Vector2(1.0f,1.0f);
                    }
                    //endregion

                    //handle collision
                    if (obj1.TYPE != Type.Static && obj2.TYPE != Type.Static)
                    {
                        var mag1 = obj1.VELOCITY.mag();
                        var mag2 = obj2.VELOCITY.mag();

                        obj1.VELOCITY = dir.multiply(-mag1);
                        obj2.VELOCITY = dir.multiply(mag2);
                    }
                    if (obj1.TYPE != Type.Static && obj2.TYPE == Type.Static)
                    {
                        var mag1 = obj1.VELOCITY.mag();

                        obj1.VELOCITY = dir.multiply(mag1);
                    }
                    if (obj1.TYPE == Type.Static && obj2.TYPE != Type.Static)
                    {
                        if(dir.x != 0)
                            obj2.VELOCITY.x = dir.multiply(-Math.abs(obj2.VELOCITY.x)).x;
                        if(dir.y != 0)
                            obj2.VELOCITY.y = dir.multiply(-Math.abs(obj2.VELOCITY.y)).y;
                    }

                    obj1.otherObjIndex = c2.parentIndex; obj1.touchingObject = true;
                    obj2.otherObjIndex = c1.parentIndex; obj2.touchingObject = true;
                }
                //endregion
            }
        }
    }
}
enum Type
{
    Static,
    Dynamic,
    DynamicAngular, //default
    Inactive
}
class Object
{
    ///vars for use in other files
    Boolean touchingObject;
    int otherObjIndex;
    ///

    Type TYPE;
    Vector2 SCALE;
    Vector3 POS;
    Vector2 VELOCITY;
    float ANGULAR_VEL;

    float MASS;
    float FRICTION;
    float ELASTICITY;
    float ROTATION;
    float GRAVITY_MULTIPLIER = 1.0f;

    ArrayList<Square> HIT_BOX_SQUARE;
    ArrayList<Circle> HIT_BOX_CIRCLE;


    public Object(Type t,Vector3 pos,    ArrayList<Square> hit_boxes, ArrayList<Circle>  hit_circle)
    {
        TYPE = t;
        POS = pos;
        HIT_BOX_SQUARE = hit_boxes;
        HIT_BOX_CIRCLE = hit_circle;
        VELOCITY = new Vector2(0.0f,0.0f);
    }
}
class Square
{
    Vector2 ORIGIN;
    Vector2 widthHeight;
    int parentIndex;
    Square(Vector2 origin,Vector2 widthAndHeight, int parent)
    {
        ORIGIN = origin;
        widthHeight = widthAndHeight;
        parentIndex = parent;
    }
}
class Circle
{
    Vector2 ORIGIN;
    float RADIUS;
    int parentIndex;
    public Circle(Vector2 origin,float radius, int parent)
    {
        ORIGIN = origin;
        RADIUS = radius;
        parentIndex = parent;
    }
}

class Vector2 {
    public float x;
    public float y;

    public Vector2(Float X, Float Y)
    {
        x = X;
        y = Y;
    }

    public Vector2(String X, String Y)
    {
        x = Float.valueOf(X);
        y = Float.valueOf(Y);
    }
    float dotProduct(Vector2 v2)
    {
        return this.x * v2.x + this.y * v2.y;
    }
    public Vector2 add(Vector2 v)
    {
        Vector2 out = new Vector2(0.0f, 0.0f);
        out.x = x + v.x;
        out.y = y + v.y;
        return out;
    }
    public Vector2 add(Vector3 v)
    {
        Vector2 out = new Vector2(0.0f, 0.0f);
        out.x = x + v.x;
        out.y = y + v.y;
        return out;
    }
    public Vector2 subtract(Vector2 v)
    {
        Vector2 out = new Vector2(0.0f, 0.0f);
        out.x = x - v.x;
        out.y = y - v.y;
        return out;
    }

    public Vector2 multiply(Vector2 v)
    {
        Vector2 out = new Vector2(0.0f, 0.0f);
        out.x = x * v.x;
        out.y = y * v.y;
        return out;
    }

    public Vector2 multiply(Float n)
    {
        Vector2 out = new Vector2(0.0f, 0.0f);
        out.x = x * n;
        out.y = y * n;
        return out;
    }

    public Vector2 divide(Float n)
    {
        Vector2 out = new Vector2(0.0f, 0.0f);
        out.x = x / n;
        out.y = y / n;
        return out;
    }

    public Vector2 divide(Vector2 n)
    {
        Vector2 out = new Vector2(0.0f, 0.0f);
        out.x = x / n.x;
        out.y = y / n.y;
        return out;
    }


    Vector2 clipRot(Vector2 v)
    {
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

    Vector2 clipRot(float x, float y)
    {
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
        if(mag == 0)
            return new Vector2(0.0f,1.0f);
        return this.divide(mag);
    }
    Vector2 clamp()
    {
        Vector2 out = new Vector2(0.0f,0.0f);
        if (this.x > 1.0f)
            out.x = 1.0f;
        if(this.y > 1.0f)
            out.y = 1.0f;

        //now for negative

        if(this.x < -1.0f)
            out.x = -1.0f;
        if(this.y < -1.0f)
            out.y = -1.0f;

        return out;
    }
}


class Vector3
{
    float x;
    float y;
    float z;

    public  Vector3 subtract(Vector3 x,Vector3 v)
    {
        Vector3 out = new Vector3(0.0f,0.0f,0.0f);
        out.x = x.x - v.x;
        out.y = x.y - v.y;
        out.z = x.z - v.z;
        return out;
    }
    public Vector3 add(Vector3 x,Vector3 v)
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
    public String toStr(Vector3 in)
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

    Vector3 normalize(Vector3 x)
    {
        float mag = (float)Math.sqrt((x.x * x.x)+(x.y * x.y) +( x.z * x.z));
        return x.divide(mag);
    }
}
