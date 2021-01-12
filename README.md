# Parquet Extension for Mule 4.x
Mule SDK connector that provides the ability to read Parquet files into JSON or write Parquet files from Avro data.


## Overview
[Apache Parquet](https://parquet.apache.org/) is a columnar data storage format, which provides a way to store tabular data column wise. Columns of same date-time are stored together as rows in Parquet format, so as to offer better storage, compression and data retrieval.

Using Parquet format has two advantages

* Reduced storage
* Query performance

## Operations

### Read Parquet File

This operation allows you to read a parquet file from a local file system. It returns the data back in JSON format.

* Parquet File Location - This is the location on the local file system where the operation will grab the parquet file.

<img src="https://raw.githubusercontent.com/djuang1/parquet/main/doc/img/read_parquet.png" width="600px">

### Write Avro to Parquet File

Writing data to a parquet file isn't a straightforward process. It requires a schema that needs to be defined around the data. This operation allows you leverage [Avro format support](https://docs.mulesoft.com/mule-runtime/4.3/dataweave-formats-avro) in MuleSoft to format the data using DataWeave before writing it to a parquet file.

* Body - This is the output from DataWeave component with data set with the MIME type of `application/avro`
* File Output Location - This is the location to write the parquet file on the local file system.
* Compression Codec - You can select the compression technique when configuring the operation. The GZip compression rate is higher than Snappy, and creates smaller files. However, Snappy generally provides better performance.

<img src="https://raw.githubusercontent.com/djuang1/parquet/main/doc/img/write_avro_to_parquet.png" width="600px">