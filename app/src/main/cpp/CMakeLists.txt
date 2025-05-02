# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("opencv")

# Đường dẫn đến OpenCV Android SDK
set(OpenCV_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../opencv-sdk/native)  # Thay đổi đường dẫn tùy vào vị trí của bạn

# Include header của OpenCV
include_directories(${OpenCV_DIR}/jni/include)

# Tạo shared library từ native code
add_library(
        native-lib  # Tên thư viện
        SHARED         # Loại thư viện (SHARED cho .so, STATIC cho .a)
        native-lib.cpp  # Đường dẫn đến file nguồn chính
)

# Tìm kiếm thư viện log (Android NDK)
find_library(
        log-lib  # Tên biến chứa path đến thư viện
        log       # Tên thư viện hệ thống
)

# Link thư viện log vào native library
target_link_libraries(
        native-lib  # Tên thư viện đích
        ${log-lib}     # Thư viện cần link
        ${OpenCV_DIR}/libs/${ANDROID_ABI}/libopencv_java4.so
)