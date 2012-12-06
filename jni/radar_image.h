#include <jni.h>
#include <android/log.h>
#include <exceptions.h>
#include <cmath>
#include <GLES/gl.h>

#define PI 3.14159265

#define VEL_BINS2 1200
#define VEL_BINS 920
#define REFL_BINS 460
#define LEGACY_BINS 230
#define LEGACY_OFFSET 4
#define REFL_OFFSET 2

static void copyArray(const int length, int* original, int* copy){
	for (int i = 0; i < length; i++)
		copy[i] = original[i];
}

static void copyArray(const int length, GLfloat* original, GLfloat* copy){
	for (int i = 0; i < length; i++)
		copy[i] = original[i];
}

class RadarImage{
public:
	RadarImage();
	~RadarImage();

	GLubyte getBlue(int color);
	GLubyte getRed(int color);
	GLubyte getGreen(int color);

	void init(JNIEnv* env, jobject paint_array, jobjectArray rle,
			unsigned short radial_bin_num);
	void changeAlpha(short new_alpha);
	void buffer();
	void unbuffer();
	void draw();

	bool is_init;

private:
	// This is because I chose to skip the first bin in
	// every radial to save on code complexity.
	const static unsigned int MIN_BIN_VALUE = 1;

	const static unsigned int R = 0;
	const static unsigned int G = 1;
	const static unsigned int B = 2;
	const static unsigned int A = 3;

	const static unsigned int X = 0;
	const static unsigned int Y = 1;

	const static unsigned int VERT1 = 0;
	const static unsigned int VERT2 = 1;
	const static unsigned int VERT3 = 2;
	const static unsigned int VERT4 = 3;

	/**
	 *   Vert2       Vert4 - This is the vertex that is the reference
	 *   *-----------*       to the color of the bin.
	 *   |           |
	 *   |           |  ----> Away from center.
	 *   |           |
	 *   *-----------*
	 *   Vert1       Vert3 - Calculating x-y coords based on radial index
	 *                       and bin index would give you this point.
	 */

	const static unsigned int TRIANGLE_OFFSET = 2;

	const static unsigned int VERTEX_OFFSET = 2;
	const static unsigned int INDICES_OFFSET = 3;
	const static unsigned int COLOR_OFFSET = 4;

	const static unsigned int MAX_VERTS_REUSED = 3;

	const static unsigned int RADIALS = 360;
	const static unsigned int SLICE_NUM = 15;
	const static unsigned int SLICE_SPREAD = RADIALS / SLICE_NUM;

	const static unsigned short DEFAULT_ALPHA = 0xC8;

	GLubyte* colors[SLICE_NUM];
	GLushort* indices[SLICE_NUM];
	GLfloat* verts[SLICE_NUM];
	GLushort vert_count[SLICE_NUM];
	int bin_count[SLICE_NUM];

	GLuint vbos[SLICE_NUM];
	GLuint cbos[SLICE_NUM];
	GLuint ibos[SLICE_NUM];
	GLubyte image_alpha;
	bool is_buffered;

	float toRadians(int angle_in_degrees);
	void initColorPalette(JNIEnv* env, jobject paint_array, int* &color_palette);
	void checkAdjacentBins(int bin_num, int radial_index, int bin_index, bool* corners,
			int* color_palette, GLubyte** values);

	void calcNewVerts(int bin_num, int slice_index, int radial_index, int bin_index,
			int* color_palette,	GLushort*** vert_inds, GLfloat* vert_array, GLubyte** values);

	void addVert(GLfloat* vert_array, int bin_num, int slice_index, int radial_index, int bin_index);
	void initVerts(int slice_index, GLfloat* vert_array);
	void initIndices(int bin_num, int slice_index, int* color_palette, GLushort*** inds, GLubyte** values);
	void initColors(int bin_num, int slice_index, int* color_palette, GLushort*** inds, GLubyte** values);
	void resetCorners(bool* corners);
};

RadarImage::RadarImage(){
	 image_alpha = DEFAULT_ALPHA;
	 is_buffered = false;
	 is_init = false;
}

