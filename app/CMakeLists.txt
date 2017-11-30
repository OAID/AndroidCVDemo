# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.6)

# Creates and names a library, sets it as either STATIC
set(CMAKE_VERBOSE_MAKEFILE on)
set(libs "${CMAKE_SOURCE_DIR}/src/main/jniLibs")
set(facesrc "${CMAKE_SOURCE_DIR}/src/main/cpp/facecaffe")
include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/include)
add_library(libopencv_java3 SHARED IMPORTED )
set_target_properties(libopencv_java3 PROPERTIES
    IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_java3.so")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DCPU_ONLY=1  -std=gnu++11 -fexceptions -frtti")
aux_source_directory(${CMAKE_SOURCE_DIR}/src/main/cpp/ DIR_LIB_SRCS)
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds it for you.
# Gradle automatically packages shared libraries with your APK.

add_library( native-lib   SHARED ${DIR_LIB_SRCS})


# Searches for a specified prebuilt library and stores the path as a
# variable. Because system libraries are included in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.
ADD_SUBDIRECTORY(${CMAKE_SOURCE_DIR}/src/main/cpp/dssttrack)
ADD_SUBDIRECTORY(${CMAKE_SOURCE_DIR}/src/main/cpp/handdetect)
target_link_libraries( # Specifies the target library.
                       native-lib android log
			libopencv_java3 dssttrack handdetect
                       
                       ${log-lib} )
