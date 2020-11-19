cmake -DCMAKE_BUILD_TYPE=Debug | Release | RelWithDebInfo | MinSizeRel

# Ubuntu 16+
cmake .. -DSCALA_VERSION=2.11.11 -DEIGEN3_INCLUDE_DIR=../../eigen -DENABLE_CPP_EXAMPLES=ON -DENABLE_SWIG=ON -DENABLE_ZIP=ON -DENABLE_NONNATIVE=ON -DENABLE_RELOCATABLE=ON
# Ubuntu 14
cmake .. -DSCALA_VERSION=2.11.11 -DEIGEN3_INCLUDE_DIR=../eigen -DENABLE_CPP_EXAMPLES=ON -DENABLE_SWIG=ON -DENABLE_ZIP=ON -DENABLE_NONNATIVE=ON -DENABLE_RELOCATABLE=ON

# Ubuntu 16+ CUDA (for Clara)
# For swig: ./configure --prefix=/work/kalcock/bin/swig
cmake .. -DCUDA_TOOLKIT_ROOT_DIR=/usr/local/cuda -DBACKEND=cuda -DSWIG_EXECUTABLE=/work/kalcock/bin/swig/bin/swig -DSWIG_DIR=/work/kalcock/bin/swig/share/swig/3.0.12 -DSCALA_VERSION=2.11.11 -DEIGEN3_INCLUDE_DIR=../../eigen -DENABLE_CPP_EXAMPLES=ON -DENABLE_SWIG=ON -DENABLE_ZIP=ON -DENABLE_NONNATIVE=ON -DENABLE_RELOCATABLE=ON
