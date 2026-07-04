#include "renderer.h"
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <android/log.h>
#include <cmath>
#include <cstring>
#include <vector>
#include <sstream>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#define LOG_TAG "Renderer"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern AAssetManager* gAssetManager;

// ========== СТРУКТУРА ДЛЯ OBJ ==========
struct ObjModel {
    std::vector<float> vertices;
    std::vector<unsigned int> indices; // Изменено на unsigned int
};
// ========== ЗАГРУЗЧИК OBJ ==========
ObjModel loadOBJ(AAssetManager* assetManager, const std::string& filepath) {
    ObjModel model;
    if (!assetManager) {
        LOGD("❌ AssetManager is null!");
        return model;
    }

    LOGD("Loading: %s", filepath.c_str());

    AAsset* asset = AAssetManager_open(assetManager, filepath.c_str(), AASSET_MODE_BUFFER);
    if (!asset) {
        LOGD("❌ Failed to open asset: %s", filepath.c_str());
        return model;
    }

    const char* data = (const char*)AAsset_getBuffer(asset);
    size_t size = AAsset_getLength(asset);

    if (!data || size == 0) {
        LOGD("❌ Asset is empty");
        AAsset_close(asset);
        return model;
    }

    std::vector<float> positions;
    std::vector<float> colors;
    std::vector<unsigned int> idx;

    std::string content(data, size);
    std::istringstream stream(content);
    std::string line;

    while (std::getline(stream, line)) {
        std::istringstream iss(line);
        std::string prefix;
        iss >> prefix;
        if (prefix == "v") {
            float x, y, z;
            iss >> x >> y >> z;
            positions.push_back(x);
            positions.push_back(y);
            positions.push_back(z);

            // Пытаемся прочитать цвет вершины (если есть)
            float r, g, b;
            if (iss >> r >> g >> b) {
                colors.push_back(r);
                colors.push_back(g);
                colors.push_back(b);
            } else {
                float r = 0.5f + 0.5f * (y + 1.0f);
                float g = 0.5f + 0.5f * (x + 1.0f);
                float b = 0.5f + 0.5f * (z + 1.0f);

                colors.push_back(r);
                colors.push_back(g);
                colors.push_back(b);
            }
        } else if (prefix == "f") {
            // Читаем как строки, чтобы обработать форматы "1/1/1" или "1//1"
            std::string v1, v2, v3;
            if (iss >> v1 >> v2 >> v3) {
                // Извлекаем индекс вершины (до первого '/')
                auto parseIdx = [](const std::string& s) -> unsigned int {
                    size_t slashPos = s.find('/');
                    if (slashPos != std::string::npos) {
                        return std::stoul(s.substr(0, slashPos));
                    }
                    return std::stoul(s);
                };

                idx.push_back(parseIdx(v1) - 1);
                idx.push_back(parseIdx(v2) - 1);
                idx.push_back(parseIdx(v3) - 1);
            }
        }
    }

    AAsset_close(asset);

    if (positions.empty() || idx.empty()) {
        LOGD("❌ No vertices or indices found");
        return model;
    }

    // Собираем вершины с цветами
    model.vertices.reserve(positions.size() + colors.size());
    for (size_t i = 0; i < positions.size(); i += 3) {
        model.vertices.push_back(positions[i]);
        model.vertices.push_back(positions[i + 1]);
        model.vertices.push_back(positions[i + 2]);
        model.vertices.push_back(colors[i]);
        model.vertices.push_back(colors[i + 1]);
        model.vertices.push_back(colors[i + 2]);
    }

    model.indices.assign(idx.begin(), idx.end());

    LOGD("✅ Loaded %zu vertices, %zu indices", positions.size() / 3, idx.size());
    return model;
}

// ========== ШЕЙДЕРЫ ==========
static const char* vertexShaderSource =
        "attribute vec3 aPos;\n"
        "attribute vec3 aColor;\n"
        "uniform mat4 uTransform;\n"
        "varying vec3 vColor;\n"
        "void main() {\n"
        "   gl_Position = uTransform * vec4(aPos, 1.0);\n"
        "   vColor = aColor;\n"
        "}\n";

static const char* fragmentShaderSource =
        "precision mediump float;\n"
        "varying vec3 vColor;\n"
        "void main() {\n"
        "   gl_FragColor = vec4(vColor, 1.0);\n"  // ✅ Используем vColor
        "}\n";

// ========== ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ==========
GLuint compileShader(GLenum type, const char* source) {
    GLuint shader = glCreateShader(type);
    glShaderSource(shader, 1, &source, nullptr);
    glCompileShader(shader);

    GLint compiled;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
    if (!compiled) {
        GLint infoLen = 0;
        glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
        if (infoLen > 1) {
            char* infoLog = (char*)malloc(infoLen);
            glGetShaderInfoLog(shader, infoLen, nullptr, infoLog);
            LOGD("Shader error: %s", infoLog);
            free(infoLog);
        }
        glDeleteShader(shader);
        return 0;
    }
    return shader;
}

