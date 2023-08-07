package a4;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.*;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.io.*;
import java.lang.Math;
import java.lang.String;
import java.nio.*;
import javax.swing.*;
import org.joml.*;
import org.joml.*;
import static com.jogamp.opengl.GL4.*;
import static java.lang.Math.*;


public class Code extends JFrame implements GLEventListener, KeyListener,
MouseMotionListener, MouseWheelListener
{	private GLCanvas myCanvas;
	private int renderingProgram1, renderingProgram2, renderingProgram3, 
				renderingEnv, rendering3Dtx;
	private int vao[] = new int[1];
	private int vbo[] = new int[32];

	// model stuff
	private ImportedModel pyramid, column, rock, bulb, clam, 
							shark, boat, rod;
	private Torus myTorus;
	private Sphere mySphere;
	private Plane plane = new Plane();
	private int numPyramidVertices, numTorusVertices, numTorusIndices, numColumnVertices,
				numRockVertices, numBulbVertices, numSphereVerts, numClamVertices, numSharkVertices, numBoatVertices, numRodVertices;
	private int marbleTexture, grassTexture, stoneTexture,
				rockTexture, bulbTexture, bulbTextureOff, waterTexture, skyboxTexture1, sharkTexture, boatTexture, rodTexture;
	
	// location of  pyramid, and camera
	private Vector3f pyrLoc = new Vector3f(10f, 2f, -20f);
	private Vector3f colLoc = new Vector3f(-5f, -2f, -10f);
	private Vector3f rockLoc = new Vector3f(0f, -2f, 0f);
	private Vector3f rockLoc2 = new Vector3f(1.5f, -2f, 1.5f);
	
	
	// white light properties
	private float[] globalAmbient = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
		
	// gold material
	private float[] GmatAmb = Utils.goldAmbient();
	private float[] GmatDif = Utils.goldDiffuse();
	private float[] GmatSpe = Utils.goldSpecular();
	private float GmatShi = Utils.goldShininess();
	
	// bronze material
	private float[] BmatAmb = Utils.bronzeAmbient();
	private float[] BmatDif = Utils.bronzeDiffuse();
	private float[] BmatSpe = Utils.bronzeSpecular();
	private float BmatShi = Utils.bronzeShininess();
	
	//default material
	private float[] DmatAmb = Utils.defaultAmbient();
	private float[] DmatDif = Utils.defaultDiffuse();
	private float[] DmatSpe = Utils.defaultSpecular();
	private float DmatShi = Utils.defaultShininess();
	
	//emerald material
	private float[] EmatAmb = Utils.defaultAmbient();
	private float[] EmatDif = Utils.defaultDiffuse();
	private float[] EmatSpe = Utils.defaultSpecular();
	private float EmatShi = Utils.defaultShininess();
	
	private float[] thisAmb, thisDif, thisSpe, matAmb, matDif, matSpe;
	private float thisShi, matShi;
	
	// shadow stuff
	private int scSizeX, scSizeY;
	private int [] shadowTex = new int[1];
	private int [] shadowBuffer = new int[1];
	private Matrix4f lightVmat = new Matrix4f();
	private Matrix4f lightPmat = new Matrix4f();
	private Matrix4f shadowMVP1 = new Matrix4f();
	private Matrix4f shadowMVP2 = new Matrix4f();
	private Matrix4f b = new Matrix4f();

	// allocate variables for display() function
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4f pMat = new Matrix4f();  // perspective matrix
	private Matrix4f vMat = new Matrix4f();  // view matrix
	private Matrix4f mMat = new Matrix4f();  // model matrix
	private Matrix4f mvMat = new Matrix4f(); // model-view matrix
	private Matrix4f invTrMat = new Matrix4f(); // inverse-transpose
	private int mLoc, vLoc, pLoc, nLoc, sLoc, mvLoc,
				 alphaLoc, flipLoc;
	private int globalAmbLoc, ambLoc, diffLoc, specLoc,
				posLoc, mambLoc, mdiffLoc, mspecLoc, mshiLoc;
	private float aspect;
	private Vector3f origin = new Vector3f(0.0f, 0.0f, 0.0f);
	private Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);

	//Camera
	private Vector3f cameraLoc = new Vector3f(0.0f, -1f, 5.0f);
	private Vector3f newCamLoc;  //creates cam
	private Camera cam = new Camera(cameraLoc);
	
	//light
	private Light light;
	private Vector3f initialLightLoc = new Vector3f(3f,3f,3f);
	private Vector3f currentLightPos = new Vector3f();
	private float[] lightPos = new float[3];

	//skybox
	private int skyboxTexture;
	private Cube cube = new Cube();
	
	private boolean on = true;
	
	//3dTexture
	private int stripeTexture;
	private int texWidth = 200;
	private int texHeight= 200;
	private int texDepth = 200;
	private double[][][] tex3Dpattern = new double[texWidth][texHeight][texDepth];
	
	private int rodNormalMap;

	public Code()
	{	setTitle("Homework4");
		setSize(800, 800);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		myCanvas.addKeyListener(this);
		myCanvas.addMouseMotionListener(this);
		myCanvas.addMouseWheelListener(this);
		this.add(myCanvas);
		this.setVisible(true);
		Animator animator = new Animator(myCanvas);
		animator.start();
	}

	public void display(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		drawSkyBox();
		
		currentLightPos.set(light.getLocation());
		
		lightVmat.identity().setLookAt(currentLightPos, origin, up);	// vector from light to origin
		lightPmat.identity().setPerspective((float) Math.toRadians(90.0f), aspect, 0.1f, 1000.0f);

		gl.glBindFramebuffer(GL_FRAMEBUFFER, shadowBuffer[0]);
		gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadowTex[0], 0);
	
		gl.glDrawBuffer(GL_NONE);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_POLYGON_OFFSET_FILL);	//  for reducing
		gl.glPolygonOffset(3.0f, 5.0f);		//  shadow artifacts
		
		passOne();
		
		gl.glDisable(GL_POLYGON_OFFSET_FILL);	// artifact reduction, continued
		
		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);
	
		gl.glDrawBuffer(GL_FRONT);
		
		passTwo();
	}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void passOne()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		gl.glUseProgram(renderingProgram1);

		sLoc = gl.glGetUniformLocation(renderingProgram1, "shadowMVP");
		
		
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		
		//draw Plane
		mMat.identity();
		mMat.translate(0f, -2f, 0f);
		mMat.scale(20f);
		mMat.rotateY((float)Math.toRadians(40.0f));
		
		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);
		
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, 6);
		
		//draw column
		mMat.identity();
		mMat.translate(colLoc.x(), colLoc.y(), colLoc.z());
		mMat.scale(.25f);
		
		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);
		
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glDisable(GL_CULL_FACE);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, numColumnVertices);
		
		//draw rock
		mMat.identity();
		mMat.translate(rockLoc2.x()+.6f, rockLoc2.y(), rockLoc2.z());
		mMat.scale(.25f);
		mMat.rotateY((float)Math.toRadians(190.0f));
		
		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);
		
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glDisable(GL_CULL_FACE);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, numRockVertices);
		
		//draw sphere
		mMat.identity();
		mMat.translate(2.1f, -1.2f, 2.5f);
		mMat.scale(.1f);
		
		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);
		
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[20]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glDisable(GL_CULL_FACE);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVerts);
		
		//Shark
		mMat.identity();
		mMat.translate(-3f, -2.8f, 1f);
		mMat.scale(.75f);
		mMat.rotateY((float)Math.toRadians(63.0f));
		
		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);
		
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[26]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glDisable(GL_CULL_FACE);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, numSharkVertices);
		
		//clam
		mMat.identity();
		mMat.translate(2.1f, -1.4f, 2.3f);
		mMat.rotateY((float)Math.toRadians(-90.0f));
		
		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);
		
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[23]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glDisable(GL_CULL_FACE);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, numClamVertices);
		
		//boat
		mMat.identity();
		mMat.translate(0f, -2f, 0f);
		mMat.scale(1.25f);
		mMat.rotateY((float)Math.toRadians(-123.0f));
		
		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);
		
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glDisable(GL_CULL_FACE);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, numBoatVertices);
		
		
	}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void passTwo()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		gl.glUseProgram(renderingProgram2);
		
		mLoc = gl.glGetUniformLocation(renderingProgram2, "m_matrix");
		vLoc = gl.glGetUniformLocation(renderingProgram2, "v_matrix");
		pLoc = gl.glGetUniformLocation(renderingProgram2, "p_matrix");
		nLoc = gl.glGetUniformLocation(renderingProgram2, "norm_matrix");
		sLoc = gl.glGetUniformLocation(renderingProgram2, "shadowMVP");
		alphaLoc = gl.glGetUniformLocation(renderingProgram2, "alpha");
		flipLoc = gl.glGetUniformLocation(renderingProgram2, "flipNormal");
		
		vMat = cam.getViewM();
		
		//draw plane
		
		thisAmb = DmatAmb; // the plane is default
		thisDif = DmatDif;
		thisSpe = DmatSpe;
		thisShi = DmatShi;
		
		mMat.identity();
		mMat.translate(0f, -2f, 0f);
		mMat.scale(20f);
		
		currentLightPos.set(light.getLocation());
		installLights(renderingProgram2);
		
		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);
		
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		
		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		gl.glVertexAttribPointer(2,2,GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, waterTexture);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, 6);
		
		//draw column
		thisAmb = DmatAmb; // the plane is default
		thisDif = DmatDif;
		thisSpe = DmatSpe;
		thisShi = DmatShi;
		
		mMat.identity();
		mMat.translate(colLoc.x(), colLoc.y(), colLoc.z());
		mMat.scale(.5f);
		
		currentLightPos.set(light.getLocation());
		installLights(renderingProgram2);
		
		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);
		
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		
		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
		gl.glVertexAttribPointer(2,2,GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, marbleTexture);
		

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glDisable(GL_CULL_FACE);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, numColumnVertices);
		
		gl.glBindTexture(GL_TEXTURE_2D, 0);
		
		
		//draw sphere
		gl.glUseProgram(renderingEnv);
		
		vMat = cam.getViewM();
		
		mvLoc = gl.glGetUniformLocation(renderingEnv, "mv_matrix");
		pLoc = gl.glGetUniformLocation(renderingEnv, "p_matrix");
		nLoc = gl.glGetUniformLocation(renderingEnv, "norm_matrix");

		mMat.identity();
		mMat.translate(2.1f, -1.2f, 2.5f);
		mMat.scale(.1f);
		
		

		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);
		
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);

		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[20]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[22]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_CUBE_MAP, skyboxTexture1);


		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDepthFunc(GL_LEQUAL);
		
		
		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVerts);
		
		//CLAM
		gl.glUseProgram(rendering3Dtx);
		
		
		mvLoc = gl.glGetUniformLocation(rendering3Dtx, "mv_matrix");
		pLoc = gl.glGetUniformLocation(rendering3Dtx, "p_matrix");
		
		vMat = cam.getViewM();
		
		mMat.identity();
		mMat.translate(2.1f, -1.4f, 2.3f);
		mMat.rotateY((float)Math.toRadians(-90.0f));

		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);

		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[23]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_3D, stripeTexture);
		
		gl.glDisable(GL_CULL_FACE);
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, numClamVertices);
		
		
		//Draw Boat
		gl.glUseProgram(renderingProgram2);
		thisAmb = DmatAmb; // the plane is default
		thisDif = DmatDif;
		thisSpe = DmatSpe;
		thisShi = DmatShi;
		
		mMat.identity();
		mMat.translate(0f, -2f, 0f);
		mMat.scale(1.25f);
		mMat.rotateY((float)Math.toRadians(-123.0f));
		
		currentLightPos.set(light.getLocation());
		installLights(renderingProgram2);
		
		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);
		
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		
		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
		gl.glVertexAttribPointer(2,2,GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, boatTexture);
		

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glDisable(GL_CULL_FACE);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, numBoatVertices);
		gl.glBindTexture(GL_TEXTURE_2D, 0);
		
		//Draw rod
		thisAmb = DmatAmb; // the plane is default
		thisDif = DmatDif;
		thisSpe = DmatSpe;
		thisShi = DmatShi;
		
		mMat.identity();
		mMat.translate(.5f, -2f, 0f);
		mMat.scale(3f);
		mMat.rotateY((float)Math.toRadians(30.0f));
		
		currentLightPos.set(light.getLocation());
		installLights(renderingProgram2);
		
		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);
		
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		
		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[29]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[30]);
		gl.glVertexAttribPointer(2,2,GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(gl.GL_TEXTURE_2D, rodTexture);
		
		gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(gl.GL_TEXTURE_2D, rodNormalMap);
		

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[31]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glDisable(GL_CULL_FACE);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, numRodVertices);
		gl.glBindTexture(GL_TEXTURE_2D, 0);
		
		//Shark
		thisAmb = DmatAmb; // the plane is default
		thisDif = DmatDif;
		thisSpe = DmatSpe;
		thisShi = DmatShi;
		
		mMat.identity();
		mMat.translate(-3f, -2.8f, 1f);
		mMat.scale(.75f);
		mMat.rotateY((float)Math.toRadians(63.0f));
		
		currentLightPos.set(light.getLocation());
		installLights(renderingProgram2);
		
		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);
		
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		
		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[26]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[27]);
		gl.glVertexAttribPointer(2,2,GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, sharkTexture);
		

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[28]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glDisable(GL_CULL_FACE);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, numSharkVertices);
		gl.glBindTexture(GL_TEXTURE_2D, 0);
		
		
		//ROCK Transparency
		gl.glUseProgram(renderingProgram2);
		thisAmb = DmatAmb; // the plane is default
		thisDif = DmatDif;
		thisSpe = DmatSpe;
		thisShi = DmatShi;
		
		mMat.identity();
		mMat.translate(rockLoc2.x()+.6f, rockLoc2.y(), rockLoc2.z());
		mMat.scale(.25f);
		mMat.rotateY((float)Math.toRadians(190.0f));
		
		currentLightPos.set(light.getLocation());
		installLights(renderingProgram2);
		
		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);
		
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		
		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
		gl.glProgramUniform1f(renderingProgram2, alphaLoc, 1.0f);
		gl.glProgramUniform1f(renderingProgram2, flipLoc, 1.0f);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[15]);
		gl.glVertexAttribPointer(2,2,GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, rockTexture);
		

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[16]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		
		//Transparency
		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		gl.glBlendEquation(GL_FUNC_ADD);

		gl.glEnable(GL_CULL_FACE);
		
		gl.glCullFace(GL_FRONT);
		gl.glProgramUniform1f(renderingProgram2, alphaLoc, 0.3f);
		gl.glProgramUniform1f(renderingProgram2, flipLoc, -1.0f);
		gl.glDrawArrays(GL_TRIANGLES, 0, numRockVertices);
		
		gl.glCullFace(GL_BACK);
		gl.glProgramUniform1f(renderingProgram2, alphaLoc, 0.7f);
		gl.glProgramUniform1f(renderingProgram2, flipLoc, 1.0f);
		gl.glDrawArrays(GL_TRIANGLES, 0, numRockVertices);

		gl.glDisable(GL_BLEND);
		gl.glCullFace(GL_BACK);
	}
	
	public void drawSkyBox(){
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		vMat.identity().set(cam.getViewM());

		// draw cube map
		
		gl.glUseProgram(renderingProgram3);
		
		mMat.identity().setTranslation(cam.getLocation().x(), cam.getLocation().y(), cam.getLocation().z());
		
		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);
		
		mvLoc = gl.glGetUniformLocation(renderingProgram3, "mv_matrix");
		pLoc = gl.glGetUniformLocation(renderingProgram3, "p_matrix");
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
				
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, skyboxTexture);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);	     // cube is CW, but we are viewing the inside
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		gl.glEnable(GL_DEPTH_TEST);
		
		//draw bulb 
		mMat.identity().setTranslation(light.getLocation().x(), light.getLocation().y(), light.getLocation().z());
		mMat.scale(.05f);
		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);
		
		mvLoc = gl.glGetUniformLocation(renderingProgram3, "mv_matrix");
		pLoc = gl.glGetUniformLocation(renderingProgram3, "p_matrix");
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
				
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[17]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[18]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		if(on) {
			gl.glActiveTexture(GL_TEXTURE0);
			gl.glBindTexture(GL_TEXTURE_2D, bulbTexture);
		}
		else {
			gl.glActiveTexture(GL_TEXTURE0);
			gl.glBindTexture(GL_TEXTURE_2D, bulbTextureOff);
		}

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);	     // cube is CW, but we are viewing the inside
		gl.glDrawArrays(GL_TRIANGLES, 0, numBulbVertices);	
		
	}
	
	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		renderingProgram1 = Utils.createShaderProgram("a4/vert1shader.glsl", "a4/frag1shader.glsl");
		renderingProgram2 = Utils.createShaderProgram("a4/vert2shader.glsl", "a4/frag2shader.glsl");
		renderingProgram3 = Utils.createShaderProgram("a4/vert3shader.glsl", "a4/frag3shader.glsl");
		renderingEnv = Utils.createShaderProgram("a4/vertEnvShader.glsl", "a4/fragEnvShader.glsl");
		rendering3Dtx = Utils.createShaderProgram("a4/vert3DShader.glsl", "a4/frag3DShader.glsl");
		
		light = new Light(initialLightLoc);

		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
		
		skyboxTexture1 = Utils.loadCubeMap("cubeMap");
		gl.glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
		
		skyboxTexture = Utils.loadTexture("textures/night.jpg");
		marbleTexture = Utils.loadTexture("textures/marble.jpg");
		waterTexture = Utils.loadTexture("textures/water.jpg");
		rockTexture = Utils.loadTexture("textures/rock.jpg");
		bulbTexture = Utils.loadTexture("textures/lightBulbtx.png");
		bulbTextureOff = Utils.loadTexture("textures/lightBulbOfftx.png");
		sharkTexture = Utils.loadTexture("textures/shark.jpg");
		boatTexture = Utils.loadTexture("textures/boat.jpeg");
		rodTexture = Utils.loadTexture("textures/rod.png");
		rodNormalMap = Utils.loadTexture("textures/rodN.png");

		setupVertices();
		setupShadowBuffers();
		
		b.set(
			0.5f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.5f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.5f, 0.0f,
			0.5f, 0.5f, 0.5f, 1.0f);

		generate3Dpattern();	
		stripeTexture = load3DTexture();
	}
	
	private void setupShadowBuffers()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		scSizeX = myCanvas.getWidth();
		scSizeY = myCanvas.getHeight();
	
		gl.glGenFramebuffers(1, shadowBuffer, 0);
	
		gl.glGenTextures(1, shadowTex, 0);
		gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32,
						scSizeX, scSizeY, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
		
		// may reduce shadow border artifacts
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	}

	private void setupVertices()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		// pyramid definition
		boat = new ImportedModel("objects/boat.obj");
		numBoatVertices = boat.getNumVertices();
		Vector3f[] boVertices = boat.getVertices();
		Vector2f[] boTexCoord = boat.getTexCoords();
		Vector3f[] boNormals = boat.getNormals();
		
		float[] boatPvalues = new float[numBoatVertices*3];
		float[] boatNvalues = new float[numBoatVertices*3];
		float[] boatTvalues = new float[numBoatVertices*2];
		
		for (int i=0; i<numBoatVertices; i++)
		{	boatPvalues[i*3]   = (float) (boVertices[i]).x();
			boatPvalues[i*3+1] = (float) (boVertices[i]).y();
			boatPvalues[i*3+2] = (float) (boVertices[i]).z();
			boatTvalues[i*2]   = (float) (boTexCoord[i]).x();
			boatTvalues[i*2+1] = (float) (boTexCoord[i]).y();
			boatNvalues[i*3]   = (float) (boNormals[i]).x();
			boatNvalues[i*3+1] = (float) (boNormals[i]).y();
			boatNvalues[i*3+2] = (float) (boNormals[i]).z();
		}

		//Skybox
		float [] cubeVerts = cube.getVertices();
		float [] cubeTexts = cube.getTexCoords();
		
		//plane
		float [] planeVerts		= plane.getVertices();
		float [] planeTexCoord   	= plane.getTexCoords();
		float [] planeNorms		= plane.getNormals();
		
		// column
		column = new ImportedModel("objects/column.obj");
		numColumnVertices = column.getNumVertices();
		Vector3f[] cVertices = column.getVertices();
		Vector2f[] cTexCoord = column.getTexCoords();
		Vector3f[] cNormals = column.getNormals();
		
		float[] columnPValues = new float[numColumnVertices*3];
		float[] columnTValues = new float[numColumnVertices*2];
		float[] columnNvalues = new float[numColumnVertices*3];
		
		for (int i=0; i<numColumnVertices; i++)
		{	columnPValues[i*3]   = (float) (cVertices[i]).x();
			columnPValues[i*3+1] = (float) (cVertices[i]).y();
			columnPValues[i*3+2] = (float) (cVertices[i]).z();
			columnTValues[i*2]   = (float) (cTexCoord[i]).x();
			columnTValues[i*2+1] = (float) (cTexCoord[i]).y();
			columnNvalues[i*3]   = (float) (cNormals[i]).x();
			columnNvalues[i*3+1] = (float) (cNormals[i]).y();
			columnNvalues[i*3+2] = (float) (cNormals[i]).z();
		}
		
		//rock
		rock = new ImportedModel("objects/rock.obj");
		numRockVertices = rock.getNumVertices();
		Vector3f[] rVertices = rock.getVertices();
		Vector2f[] rTexCoord = rock.getTexCoords();
		Vector3f[] rNormals =  rock.getNormals();
		
		float[] rockPValues = new float[numRockVertices*3];
		float[] rockTValues = new float[numRockVertices*2];
		float[] rockNvalues = new float[numRockVertices*3];
		
		for (int i=0; i<numRockVertices; i++)
		{	rockPValues[i*3]   = (float) (rVertices[i]).x();
			rockPValues[i*3+1] = (float) (rVertices[i]).y();
			rockPValues[i*3+2] = (float) (rVertices[i]).z();
			rockTValues[i*2]   = (float) (rTexCoord[i]).x();
			rockTValues[i*2+1] = (float) (rTexCoord[i]).y();
			rockNvalues[i*3]   = (float) (rNormals[i]).x();
			rockNvalues[i*3+1] = (float) (rNormals[i]).y();
			rockNvalues[i*3+2] = (float) (rNormals[i]).z();
		}
		//bulb
		bulb = new ImportedModel("objects/bulb.obj");
		numBulbVertices = bulb.getNumVertices();
		Vector3f[] bVertices = bulb.getVertices();
		Vector2f[] bTexCoord = bulb.getTexCoords();
		Vector3f[] bNormals =  bulb.getNormals();
		
		float[] bulbPValues = new float[numBulbVertices*3];
		float[] bulbTValues = new float[numBulbVertices*2];
		float[] bulbNvalues = new float[numBulbVertices*3];
		
		for (int i=0; i<numBulbVertices; i++)
		{	bulbPValues[i*3]   = (float) (bVertices[i]).x();
			bulbPValues[i*3+1] = (float) (bVertices[i]).y();
			bulbPValues[i*3+2] = (float) (bVertices[i]).z();
			bulbTValues[i*2]   = (float) (bTexCoord[i]).x();
			bulbTValues[i*2+1] = (float) (bTexCoord[i]).y();
			bulbNvalues[i*3]   = (float) (bNormals[i]).x();
			bulbNvalues[i*3+1] = (float) (bNormals[i]).y();
			bulbNvalues[i*3+2] = (float) (bNormals[i]).z();
		}
		//clam
		clam = new ImportedModel("objects/clam.obj");
		numClamVertices = clam.getNumVertices();
		Vector3f[] clVertices = clam.getVertices();
		Vector2f[] clTexCoord = clam.getTexCoords();
		Vector3f[] clNormals =  clam.getNormals();
		
		float[] clamPValues = new float[numClamVertices*3];
		float[] clamTValues = new float[numClamVertices*2];
		float[] clamNvalues = new float[numClamVertices*3];
		
		for (int i=0; i<numClamVertices; i++)
		{	clamPValues[i*3]   = (float) (clVertices[i]).x();
			clamPValues[i*3+1] = (float) (clVertices[i]).y();
			clamPValues[i*3+2] = (float) (clVertices[i]).z();
			clamTValues[i*2]   = (float) (clTexCoord[i]).x();
			clamTValues[i*2+1] = (float) (clTexCoord[i]).y();
			clamNvalues[i*3]   = (float) (clNormals[i]).x();
			clamNvalues[i*3+1] = (float) (clNormals[i]).y();
			clamNvalues[i*3+2] = (float) (clNormals[i]).z();
		}
		
		
		//sphere
		mySphere = new Sphere(96);
		numSphereVerts = mySphere.getIndices().length;
		
		int[] indices = mySphere.getIndices();
		Vector3f[] spVert = mySphere.getVertices();
		Vector2f[] spTex = mySphere.getTexCoords();
		Vector3f[] spNorm = mySphere.getNormals();
		
		float[] spherePVs = new float[indices.length*3];
		float[] sphereTVs = new float[indices.length*2];
		float[] sphereNVs = new float[indices.length*3];
		
		for (int i=0; i<indices.length; i++)
		{	spherePVs[i*3] = (float) (spVert[indices[i]]).x;
			spherePVs[i*3+1] = (float) (spVert[indices[i]]).y;
			spherePVs[i*3+2] = (float) (spVert[indices[i]]).z;
			sphereTVs[i*2] = (float) (spTex[indices[i]]).x;
			sphereTVs[i*2+1] = (float) (spTex[indices[i]]).y;
			sphereNVs[i*3] = (float) (spNorm [indices[i]]).x;
			sphereNVs[i*3+1]= (float)(spNorm [indices[i]]).y;
			sphereNVs[i*3+2]=(float) (spNorm [indices[i]]).z;
		}
		
		//shark
		shark = new ImportedModel("objects/shark.obj");
		numSharkVertices = shark.getNumVertices();
		Vector3f[] shVertices = shark.getVertices();
		Vector2f[] shTexCoord = shark.getTexCoords();
		Vector3f[] shNormals =  shark.getNormals();
		
		float[] shPValues = new float[numSharkVertices*3];
		float[] shTValues = new float[numSharkVertices*2];
		float[] shNvalues = new float[numSharkVertices*3];
		
		for (int i=0; i<numSharkVertices; i++)
		{	shPValues[i*3]   = (float) (shVertices[i]).x();
			shPValues[i*3+1] = (float) (shVertices[i]).y();
			shPValues[i*3+2] = (float) (shVertices[i]).z();
			shTValues[i*2]   = (float) (shTexCoord[i]).x();
			shTValues[i*2+1] = (float) (shTexCoord[i]).y();
			shNvalues[i*3]   = (float) (shNormals[i]).x();
			shNvalues[i*3+1] = (float) (shNormals[i]).y();
			shNvalues[i*3+2] = (float) (shNormals[i]).z();
		}
		
		
		//Creating the rod
		rod = new ImportedModel("objects/rod.obj");
		numRodVertices = rod.getNumVertices();
		Vector3f[] roVertices = rod.getVertices();
		Vector2f[] roTexCoord = rod.getTexCoords();
		Vector3f[] roNormals =  rod.getNormals();
		
		float[] rPValues = new float[numRodVertices*3];
		float[] rTValues = new float[numRodVertices*2];
		float[] rNvalues = new float[numRodVertices*3];
		
		for (int i=0; i<numRodVertices; i++)
		{	rPValues[i*3]   = (float) (roVertices[i]).x();
			rPValues[i*3+1] = (float) (roVertices[i]).y();
			rPValues[i*3+2] = (float) (roVertices[i]).z();
			rTValues[i*2]   = (float) (roTexCoord[i]).x();
			rTValues[i*2+1] = (float) (roTexCoord[i]).y();
			rNvalues[i*3]   = (float) (roNormals[i]).x();
			rNvalues[i*3+1] = (float) (roNormals[i]).y();
			rNvalues[i*3+2] = (float) (roNormals[i]).z();
		}

		// buffers definition
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);

		gl.glGenBuffers(vbo.length, vbo, 0);
	
		//  load the boat  vertices into the second buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer boatVertBuf = Buffers.newDirectFloatBuffer(boatPvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, boatVertBuf.limit()*4, boatVertBuf, GL_STATIC_DRAW);

		// load the boatnormal coordinates into the fourth buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		FloatBuffer boatNorBuf = Buffers.newDirectFloatBuffer(boatPvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, boatNorBuf.limit()*4, boatNorBuf, GL_STATIC_DRAW);
		
		
		//boat Texture
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(boatTvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);
		
		
		//sKYBOX
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		FloatBuffer cvertBuf = Buffers.newDirectFloatBuffer(cubeVerts);
		gl.glBufferData(GL_ARRAY_BUFFER, cvertBuf.limit()*4, cvertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		FloatBuffer ctexBuf = Buffers.newDirectFloatBuffer(cubeTexts);
		gl.glBufferData(GL_ARRAY_BUFFER, ctexBuf.limit()*4, ctexBuf, GL_STATIC_DRAW);
		
		//Plane
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		FloatBuffer vertBuf2 = Buffers.newDirectFloatBuffer(planeVerts);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf2.limit()*4, vertBuf2, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		FloatBuffer texBuf2 = Buffers.newDirectFloatBuffer(planeTexCoord);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf2.limit()*4, texBuf2, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		FloatBuffer norBuf2 = Buffers.newDirectFloatBuffer(planeNorms);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf2.limit()*4, norBuf2, GL_STATIC_DRAW);
		
		//column
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		FloatBuffer vertBuf3 = Buffers.newDirectFloatBuffer(columnPValues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf3.limit()*4, vertBuf3, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
		FloatBuffer texBuf3 = Buffers.newDirectFloatBuffer(columnTValues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf3.limit()*4, texBuf3, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
		FloatBuffer norBuf3 = Buffers.newDirectFloatBuffer(columnNvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf3.limit()*4, norBuf3, GL_STATIC_DRAW);
		
		//rock
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
		FloatBuffer vertBuf4 = Buffers.newDirectFloatBuffer(rockPValues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf4.limit()*4, vertBuf4, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[15]);
		FloatBuffer texBuf4 = Buffers.newDirectFloatBuffer(rockTValues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf4.limit()*4, texBuf4, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[16]);
		FloatBuffer norBuf4 = Buffers.newDirectFloatBuffer(rockNvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf4.limit()*4, norBuf4, GL_STATIC_DRAW);
		
		//rock
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[17]);
		FloatBuffer vertBuf5 = Buffers.newDirectFloatBuffer(bulbPValues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf5.limit()*4, vertBuf5, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[18]);
		FloatBuffer texBuf5 = Buffers.newDirectFloatBuffer(bulbTValues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf5.limit()*4, texBuf5, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[19]);
		FloatBuffer norBuf5 = Buffers.newDirectFloatBuffer(bulbNvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf5.limit()*4, norBuf5, GL_STATIC_DRAW);
		
		//sphere
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[20]);
		FloatBuffer vertBuf6 = Buffers.newDirectFloatBuffer(spherePVs);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf6.limit()*4, vertBuf6, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[21]);
		FloatBuffer texBuf6 = Buffers.newDirectFloatBuffer(sphereTVs);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf6.limit()*4, texBuf6, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[22]);
		FloatBuffer norBuf6 = Buffers.newDirectFloatBuffer(sphereNVs);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf6.limit()*4, norBuf6, GL_STATIC_DRAW);
		
		//clam
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[23]);
		FloatBuffer vertBuf7 = Buffers.newDirectFloatBuffer(clamPValues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf7.limit()*4, vertBuf7, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[24]);
		FloatBuffer texBuf7 = Buffers.newDirectFloatBuffer(clamTValues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf7.limit()*4, texBuf7, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[25]);
		FloatBuffer norBuf7 = Buffers.newDirectFloatBuffer(clamNvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf7.limit()*4, norBuf7, GL_STATIC_DRAW);
		
		//shark
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[26]);
		FloatBuffer vertBuf8 = Buffers.newDirectFloatBuffer(shPValues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf8.limit()*4, vertBuf8, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[27]);
		FloatBuffer texBuf8 = Buffers.newDirectFloatBuffer(shTValues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf8.limit()*4, texBuf8, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[28]);
		FloatBuffer norBuf8 = Buffers.newDirectFloatBuffer(shNvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf8.limit()*4, norBuf8, GL_STATIC_DRAW);
		
		//shark
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[29]);
		FloatBuffer vertBuf9 = Buffers.newDirectFloatBuffer(rPValues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf9.limit()*4, vertBuf9, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[30]);
		FloatBuffer texBuf9 = Buffers.newDirectFloatBuffer(rTValues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf8.limit()*4, texBuf9, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[31]);
		FloatBuffer norBuf9 = Buffers.newDirectFloatBuffer(rNvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf9.limit()*4, norBuf9, GL_STATIC_DRAW);
		
	}
	
	private void installLights(int renderingProgram)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		lightPos[0]=light.getX(); lightPos[1]=light.getY(); lightPos[2]=light.getZ();
		
		// set current material values
		matAmb = thisAmb;
		matDif = thisDif;
		matSpe = thisSpe;
		matShi = thisShi;
		
		// get the locations of the light and material fields in the shader
		globalAmbLoc = gl.glGetUniformLocation(renderingProgram, "globalAmbient");
		ambLoc = gl.glGetUniformLocation(renderingProgram, "light.ambient");
		diffLoc = gl.glGetUniformLocation(renderingProgram, "light.diffuse");
		specLoc = gl.glGetUniformLocation(renderingProgram, "light.specular");
		posLoc = gl.glGetUniformLocation(renderingProgram, "light.position");
		mambLoc = gl.glGetUniformLocation(renderingProgram, "material.ambient");
		mdiffLoc = gl.glGetUniformLocation(renderingProgram, "material.diffuse");
		mspecLoc = gl.glGetUniformLocation(renderingProgram, "material.specular");
		mshiLoc = gl.glGetUniformLocation(renderingProgram, "material.shininess");
	
		//  set the uniform light and material values in the shader
		gl.glProgramUniform4fv(renderingProgram, globalAmbLoc, 1, globalAmbient, 0);
		gl.glProgramUniform4fv(renderingProgram, ambLoc, 1, light.getAmbient(), 0);
		gl.glProgramUniform4fv(renderingProgram, diffLoc, 1, light.getDiffuse(), 0);
		gl.glProgramUniform4fv(renderingProgram, specLoc, 1, light.getSpecular(), 0);
		gl.glProgramUniform3fv(renderingProgram, posLoc, 1, lightPos, 0);
		gl.glProgramUniform4fv(renderingProgram, mambLoc, 1, matAmb, 0);
		gl.glProgramUniform4fv(renderingProgram, mdiffLoc, 1, matDif, 0);
		gl.glProgramUniform4fv(renderingProgram, mspecLoc, 1, matSpe, 0);
		gl.glProgramUniform1f(renderingProgram, mshiLoc, matShi);
	}

	public static void main(String[] args) { new Code(); }
	public void dispose(GLAutoDrawable drawable) {}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);

		setupShadowBuffers();
	}
	
	 //Takes Input
	public void keyTyped(KeyEvent e){}
	public void keyReleased(KeyEvent e){}
	public void keyPressed(KeyEvent e){
		Vector3f up = new Vector3f(cam.getV());
		Vector3f rt = new Vector3f(cam.getU());
		Vector3f fwd = new Vector3f(cam.getN());
		switch (e.getKeyCode()){
			case KeyEvent.VK_W :
				newCamLoc = cam.getLocation().add(fwd.mul(.1f));
				cam.setLocation(newCamLoc);
				break;
			case KeyEvent.VK_S :
				newCamLoc = cam.getLocation().add(fwd.mul(-.1f));
				cam.setLocation(newCamLoc);
				break;
			case KeyEvent.VK_A :
				newCamLoc = cam.getLocation().add(rt.mul(-.1f));
				cam.setLocation(newCamLoc);
				break;
			case KeyEvent.VK_D :
				newCamLoc = cam.getLocation().add(rt.mul(.1f));
				cam.setLocation(newCamLoc);
				break;
			case KeyEvent.VK_E :
				newCamLoc = cam.getLocation().add(up.mul(-.1f));
				cam.setLocation(newCamLoc);
				break;
			case KeyEvent.VK_Q :
				newCamLoc = cam.getLocation().add(up.mul(.1f));
				cam.setLocation(newCamLoc);
				break;
			case KeyEvent.VK_LEFT :	
			case KeyEvent.VK_J	:
				cam.yaw(.01f);
				break;
			case KeyEvent.VK_RIGHT :
			case KeyEvent.VK_L	:
				cam.yaw(-.01f);
				break;
			case KeyEvent.VK_UP :
			case KeyEvent.VK_I	:
				cam.pitch(.01f);
				break;
			case KeyEvent.VK_DOWN :
			case KeyEvent.VK_K	:
				cam.pitch(-.01f);
				break;
			case KeyEvent.VK_SPACE :
				break;
			case KeyEvent.VK_T :
				if(on){
					light.setDiffuse(new float[] { 0f,0f, 0f, 1.0f });
					light.setAmbient(new float[] { 0f, 0f, 0f, 1.0f });
					light.setSpecular(new float[] { 0f, 0f, 0f, 1.0f });
					on = !on;
				}
				else {
					light.setDiffuse(new float[] {   1.0f, 1.0f, 1.0f, 1.0f });
					light.setAmbient(new float[] {0.1f, 0.1f, 0.1f, 1.0f });
					light.setSpecular(new float[] { 1.0f, 1.0f, 1.0f, 1.0f });
					on = !on;
				}
				break;
			default: break;
		}
	}
	
	public void mouseMoved(MouseEvent e){}
	private float lastX;
	private float lastY;
	//Mouse motion events
	public void mouseDragged(MouseEvent e){
		Vector3f lightLocation = light.getLocation();
		if(e.getX() > lastX){
			Vector3f offset = new Vector3f(.02f, 0f, 0f);
			lightLocation.add(offset);
			light.setLocation(lightLocation);
		}
		else if (e.getX() < lastX){
			Vector3f offset = new Vector3f(-.02f, 0f, 0f);
			lightLocation.add(offset);
			light.setLocation(lightLocation);
		}
		if(e.getY() < lastY){
			Vector3f offset = new Vector3f(0f, .02f, 0f);
			lightLocation.add(offset);
			light.setLocation(lightLocation);
		}
		else if(e.getY() > lastY){
			Vector3f offset = new Vector3f(0f, -.02f, 0f);
			lightLocation.add(offset);
			light.setLocation(lightLocation);
		}
		lastX = e.getX();
		lastY = e.getY();
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e){
		Vector3f lightLocation = light.getLocation();
		if(e.getWheelRotation()>0){
			Vector3f offset = new Vector3f(0f, 0f, .05f);
			lightLocation.add(offset);
			light.setLocation(lightLocation);
		}
		else{
			Vector3f offset = new Vector3f(0f, 0f, -.05f);
			lightLocation.add(offset);
			light.setLocation(lightLocation);
		}
		
	}
	
	// 3D Texture section

	private void fillDataArray(byte data[])
	{ for (int i=0; i<texWidth; i++)
	  { for (int j=0; j<texHeight; j++)
	    { for (int k=0; k<texDepth; k++)
	      {
		if (tex3Dpattern[i][j][k] == 1.0)
		{	// bluish silver
			data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+0] = (byte) 99; //red
			data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+1] = (byte) 127; //green
			data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+2] = (byte) 154;   //blue
			data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+3] = (byte) 0;   //alpha
		}
		else
		{	// purple
			data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+0] = (byte) 89;   //red
			data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+1] = (byte) 20;   //green
			data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+2] = (byte) 41; //blue
			data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+3] = (byte) 0;   //alpha
		}
	} } } }

	private int load3DTexture()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		byte[] data = new byte[texWidth*texHeight*texDepth*4];
		
		fillDataArray(data);

		ByteBuffer bb = Buffers.newDirectByteBuffer(data);

		int[] textureIDs = new int[1];
		gl.glGenTextures(1, textureIDs, 0);
		int textureID = textureIDs[0];

		gl.glBindTexture(GL_TEXTURE_3D, textureID);

		gl.glTexStorage3D(GL_TEXTURE_3D, 1, GL_RGBA8, texWidth, texHeight, texDepth);
		gl.glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0,
				texWidth, texHeight, texDepth, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, bb);
		
		gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

		return textureID;
	}

	void generate3Dpattern()
	{	for (int x=0; x<texWidth; x++)
		{	for (int y=0; y<texHeight; y++)
			{	for (int z=0; z<texDepth; z++)
				{	if ((y/10)%2 == 0)
						tex3Dpattern[x][y][z] = 0.0;
					else
						tex3Dpattern[x][y][z] = 1.0;
	}	}	}	}
	
}