# build

Files in this directory are used not to build *fatdynet*, but rather to build
the *DyNet* libraries which are used for it (in `src/main/resources`).  Instructions
for the complete build process are located on the *fatdynet*'s wiki.

The `cmake` files are used to generate the build files and are issued from
the `build` directory.  The `relink` files are issued from the `build/dynet`
directory after the initial build has completed.
