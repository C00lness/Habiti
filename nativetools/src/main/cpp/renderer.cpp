#include "renderer.h"
#include <GLES2/gl2.h>
#include <android/log.h>
#include <cmath>
#include <cstring>
#include <vector>

#define LOG_TAG "CubeRenderer"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

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
        "   gl_FragColor = vec4(vColor, 1.0);\n"
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

// ========== ГЕНЕРАЦИЯ СФЕРЫ ==========
std::vector<float> generateSphere(float radius, int segments) {
    std::vector<float> vertices;
    for (int i = 0; i <= segments; ++i) {
        float theta = (float)i / segments * M_PI;
        for (int j = 0; j <= segments; ++j) {
            float phi = (float)j / segments * 2.0f * M_PI;

            float x = radius * sinf(theta) * cosf(phi);
            float y = radius * cosf(theta);
            float z = radius * sinf(theta) * sinf(phi);

            float nx = x / radius;
            float ny = y / radius;
            float nz = z / radius;

            float r = 0.5f + 0.5f * nx;
            float g = 0.5f + 0.5f * ny;
            float b = 0.5f + 0.5f * nz;

            vertices.push_back(x);
            vertices.push_back(y);
            vertices.push_back(z);
            vertices.push_back(r);
            vertices.push_back(g);
            vertices.push_back(b);
        }
    }
    return vertices;
}

std::vector<GLushort> generateSphereIndices(int segments) {
    std::vector<GLushort> indices;
    for (int i = 0; i < segments; ++i) {
        for (int j = 0; j < segments; ++j) {
            int a = i * (segments + 1) + j;
            int b = i * (segments + 1) + j + 1;
            int c = (i + 1) * (segments + 1) + j;
            int d = (i + 1) * (segments + 1) + j + 1;

            indices.push_back(a);
            indices.push_back(b);
            indices.push_back(c);

            indices.push_back(b);
            indices.push_back(d);
            indices.push_back(c);
        }
    }
    return indices;
}

// ========== РЕАЛИЗАЦИЯ RENDERER ==========
Renderer::Renderer() = default;

Renderer::~Renderer() {
    if (program_) glDeleteProgram(program_);
    if (vbo_) glDeleteBuffers(1, &vbo_);
    if (ibo_) glDeleteBuffers(1, &ibo_);
}

void Renderer::init() {
    program_ = createProgram();
    if (program_ == 0) {
        LOGD("Failed to create program");
        return;
    }

    aPos_ = glGetAttribLocation(program_, "aPos");
    aColor_ = glGetAttribLocation(program_, "aColor");
    uTransform_ = glGetUniformLocation(program_, "uTransform");

    // Генерируем сферу
    int segments = 20;
    auto vertices = generateSphere(0.5f, segments);
    auto indices = generateSphereIndices(segments);

    // VBO
    glGenBuffers(1, &vbo_);
    glBindBuffer(GL_ARRAY_BUFFER, vbo_);
    glBufferData(GL_ARRAY_BUFFER, vertices.size() * sizeof(float), vertices.data(), GL_STATIC_DRAW);

    // IBO
    glGenBuffers(1, &ibo_);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo_);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.size() * sizeof(GLushort), indices.data(), GL_STATIC_DRAW);

    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

    indexCount_ = (GLsizei)indices.size();

    LOGD("Sphere renderer ready (%zu vertices, %zu indices)", vertices.size() / 6, indices.size());
}

void Renderer::updateProgress(float progress) {
    progress_ = progress;
    LOGD("Progress received: %f", progress_);
}

void Renderer::drawFrame() {
    if (program_ == 0) return;

    glClearColor(0.1f, 0.1f, 0.2f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    glUseProgram(program_);

    float scale = 0.4f + progress_ * 0.8f;
    static float angle = 0.0f;
    angle += 0.01f;

    // Матрица трансформации
    float transform[16] = {
            cosf(angle) * scale, -sinf(angle) * scale, 0.0f, 0.0f,
            sinf(angle) * scale,  cosf(angle) * scale, 0.0f, 0.0f,
            0.0f,                 0.0f,                scale, 0.0f,
            0.0f,                 0.0f,                0.0f, 1.0f
    };

    glUniformMatrix4fv(uTransform_, 1, GL_FALSE, transform);

    glBindBuffer(GL_ARRAY_BUFFER, vbo_);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo_);

    glEnableVertexAttribArray(aPos_);
    glVertexAttribPointer(aPos_, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void*)0);

    glEnableVertexAttribArray(aColor_);
    glVertexAttribPointer(aColor_, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void*)(3 * sizeof(float)));

    glDrawElements(GL_TRIANGLES, indexCount_, GL_UNSIGNED_SHORT, 0);

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
}