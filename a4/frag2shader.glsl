#version 430

in vec3 varyingNormal, varyingLightDir, varyingVertPos, varyingHalfVec;
in vec4 shadow_coord;
in vec2 tc;
in vec3 vertEyeSpacePos;
out vec4 fragColor;
 
struct PositionalLight
{	vec4 ambient, diffuse, specular;
	vec3 position;
};

struct Material
{	vec4 ambient, diffuse, specular;
	float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 m_matrix;
uniform mat4 v_matrix; 
uniform mat4 p_matrix;
uniform mat4 norm_matrix;
uniform mat4 shadowMVP;
layout (binding=0) uniform sampler2DShadow shadowTex;
layout (binding=1) uniform sampler2D samp;

uniform float alpha;
uniform float flipNormal;

float lookup(float x, float y)
{  	float t = textureProj(shadowTex, shadow_coord + vec4(x * 0.001 * shadow_coord.w,
                                                         y * 0.001 * shadow_coord.w,
                                                         -0.01, 0.0));
	return t;
}

void main(void)
{	float shadowFactor=0.0;
	
	vec3 L = normalize(varyingLightDir);
	vec3 N = normalize(varyingNormal);
	vec3 V = normalize(-v_matrix[3].xyz - varyingVertPos);
	vec3 H = normalize(varyingHalfVec);

	float swidth = 2.5;
	vec2 o = mod(floor(gl_FragCoord.xy), 2.0) * swidth;
	shadowFactor += lookup(-1.5*swidth + o.x,  1.5*swidth - o.y);
	shadowFactor += lookup(-1.5*swidth + o.x, -0.5*swidth - o.y);
	shadowFactor += lookup( 0.5*swidth + o.x,  1.5*swidth - o.y);
	shadowFactor += lookup( 0.5*swidth + o.x, -0.5*swidth - o.y);
	shadowFactor = shadowFactor / 4.0;

	// hi res PCF
	float width = 2.5;
	float endp = width * 3.0 + width/2.0;
	for (float m=-endp ; m<=endp ; m=m+width)
	{	for (float n=-endp ; n<=endp ; n=n+width)
		{	shadowFactor += lookup(m,n);
	}	}
	shadowFactor = shadowFactor / 64.0;

	// this would produce normal hard shadows
//	shadowFactor = lookup(0.0, 0.0);

	vec4 shadowColor = globalAmbient * material.ambient + light.ambient * material.ambient;
	
	vec4 lightedColor = light.diffuse * material.diffuse * max(dot(L,N),0.0)
				+ light.specular * material.specular
				* pow(max(dot(H,N),0.0),material.shininess*3.0);

	vec4 color = .3*texture(samp,tc) +  .7 * vec4((shadowColor.xyz + shadowFactor*(lightedColor.xyz)),1.0);

	//fog 
	vec4 fogColor = vec4(0.7, 0.8, 0.9, 1.0);	// bluish gray
	float fogStart = 0;
	float fogEnd = 10;
	
	float dist = length(vertEyeSpacePos.xyz);
	float fogFactor = clamp(((fogEnd-dist)/(fogEnd-fogStart)), 0.0, 1.0);

	fragColor =  mix(fogColor, color, fogFactor);

	// the following is added for transparency
	fragColor = vec4(fragColor.xyz, alpha);
}

