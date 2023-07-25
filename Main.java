import processing.core.PApplet;

import java.util.ArrayList;


public class Main extends PApplet
{
    Physics physics = new Physics();
    ArrayList<Object> bricks = new ArrayList<>();
    ArrayList<Integer> brickHealth = new ArrayList<>();
    public void setup()
    {
        windowResize(1000, 1000);
        frameRate(526);
        rectMode(2);
        fill(125,125,255,255);

        var obj = physics.CreateObject(new Vector3(500.0f, 1500.0f, 0f), new Vector2(75.0f, 25.0f)); obj.TYPE = Type.Static; //bat
        obj = physics.CreateObject(new Vector3(500.0f, 500.0f, 0f), 50);    obj.VELOCITY = new Vector2(-1.0f, -2.0f); //ball

        //init bricks,
        obj = physics.CreateObject(new Vector3(100.0f, 850.0f, 0f), new Vector2(75.0f, 50.0f)); obj.TYPE = Type.Static; bricks.add(obj); brickHealth.add(3);
        obj = physics.CreateObject(new Vector3(300.0f, 850.0f, 0f), new Vector2(75.0f, 50.0f)); obj.TYPE = Type.Static; bricks.add(obj); brickHealth.add(3);
        obj = physics.CreateObject(new Vector3(500.0f, 850.0f, 0f), new Vector2(75.0f, 50.0f)); obj.TYPE = Type.Static; bricks.add(obj); brickHealth.add(3);
        obj = physics.CreateObject(new Vector3(700.0f, 850.0f, 0f), new Vector2(75.0f, 50.0f)); obj.TYPE = Type.Static; bricks.add(obj); brickHealth.add(3);
        obj = physics.CreateObject(new Vector3(900.0f, 850.0f, 0f), new Vector2(75.0f, 50.0f)); obj.TYPE = Type.Static; bricks.add(obj); brickHealth.add(3);
        //row 2
        obj = physics.CreateObject(new Vector3(100.0f, 700.0f, 0f), new Vector2(75.0f, 50.0f)); obj.TYPE = Type.Static; bricks.add(obj); brickHealth.add(3);
        obj = physics.CreateObject(new Vector3(300.0f, 700.0f, 0f), new Vector2(75.0f, 50.0f)); obj.TYPE = Type.Static; bricks.add(obj); brickHealth.add(3);
        obj = physics.CreateObject(new Vector3(500.0f, 700.0f, 0f), new Vector2(75.0f, 50.0f)); obj.TYPE = Type.Static; bricks.add(obj); brickHealth.add(3);
        obj = physics.CreateObject(new Vector3(700.0f, 700.0f, 0f), new Vector2(75.0f, 50.0f)); obj.TYPE = Type.Static; bricks.add(obj); brickHealth.add(3);
        obj = physics.CreateObject(new Vector3(900.0f, 700.0f, 0f), new Vector2(75.0f, 50.0f)); obj.TYPE = Type.Static; bricks.add(obj); brickHealth.add(3);

        //now add bounds to screen
        obj = physics.CreateObject(new Vector3(500.0f,2000.0f,0f),new Vector2(1000.0f,1000.0f)); obj.TYPE = Type.Static; //t
        obj = physics.CreateObject(new Vector3(500.0f,-1000.0f,0f),new Vector2(1000.0f,1000.0f)); obj.TYPE = Type.Static;//b
        obj = physics.CreateObject(new Vector3(2000.0f,500.0f,0f),new Vector2(1000.0f,1000.0f));  obj.TYPE = Type.Static;//r
        obj = physics.CreateObject(new Vector3(-1000.0f,500.0f,0f),new Vector2(1000.0f,1000.0f)); obj.TYPE = Type.Static;//l
        delay(1000);
    }
    public void draw()
    {
        physics.PHYSICS_OBJECTS.get(0).POS = new Vector3((float)mouseX,100f,0.0f);
        physics.physicsTick(0.0f);
        background(255);


        //tick bricks
        for(int i = 0; i < bricks.size(); i++) {
            var obj = bricks.get(i);
            if (obj.touchingObject ) //ignore every object other than the circle
            {
                brickHealth.set(i, brickHealth.get(i) - 1);

                if(brickHealth.get(i) <= 0)
                {
                   bricks.get(i).TYPE = Type.Inactive;
                }
            }



            switch (brickHealth.get(i))
            {
                case 3:
                    fill(0,255,0,255);
                    rect(obj.POS.x,1000-obj.POS.y,obj.HIT_BOX_SQUARE.get(0).widthHeight.x,obj.HIT_BOX_SQUARE.get(0).widthHeight.y);
                    break;
                case 2:
                    fill(255,255,0,255);
                    rect(obj.POS.x,1000-obj.POS.y,obj.HIT_BOX_SQUARE.get(0).widthHeight.x,obj.HIT_BOX_SQUARE.get(0).widthHeight.y);
                    break;
                case 1:
                    fill(255,0,0,255);
                    rect(obj.POS.x,1000-obj.POS.y,obj.HIT_BOX_SQUARE.get(0).widthHeight.x,obj.HIT_BOX_SQUARE.get(0).widthHeight.y);
                    break;
                case 0: //if health is 0 do not draw
                    break;
            }
        }


        fill(125,125,255,255);
        for(var obj : physics.PHYSICS_OBJECTS) //draw objects
        {
            if (bricks.contains(obj)) //dont draw bricks, this is already done in the loop above
                continue;

            if(obj.HIT_BOX_SQUARE.size() != 0)
                rect(obj.POS.x,1000-obj.POS.y,obj.HIT_BOX_SQUARE.get(0).widthHeight.x,obj.HIT_BOX_SQUARE.get(0).widthHeight.y);
            else
                circle(obj.POS.x,1000 - obj.POS.y,obj.HIT_BOX_CIRCLE.get(0).RADIUS);
        }
    }
    public static void main(String[] args)
    {
        PApplet.main("Main");
    }
}
