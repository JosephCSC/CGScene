package a4;

import java.nio.*;
import javax.swing.*;
import java.lang.Math;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.common.nio.Buffers;
import org.joml.*;

public class Light
{
	float[] ambient = new float[] { 0.1f, 0.1f, 0.1f, 1.0f };
	float[] diffuse = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
	float[] specular = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
	
	Vector3f position;
	
	public Light (Vector3f position){
		this.position = position;
	}
	
	public Light (Vector3f position, float[] ambient, float[] diffuse, float[] specular){
		this.position = position;
		this.ambient = ambient;
		this.diffuse = diffuse;
		this.specular = specular;
	}
	
	public float getX(){return position.x();}
	public float getY(){return position.y();}
	public float getZ(){return position.z();}
	public float[] getAmbient(){return ambient;}
	public float[] getDiffuse(){return diffuse;}
	public float[] getSpecular(){return specular;}
	public Vector3f getLocation(){return position;}
	
	public void setAmbient(float[] a){
		this.ambient = a;
	}
	public void setDiffuse(float[] d){this.diffuse = d;}
	public void setSpecular(float[] s){this.specular = s;}
	
	public void setLocation(Vector3f p){position = p;}
}