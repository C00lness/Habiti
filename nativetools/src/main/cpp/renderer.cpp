#include "renderer.h"
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <android/log.h>
#include <cmath>
#include <cstring>
#include <vector>
#include <sstream>
#include <map>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#define STB_IMAGE_IMPLEMENTATION
#include "stb_image.h"

#define LOG_TAG "Renderer"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern AAssetManager* gAssetManager;

// ========== КОНСТРУКТОР / ДЕСТРУКТОР ==========
Renderer::Renderer()
        : program_(0), vbo_(0), ibo_(0),
          indexCount_(0),
          screenWidth_(0), screenHeight_(0), progress_(0.0f) {
}

Renderer::~Renderer() {
    // Удаляем текстуры этого рендерера
    for (auto& tex : textures_) {
        if (tex.second.id != 0) {
            glDeleteTextures(1, &tex.second.id);
        }
    }
    textures_.clear();
    materials_.clear();
    materialTextureMap_.clear();

    if (program_) glDeleteProgram(program_);
    if (vbo_) glDeleteBuffers(1, &vbo_);
    if (ibo_) glDeleteBuffers(1, &ibo_);
}

// ========== ЗАГРУЗКА ТЕКСТУР ==========
unsigned int Renderer::loadTextureFromAsset(const std::string& filename) {
    // Проверяем, не загружена ли уже
    auto it = textures_.find(filename);
    if (it != textures_.end()) {
        return it->second.id;
    }

    LOGD("Loading texture: %s", filename.c_str());

    AAsset* asset = AAssetManager_open(gAssetManager, filename.c_str(), AASSET_MODE_BUFFER);
    if (!asset) {
        LOGD("❌ Failed to open texture: %s", filename.c_str());
        return 0;
    }

    const void* data = AAsset_getBuffer(asset);
    size_t size = AAsset_getLength(asset);

    if (!data || size == 0) {
        LOGD("❌ Texture is empty: %s", filename.c_str());
        AAsset_close(asset);
        return 0;
    }

    int width, height, channels;
    unsigned char* imageData = stbi_load_from_memory(
            (const unsigned char*)data,
            (int)size,
            &width,
            &height,
            &channels,
            0
    );

    AAsset_close(asset);

    if (!imageData) {
        LOGD("❌ Failed to decode texture: %s", filename.c_str());
        return 0;
    }

    unsigned int textureID;
    glGenTextures(1, &textureID);
    glBindTexture(GL_TEXTURE_2D, textureID);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    GLenum format = (channels == 4) ? GL_RGBA : GL_RGB;
    glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format, GL_UNSIGNED_BYTE, imageData);

    stbi_image_free(imageData);

    Texture tex;
    tex.id = textureID;
    tex.width = width;
    tex.height = height;
    tex.filename = filename;
    textures_[filename] = tex;

    LOGD("✅ Loaded texture: %s (%dx%d, %d channels)", filename.c_str(), width, height, channels);
    return textureID;
}

// ========== ПАРСИНГ MTL ==========
void Renderer::parseMTL(const std::string& mtlPath) {
    LOGD("Parsing MTL: %s", mtlPath.c_str());

    AAsset* asset = AAssetManager_open(gAssetManager, mtlPath.c_str(), AASSET_MODE_BUFFER);
    if (!asset) {
        LOGD("❌ Failed to open MTL: %s", mtlPath.c_str());
        return;
    }

    const char* data = (const char*)AAsset_getBuffer(asset);
    size_t size = AAsset_getLength(asset);

    if (!data || size == 0) {
        AAsset_close(asset);
        return;
    }

    std::string content(data, size);
    std::istringstream stream(content);
    std::string line;
    Material currentMaterial;
    bool hasCurrent = false;

    while (std::getline(stream, line)) {
        std::istringstream iss(line);
        std::string prefix;
        iss >> prefix;

        if (prefix == "newmtl") {
            if (hasCurrent) {
                materials_[currentMaterial.name] = currentMaterial;
                if (!currentMaterial.textureFile.empty()) {
                    materialTextureMap_[currentMaterial.name] = currentMaterial.textureFile;
                    loadTextureFromAsset(currentMaterial.textureFile);
                }
            }

            std::string name;
            iss >> name;
            currentMaterial = Material();
            currentMaterial.name = name;
            currentMaterial.hasTexture = false;
            currentMaterial.diffuse[0] = 0.7f;
            currentMaterial.diffuse[1] = 0.7f;
            currentMaterial.diffuse[2] = 0.7f;
            hasCurrent = true;

            LOGD("🔵 Found material: %s", name.c_str());
        }
        else if (prefix == "Kd" && hasCurrent) {
            iss >> currentMaterial.diffuse[0]
                >> currentMaterial.diffuse[1]
                >> currentMaterial.diffuse[2];
            LOGD("🔵 Kd for %s: %f %f %f",
                 currentMaterial.name.c_str(),
                 currentMaterial.diffuse[0],
                 currentMaterial.diffuse[1],
                 currentMaterial.diffuse[2]);
        }
        else if (prefix == "map_Kd" && hasCurrent) {
            std::string texFile;
            iss >> texFile;
            currentMaterial.textureFile = texFile;
            currentMaterial.hasTexture = true;
            LOGD("🔵 map_Kd for %s: %s", currentMaterial.name.c_str(), texFile.c_str());
        }
    }

    if (hasCurrent) {
        materials_[currentMaterial.name] = currentMaterial;
        if (!currentMaterial.textureFile.empty()) {
            materialTextureMap_[currentMaterial.name] = currentMaterial.textureFile;
            loadTextureFromAsset(currentMaterial.textureFile);
        }
    }

    AAsset_close(asset);
    LOGD("✅ Parsed %zu materials, %zu textures", materials_.size(), textures_.size());
}

