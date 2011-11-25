#version 120

uniform sampler2D textureAtlas;

uniform float tick;

const float gamma = 2.2;
uniform float daylight = 1.0;
uniform bool swimming;

varying vec4 vertexWorldPos;

uniform vec4 playerPosition;

varying float fog;

vec4 srgbToLinear(vec4 color){
    return pow(color, vec4(1.0 / gamma));
}

vec4 linearToSrgb(vec4 color){
    return pow(color, vec4(gamma));
}

void main(){
    const vec4 grassCoordinate = vec4(0.0625 * 3, 0.0625 * 4, 0.0625 * 0, 0.0625 * 1);
    const vec4 waterCoordinate = vec4(0.0625 * 15, 0.0625 * 16, 0.0625 * 12, 0.0625 * 13);
    const vec4 lavaCoordinate = vec4(0.0625 * 15, 0.0625 * 16, 0.0625 * 15, 0.0625 * 16);

    vec4 texCoord = gl_TexCoord[0];

    float daylightTrans = pow(0.86, (1.0-daylight)*15.0);

    if (texCoord.x >= waterCoordinate.x && texCoord.x < waterCoordinate.y && texCoord.y >= waterCoordinate.z && texCoord.y < waterCoordinate.w) {
        texCoord.x -= waterCoordinate.x;
        texCoord.x *= 16;
        texCoord.y -= waterCoordinate.z;
        texCoord.y *= 16;
        texCoord.y /= 64;

        texCoord.y += mod(tick,48) * 1/64;
    } else if (texCoord.x >= lavaCoordinate.x && texCoord.x < lavaCoordinate.y && texCoord.y >= lavaCoordinate.z && texCoord.y < lavaCoordinate.w) {
        texCoord.x -= lavaCoordinate.x;
        texCoord.x *= 16;
        texCoord.y -= lavaCoordinate.z;
        texCoord.y *= 16;
        texCoord.y /= 128;

        texCoord.y += mod(tick,100) * 1/128;
    }

    vec4 color = texture2D(textureAtlas, vec2(texCoord));
    color = srgbToLinear(color);

    if (color.a < 0.1)
        discard;

    // APPLY TEXTURE OFFSET
    if (!(texCoord.x >= grassCoordinate.x && texCoord.x < grassCoordinate.y && texCoord.y >= grassCoordinate.z && texCoord.y < grassCoordinate.w)) {
        color.rgb *= gl_Color.rgb;
        color.a *= gl_Color.a;
    } else {
        // MASK GRASS
        if (texture2D(textureAtlas, vec2(texCoord.x + 3*0.0625, texCoord.y + 2*0.0625)).a > 0) {
            color.rgb *= gl_Color.rgb;
            color.a *= gl_Color.a;
        }
    }

    // CALCULATE DAYLIGHT AND BLOCKLIGHT
    float torchDistance = abs(length(vertexWorldPos));

    float daylightValue = clamp(daylightTrans, 0.0, 1.0) * pow(0.92, (1.0-gl_TexCoord[1].x)*15.0);
    float blocklightValue = gl_TexCoord[1].y;
    float occlusionValue = gl_TexCoord[1].z;

    vec3 daylightColorValue = vec3(daylightValue) * occlusionValue;
    vec3 blocklightColorValue = vec3(blocklightValue  + clamp(1.0-(torchDistance / 10), 0.0, 1.0) * (1.0-daylightTrans)) * occlusionValue;

    blocklightColorValue = clamp(blocklightColorValue,0.0,1.0);
    daylightColorValue = clamp(daylightColorValue, 0.0, 1.0);

    blocklightColorValue.x *= 1.2;
    blocklightColorValue.y *= 1.1;
    blocklightColorValue.z *= 0.7;

    color.xyz *= clamp(daylightColorValue + blocklightColorValue * (1.0-daylightValue), 0, 1);

    if (!swimming) {
        gl_FragColor.rgb = linearToSrgb(mix(color, vec4(0.89,0.945,1.0,1.0) * daylightTrans, clamp(fog, 0.0, 0.80))).rgb;
        gl_FragColor.a = color.a;
    } else {
        color.rg *= 0.6;
        color.b *= 0.9;
        gl_FragColor.rgb = linearToSrgb(color).rgb;
        gl_FragColor.a = color.a;
    }
}
