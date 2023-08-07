package a4;

import org.joml.*;
import static java.lang.Math.*;

public class Camera
{
	private Vector3f loc;
	
	private Vector3f u = new Vector3f(1.0f, 0.0f, 0.0f); //Vector for U
	private Vector3f v = new Vector3f(0.0f, 1.0f, 0.0f);  //Vector for v
	private Vector3f n = new Vector3f(0.0f, 0.0f, -1.0f);  // Vector for n

	private Matrix4f vMat = new Matrix4f();
	private Matrix4f vMatT = new Matrix4f();
	private Matrix4f vMatR = new Matrix4f();
	
	public Camera()
	{	
		loc = new Vector3f(0.0f, 2.0f, 8f);
	}
	
	public Camera(float x, float y, float z){
		loc = new Vector3f(x, y, z);
	}

	public Camera(Vector3f p){
		loc =p;
	}
	
	private void initCamera()
	{	
	}
	
	public Vector3f getU(){return u;}
	public Vector3f getV(){return v;}
	public Vector3f getN(){return n;}

	public void setLocation(Vector3f newLoc){loc = newLoc;}
	
	public Vector3f getLocation(){return loc;}

	public Matrix4f getViewM(){
		vMatT.identity();
		vMatT.set(  1.0f, 0.0f, 0.0f, 0.0f,
				0.0f, 1.0f, 0.0f, 0.0f,
				0.0f, 0.0f, 1.0f, 0.0f,
				-loc.x(), -loc.y(), -loc.z(), 1.0f);

		vMatR.identity();
		vMatR.set(  u.x(), v.x(), -n.x(), 0.0f,
			        u.y(), v.y(), -n.y(), 0.0f,
			        u.z(), v.z(), -n.z(), 0.0f,
			         0.0f,  0.0f,  0.0f,  1.0f);
		
		vMat.identity();
		vMat.mul(vMatR);
		vMat.mul(vMatT);
		
		return vMat;
	}

	public void yaw(float degree){
		Vector3f upVector, rightVector, fwdVector;
		
		rightVector = u;
		upVector = v;
		fwdVector = n;
		rightVector.rotateAxis(degree, upVector.x(), upVector.y(), upVector.z());
		fwdVector.rotateAxis(degree, upVector.x(), upVector.y(), upVector.z());
		u.set(rightVector);
		n.set(fwdVector);

	}
	public void pitch(float degree){
		Vector3f upVector, rightVector, fwdVector;
		
		rightVector = u;
		upVector = v;
		fwdVector = n;
		
		upVector.rotateAxis(degree, rightVector.x(), rightVector.y(), rightVector.z());
		fwdVector.rotateAxis(degree, rightVector.x(), rightVector.y(), rightVector.z());
		v.set(upVector);
		n.set(fwdVector);
	}
}