// ========== ЗАГРУЗЧИК OBJ ==========
ObjModel Renderer::loadOBJWithMTL(const std::string& filepath) {
    ObjModel model;
    if (!gAssetManager) {
        LOGD("❌ AssetManager is null!");
        return model;
    }

    LOGD("Loading OBJ: %s", filepath.c_str());

    AAsset* asset = AAssetManager_open(gAssetManager, filepath.c_str(), AASSET_MODE_BUFFER);
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
    std::vector<float> texCoords;
    std::string currentMaterial;

    struct Face {
        unsigned int v[3];
        unsigned int vt[3];
    };
    std::vector<Face> faces;
    std::vector<std::string> faceMaterials;

    std::string content(data, size);
    std::istringstream stream(content);
    std::string line;
    bool mtlLoaded = false;

    while (std::getline(stream, line)) {
        std::istringstream iss(line);
        std::string prefix;
        iss >> prefix;

        if (prefix == "mtllib") {
            std::string mtlFile;
            iss >> mtlFile;
            LOGD("🔵 Found MTL reference: %s", mtlFile.c_str());
            parseMTL(mtlFile);
            mtlLoaded = true;
        }
        else if (prefix == "v") {
            float x, y, z;
            iss >> x >> y >> z;
            positions.push_back(x);
            positions.push_back(y);
            positions.push_back(z);
        }
        else if (prefix == "vt") {
            float u, v;
            iss >> u >> v;
            texCoords.push_back(u);
            texCoords.push_back(v);
        }
        else if (prefix == "usemtl") {
            iss >> currentMaterial;
            LOGD("🔵 Using material: %s", currentMaterial.c_str());
        }
        else if (prefix == "f") {
            std::string v1, v2, v3;
            if (iss >> v1 >> v2 >> v3) {
                Face face;
                faceMaterials.push_back(currentMaterial);

                auto parseVertex = [](const std::string& s, unsigned int& v, unsigned int& vt) {
                    size_t slash1 = s.find('/');
                    size_t slash2 = s.find('/', slash1 + 1);

                    v = std::stoul(s.substr(0, slash1));

                    if (slash1 != std::string::npos && slash1 + 1 < s.length()) {
                        if (slash2 == std::string::npos) {
                            vt = std::stoul(s.substr(slash1 + 1));
                        } else if (slash2 > slash1 + 1) {
                            vt = std::stoul(s.substr(slash1 + 1, slash2 - slash1 - 1));
                        } else {
                            vt = 0;
                        }
                    } else {
                        vt = 0;
                    }
                };

                parseVertex(v1, face.v[0], face.vt[0]);
                parseVertex(v2, face.v[1], face.vt[1]);
                parseVertex(v3, face.v[2], face.vt[2]);

                faces.push_back(face);
            }
        }
    }

    AAsset_close(asset);

    if (!mtlLoaded) {
        LOGD("⚠️ No MTL file referenced in OBJ!");
    }

    if (positions.empty() || faces.empty()) {
        LOGD("❌ No vertices or faces found");
        return model;
    }

    LOGD("🔵 Positions: %zu, TexCoords: %zu, Faces: %zu",
         positions.size()/3, texCoords.size()/2, faces.size());

    // Собираем вершины с UV координатами
    for (const auto& face : faces) {
        for (int i = 0; i < 3; i++) {
            unsigned int vIdx = face.v[i] - 1;
            unsigned int vtIdx = face.vt[i] - 1;

            ObjModel::VertexData vd;
            vd.x = positions[vIdx * 3];
            vd.y = positions[vIdx * 3 + 1];
            vd.z = positions[vIdx * 3 + 2];

            if (vtIdx < texCoords.size() / 2) {
                vd.u = texCoords[vtIdx * 2];
                vd.v = 1.0f - texCoords[vtIdx * 2 + 1];
            } else {
                vd.u = 0.0f;
                vd.v = 0.0f;
                LOGD("⚠️ Missing UV for vertex %d", vIdx);
            }

            model.vertexData.push_back(vd);
        }
    }

    // Группируем по материалам
    size_t vertexOffset = 0;
    for (size_t i = 0; i < faces.size(); i++) {
        ObjModel::MeshGroup group;
        group.materialName = faceMaterials[i];

        for (int j = 0; j < 3; j++) {
            group.indices.push_back(vertexOffset + j);
        }
        vertexOffset += 3;

        model.groups.push_back(group);
    }

    LOGD("✅ Loaded %zu vertices, %zu faces, %zu groups",
         model.vertexData.size(), faces.size(), model.groups.size());
    return model;
}

