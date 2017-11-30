find_library( libopencv_java3 log)
aux_source_directory(${CMAKE_SOURCE_DIR}/src/main/cpp/handdetect DIR_LIB_SRCS)

add_library (handdetect STATIC ${DIR_LIB_SRCS})