void RadarImage::init(JNIEnv* env, jobject paint_array, jobjectArray rle,
		unsigned short radial_bin_num){
	int color;
	jbyteArray oneDim;
	jbyte* elements;

	int* color_palette;

	initColorPalette(env, paint_array, color_palette);

	GLubyte** values;
	GLushort*** vert_inds;
	GLfloat* temp_verts;

	values = new GLubyte* [SLICE_SPREAD];
	vert_inds = new GLushort** [SLICE_SPREAD];
	temp_verts = new GLfloat [(SLICE_SPREAD + 1) * radial_bin_num * VERTEX_OFFSET];


	for (int i = 0; i < SLICE_SPREAD; i++){
		values[i] = new GLubyte [radial_bin_num];
		vert_inds[i] = new GLushort* [radial_bin_num];
		for (int n = 0; n < radial_bin_num; n++)
			vert_inds[i][n] = new GLushort [4];
	}

	for (int slice = 0; slice < SLICE_NUM; slice++){

		bin_count[slice] = 0;
		vert_count[slice] = 0;

		for (int radial = 0; radial < SLICE_SPREAD; radial++){
			oneDim = (jbyteArray)env->GetObjectArrayElement(rle, (radial + slice * SLICE_SPREAD) % 360);
			elements = env->GetByteArrayElements(oneDim, NULL);

			if (elements == NULL)
				throwNPE(env, "elements is null");

			for (int bin = MIN_BIN_VALUE; bin < radial_bin_num; bin++){
				GLubyte value = (elements[bin] & 0xff);
				color = color_palette[value];

				if (color != 0){
					values[radial][bin] = (elements[bin] & 0xff);
					bin_count[slice]++;
					calcNewVerts(radial_bin_num, slice, radial, bin, color_palette,
							vert_inds, temp_verts, values);
				}
				else
					values[radial][bin] = 0;
			}
			env->ReleaseByteArrayElements(oneDim, elements, JNI_ABORT);
		}

		initVerts(slice, temp_verts);
		initIndices(radial_bin_num, slice, color_palette, vert_inds, values);
		initColors(radial_bin_num, slice, color_palette, vert_inds, values);
	}

	glGenBuffers(SLICE_NUM, vbos);
	glGenBuffers(SLICE_NUM, ibos);
	glGenBuffers(SLICE_NUM, cbos);

	for (int i = 0; i < SLICE_SPREAD; i++){
		for (int n = 0; n < radial_bin_num; n++)
			delete[] vert_inds[i][n];

		delete[] vert_inds[i];
		delete[] values[i];
	}

	delete[] values;
	delete[] vert_inds;
	delete[] temp_verts;

	delete[] color_palette;

	is_init = true;
	__android_log_print(ANDROID_LOG_INFO, "Colors", "Colors initialized.");
}

RadarImage::~RadarImage(){
	if (is_init){
		unbuffer();
		for (int i = 0; i < SLICE_NUM; i++){
			delete[] colors[i];
			delete[] verts[i];
			delete[] indices[i];
		}
	}
}

void RadarImage::initVerts(int slice_index, GLfloat* vert_array){
	verts[slice_index] = new GLfloat [vert_count[slice_index] * VERTEX_OFFSET];

	copyArray((int) vert_count[slice_index] * VERTEX_OFFSET, vert_array, verts[slice_index]);
}

void RadarImage::initIndices(int bin_num, int slice_index, int* color_palette,
		GLushort*** inds, GLubyte** values){
	indices[slice_index] = new GLushort [bin_count[slice_index] * INDICES_OFFSET * 2];

	int index = 0;
	for (int radial = 0; radial < SLICE_SPREAD; radial++){
		for (int bin = MIN_BIN_VALUE; bin < bin_num; bin++){
			if (color_palette[values[radial][bin]] != 0){
				indices[slice_index][index + VERT1] = inds[radial][bin][VERT2];
				indices[slice_index][index + VERT2] = inds[radial][bin][VERT1];
				indices[slice_index][index + VERT3] = inds[radial][bin][VERT4];

				indices[slice_index][INDICES_OFFSET + index + VERT1] = inds[radial][bin][VERT1];
				indices[slice_index][INDICES_OFFSET + index + VERT2] = inds[radial][bin][VERT3];
				indices[slice_index][INDICES_OFFSET + index + VERT3] = inds[radial][bin][VERT4];

				index += INDICES_OFFSET * 2;
			}
		}
	}
}

