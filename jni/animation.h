#include <radar_image.h>
#include <string>
//#include <vector>
//#include <iterator>
#include <sstream>

class Animation{
public:
	Animation();
	~Animation();

//	void init(JNIEnv* env, jobject paint_array, jint radial_bin_num);
	void init(jint radial_bin_num, jint num_frames);
	void addFrame(JNIEnv* env, jobject paint_array, jobjectArray rle, jint index);
	void drawFrame(jint index);
	void changeAlpha(short new_alpha);
	void bufferFrame(jint index);
	void unbufferFrame(jint index);
	void reset();
private:
	const static unsigned int MAX_FRAMES = 10;

	unsigned short bin_num;
	unsigned short image_alpha;

	unsigned int frame_num;

	bool is_paused;
	bool is_init;

//	vector<RadarImage> frames;
	RadarImage** frames;
};

Animation::Animation(){
	image_alpha = 0xC8;
	bin_num = 0;
	frame_num = 0;
	is_init = false;
}

Animation::~Animation(){
	if (is_init){
		for (int i = 0; i < frame_num; i++)
			delete frames[i];
		delete[] frames;
	}
}

void Animation::drawFrame(jint index){
	if (is_init)
		frames[index]->draw();
}

void Animation::bufferFrame(jint index){
	if (is_init)
		frames[index]->buffer();
}

void Animation::unbufferFrame(jint index){
	if (is_init)
		frames[index]->unbuffer();
}

void Animation::init(jint radial_bin_num, jint num_frames){
	bin_num = radial_bin_num;

	if (num_frames > 0)
		frame_num = num_frames;
	else
		frame_num = MAX_FRAMES;

	frames = new RadarImage* [frame_num];

	for (int i = 0; i < frame_num; i++)
		frames[i] = new RadarImage;

	is_init = true;
}

void Animation::reset(){
	if (is_init){
		for (int i = 0; i < frame_num; i++)
			delete frames[i];
		delete[] frames;
	}

	is_init = false;
	bin_num = 0;
	frame_num = 0;
}

void Animation::addFrame(JNIEnv* env, jobject paint_array, jobjectArray rle, jint index){
	if (is_init)
		if (index >= 0 && index < frame_num){
			if (!frames[index]->is_init)
				frames[index]->init(env, paint_array, rle, bin_num);
			else{
				delete frames[frame_num - 1];
				for (int i = frame_num - 1; i > index; i--)
					frames[i] = frames[i + 1];
				frames[index] = new RadarImage;
				frames[index]->init(env, paint_array, rle, bin_num);
			}
		}
		else{
			std::string message ("Frame Index: ");
			std::string result;
			std::ostringstream convert;
			convert << index;
			result = convert.str();
			message.append(result);
			throwAIOBE(env,  message.c_str());
		}
}


void Animation::changeAlpha(short new_alpha){
	for (int i = 0; i < frame_num; i++)
		frames[i]->changeAlpha(new_alpha);
}
