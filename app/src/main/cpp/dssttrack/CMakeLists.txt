find_library( libopencv_java3 log)
aux_source_directory(${CMAKE_SOURCE_DIR}/src/main/cpp/dssttrack DIR_LIB_SRCS)

add_library (dssttrack STATIC ${DIR_LIB_SRCS})