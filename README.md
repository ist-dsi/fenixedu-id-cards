Identification Cards
=========

> IST Identification Cards Module

## Using this module

To use this module, follow the steps below:

- Install the module:

```sh
mvn clean install
```
- Add this module to your webapp or modules dependencies list:

```xml
<dependency>
    <groupId>pt.ist</groupId>
    <artifactId>fenixedu-id-cards</artifactId>
    <version>DEV-SNAPSHOT</version>
</dependency>
```

## Skipping frontend compilation

In case you just want to compile the module without the frontend, you can do it with the script below:

```sh
mvn clean install -Dexec.skip
```

## Using the staging profile

This will build the frontend differently, packaging sources with different routes and api client based on a context environment variable.

```sh
mvn clean install -Pstaging
```

The staging context variable is defined [here](https://github.com/ist-dsi/fenixedu-id-cards/blob/master/src/main/frontend/.env.staging).

## Troubleshooting

Installing this module will trigger npm scripts to install the frontend, therefore, if there are some errors while ``mvn clean install``, the requirements for the frontend application are available [here](https://github.com/ist-dsi/fenixedu-id-cards/blob/master/src/main/frontend/README.md).