void RadarImage::initColors(int bin_num, int slice_index, int* color_palette,
		GLushort*** inds, GLubyte** values){
	colors[slice_index] = new GLubyte [vert_count[slice_index] * COLOR_OFFSET];

	int color, index = 0;
	for (int radial = 0; radial < SLICE_SPREAD; radial++){
		for (int bin = MIN_BIN_VALUE; bin < bin_num; bin++){
			color = color_palette[values[radial][bin]];
			if (color != 0){
				index = inds[radial][bin][VERT4] * COLOR_OFFSET;
				colors[slice_index][index + R] = getRed(color);
				colors[slice_index][index + G] = getGreen(color);
				colors[slice_index][index + B] = getBlue(color);
				colors[slice_index][index + A] = image_alpha;
			}
		}
	}
}

void RadarImage::changeAlpha(short new_alpha){
	if (is_init){
		image_alpha = new_alpha;

		for (int i = 0; i < SLICE_NUM; i++)
			for (int n = 0; n < vert_count[i]; n++)
				colors[i][n * COLOR_OFFSET + A] = (GLubyte) new_alpha;
	}
}

void RadarImage::addVert(GLfloat* vert_array, int bin_num, int slice_index,
		int radial_index, int bin_index){
	int index = vert_count[slice_index] * VERTEX_OFFSET;
	float angle = toRadians((radial_index + slice_index * SLICE_SPREAD) % RADIALS);
	vert_array[index + X] = (GLfloat)( ((double) bin_index + 1.0) / bin_num * cos(angle));
	vert_array[index + Y] = (GLfloat)( ((double) bin_index + 1.0) / bin_num * sin(angle));
	vert_count[slice_index]++;
}

void RadarImage::checkAdjacentBins(int bin_num, int radial_index, int bin_index, bool* corners,
		int* color_palette, GLubyte** values){

	resetCorners(corners);

	if (bin_index > MIN_BIN_VALUE && color_palette[values[radial_index][bin_index - 1]] != 0){
		corners[VERT1] = true;
		corners[VERT2] = true;
	}
	if (radial_index > 0){
		if (bin_index < bin_num - 1 && color_palette[values[radial_index - 1][bin_index + 1]] != 0){
			corners[VERT3] = true;
		}
		if (bin_index > MIN_BIN_VALUE && color_palette[values[radial_index - 1][bin_index - 1]] != 0){
			corners[VERT1] = true;
		}
		if (color_palette[values[radial_index - 1][bin_index]] != 0){
			corners[VERT1] = true;
			corners[VERT3] = true;
		}
	}
}

void RadarImage::calcNewVerts(int bin_num, int slice_index, int radial_index, int bin_index,
		int* color_palette,	GLushort*** vert_inds, GLfloat* vert_array, GLubyte** values){

	bool corners[MAX_VERTS_REUSED];
	resetCorners(corners);
	checkAdjacentBins(bin_num, radial_index, bin_index, corners, color_palette, values);

	if (corners[VERT1]){
		if (radial_index > 0 && color_palette[values[radial_index - 1][bin_index - 1]] != 0)
			vert_inds[radial_index][bin_index][VERT1] = vert_inds[radial_index - 1][bin_index - 1][VERT4];
		else if (radial_index > 0 && color_palette[values[radial_index - 1][bin_index]] != 0)
			vert_inds[radial_index][bin_index][VERT1] = vert_inds[radial_index - 1][bin_index][VERT2];
		else
			vert_inds[radial_index][bin_index][VERT1] = vert_inds[radial_index][bin_index - 1][VERT3];
	}
	else{
		vert_inds[radial_index][bin_index][VERT1] = vert_count[slice_index];
		addVert(vert_array, bin_num, slice_index, radial_index, bin_index - 1);
	}

	if (corners[VERT2])
		vert_inds[radial_index][bin_index][VERT2] = vert_inds[radial_index][bin_index - 1][VERT4];
	else{
		vert_inds[radial_index][bin_index][VERT2] = vert_count[slice_index];
		addVert(vert_array, bin_num, slice_index, radial_index + 1, bin_index - 1);
	}

	if (corners[VERT3])
		vert_inds[radial_index][bin_index][VERT3] = (color_palette[values[radial_index - 1][bin_index]] != 0 ?
				vert_inds[radial_index - 1][bin_index][VERT4] : vert_inds[radial_index - 1][bin_index + 1][VERT2]);
	else{
		vert_inds[radial_index][bin_index][VERT3] = vert_count[slice_index];
		addVert(vert_array, bin_num,  slice_index, radial_index, bin_index);
	}

	vert_inds[radial_index][bin_index][VERT4] = vert_count[slice_index];
	addVert(vert_array, bin_num, slice_index, radial_index + 1, bin_index);
}

