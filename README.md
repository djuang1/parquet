# Parquet Extension for Mule 4.x
Mule SDK connector that provides the ability to read Parquet files into JSON or write Parquet files from Avro data.


## Overview
Apache Parquet is a columnar data storage format, which provides a way to store tabular data column wise. Columns of same date-time are stored together as rows in Parquet format, so as to offer better storage, compression and data retrieval.

Using Parquet format has two advantages

* Reduced storage
* Query performance

## Operations

### Read Parquet File

<img src="https://raw.githubusercontent.com/djuang1/parquet/master/doc/img/read_parquet.png" width="500px">

### Write Avro to Parquet File

<img src="https://raw.githubusercontent.com/djuang1/parquet/master/doc/img/write_avro_to_parquet.png" width="500px">