// ========== ШЕЙДЕРЫ ==========
static const char* vertexShaderSource =
        "attribute vec3 aPos;\n"
        "attribute vec2 aTexCoord;\n"
        "uniform mat4 uTransform;\n"
        "varying vec2 vTexCoord;\n"
        "void main() {\n"
        "   gl_Position = uTransform * vec4(aPos, 1.0);\n"
        "   vTexCoord = aTexCoord;\n"
        "}\n";

static const char* fragmentShaderSource =
        "precision mediump float;\n"
        "varying vec2 vTexCoord;\n"
        "uniform sampler2D uTexture;\n"
        "void main() {\n"
        "   gl_FragColor = texture2D(uTexture, vTexCoord);\n"
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

void Renderer::init() {
    program_ = createProgram();
    if (program_ == 0) {
        LOGD("Failed to create program");
        return;
    }

    aPos_ = glGetAttribLocation(program_, "aPos");
    aTexCoord_ = glGetAttribLocation(program_, "aTexCoord");
    uTransform_ = glGetUniformLocation(program_, "uTransform");
    uTexture_ = glGetUniformLocation(program_, "uTexture");

    LOGD("✅ Program created: %d", program_);
    LOGD("aPos=%d, aTexCoord=%d, uTransform=%d, uTexture=%d",
         aPos_, aTexCoord_, uTransform_, uTexture_);

    glGenBuffers(1, &vbo_);
    glGenBuffers(1, &ibo_);

    indexCount_ = 0;

    LOGD("Renderer initialized");
}

void Renderer::loadModel(const std::string& path) {
    if (!gAssetManager) {
        LOGD("❌ AssetManager is null!");
        return;
    }

    // Сохраняем путь
    currentModelPath_ = path;

    // Очищаем старые текстуры этого рендерера
    for (auto& tex : textures_) {
        if (tex.second.id != 0) {
            glDeleteTextures(1, &tex.second.id);
        }
    }
    textures_.clear();
    materials_.clear();
    materialTextureMap_.clear();

    // Загружаем MTL если есть
    std::string mtlPath = path;
    size_t dotPos = mtlPath.find_last_of('.');
    if (dotPos != std::string::npos) {
        mtlPath = mtlPath.substr(0, dotPos) + ".mtl";
        LOGD("🔵 Loading MTL: %s", mtlPath.c_str());
        parseMTL(mtlPath);
    }

    currentModel = loadOBJWithMTL( path);
    if (currentModel.vertexData.empty() || currentModel.groups.empty()) {
        LOGD("❌ Failed to load model: %s", path.c_str());
        indexCount_ = 0;
        currentModelPath_ = path;
        return;
    }

    // Подготавливаем данные для VBO (позиция + UV = 5 float на вершину)
    std::vector<float> vertexData;
    for (const auto& v : currentModel.vertexData) {
        vertexData.push_back(v.x);
        vertexData.push_back(v.y);
        vertexData.push_back(v.z);
        vertexData.push_back(v.u);
        vertexData.push_back(v.v);
    }

    // Собираем все индексы
    std::vector<unsigned int> allIndices;
    for (const auto& group : currentModel.groups) {
        allIndices.insert(allIndices.end(), group.indices.begin(), group.indices.end());
    }

    glBindBuffer(GL_ARRAY_BUFFER, vbo_);
    glBufferData(GL_ARRAY_BUFFER,
                 vertexData.size() * sizeof(float),
                 vertexData.data(),
                 GL_STATIC_DRAW);

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo_);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER,
                 allIndices.size() * sizeof(unsigned int),
                 allIndices.data(),
                 GL_STATIC_DRAW);

    indexCount_ = (GLsizei)allIndices.size();
    currentModelPath_ = path;

    LOGD("✅ Loaded: %s, vertices=%zu, indices=%d, textures=%zu",
         path.c_str(), currentModel.vertexData.size(), indexCount_, textures_.size());
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

