cmake_minimum_required(VERSION 3.22.1)
project("nativetools")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(${CMAKE_PROJECT_NAME} SHARED
        native-lib.cpp
        renderer.cpp
)

# Находим библиотеки
find_library(log-lib log)
find_library(GLESv2-lib GLESv2)
find_library(android-lib android)

# 👇 ПРОВЕРКА, ЧТО GLESv2 НАЙДЕНА
if(GLESv2-lib STREQUAL "GLESv2-NOTFOUND")
    message(FATAL_ERROR "❌ GLESv2 not found!")
else()
    message("✅ Found GLESv2 at: ${GLESv2-lib}")
endif()

# 👇 ЛИНКУЕМ ОБЕ БИБЛИОТЕКИ
target_link_libraries(${CMAKE_PROJECT_NAME}
        ${log-lib}
        ${GLESv2-lib}
        ${android-lib})