void RadarImage::resetCorners(bool* corners){
	for (int i = 0; i < MAX_VERTS_REUSED; i++)
		corners[i] = false;
}

void RadarImage::buffer(){
	if (!is_buffered && is_init){
		for (int slice = 0; slice < SLICE_NUM; slice++){
			glBindBuffer(GL_ARRAY_BUFFER, vbos[slice]);
			glBufferData(GL_ARRAY_BUFFER, vert_count[slice] * VERTEX_OFFSET * sizeof(GLfloat),
					verts[slice], GL_STATIC_DRAW);

			glBindBuffer(GL_ARRAY_BUFFER, cbos[slice]);
			glBufferData(GL_ARRAY_BUFFER, vert_count[slice] * COLOR_OFFSET * sizeof(GLubyte),
					colors[slice], GL_STATIC_DRAW);

			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibos[slice]);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, bin_count[slice] * INDICES_OFFSET * 2 * sizeof(GLushort),
					indices[slice], GL_STATIC_DRAW);

		}

		is_buffered = true;
	}
}


void RadarImage::unbuffer(){
	if (is_buffered && is_init){
		glDeleteBuffers(SLICE_NUM, ibos);
		glDeleteBuffers(SLICE_NUM, vbos);
		glDeleteBuffers(SLICE_NUM, cbos);

		is_buffered = false;
	}
}

void RadarImage::draw(){
	if (is_init){
		glEnableClientState(GL_VERTEX_ARRAY);
		glEnableClientState(GL_COLOR_ARRAY);

		if (is_buffered)
			for (int i = 0; i < SLICE_NUM; i++){
				glBindBuffer(GL_ARRAY_BUFFER, vbos[i]);
				glVertexPointer(VERTEX_OFFSET, GL_FLOAT, 0, (GLvoid*)0);

				glBindBuffer(GL_ARRAY_BUFFER, cbos[i]);
				glColorPointer(COLOR_OFFSET, GL_UNSIGNED_BYTE, 0, (GLvoid*)0);

				glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibos[i]);
				glDrawElements(GL_TRIANGLES, bin_count[i] * INDICES_OFFSET * 2, GL_UNSIGNED_SHORT, (GLvoid*)0);
			}
		else
			for (int i = 0; i < SLICE_NUM; i++){
				glVertexPointer(VERTEX_OFFSET, GL_FLOAT, 0, verts[i]);
				glColorPointer(COLOR_OFFSET, GL_UNSIGNED_BYTE, 0, colors[i]);

				glDrawElements(GL_TRIANGLES, bin_count[i] * INDICES_OFFSET * 2, GL_UNSIGNED_SHORT, indices[i]);
			}

		glDisableClientState(GL_VERTEX_ARRAY);
		glDisableClientState(GL_COLOR_ARRAY);
	}
}

GLubyte RadarImage::getBlue(int color){
	return color & 0xff;
}

GLubyte RadarImage::getRed(int color){
	return (color >> 16) & 0xff;
}

GLubyte RadarImage::getGreen(int color){
	return (color >> 8) & 0xff;
}

float RadarImage::toRadians(int angle_in_degrees){
	return angle_in_degrees * PI / 180.0;
}

void RadarImage::initColorPalette(JNIEnv* env, jobject paint_array, int* &color_palette){
	jmethodID getcolors, getcolornum;
	jintArray color_array;
	jint* temp_colors;
	int color_num;

	jclass paintArray = env->FindClass("me/mattsutter/conditionred/products/PaintArray");

	if (paintArray == NULL)
		throwNPE(env, "Class not found");

	getcolornum = env->GetMethodID(paintArray, "getColorNum", "()I");
	color_num = (int) env->CallIntMethod(paint_array, getcolornum);
	color_palette = new int[color_num];

	getcolors = env->GetMethodID(paintArray, "getColors", "()[I");
	color_array = (jintArray) env->CallObjectMethod(paint_array, getcolors);

	// Just in case JNI doesn't give us a copy...
	temp_colors = env->GetIntArrayElements(color_array, NULL);
	copyArray(color_num, temp_colors, color_palette);
}