void Renderer::updateProgress(float progress) {
    progress_ = progress;
    LOGD("Progress: %f", progress_);
}

void Renderer::drawFrame() {
    if (program_ == 0 || vbo_ == 0 || ibo_ == 0) {
        return;
    }

    loadModelForProgress(progress_);

    if (indexCount_ == 0 || currentModel.groups.empty()) {
        return;
    }

    glViewport(0, 0, screenWidth_, screenHeight_);
    glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    glUseProgram(program_);

    float scale = 0.5f + progress_ * 0.8f;
    static float angle = 0.0f;
    angle += 0.005f;

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
    glVertexAttribPointer(aPos_, 3, GL_FLOAT, GL_FALSE, 5 * sizeof(float), (void*)0);

    glEnableVertexAttribArray(aTexCoord_);
    glVertexAttribPointer(aTexCoord_, 2, GL_FLOAT, GL_FALSE, 5 * sizeof(float), (void*)(3 * sizeof(float)));

    // Используем ЛОКАЛЬНЫЕ текстуры
    unsigned int lastTextureId = 0;
    unsigned int baseIndex = 0;

    static bool loggedOnce = false;
    if (!loggedOnce) {
        LOGD("🔵 Rendering %zu groups with %zu textures",
             currentModel.groups.size(), textures_.size());
        loggedOnce = true;
    }

    for (const auto& group : currentModel.groups) {
        unsigned int currentTextureId = 0;

        auto it = materialTextureMap_.find(group.materialName);
        if (it != materialTextureMap_.end()) {
            auto texIt = textures_.find(it->second);
            if (texIt != textures_.end()) {
                currentTextureId = texIt->second.id;
            }
        }

        if (currentTextureId != lastTextureId) {
            if (currentTextureId != 0) {
                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, currentTextureId);
                glUniform1i(uTexture_, 0);
                lastTextureId = currentTextureId;
            }
        }

        glDrawElements(GL_TRIANGLES,
                       (GLsizei)group.indices.size(),
                       GL_UNSIGNED_INT,
                       (void*)(baseIndex * sizeof(unsigned int)));

        baseIndex += group.indices.size();
    }

    glDisableVertexAttribArray(aPos_);
    glDisableVertexAttribArray(aTexCoord_);
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

// ========== НОВЫЕ МЕТОДЫ ДЛЯ УПРАВЛЕНИЯ ТЕКСТУРАМИ ==========

void Renderer::reloadTextures() {
    LOGD("🔄 Reloading textures for renderer %p", this);

    // Удаляем старые текстуры
    for (auto& tex : textures_) {
        if (tex.second.id != 0) {
            glDeleteTextures(1, &tex.second.id);
        }
    }
    textures_.clear();
    materialTextureMap_.clear();
    materials_.clear();

    // Перезагружаем MTL для текущей модели
    if (!currentModelPath_.empty()) {
        std::string mtlPath = currentModelPath_;
        size_t dotPos = mtlPath.find_last_of('.');
        if (dotPos != std::string::npos) {
            mtlPath = mtlPath.substr(0, dotPos) + ".mtl";
            LOGD("🔄 Loading MTL: %s", mtlPath.c_str());
            parseMTL(mtlPath);
        }

        // Перезагружаем VBO с теми же данными
        if (!currentModel.vertexData.empty()) {
            std::vector<float> vertexData;
            for (const auto& v : currentModel.vertexData) {
                vertexData.push_back(v.x);
                vertexData.push_back(v.y);
                vertexData.push_back(v.z);
                vertexData.push_back(v.u);
                vertexData.push_back(v.v);
            }

            glBindBuffer(GL_ARRAY_BUFFER, vbo_);
            glBufferData(GL_ARRAY_BUFFER,
                         vertexData.size() * sizeof(float),
                         vertexData.data(),
                         GL_STATIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }
    }

    LOGD("✅ Textures reloaded: %zu textures", textures_.size());
}

void Renderer::resetRenderer() {
    LOGD("🔄 Resetting renderer %p", this);

    // Очищаем все
    if (program_) {
        glDeleteProgram(program_);
        program_ = 0;
    }
    if (vbo_) {
        glDeleteBuffers(1, &vbo_);
        vbo_ = 0;
    }
    if (ibo_) {
        glDeleteBuffers(1, &ibo_);
        ibo_ = 0;
    }

    // Очищаем текстуры
    for (auto& tex : textures_) {
        if (tex.second.id != 0) {
            glDeleteTextures(1, &tex.second.id);
        }
    }
    textures_.clear();
    materialTextureMap_.clear();
    materials_.clear();

    currentModel = ObjModel();
    indexCount_ = 0;
    currentModelPath_ = "";

    // Переинициализируем
    init();

    LOGD("✅ Renderer reset complete");
}