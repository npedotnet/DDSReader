//
//  Shader.vsh
//  DDSGLViewer_iOS
//
// Copyright (c) 2015 Kenji Sasaki
// Released under the MIT license.
// https://github.com/npedotnet/DDSReader/blob/master/LICENSE
//
// English document
// https://github.com/npedotnet/DDSReader/blob/master/README.md
//
// Japanese document
// http://3dtech.jp/wiki/index.php?DDSReader
//

attribute vec4 position;
attribute vec2 texcoord;

varying lowp vec2 vTexcoord;

uniform mat4 modelViewProjectionMatrix;

void main()
{
    vTexcoord = texcoord;
    gl_Position = modelViewProjectionMatrix * position;
}
