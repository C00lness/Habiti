#ifndef RENDERER_H
#define RENDERER_H

#include <GLES2/gl2.h>
#include <string>

class Renderer {
public:
    Renderer();
    ~Renderer();

    void init();
    void updateProgress(float progress);
    void drawFrame();
    void resize(int width, int height);

private:
    GLuint program_ = 0;
    GLuint vbo_ = 0;
    GLuint ibo_ = 0;
    GLint aPos_ = 0;
    GLint aColor_ = 0;
    GLint uTransform_ = 0;

    float progress_ = 0.0f;
    int screenWidth_ = 0;
    int screenHeight_ = 0;
    GLsizei indexCount_ = 0;
    std::string currentModelPath_;

    void loadModel(const std::string& path);
    void loadModelForProgress(float progress);
    std::string getModelPath(float progress);
};

#endif