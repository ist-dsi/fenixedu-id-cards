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
    <version>{version}</version>
</dependency>
```

## Skipping frontend compilation

In case you just want to compile the module without the frontend, you can do it with the script below:

```sh
mvn clean install -Dexec.skip
```

## Troubleshooting

Installing this module will trigger npm scripts to install the frontend, therefore, if there are some errors while ``mvn clean install``, the requirements for the frontend application are available [here](https://github.com/ist-dsi/fenixedu-id-cards/blob/master/src/main/frontend/README.md).
