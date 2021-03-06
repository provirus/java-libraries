java-libraries
==============

Many common tools.

License: The MIT License (MIT)


Projects
--------

- jl-smalltools: A couple of small tools that are mostly one class each.
- jl-incubator: Everything that is new and that might be removed or that might be changed (like a different API)

Usage
-----

Include this library to your project.
When you use any classes, check the Javadoc to see which dependencies you should add.

Process
-------

Versioning:
- The version number is in the format MAJOR.MINOR.BUGFIX (e.g 0.1.0).
- The API in a MAJOR release is stable (when not in incubator). Everything that will be removed in the next MAJOR release are marked as deprecated.

For new things:
- New ideas and API should start in "jl-incubator" unless it is very small and that it is sure that nothing will be changed in it.
- When an API is stable, it can go in one of the other stable projects.

For changes/removal in the stable API:
- When something is in the stable API, it will be there for all the releases in the same MAJOR version.
- Everything that will be removed in the next MAJOR version is marked as @deprecated and the Javadoc will explain what to use instead if there is a workaround.


Design choices
--------------

- No transitive libraries: Since the goal is to provide a lot of diversity while reusing other libraries, it is better for you to include only the libraries you need.


Deployment instructions
-----------------------

Execute:

```
# For locally testing and skip checking the tests:
./create-local-release-no-tests.sh   # Will use master-SNAPSHOT as the version
./create-local-release-no-tests.sh THE_VERSION

# For locally testing:
./create-local-release.sh   # Will use master-SNAPSHOT as the version
./create-local-release.sh THE_VERSION

# For creating a public release and publishing it
./create-public-release.sh THE_VERSION
```

You can see releases available:
- https://repo1.maven.org/maven2/com/foilen/jl-smalltools
- https://repo1.maven.org/maven2/com/foilen/jl-incubator
