# PivotMaker
## Table Of Contents

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

- [Features](#features)
- [How To Use](#how-to-use)
- [Built with](#built-with)
- [Solution Overview](#solution-overview)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->


## Features

The aim of the project is to create a java library able to create Pivot Table.

## How To Use

To clone and run test, you'll need [Git](https://git-scm.com) and [Maven](https://maven.apache.org/)  installed on your computer. When you installed these software you can start to test the library:

#### 1) Clone repo
Use git to clone this repo. Please remember that all files needs to be in a folder named "truefilm" (case insensitive)
```shell script
# Clone repo
$ git clone git@github.com:alexh2o17/PivotMaker.git
```
#### 1) Test Library
Start test using Maven

```shell script
# Start Test
$ mvn clean test
```
#### 2) Use in your own application
This library is not present on MaverRepository, so you first have to install in local.
```shell script
# Install Library
$ mvn clean install
```

After install you can use in your application adding these in your pom:
```shell script
# Add in your pom
        <dependency>
            <groupId>com.h2o</groupId>
            <artifactId>pivot-maker_2.12</artifactId>
            <version>1.0</version>
        </dependency>
```

## Built with 

- [Scala](https://www.scala-lang.org/) - Scala combines object-oriented and functional programming in one concise, high-level language.


## Solution Overview

The biggest challenge of this project was the choice of the data model to represent the pivot table.

The aim of the project, infact, is to create a Pivot Table from: 
* a set of Data
* an aggregation function defined by the user
* an aggregation order (and a numerical field)

The Pivot Table in output needs to be able to be queried to obtain the value of the aggregation at any level, for any labeling group.

The Pivot Table is represented by a Tree, where every node is composed by a name, a result and children nodes:

![Alt text](img/model.png?raw=true "Model")

In the Libray the Pivot Table is composed by a PivotHeader, that contains information about aggregation order, and a Map[String, PivotNode], that defines the tree.

The PivotTable expose two methods to be queried:
* getResultByValue: get aggregation result by a list of String value
* getResultByIndex: get aggregation result by name of one of the index in the aggregation order list of index

See tests to find some examples of use

To create Pivot Table use the *PivotTable.create* method that takes in input:
* data: List of List of strings that rapresent the table (For example you can read a csv by lines and divide every line in List of strings by a separator)
* aggregationFunction: Any method that takes in input a List of Integer and return a single integer value
* aggregationOrder: Ordered List of aggregation index
* resultIndex: Numerical field used in aggregation function (Added to made the method to accept data that has numerical column in any position)
