//
//  Shader.fsh
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

varying lowp vec2 vTexcoord;

uniform sampler2D texture;

void main()
{
    gl_FragColor = texture2D(texture, vTexcoord);
}
