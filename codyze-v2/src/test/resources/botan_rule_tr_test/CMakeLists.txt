cmake_minimum_required(VERSION 3.10)
project(botan-rule-tr-test)

set(CMAKE_CXX_STANDARD 17)

# you have to install botan with: apt install libbotan-2-dev
# NOTE: this is probably not the BSI-version of botan!
include_directories(/usr/include/botan-2)

link_libraries(botan-2)

file(GLOB SOURCES "*.cpp")
message("bla: ${SOURCES}")

#excluded due to std::unique_ptr
list(FILTER SOURCES EXCLUDE REGEX ".*4_01.cpp")
list(FILTER SOURCES EXCLUDE REGEX ".*5_3_01.cpp")
list(FILTER SOURCES EXCLUDE REGEX ".*5_3_02.cpp")
list(FILTER SOURCES EXCLUDE REGEX ".*5_3_03.cpp")

# excluded due to other problems
list(FILTER SOURCES EXCLUDE REGEX ".*5_4_1_01.cpp")
list(FILTER SOURCES EXCLUDE REGEX ".*5_4_1_02.cpp")
list(FILTER SOURCES EXCLUDE REGEX ".*5_4_2_01.cpp")
list(FILTER SOURCES EXCLUDE REGEX ".*5_4_3_01.cpp")




#add_executable(botan-rule-tr-test ${SOURCES})
add_executable(botan-rule-tr-test "7_2_2_1_01.cpp")