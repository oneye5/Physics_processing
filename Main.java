import processing.core.PApplet;

import java.util.ArrayList;

public class Main extends PApplet
{
    Physics physics = new Physics();
    public void setup()
    {
     windowResize(1000,1000);
        physics.CreateObject(new Vector3(200.0f,200.0f,0f),200);
       // physics.PHYSICS_OBJECTS.get(0).TYPE = Object.Type.Static;
        physics.CreateObject(new Vector3(400.0f,600.0f,0f),200);
        physics.CreateObject(new Vector3(700.0f,800.0f,0f),100);
    }
    public void draw()
    {
       background(255);
        var hit = physics.physicsTick(0.0f);
        fill(0.0f,0.5f);


        for(int i = 0; i < physics.PHYSICS_OBJECTS.size(); i++)
        {
            circle(physics.PHYSICS_OBJECTS.get(i).POS.x, 1000 - physics.PHYSICS_OBJECTS.get(i).POS.y, physics.PHYSICS_OBJECTS.get(i).HIT_BOX_CIRCLE[0].RADIUS);
        }

        fill(255,0,255,125);
        if(hit != null)
        circle(hit.hitPosition.x,1000 - hit.hitPosition.y,10);
//


        physics.PHYSICS_OBJECTS.get(0).POS = new Vector3((float)mouseX,(float)mouseY,0.0f);
    }
    public static void main(String[] args)
    {
        PApplet.main("Main");
    }
}
