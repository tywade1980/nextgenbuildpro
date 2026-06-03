cmake_minimum_required(VERSION 3.18)
project(ai_inference)

add_library(ai_inference SHARED ai_inference.cpp)

find_library(log-lib log)

target_link_libraries(ai_inference PRIVATE ${log-lib})