GLuint createProgram() {
    GLuint vertexShader = compileShader(GL_VERTEX_SHADER, vertexShaderSource);
    GLuint fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentShaderSource);

    GLuint program = glCreateProgram();
    glAttachShader(program, vertexShader);
    glAttachShader(program, fragmentShader);
    glLinkProgram(program);

    GLint linked;
    glGetProgramiv(program, GL_LINK_STATUS, &linked);
    if (!linked) {
        GLint infoLen = 0;
        glGetProgramiv(program, GL_INFO_LOG_LENGTH, &infoLen);
        if (infoLen > 1) {
            char* infoLog = (char*)malloc(infoLen);
            glGetProgramInfoLog(program, infoLen, nullptr, infoLog);
            LOGD("Program error: %s", infoLog);
            free(infoLog);
        }
        glDeleteProgram(program);
        return 0;
    }

    glDeleteShader(vertexShader);
    glDeleteShader(fragmentShader);
    return program;
}

// ========== РЕАЛИЗАЦИЯ RENDERER ==========
Renderer::Renderer()
        : program_(0), vbo_(0), ibo_(0), indexCount_(0),
          screenWidth_(0), screenHeight_(0), progress_(0.0f) {
}

Renderer::~Renderer() {
    if (program_) glDeleteProgram(program_);
    if (vbo_) glDeleteBuffers(1, &vbo_);
    if (ibo_) glDeleteBuffers(1, &ibo_);
}

void Renderer::loadModel(const std::string& path) {
    if (!gAssetManager) {
        LOGD("❌ AssetManager is null!");
        return;
    }

    ObjModel model = loadOBJ(gAssetManager, path);
    if (model.vertices.empty() || model.indices.empty()) {
        LOGD("❌ Failed to load model: %s", path.c_str());
        indexCount_ = 0;
        return;
    }

    glBindBuffer(GL_ARRAY_BUFFER, vbo_);
    glBufferData(GL_ARRAY_BUFFER,
                 model.vertices.size() * sizeof(float),
                 model.vertices.data(),
                 GL_STATIC_DRAW);

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo_);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER,
                 model.indices.size() * sizeof(unsigned int),
                 model.indices.data(),
                 GL_STATIC_DRAW);

    indexCount_ = (GLsizei)model.indices.size();
    currentModelPath_ = path;

    LOGD("✅ Loaded: %s, vertices=%zu, indices=%zu",
         path.c_str(), model.vertices.size() / 6, model.indices.size());
}

std::string Renderer::getModelPath(float progress) {
    if (progress < 0.33f) {
        return "egg.obj";
    } else if (progress < 0.66f) {
        return "baby_dragon.obj";
    } else {
        return "dragon.obj";
    }
}

void Renderer::loadModelForProgress(float progress) {
    std::string path = getModelPath(progress);
    if (path == currentModelPath_) {
        return;
    }
    loadModel(path);
}

void Renderer::init() {
    program_ = createProgram();
    if (program_ == 0) {
        LOGD("Failed to create program");
        return;
    }
    LOGD("✅ Program created: %d", program_);
    aPos_ = glGetAttribLocation(program_, "aPos");
    aColor_ = glGetAttribLocation(program_, "aColor");
    uTransform_ = glGetUniformLocation(program_, "uTransform");

    glGenBuffers(1, &vbo_);
    glGenBuffers(1, &ibo_);

    indexCount_ = 0;

    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

    LOGD("Renderer initialized");
}

void Renderer::updateProgress(float progress) {
    progress_ = progress;
    LOGD("Progress: %f", progress_);
}

void Renderer::drawFrame() {
    if (program_ == 0 || vbo_ == 0 || ibo_ == 0) {
        LOGD("Skipping draw: program=%d, vbo=%d, ibo=%d",
             program_, vbo_, ibo_);
        return;
    }

    // Загружаем модель по прогрессу
    loadModelForProgress(progress_);

    if (indexCount_ == 0) {
        LOGD("Skipping draw: indexCount is 0");
        return;
    }

    // Используем реальные размеры экрана
    glViewport(0, 0, screenWidth_, screenHeight_);
    glClearColor(0.1f, 0.1f, 0.2f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    glUseProgram(program_);

    float scale = 0.5f + progress_ * 0.8f;
    static float angle = 0.0f;
    angle += 0.02f;

    float transform[16] = {
            cosf(angle) * scale, 0.0f, sinf(angle) * scale, 0.0f,
            0.0f,                scale, 0.0f,               0.0f,
            -sinf(angle) * scale,0.0f, cosf(angle) * scale, 0.0f,
            0.0f,                0.0f, 0.0f,                1.0f
    };

    glUniformMatrix4fv(uTransform_, 1, GL_FALSE, transform);

    glBindBuffer(GL_ARRAY_BUFFER, vbo_);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo_);

    glEnableVertexAttribArray(aPos_);
    glVertexAttribPointer(aPos_, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void*)0);

    glEnableVertexAttribArray(aColor_);
    glVertexAttribPointer(aColor_, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void*)(3 * sizeof(float)));

    // Используем GL_UNSIGNED_INT вместо GL_UNSIGNED_SHORT
    glDrawElements(GL_TRIANGLES, indexCount_, GL_UNSIGNED_INT, 0);

    glDisableVertexAttribArray(aPos_);
    glDisableVertexAttribArray(aColor_);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    glUseProgram(0);
}

void Renderer::resize(int width, int height) {
    screenWidth_ = width;
    screenHeight_ = height;
    glViewport(0, 0, width, height);
    LOGD("✅ Viewport set: %dx%d", width, height);
}