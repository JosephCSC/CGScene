package a4;

import org.joml.*;
import static java.lang.Math.*;

public class Plane
{
	private float[] vertices;
	private float[] texCoords;
	private float[] normals;


	public Plane()
	{
		initPlane();
	}
	
	private void initPlane()
	{
		vertices = new float[]
		{	-1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, -1.0f, 0.0f, -1.0f,
			 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, -1.0f,-1.0f, 0.0f, -1.0f,
		};
		
		
		
		texCoords = new float[]{
			0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,	
		};


		normals = new float[]{
			0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
			0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
		};
		
	}	
	
	public float[] getVertices() { return vertices; }
	public float[] getTexCoords() { return texCoords; }
	public float[] getNormals() {return normals;}

}