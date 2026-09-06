cmake_minimum_required(VERSION 3.22.1)
project(eos_bridge)

set(BASE_PROJECT_DIR ${CMAKE_SOURCE_DIR}/../../..)
set(EOSSDK_DIR ${BASE_PROJECT_DIR}/SDK)

add_library(EOSSDK SHARED IMPORTED)
set_property(TARGET EOSSDK PROPERTY IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../../../build/unpacked/eossdk-StaticSTDC-release/jni/${ANDROID_ABI}/libEOSSDK.so)
set_property(TARGET EOSSDK PROPERTY INTERFACE_INCLUDE_DIRECTORIES ${EOSSDK_DIR}/Include)

add_library(
    eos_bridge
    SHARED
    eos_bridge.cpp
)

find_library(log-lib log)

target_link_libraries(
    eos_bridge
    EOSSDK
    ${log-lib}
)
