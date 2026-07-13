#ifndef RENDERER_H
#define RENDERER_H

#include <GLES2/gl2.h>
#include <string>
#include <vector>
#include <map>

struct ObjModel {
    struct VertexData {
        float x, y, z;
        float u, v;
    };
    std::vector<VertexData> vertexData;

    struct MeshGroup {
        std::string materialName;
        std::vector<unsigned int> indices;
    };
    std::vector<MeshGroup> groups;
};

class Renderer {
public:
    Renderer();
    ~Renderer();

    void setScale(float scale) { customScale_ = scale; }
    void init();
    void loadModel(const std::string& path);
    void loadModelForProgress(float progress);
    std::string getModelPath(float progress);
    void updateProgress(float progress);
    void drawFrame();
    void resize(int width, int height);

    // Новые методы
    void reloadTextures();
    void resetRenderer();
    float getCurrentProgress() const { return progress_; }
    std::string getCurrentModelPath() const { return currentModelPath_; }

private:
    // OpenGL ресурсы
    GLuint program_ = 0;
    GLuint vbo_ = 0;
    GLuint ibo_ = 0;
    GLsizei indexCount_ = 0;
    float customScale_ = 0.5f;
    // Атрибуты шейдера
    GLint aPos_ = 0;
    GLint aTexCoord_ = 0;
    GLint uTransform_ = 0;
    GLint uTexture_ = 0;

    // Состояние
    float progress_ = 0.0f;
    int screenWidth_ = 0;
    int screenHeight_ = 0;
    std::string currentModelPath_;
    ObjModel currentModel;

    // ЛОКАЛЬНЫЕ текстуры (каждый Renderer имеет свои)
    struct Texture {
        unsigned int id;
        int width, height;
        std::string filename;
    };

    struct Material {
        std::string name;
        std::string textureFile;
        float diffuse[3];
        bool hasTexture;
    };

    std::map<std::string, Texture> textures_;
    std::map<std::string, Material> materials_;
    std::map<std::string, std::string> materialTextureMap_;

    // Приватные методы
    unsigned int loadTextureFromAsset(const std::string& filename);
    void parseMTL(const std::string& mtlPath);
    ObjModel loadOBJWithMTL(const std::string& filepath);
};

#endif // RENDERER_H